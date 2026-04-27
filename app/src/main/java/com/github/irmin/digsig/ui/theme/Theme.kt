package com.github.irmin.digsig.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color(0xFF082C7E),
    secondary = Indigo80,
    onSecondary = Color(0xFF2B2777),
    tertiary = Teal80,
    onTertiary = Color(0xFF004B49),
    background = SlateDark,
    onBackground = Color(0xFFE9ECF8),
    surface = Color(0xFF0B1220),
    onSurface = Color(0xFFE9ECF8),
    surfaceVariant = Color(0xFF1D2940),
    onSurfaceVariant = Color(0xFFC0C9DE),
    outline = Color(0xFF31405E),
    outlineVariant = Color(0xFF1F2940),
    error = Color(0xFFFF6B81),
    errorContainer = Color(0xFF4A1825),
    secondaryContainer = Color(0xFF202457),
    onSecondaryContainer = Color(0xFFE2E1FF)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    secondary = Indigo40,
    onSecondary = Color.White,
    tertiary = Teal40,
    onTertiary = Color.White,
    background = SlateLight,
    onBackground = Color(0xFF121826),
    surface = Color(0xFFFAFBFF),
    onSurface = Color(0xFF121826),
    surfaceVariant = Color(0xFFE2E9F6),
    onSurfaceVariant = Color(0xFF465066),
    outline = Color(0xFFBBC7DD),
    outlineVariant = Color(0xFFD6DEEE),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    secondaryContainer = Color(0xFFE7E7FF),
    onSecondaryContainer = Color(0xFF23205A)
)

@Composable
fun DigSigTheme(
    darkTheme: Boolean = true,
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