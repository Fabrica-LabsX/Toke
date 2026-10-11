package com.flabs.cobrate.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Modos de tema soportados por TOKE!
 */
enum class ThemeMode {
    LIGHT, DARK
}

val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF10B968),
    onPrimary = TokeSurface,
    primaryContainer = Color(0xFF111D16),
    onPrimaryContainer = Color(0xFF10B968),
    
    secondary = Color(0xFF10B968),
    onSecondary = TokeSurface,
    secondaryContainer = Color(0xFF111D16),
    onSecondaryContainer = Color(0xFF10B968),
    
    background = TokeDarkBackground,
    onBackground = Color(0xFFFFFFFF),
    surface = TokeDarkSurface,
    onSurface = Color(0xFFFFFFFF),
    
    outline = Color(0x33FFFFFF),
    surfaceVariant = Color(0xFF1E222B),
    onSurfaceVariant = Color(0xFFD4D4D4),

    tertiary = Color(0xFFFBBF24),
    tertiaryContainer = Color(0xFF211A10),
    onTertiaryContainer = Color(0xFFFBBF24),

    error = TokeError,
    errorContainer = TokeErrorContainer,
    onError = TokeSurface,
    onErrorContainer = TokeOnErrorContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = TokeGreen,
    onPrimary = TokeSurface,
    primaryContainer = TokeGreenSoft,
    onPrimaryContainer = TokeGreenDeep,
    
    secondary = TokeGreen,
    onSecondary = TokeSurface,
    secondaryContainer = TokeGreenSoft,
    onSecondaryContainer = TokeGreenDeep,
    
    background = TokeBackground,
    onBackground = TokeTextPrimary,
    surface = TokeSurface,
    onSurface = TokeTextPrimary,
    
    surfaceVariant = TokeGreenTint,
    onSurfaceVariant = TokeTextSecondary,
    
    outline = TokeOutline,

    tertiary = TokeWarning,
    tertiaryContainer = TokeWarningContainer,
    onTertiaryContainer = TokeOnWarningContainer,

    error = TokeError,
    errorContainer = TokeErrorContainer,
    onError = TokeSurface,
    onErrorContainer = TokeOnErrorContainer,
)

@Composable
fun CobrateTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    // Dynamic color is available on Android 12+ (disabled for TOKE)
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = themeMode == ThemeMode.DARK
    
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        typography = Typography,
        content = content,
    )
}
