package com.lintang.sarprasq.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PastelSkyBlueDark,
    onPrimary = Color.White,
    primaryContainer = PastelSkyBlueContainer,
    onPrimaryContainer = PastelSkyBlueDark,
    secondary = PastelMintDark,
    onSecondary = Color.White,
    secondaryContainer = PastelMintLight,
    onSecondaryContainer = PastelMintDark,
    tertiary = PastelLavenderDark,
    onTertiary = Color.White,
    tertiaryContainer = PastelLavenderContainer,
    onTertiaryContainer = PastelLavenderDark,
    background = PastelBackground,
    onBackground = TextPrimary,
    surface = PastelSurface,
    onSurface = TextPrimary,
    surfaceVariant = PastelCardBorder,
    onSurfaceVariant = TextSecondary,
    outline = PastelCardBorder
)

@Composable
fun SarprasQTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

