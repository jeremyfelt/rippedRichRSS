package com.rippedrss.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rippedrss.android.data.preferences.AppPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val backgroundRefreshEnabled: Boolean = false,
    val wifiOnly: Boolean = true,
    val lastRefreshTime: Long? = null,
    val darkMode: Boolean = false
)

class SettingsViewModel(private val appPreferences: AppPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                appPreferences.backgroundRefreshEnabled,
                appPreferences.wifiOnly,
                appPreferences.lastRefreshTime,
                appPreferences.darkMode
            ) { backgroundRefresh, wifi, lastRefresh, dark ->
                SettingsUiState(
                    backgroundRefreshEnabled = backgroundRefresh,
                    wifiOnly = wifi,
                    lastRefreshTime = lastRefresh,
                    darkMode = dark
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

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setDarkMode(enabled)
        }
    }
}
