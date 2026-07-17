package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CryptoPrimary,
    onPrimary = Color.White,
    secondary = CryptoSecondary,
    onSecondary = Color.White,
    tertiary = CryptoTertiary,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = WhiteText,
    surface = DarkSurface,
    onSurface = WhiteText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = GrayText,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for cryptocurrency trading UI
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our tailored palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
