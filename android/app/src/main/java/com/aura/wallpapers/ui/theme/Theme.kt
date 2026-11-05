package com.aura.wallpapers.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.aura.wallpapers.domain.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = WallixaPrimaryContainer,
    onPrimary = WallixaOnPrimary,
    primaryContainer = WallixaPrimary,
    onPrimaryContainer = WallixaOnPrimaryContainer,
    secondary = WallixaSecondaryContainer,
    onSecondary = WallixaDarkBackground,
    secondaryContainer = WallixaDarkSurfaceContainer,
    onSecondaryContainer = WallixaAccentLavender,
    tertiary = WallixaTertiaryContainer,
    onTertiary = WallixaOnPrimary,
    background = WallixaDarkBackground,
    onBackground = WallixaDarkTextPrimary,
    surface = WallixaDarkSurface,
    onSurface = WallixaDarkTextPrimary,
    surfaceVariant = WallixaDarkSurfaceContainer,
    onSurfaceVariant = WallixaDarkTextSecondary,
    outline = WallixaDarkOutline,
    error = StatusError,
    errorContainer = StatusErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = WallixaPrimary,
    onPrimary = WallixaOnPrimary,
    primaryContainer = WallixaPrimaryContainer,
    onPrimaryContainer = WallixaOnPrimaryContainer,
    secondary = WallixaSecondary,
    onSecondary = WallixaOnPrimary,
    secondaryContainer = WallixaSecondaryContainer,
    onSecondaryContainer = WallixaOnSecondaryContainer,
    tertiary = WallixaTertiary,
    onTertiary = WallixaOnPrimary,
    background = WallixaSurface,
    onBackground = WallixaOnSurface,
    surface = WallixaSurface,
    onSurface = WallixaOnSurface,
    surfaceVariant = WallixaSurfaceContainer,
    onSurfaceVariant = WallixaOnSurfaceVariant,
    outline = WallixaOutline,
    outlineVariant = WallixaOutlineVariant,
    error = StatusError,
    errorContainer = StatusErrorContainer
)

@Composable
fun AuraWallpapersTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
