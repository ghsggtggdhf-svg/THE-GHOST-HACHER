package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1), // Elegant Indigo-500
    secondary = Color(0xFF3B82F6), // Accent Blue-500
    tertiary = AmberPremium,
    background = CosmicBlack,
    surface = SlateNavy,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = CosmicBlack,
    onBackground = TextSilver,
    onSurface = TextSilver,
    outline = BorderNavy
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = AmberPremium,
    background = SoftWhite,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = LightTextDark,
    onBackground = LightTextDark,
    onSurface = LightTextDark,
    outline = LightBorder
)

@Composable
fun SmartFinanceTheme(
    themeMode: String = "dark", // "dark", "light"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
