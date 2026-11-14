package com.example.guardiannews.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.guardiannews.BuildConfig
import com.example.guardiannews.data.GuardianApiService
import com.example.guardiannews.data.UserPreferencesRepository
import com.example.guardiannews.model.GuardianArticle
import com.example.guardiannews.model.toDomain
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

data class NewsFeedUiState(
    val articles: List<GuardianArticle> = emptyList(),
    val editorsPicks: List<GuardianArticle> = emptyList(),
    val sections: List<String> = listOf("All", "world", "technology", "science", "sport", "business", "culture"),
    val selectedSection: String? = "All",
    val searchQuery: String = "",
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
) {
    val allArticles: List<GuardianArticle>
        get() = (editorsPicks + articles).distinctBy { it.id }

    val isEmptySearchResult: Boolean
        get() = !isInitialLoading &&
                !isRefreshing &&
                errorMessage == null &&
                searchQuery.isNotBlank() &&
                articles.isEmpty() &&
                editorsPicks.isEmpty()
}

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Success(val articles: List<GuardianArticle>) : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}

class NewsFeedViewModel(
    private val apiService: GuardianApiService = GuardianApiService.create(),
    private val preferencesRepository: UserPreferencesRepository,
    private val apiKey: String = BuildConfig.GUARDIAN_API_KEY
) : ViewModel() {

    val favoriteIds: StateFlow<Set<String>> = preferencesRepository.favoriteIdsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    private val _uiState = MutableStateFlow(NewsFeedUiState(isInitialLoading = true))
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    private val _favoritesUiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val favoritesUiState: StateFlow<FavoritesUiState> = _favoritesUiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    companion object {
        private const val MAX_CONCURRENT_REQUESTS = 3
        private const val PREFETCH_DETAILS_COUNT = 5
        private const val SEARCH_DEBOUNCE_MS = 400L

        fun Factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext
                val apiService = GuardianApiService.create()
                val preferencesRepository = UserPreferencesRepository(app)
                return NewsFeedViewModel(
                    apiService = apiService,
                    preferencesRepository = preferencesRepository
                ) as T
            }
        }
    }

    init {
        loadFeed(isPullToRefresh = false)
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        searchJob?.cancel()

        if (newQuery.trim().isEmpty()) {
            loadFeed(isPullToRefresh = false)
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadFeed(isPullToRefresh = false)
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        if (_uiState.value.searchQuery.isEmpty()) return

        _uiState.update { it.copy(searchQuery = "") }
        loadFeed(isPullToRefresh = false)
    }

    fun selectSection(sectionId: String?) {
        searchJob?.cancel()
        if (_uiState.value.selectedSection == sectionId) return
        _uiState.update { it.copy(selectedSection = sectionId) }
        loadFeed(isPullToRefresh = false)
    }

    fun refreshFeed() {
        if (_uiState.value.isRefreshing) return
        loadFeed(isPullToRefresh = true)
    }

    fun retryLoad() {
        loadFeed(isPullToRefresh = false)
    }

    fun toggleFavorite(articleId: String) {
        viewModelScope.launch {
            preferencesRepository.toggleFavorite(articleId)

            val currentState = _favoritesUiState.value
            if (currentState is FavoritesUiState.Success) {
                val updatedList = currentState.articles.filterNot { it.id == articleId }
                _favoritesUiState.value = if (updatedList.isEmpty()) {
                    FavoritesUiState.Empty
                } else {
                    FavoritesUiState.Success(updatedList)
                }
            }
        }
    }

    fun loadFavoriteArticles() {
        viewModelScope.launch {
            _favoritesUiState.value = FavoritesUiState.Loading
            try {
                val savedIds = preferencesRepository.favoriteIdsFlow.first()
                if (savedIds.isEmpty()) {
                    _favoritesUiState.value = FavoritesUiState.Empty
                    return@launch
                }

                val response = apiService.getArticlesByIds(
                    ids = savedIds.joinToString(","),
                    apiKey = apiKey,
                    pageSize = savedIds.size.coerceAtMost(50)
                )

                val freshArticles = response.response.results.map { it.toDomain() }

                _favoritesUiState.value = if (freshArticles.isEmpty()) {
                    FavoritesUiState.Empty
                } else {
                    FavoritesUiState.Success(freshArticles)
                }
            } catch (e: Exception) {
                _favoritesUiState.value = FavoritesUiState.Error(
                    message = e.localizedMessage ?: "Не вдалося завантажити збережені статті"
                )
            }
        }
    }

    private fun loadFeed(isPullToRefresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isInitialLoading = !isPullToRefresh && current.articles.isEmpty(),
                    isRefreshing = isPullToRefresh,
                    errorMessage = null
                )
            }

            try {
                val currentState = _uiState.value
                val rawSection = currentState.selectedSection
                val sectionParam = rawSection?.takeUnless { it.equals("All", ignoreCase = true) }
                val trimmedQuery = currentState.searchQuery.trim().takeIf { it.isNotEmpty() }
                val semaphore = Semaphore(MAX_CONCURRENT_REQUESTS)

                val (enrichedArticles, picks) = coroutineScope {
                    val mainFeedDeferred = async {
                        semaphore.withPermit {
                            apiService.searchArticles(
                                apiKey = apiKey,
                                section = sectionParam,
                                query = trimmedQuery
                            ).response.results.map { it.toDomain() }
                        }
                    }

                    val editorsPicksDeferred = async {
                        if (trimmedQuery != null) {
                            emptyList()
                        } else {
                            runCatching {
                                semaphore.withPermit {
                                    apiService.getEditorsPicks(
                                        apiKey = apiKey,
                                        section = sectionParam
                                    ).response.editorsPicks.map { it.toDomain(isEditorsPick = true) }
                                }
                            }.getOrDefault(emptyList())
                        }
                    }

                    val baseArticles = mainFeedDeferred.await()
                    val editorsPicksList = editorsPicksDeferred.await()

                    val topArticles = baseArticles.take(PREFETCH_DETAILS_COUNT)
                    val enrichedTopMap = topArticles.map { article ->
                        async {
                            semaphore.withPermit {
                                runCatching {
                                    apiService.getArticleDetails(
                                        articleId = article.id,
                                        apiKey = apiKey
                                    ).response.content?.toDomain() ?: article
                                }.getOrDefault(article)
                            }
                        }
                    }.awaitAll().associateBy { it.id }

                    val mergedArticles = baseArticles.map { item ->
                        enrichedTopMap[item.id] ?: item
                    }

                    mergedArticles to editorsPicksList
                }

                _uiState.update {
                    it.copy(
                        articles = enrichedArticles,
                        editorsPicks = picks,
                        isInitialLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        errorMessage = e.localizedMessage ?: "Не вдалося оновити стрічку новин"
                    )
                }
            }
        }
    }
}
