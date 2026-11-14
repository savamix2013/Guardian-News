package com.example.guardiannews.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guardiannews.BuildConfig
import com.example.guardiannews.data.GuardianApiService
import com.example.guardiannews.model.GuardianArticle
import com.example.guardiannews.model.toDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

data class NewsFeedUiState(
    val articles: List<GuardianArticle> = emptyList(),
    val editorsPicks: List<GuardianArticle> = emptyList(),
    val sections: List<String> = listOf("All", "world", "technology", "science", "sport", "business", "culture"),
    val selectedSection: String? = "All",
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
) {
    val allArticles: List<GuardianArticle>
        get() = (editorsPicks + articles).distinctBy { it.id }
}

class NewsFeedViewModel(
    private val apiService: GuardianApiService = GuardianApiService.create(),
    private val apiKey: String = BuildConfig.GUARDIAN_API_KEY
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsFeedUiState(isInitialLoading = true))
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    companion object {
        private const val MAX_CONCURRENT_REQUESTS = 3
        private const val PREFETCH_DETAILS_COUNT = 5
    }

    init {
        loadFeed(isPullToRefresh = false)
    }

    fun selectSection(sectionId: String?) {
        if (_uiState.value.selectedSection == sectionId) return
        _uiState.update { it.copy(selectedSection = sectionId) }
        loadFeed(isPullToRefresh = false)
    }

    fun refreshFeed() {
        if (_uiState.value.isRefreshing) return
        loadFeed(isPullToRefresh = true)
    }

    private fun loadFeed(isPullToRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isInitialLoading = !isPullToRefresh && current.articles.isEmpty(),
                    isRefreshing = isPullToRefresh,
                    errorMessage = null
                )
            }

            try {
                val rawSection = _uiState.value.selectedSection
                val sectionParam = rawSection?.takeUnless { it.equals("All", ignoreCase = true) }
                val semaphore = Semaphore(MAX_CONCURRENT_REQUESTS)

                val (enrichedArticles, picks) = coroutineScope {
                    val mainFeedDeferred = async {
                        semaphore.withPermit {
                            apiService.searchArticles(
                                apiKey = apiKey,
                                section = sectionParam
                            ).response.results.map { it.toDomain() }
                        }
                    }

                    val editorsPicksDeferred = async {
                        runCatching {
                            semaphore.withPermit {
                                apiService.getEditorsPicks(
                                    apiKey = apiKey,
                                    section = sectionParam
                                ).response.editorsPicks.map { it.toDomain(isEditorsPick = true) }
                            }
                        }.getOrDefault(emptyList())
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
