package com.rippedrss.android.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme colors
val PrimaryLight = Color(0xFF1976D2)
val SecondaryLight = Color(0xFF424242)
val TertiaryLight = Color(0xFF00796B)
val BackgroundLight = Color(0xFFFAFAFA)
val SurfaceLight = Color(0xFFFFFFFF)
val OnPrimaryLight = Color(0xFFFFFFFF)
val OnSecondaryLight = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF1C1B1F)
val OnSurfaceLight = Color(0xFF1C1B1F)

// Dark theme colors
val PrimaryDark = Color(0xFF90CAF9)
val SecondaryDark = Color(0xFFBDBDBD)
val TertiaryDark = Color(0xFF80CBC4)
val BackgroundDark = Color(0xFF1C1B1F)
val SurfaceDark = Color(0xFF2C2C2C)
val OnPrimaryDark = Color(0xFF003258)
val OnSecondaryDark = Color(0xFF2C2C2C)
val OnBackgroundDark = Color(0xFFE6E1E5)
val OnSurfaceDark = Color(0xFFE6E1E5)

// Sepia theme colors (warm, paper-like color scheme)
val PrimarySepia = Color(0xFF8B4513)       // Saddle Brown
val SecondarySepia = Color(0xFF6B4423)     // Dark Brown
val TertiarySepia = Color(0xFF704214)      // Sepia Brown
val BackgroundSepia = Color(0xFFF4ECD8)    // Cream/Paper
val SurfaceSepia = Color(0xFFFAF6ED)       // Lighter Cream
val OnPrimarySepia = Color(0xFFFFFFFF)
val OnSecondarySepia = Color(0xFFFFFFFF)
val OnBackgroundSepia = Color(0xFF3E2723)  // Dark Brown Text
val OnSurfaceSepia = Color(0xFF3E2723)

/**
 * Theme colors for article WebView rendering.
 */
data class ArticleThemeColors(
    val backgroundColor: String,
    val textColor: String,
    val secondaryTextColor: String,
    val linkColor: String,
    val codeBackgroundColor: String,
    val borderColor: String
) {
    companion object {
        val Light = ArticleThemeColors(
            backgroundColor = "#FAFAFA",
            textColor = "#1C1B1F",
            secondaryTextColor = "#666666",
            linkColor = "#1976D2",
            codeBackgroundColor = "#F5F5F5",
            borderColor = "#E0E0E0"
        )

        val Dark = ArticleThemeColors(
            backgroundColor = "#1C1B1F",
            textColor = "#E6E1E5",
            secondaryTextColor = "#B0B0B0",
            linkColor = "#90CAF9",
            codeBackgroundColor = "#2C2C2C",
            borderColor = "#404040"
        )

        val Sepia = ArticleThemeColors(
            backgroundColor = "#F4ECD8",
            textColor = "#3E2723",
            secondaryTextColor = "#5D4037",
            linkColor = "#8B4513",
            codeBackgroundColor = "#EDE4D0",
            borderColor = "#D7CFC0"
        )

        fun forThemeMode(themeMode: ThemeMode, isSystemDark: Boolean): ArticleThemeColors {
            return when (themeMode) {
                ThemeMode.LIGHT -> Light
                ThemeMode.DARK -> Dark
                ThemeMode.SEPIA -> Sepia
                ThemeMode.SYSTEM -> if (isSystemDark) Dark else Light
            }
        }
    }
}
