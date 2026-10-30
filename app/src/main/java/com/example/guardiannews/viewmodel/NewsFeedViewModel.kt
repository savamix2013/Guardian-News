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

sealed interface NewsUiState {
    object Loading : NewsUiState
    data class Success(val articles: List<GuardianArticle>) : NewsUiState
    data class Error(val message: String) : NewsUiState

    val allArticles: List<GuardianArticle>
        get() = when (this) {
            is Success -> articles
            else -> emptyList()
        }
}

class NewsFeedViewModel(
    private val apiService: GuardianApiService = GuardianNetwork.api
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    fun loadArticles() {
        if (_uiState.value is NewsUiState.Success) return

        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            try {
                val response = apiService.searchArticles()
                val articles = response.response?.results?.map { it.toDomainModel() } ?: emptyList()
                _uiState.value = NewsUiState.Success(articles)
            } catch (e: Exception) {
                _uiState.value = NewsUiState.Error(e.message ?: "Помилка завантаження")
            }
        }
    }

    fun toggleSaved(articleId: String) {
        val currentState = _uiState.value
        if (currentState is NewsUiState.Success) {
            val updatedArticles = currentState.articles.map { article ->
                if (article.id == articleId) {
                    article.copy(isSaved = !article.isSaved)
                } else {
                    article
                }
            }
            _uiState.value = NewsUiState.Success(updatedArticles)
        }
    }
}