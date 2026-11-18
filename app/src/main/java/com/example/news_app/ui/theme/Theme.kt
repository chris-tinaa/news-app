package com.example.news_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SeaDeep,
    onPrimary = CoastSurface,
    primaryContainer = SeaLight,
    onPrimaryContainer = CoastText,
    secondary = CoastSecondary,
    onSecondary = CoastSurface,
    secondaryContainer = CoastSecondaryContainer,
    onSecondaryContainer = CoastText,
    tertiary = SandAccent,
    onTertiary = CoastSurface,
    background = CoastBackground,
    onBackground = CoastText,
    surface = CoastSurface,
    onSurface = CoastText,
    surfaceVariant = CoastSurfaceVariant,
    onSurfaceVariant = CoastSecondary,
    outline = CoastOutline
)

private val DarkColors = darkColorScheme(
    primary = SeaDeepDark,
    onPrimary = CoastText,
    primaryContainer = SeaLightDark,
    onPrimaryContainer = CoastTextDark,
    secondary = CoastSecondaryDark,
    onSecondary = CoastText,
    secondaryContainer = CoastSecondaryContainerDark,
    onSecondaryContainer = CoastTextDark,
    tertiary = SandAccentDark,
    onTertiary = CoastText,
    background = CoastBackgroundDark,
    onBackground = CoastTextDark,
    surface = CoastSurfaceDark,
    onSurface = CoastTextDark,
    surfaceVariant = CoastSurfaceVariantDark,
    onSurfaceVariant = CoastSecondaryDark,
    outline = CoastOutlineDark
)

@Composable
fun CoastlineNewsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
