package com.authfirebaseappjulon.authfirebaseappjulon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AquaAccent,
    secondary = IndigoPrimary,
    tertiary = CoralAccent,
    background = GradientTop,
    surface = IndigoDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = CoralAccent
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    secondary = AquaAccent,
    tertiary = CoralAccent,
    background = SoftSurface,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = IndigoDark,
    onTertiary = Color.White,
    onBackground = DeepText,
    onSurface = DeepText,
    outline = MutedText,
    error = ErrorModern
)

@Composable
fun AuthFirebaseAppJulonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
