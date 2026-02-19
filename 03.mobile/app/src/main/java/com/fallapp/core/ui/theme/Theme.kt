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
    onSecondary = FallaTextPrimary,
    secondaryContainer = FallaSecondaryLight,
    onSecondaryContainer = FallaTextPrimary,
    
    tertiary = FallaPrimaryDark,
    onTertiary = Seashell,
    
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
    primary = LegacyPrimaryLight,
    onPrimary = FallaTextPrimary,
    primaryContainer = LegacyPrimaryDark,
    onPrimaryContainer = Color.White,
    
    secondary = LegacySecondaryLight,
    onSecondary = FallaTextPrimary,
    secondaryContainer = LegacySecondaryDark,
    onSecondaryContainer = Color.White,
    
    tertiary = LegacyPrimary,
    onTertiary = Color.White,
    
    error = FallaErrorLight,
    onError = Color.White,
    errorContainer = FallaError,
    onErrorContainer = Color.White,
    
    background = LegacyBackgroundDark,
    onBackground = LegacyTextPrimaryDark,

    surface = LegacySurfaceDark,
    onSurface = LegacyTextPrimaryDark,
    surfaceVariant = LegacySurfaceVariantDark,
    onSurfaceVariant = LegacyTextSecondaryDark,

    outline = LegacyOutlineDark,
    outlineVariant = LegacyOutlineVariantDark,

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
