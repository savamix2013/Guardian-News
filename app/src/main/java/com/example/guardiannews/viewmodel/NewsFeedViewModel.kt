package com.example.guardiannews.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guardiannews.data.GuardianNetwork
import com.example.guardiannews.data.UserPreferencesRepository
import com.example.guardiannews.data.toDomainModel
import com.example.guardiannews.model.GuardianArticle
import com.example.guardiannews.model.availableSections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsFeedUiState(
    val sections: List<String> = availableSections,
    val defaultSection: String = UserPreferencesRepository.DEFAULT_SECTION_FALLBACK,
    val selectedSection: String = UserPreferencesRepository.DEFAULT_SECTION_FALLBACK,
    val allArticles: List<GuardianArticle> = emptyList(),
    val filteredArticles: List<GuardianArticle> = emptyList(),
    val isLoading: Boolean = false
)

class NewsFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application.applicationContext)
    private val savedArticleIds = mutableSetOf<String>()

    private val _uiState = MutableStateFlow(NewsFeedUiState(isLoading = true))
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.defaultSectionFlow.collect { savedDefaultSection ->
                _uiState.update { currentState ->
                    currentState.copy(
                        defaultSection = savedDefaultSection,
                        selectedSection = savedDefaultSection
                    )
                }
                loadArticles(savedDefaultSection)
            }
        }
    }

    fun selectSection(section: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedSection = section)
        }
        loadArticles(section)
    }

    fun resetFilter() {
        selectSection(_uiState.value.defaultSection)
    }

    fun saveDefaultSection(section: String) {
        viewModelScope.launch {
            preferencesRepository.saveDefaultSection(section)
        }
    }

    fun loadArticles(section: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val sectionParam = if (section.equals("All", ignoreCase = true)) {
                null
            } else {
                section.lowercase()
            }

            val loadedArticles = try {
                val response = GuardianNetwork.api.searchArticles(section = sectionParam)
                response.response?.results.orEmpty().map { dto ->
                    dto.toDomainModel(isSaved = savedArticleIds.contains(dto.id))
                }
            } catch (e: Exception) {
                emptyList()
            }

            _uiState.update { currentState ->
                currentState.copy(
                    allArticles = loadedArticles,
                    filteredArticles = loadedArticles,
                    isLoading = false
                )
            }
        }
    }

    fun toggleSaved(articleId: String) {
        if (savedArticleIds.contains(articleId)) {
            savedArticleIds.remove(articleId)
        } else {
            savedArticleIds.add(articleId)
        }

        _uiState.update { currentState ->
            val updated = currentState.allArticles.map { article ->
                if (article.id == articleId) {
                    article.copy(isSaved = savedArticleIds.contains(articleId))
                } else {
                    article
                }
            }
            currentState.copy(
                allArticles = updated,
                filteredArticles = updated
            )
        }
    }
}
