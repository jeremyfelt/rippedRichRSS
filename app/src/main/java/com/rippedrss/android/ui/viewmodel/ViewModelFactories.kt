package com.rippedrss.android.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rippedrss.android.data.preferences.AppPreferences
import com.rippedrss.android.data.repository.ArticleRepository
import com.rippedrss.android.data.repository.FeedRepository

class FeedViewModelFactory(
    private val feedRepository: FeedRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            return FeedViewModel(feedRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ArticleViewModelFactory(
    private val articleRepository: ArticleRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            return ArticleViewModel(articleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val appPreferences: AppPreferences,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(appPreferences, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
