package com.bansify.browser.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bansify.browser.data.ThemeMode

val LocalIsDarkBrowserTheme = staticCompositionLocalOf { false }

private val ChromeLightColorScheme = lightColorScheme(
    primary = ChromeBluePrimary,
    onPrimary = ChromeBlueOnPrimary,
    primaryContainer = ChromeBluePrimaryContainer,
    onPrimaryContainer = ChromeBlueOnPrimaryContainer,
    secondary = ChromeBluePrimary,
    onSecondary = ChromeBlueOnPrimary,
    secondaryContainer = ChromeLightSurfaceContainerHigh,
    onSecondaryContainer = ChromeLightOnSurface,
    background = ChromeLightBackground,
    onBackground = ChromeLightOnSurface,
    surface = ChromeLightSurface,
    onSurface = ChromeLightOnSurface,
    surfaceVariant = ChromeLightSurfaceContainer,
    onSurfaceVariant = ChromeLightOnSurfaceVariant,
    surfaceContainerLowest = ChromeLightSettingsCanvas,
    surfaceContainerLow = ChromeLightSurfaceContainerLow,
    surfaceContainer = ChromeLightSurfaceContainer,
    surfaceContainerHigh = ChromeLightSurfaceContainerHigh,
    surfaceContainerHighest = ChromeLightSurfaceContainerHighest,
    outline = ChromeLightOutline,
    outlineVariant = ChromeLightOutlineVariant,
    error = DangerRed
)

private val ChromeDarkColorScheme = darkColorScheme(
    primary = ChromeDarkPrimary,
    onPrimary = ChromeDarkOnPrimary,
    primaryContainer = ChromeDarkPrimaryContainer,
    onPrimaryContainer = ChromeDarkOnPrimaryContainer,
    secondary = ChromeDarkPrimary,
    onSecondary = ChromeDarkOnPrimary,
    secondaryContainer = ChromeDarkSurfaceContainerHigh,
    onSecondaryContainer = ChromeDarkOnSurface,
    background = ChromeDarkBackground,
    onBackground = ChromeDarkOnSurface,
    surface = ChromeDarkSurface,
    onSurface = ChromeDarkOnSurface,
    surfaceVariant = ChromeDarkSurfaceContainer,
    onSurfaceVariant = ChromeDarkOnSurfaceVariant,
    surfaceContainerLowest = ChromeDarkSettingsCanvas,
    surfaceContainerLow = ChromeDarkSurfaceContainerLow,
    surfaceContainer = ChromeDarkSurfaceContainer,
    surfaceContainerHigh = ChromeDarkSurfaceContainerHigh,
    surfaceContainerHighest = ChromeDarkSurfaceContainerHighest,
    outline = ChromeDarkOutline,
    outlineVariant = ChromeDarkOutlineVariant,
    error = DangerRedDark
)

private val ChromeIncognitoColorScheme = darkColorScheme(
    primary = IncognitoPrimary,
    onPrimary = ChromeDarkOnPrimary,
    primaryContainer = IncognitoSurfaceContainerHigh,
    onPrimaryContainer = IncognitoOnSurface,
    secondary = IncognitoPrimary,
    onSecondary = ChromeDarkOnPrimary,
    secondaryContainer = IncognitoOmnibox,
    onSecondaryContainer = IncognitoOnSurface,
    background = IncognitoBackground,
    onBackground = IncognitoOnSurface,
    surface = IncognitoSurface,
    onSurface = IncognitoOnSurface,
    surfaceVariant = IncognitoOmnibox,
    onSurfaceVariant = IncognitoOnSurfaceVariant,
    surfaceContainerLowest = IncognitoBackground,
    surfaceContainerLow = IncognitoSurfaceContainer,
    surfaceContainer = IncognitoOmnibox,
    surfaceContainerHigh = IncognitoSurfaceContainerHigh,
    surfaceContainerHighest = IncognitoSurfaceContainerHigh,
    outline = ChromeDarkOutline,
    outlineVariant = ChromeDarkOutlineVariant,
    error = DangerRedDark
)

@Composable
fun BansifyBrowserTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isIncognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when {
        isIncognito -> true
        themeMode == ThemeMode.DARK -> true
        themeMode == ThemeMode.LIGHT -> false
        else -> systemDark
    }

    val colorScheme = when {
        isIncognito -> ChromeIncognitoColorScheme
        useDark -> ChromeDarkColorScheme
        else -> ChromeLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDark
                controller.isAppearanceLightNavigationBars = !useDark
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkBrowserTheme provides useDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
