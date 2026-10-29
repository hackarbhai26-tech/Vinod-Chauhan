package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalHudTheme = staticCompositionLocalOf { HudThemes[0] }

@Composable
fun JarvisAITheme(
    hudThemeIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val selectedTheme = HudThemes.getOrElse(hudThemeIndex) { HudThemes[0] }

    val colorScheme = darkColorScheme(
        primary = selectedTheme.primary,
        onPrimary = Color.Black,
        primaryContainer = selectedTheme.surface,
        onPrimaryContainer = selectedTheme.primary,
        secondary = selectedTheme.secondary,
        onSecondary = Color.Black,
        secondaryContainer = selectedTheme.surface,
        onSecondaryContainer = selectedTheme.secondary,
        background = selectedTheme.backgroundStart,
        onBackground = TextPrimary,
        surface = selectedTheme.surface,
        onSurface = TextPrimary,
        surfaceVariant = selectedTheme.surface,
        onSurfaceVariant = TextSecondary,
        outline = selectedTheme.border,
        error = JarvisRed,
        onError = Color.White
    )

    CompositionLocalProvider(LocalHudTheme provides selectedTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
