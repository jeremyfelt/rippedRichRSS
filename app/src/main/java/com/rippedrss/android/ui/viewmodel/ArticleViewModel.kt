package com.rippedrss.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rippedrss.android.data.model.Article
import com.rippedrss.android.data.repository.ArticleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ArticleUiState(
    val articles: List<Article> = emptyList(),
    val unreadCount: Int = 0,
    val selectedArticle: Article? = null,
    val error: String? = null
)

class ArticleViewModel(private val articleRepository: ArticleRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
        loadUnreadCount()
    }

    private fun loadArticles() {
        viewModelScope.launch {
            articleRepository.getAllArticles()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { articles ->
                    _uiState.update { it.copy(articles = articles, error = null) }
                }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            articleRepository.getUnreadCount()
                .catch { }
                .collect { count ->
                    _uiState.update { it.copy(unreadCount = count) }
                }
        }
    }

    fun loadSavedArticles() {
        viewModelScope.launch {
            articleRepository.getSavedArticles()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { articles ->
                    _uiState.update { it.copy(articles = articles, error = null) }
                }
        }
    }

    fun loadUnreadArticles() {
        viewModelScope.launch {
            articleRepository.getUnreadArticles()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { articles ->
                    _uiState.update { it.copy(articles = articles, error = null) }
                }
        }
    }

    fun markAsRead(articleId: String, isRead: Boolean = true) {
        viewModelScope.launch {
            try {
                articleRepository.markAsRead(articleId, isRead)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleSaved(article: Article) {
        viewModelScope.launch {
            try {
                articleRepository.toggleSaved(article.uniqueId, !article.isSaved)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun selectArticle(article: Article) {
        _uiState.update { it.copy(selectedArticle = article) }
        // Mark as read when selecting
        markAsRead(article.uniqueId, true)
    }

    fun clearSelectedArticle() {
        _uiState.update { it.copy(selectedArticle = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
