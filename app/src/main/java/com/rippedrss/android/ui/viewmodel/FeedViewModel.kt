package com.rippedrss.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rippedrss.android.data.model.Feed
import com.rippedrss.android.data.repository.FeedRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FeedUiState(
    val feeds: List<Feed> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val addFeedUrl: String = "",
    val isAddingFeed: Boolean = false,
    val addFeedError: String? = null
)

class FeedViewModel(private val feedRepository: FeedRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadFeeds()
    }

    private fun loadFeeds() {
        viewModelScope.launch {
            feedRepository.getAllFeeds()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { feeds ->
                    _uiState.update { it.copy(feeds = feeds, error = null) }
                }
        }
    }

    fun refreshAllFeeds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                feedRepository.refreshAllFeeds()
                _uiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false, error = e.message) }
            }
        }
    }

    fun refreshFeed(feed: Feed) {
        viewModelScope.launch {
            try {
                feedRepository.refreshFeed(feed)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch {
            try {
                feedRepository.deleteFeed(feed)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleFavorite(feed: Feed) {
        viewModelScope.launch {
            try {
                feedRepository.toggleFavorite(feed.id, !feed.isFavorite)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, addFeedUrl = "", addFeedError = null) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, addFeedUrl = "", addFeedError = null, isAddingFeed = false) }
    }

    fun updateAddFeedUrl(url: String) {
        _uiState.update { it.copy(addFeedUrl = url, addFeedError = null) }
    }

    fun addFeed() {
        val url = _uiState.value.addFeedUrl.trim()
        if (url.isEmpty()) {
            _uiState.update { it.copy(addFeedError = "Please enter a URL") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingFeed = true, addFeedError = null) }
            try {
                val result = feedRepository.addFeed(url)
                if (result.isSuccess) {
                    hideAddDialog()
                } else {
                    _uiState.update {
                        it.copy(
                            isAddingFeed = false,
                            addFeedError = result.exceptionOrNull()?.message ?: "Failed to add feed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAddingFeed = false, addFeedError = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
