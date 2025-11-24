package com.rippedrss.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rippedrss.android.ui.theme.TextScale
import com.rippedrss.android.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {
    companion object {
        private val BACKGROUND_REFRESH_ENABLED = booleanPreferencesKey("background_refresh_enabled")
        private val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        private val LAST_REFRESH_TIME = longPreferencesKey("last_refresh_time")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val TEXT_SCALE = stringPreferencesKey("text_scale")

        // Legacy key for migration
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val backgroundRefreshEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[BACKGROUND_REFRESH_ENABLED] ?: false }

    val wifiOnly: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[WIFI_ONLY] ?: true }

    val lastRefreshTime: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[LAST_REFRESH_TIME] }

    /**
     * Theme mode setting. Migrates from legacy darkMode boolean if needed.
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            // Check for new theme mode setting first
            preferences[THEME_MODE]?.let { ThemeMode.fromValue(it) }
                ?: run {
                    // Migrate from legacy dark mode setting
                    val legacyDarkMode = preferences[DARK_MODE] ?: false
                    if (legacyDarkMode) ThemeMode.DARK else ThemeMode.SYSTEM
                }
        }

    /**
     * Text scale setting for article content.
     */
    val textScale: Flow<TextScale> = context.dataStore.data
        .map { preferences ->
            preferences[TEXT_SCALE]?.let { TextScale.fromValue(it) } ?: TextScale.DEFAULT
        }

    /**
     * Legacy dark mode accessor for backward compatibility.
     * Returns true if theme is DARK, false otherwise.
     */
    val darkMode: Flow<Boolean> = themeMode.map { it == ThemeMode.DARK }

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

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.value
            // Clear legacy setting
            preferences.remove(DARK_MODE)
        }
    }

    suspend fun setTextScale(textScale: TextScale) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SCALE] = textScale.value
        }
    }

    /**
     * Legacy dark mode setter for backward compatibility.
     */
    suspend fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    /**
     * Clears all preferences. Used for data reset.
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
