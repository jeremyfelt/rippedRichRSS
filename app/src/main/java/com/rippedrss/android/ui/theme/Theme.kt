package com.rippedrss.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = TertiaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = OnPrimaryLight,
    onSecondary = OnSecondaryLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnPrimaryDark,
    onSecondary = OnSecondaryDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark
)

private val SepiaColorScheme = lightColorScheme(
    primary = PrimarySepia,
    secondary = SecondarySepia,
    tertiary = TertiarySepia,
    background = BackgroundSepia,
    surface = SurfaceSepia,
    onPrimary = OnPrimarySepia,
    onSecondary = OnSecondarySepia,
    onBackground = OnBackgroundSepia,
    onSurface = OnSurfaceSepia
)

/**
 * Returns whether the current theme should use dark status bar icons.
 */
fun ThemeMode.usesLightStatusBar(isSystemDark: Boolean): Boolean {
    return when (this) {
        ThemeMode.LIGHT, ThemeMode.SEPIA -> true
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !isSystemDark
    }
}

@Composable
fun RippedRichRSSTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            when (themeMode) {
                ThemeMode.LIGHT -> dynamicLightColorScheme(context)
                ThemeMode.DARK -> dynamicDarkColorScheme(context)
                ThemeMode.SEPIA -> SepiaColorScheme  // No dynamic sepia
                ThemeMode.SYSTEM -> if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
        }
        else -> when (themeMode) {
            ThemeMode.LIGHT -> LightColorScheme
            ThemeMode.DARK -> DarkColorScheme
            ThemeMode.SEPIA -> SepiaColorScheme
            ThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                themeMode.usesLightStatusBar(isSystemDark)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Legacy theme function for backward compatibility.
 */
@Composable
fun RippedRichRSSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RippedRichRSSTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        dynamicColor = dynamicColor,
        content = content
    )
}
