package com.rippedrss.android.ui.theme

/**
 * Represents the available theme modes in the app.
 */
enum class ThemeMode(val displayName: String, val value: String) {
    SYSTEM("System", "system"),
    LIGHT("Light", "light"),
    DARK("Dark", "dark"),
    SEPIA("Sepia", "sepia");

    companion object {
        fun fromValue(value: String): ThemeMode {
            return entries.find { it.value == value } ?: SYSTEM
        }
    }
}

/**
 * Represents the available text scale options in the app.
 */
enum class TextScale(val displayName: String, val value: String, val scaleFactor: Float) {
    SMALLER("Smaller", "smaller", 0.85f),
    DEFAULT("Default", "default", 1.0f),
    LARGER("Larger", "larger", 1.15f),
    LARGEST("Largest", "largest", 1.3f);

    companion object {
        fun fromValue(value: String): TextScale {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}
