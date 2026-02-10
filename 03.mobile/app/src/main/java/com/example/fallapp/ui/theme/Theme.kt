package com.example.fallapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = OrangeAction,
    onPrimary = Color.White,
    primaryContainer = PeachSurface,
    onPrimaryContainer = DarkText,

    secondary = PeachSurface,
    onSecondary = DarkText,

    background = CreamBackground,
    onBackground = DarkText,

    surface = CreamBackground,
    onSurface = DarkText,

    outline = RedAccent,
    error = ErrorColor
)

@Composable
fun FallAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}