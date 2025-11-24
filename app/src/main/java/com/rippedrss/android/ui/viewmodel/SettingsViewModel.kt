package com.rippedrss.android.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rippedrss.android.data.AppDatabase
import com.rippedrss.android.data.preferences.AppPreferences
import com.rippedrss.android.ui.theme.TextScale
import com.rippedrss.android.ui.theme.ThemeMode
import com.rippedrss.android.util.ArticleHtmlCache
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val backgroundRefreshEnabled: Boolean = false,
    val wifiOnly: Boolean = true,
    val lastRefreshTime: Long? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textScale: TextScale = TextScale.DEFAULT,
    val isResetting: Boolean = false,
    // Legacy compatibility
    val darkMode: Boolean = false
)

class SettingsViewModel(
    private val appPreferences: AppPreferences,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _resetComplete = MutableSharedFlow<Boolean>()
    val resetComplete: SharedFlow<Boolean> = _resetComplete.asSharedFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                appPreferences.backgroundRefreshEnabled,
                appPreferences.wifiOnly,
                appPreferences.lastRefreshTime,
                appPreferences.themeMode,
                appPreferences.textScale
            ) { backgroundRefresh, wifi, lastRefresh, theme, scale ->
                SettingsUiState(
                    backgroundRefreshEnabled = backgroundRefresh,
                    wifiOnly = wifi,
                    lastRefreshTime = lastRefresh,
                    themeMode = theme,
                    textScale = scale,
                    darkMode = theme == ThemeMode.DARK
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setBackgroundRefreshEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setBackgroundRefreshEnabled(enabled)
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setWifiOnly(enabled)
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            appPreferences.setThemeMode(themeMode)
        }
    }

    fun setTextScale(textScale: TextScale) {
        viewModelScope.launch {
            appPreferences.setTextScale(textScale)
            // Clear article cache when text scale changes
            ArticleHtmlCache.getInstance(context).clearAll()
        }
    }

    /**
     * Legacy setter for backward compatibility.
     */
    fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    /**
     * Resets all app data including feeds, articles, and settings.
     */
    fun resetAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true) }

            try {
                // Clear database
                val database = AppDatabase.getDatabase(context)
                database.clearAllTables()

                // Clear preferences
                appPreferences.clearAll()

                // Clear article cache
                ArticleHtmlCache.getInstance(context).clearAll()

                _resetComplete.emit(true)
            } catch (e: Exception) {
                _resetComplete.emit(false)
            } finally {
                _uiState.update { it.copy(isResetting = false) }
            }
        }
    }
}
