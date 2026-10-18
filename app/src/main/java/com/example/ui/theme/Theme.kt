package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PaseoColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = CrimsonContainer,
    onPrimaryContainer = OnCrimsonContainer,
    secondary = CrimsonGlow,
    onSecondary = Color.White,
    secondaryContainer = PaseoSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldAccent,
    onTertiary = Color.Black,
    tertiaryContainer = EmeraldDark,
    onTertiaryContainer = EmeraldAccent,
    background = PaseoDarkBackground,
    onBackground = TextPrimary,
    surface = PaseoSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = PaseoSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = PaseoBorderGreen,
    outlineVariant = PaseoBorderRed,
    error = CrimsonGlow,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // For PASEO SENSI, always enforce the signature dark gaming aesthetic
    MaterialTheme(
        colorScheme = PaseoColorScheme,
        typography = Typography,
        content = content
    )
}
