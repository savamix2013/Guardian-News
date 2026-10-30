package com.example.guardiannews.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guardiannews.data.GuardianApiService
import com.example.guardiannews.data.GuardianNetwork
import com.example.guardiannews.data.toDomainModel
import com.example.guardiannews.model.GuardianArticle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface NewsUiState {
    val allArticles: List<GuardianArticle>
        get() = (this as? Success)?.articles ?: emptyList()

    object Loading : NewsUiState
    object Empty : NewsUiState
    data class Success(val articles: List<GuardianArticle>) : NewsUiState
    data class Error(val message: String) : NewsUiState
}

class NewsFeedViewModel(
    private val apiService: GuardianApiService = GuardianNetwork.api
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _selectedSection = MutableStateFlow("All")
    val selectedSection: StateFlow<String> = _selectedSection.asStateFlow()

    private val savedArticleIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        loadNews()
    }

    fun loadNews(section: String? = null) {
        val targetSection = section ?: _selectedSection.value
        _selectedSection.value = targetSection

        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            try {
                val apiSection = if (targetSection == "All" || targetSection.isBlank()) null else targetSection
                val response = apiService.searchArticles(section = apiSection)
                val dtos = response.response?.results ?: emptyList()

                if (dtos.isEmpty()) {
                    _uiState.value = NewsUiState.Empty
                } else {
                    val articles = dtos.map { dto ->
                        dto.toDomainModel(isSaved = savedArticleIds.value.contains(dto.id))
                    }
                    _uiState.value = NewsUiState.Success(articles)
                }
            } catch (e: IOException) {
                _uiState.value = NewsUiState.Error(
                    "Відсутнє підключення до мережі. Перевірте інтернет або авіарежим."
                )
            } catch (e: HttpException) {
                _uiState.value = NewsUiState.Error(
                    "Помилка сервера (${e.code()}). Спробуйте пізніше."
                )
            } catch (e: Exception) {
                _uiState.value = NewsUiState.Error(
                    "Отримано некоректні дані від сервера."
                )
            }
        }
    }

    fun toggleSaved(articleId: String) {
        val currentSaved = savedArticleIds.value.toMutableSet()
        if (currentSaved.contains(articleId)) {
            currentSaved.remove(articleId)
        } else {
            currentSaved.add(articleId)
        }
        savedArticleIds.value = currentSaved

        val currentState = _uiState.value
        if (currentState is NewsUiState.Success) {
            val updatedArticles = currentState.articles.map { article ->
                if (article.id == articleId) {
                    article.copy(isSaved = currentSaved.contains(articleId))
                } else {
                    article
                }
            }
            _uiState.value = NewsUiState.Success(updatedArticles)
        }
    }

    fun retry(section: String? = null) {
        loadNews(section ?: _selectedSection.value)
    }
}
