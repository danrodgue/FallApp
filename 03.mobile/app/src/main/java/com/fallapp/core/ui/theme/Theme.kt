package com.fallapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Esquema de colores claro de FallApp.
 * Usa la paleta de colores coincidente con 02.desktop (rojo #c62828).
 */
private val LightColorScheme = lightColorScheme(
    primary = FallaPrimary,
    onPrimary = Color.White,
    primaryContainer = FallaPrimaryLight,
    onPrimaryContainer = FallaTextPrimary,
    
    secondary = FallaSecondary,
    onSecondary = Color.White,
    secondaryContainer = FallaSecondaryLight,
    onSecondaryContainer = FallaTextPrimary,
    
    tertiary = FallaPrimaryDark,
    onTertiary = Color.White,
    
    error = FallaError,
    onError = Color.White,
    errorContainer = FallaErrorLight,
    onErrorContainer = FallaErrorDark,
    
    background = FallaBackgroundLight,
    onBackground = FallaTextPrimary,
    
    surface = FallaCardBackground,
    onSurface = FallaTextPrimary,
    surfaceVariant = FallaSurfaceLight,
    onSurfaceVariant = FallaTextSecondary,
    
    outline = FallaBorder,
    outlineVariant = FallaBorderLight,
    
    scrim = Color.Black
)

/**
 * Esquema de colores oscuro de FallApp.
 * Invertido para modo oscuro pero manteniendo los acentos rojos.
 */
private val DarkColorScheme = darkColorScheme(
    primary = FallaPrimaryLight,
    onPrimary = FallaTextPrimary,
    primaryContainer = FallaPrimaryDark,
    onPrimaryContainer = Color.White,
    
    secondary = FallaSecondaryLight,
    onSecondary = FallaTextPrimary,
    secondaryContainer = FallaSecondaryDark,
    onSecondaryContainer = Color.White,
    
    tertiary = FallaPrimary,
    onTertiary = Color.White,
    
    error = FallaErrorLight,
    onError = FallaErrorDark,
    errorContainer = FallaError,
    onErrorContainer = Color.White,
    
    background = Color(0xFF1a0a0a),
    onBackground = Color(0xFFfef7f7),
    
    surface = Color(0xFF2a1a1a),
    onSurface = Color(0xFFfef7f7),
    surfaceVariant = Color(0xFF3a2a2a),
    onSurfaceVariant = Color(0xFFe5d5d5),
    
    outline = Color(0xFF5a4a4a),
    outlineVariant = Color(0xFF4a3a3a),
    
    scrim = Color.Black
)

/**
 * Tema de FallApp con paleta de colores coincidente con 02.desktop.
 * 
 * Características:
 * - Color primario: Rojo de las Fallas (#c62828)
 * - Backgrounds: Tonos claros y cálidos (#fff5f6)
 * - Textos: Oscuros para buen contraste (#1a0a0a)
 * - Soporte para modo claro y oscuro
 * 
 * @param darkTheme Si true, usa el esquema oscuro. Por defecto detecta el tema del sistema.
 * @param content Contenido de la app que usará este tema.
 */
@Composable
fun FallAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
