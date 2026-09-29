package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BingoGemsColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldLight,
    secondary = DiamondCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E4354),
    onSecondaryContainer = Color(0xFFA5F3FC),
    tertiary = AmberJackpot,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF5A3906),
    onTertiaryContainer = GoldAccent,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = RubyRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BingoGemsColorScheme,
        typography = Typography,
        content = content
    )
}
