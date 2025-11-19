package com.rippedrss.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {
    companion object {
        private val BACKGROUND_REFRESH_ENABLED = booleanPreferencesKey("background_refresh_enabled")
        private val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        private val LAST_REFRESH_TIME = longPreferencesKey("last_refresh_time")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val backgroundRefreshEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[BACKGROUND_REFRESH_ENABLED] ?: false }

    val wifiOnly: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[WIFI_ONLY] ?: true }

    val lastRefreshTime: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[LAST_REFRESH_TIME] }

    val darkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DARK_MODE] ?: false }

    suspend fun setBackgroundRefreshEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BACKGROUND_REFRESH_ENABLED] = enabled
        }
    }

    suspend fun setWifiOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_ONLY] = enabled
        }
    }

    suspend fun setLastRefreshTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_REFRESH_TIME] = timestamp
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }
}
