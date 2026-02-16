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
 * Usa tonos beige oscuro cálidos en lugar de grises fríos.
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
    
    background = Color(0xFF2a1f1a),      // Beige oscuro cálido
    onBackground = Color(0xFFf5e6d3),    // Beige muy claro

    surface = Color(0xFF3a2f2a),         // Beige oscuro para tarjetas
    onSurface = Color(0xFFf5e6d3),       // Beige muy claro
    surfaceVariant = Color(0xFF4a3f3a),  // Beige medio oscuro
    onSurfaceVariant = Color(0xFFd4c4b0), // Beige medio

    outline = Color(0xFF5a4a4a),         // Marrón medio para bordes
    outlineVariant = Color(0xFF4a3a3a),  // Marrón oscuro

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
