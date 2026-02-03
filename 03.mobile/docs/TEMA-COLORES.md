# 🎨 Tema y Paleta de Colores - FallApp Mobile

**Fecha:** 2026-02-03  
**Estado:** ✅ IMPLEMENTADO  
**Versión:** 1.0.0

---

## 📋 Resumen

FallApp Mobile utiliza una **paleta de colores coincidente con la versión desktop** (02.desktop), centrada en el **rojo característico de las Fallas valencianas** (`#c62828`). El tema está implementado con Material 3 y soporte para modo claro y oscuro.

**Objetivos del diseño:**
- ✅ Consistencia visual entre desktop y mobile
- ✅ Identidad de marca basada en las Fallas
- ✅ Accesibilidad y contraste adecuados
- ✅ Soporte para modo claro y oscuro
- ✅ Uso de Material Design 3

---

## 🎨 Paleta de Colores

### Colores Primarios (Rojo de las Fallas)

| Color | Hex | Uso |
|-------|-----|-----|
| **Primary** | `#c62828` | Botones principales, FABs, íconos destacados |
| **Primary Dark** | `#9b2222` | Hover states, ripples, sombras |
| **Primary Light** | `#ff5f52` | Backgrounds sutiles, contenedores primarios |

```kotlin
// Color.kt
val FallaPrimary = Color(0xFFc62828)
val FallaPrimaryDark = Color(0xFF9b2222)
val FallaPrimaryLight = Color(0xFFff5f52)
```

### Backgrounds

| Color | Hex | Uso |
|-------|-----|-----|
| **Background Light** | `#fff5f6` | Fondo principal de la app (modo claro) |
| **Card Background** | `#ffffff` | Tarjetas, surfaces, diálogos |
| **Surface Light** | `#fef7f7` | Variante de surface para jerarquía |

```kotlin
val FallaBackgroundLight = Color(0xFFfff5f6)
val FallaCardBackground = Color(0xFFffffff)
val FallaSurfaceLight = Color(0xFFfef7f7)
```

### Textos

| Color | Hex | Uso |
|-------|-----|-----|
| **Text Primary** | `#1a0a0a` | Títulos, textos principales |
| **Text Secondary** | `#4a2b2b` | Subtítulos, textos de soporte |
| **Text Muted** | `#6b7280` | Hints, textos deshabilitados, metadatos |

```kotlin
val FallaTextPrimary = Color(0xFF1a0a0a)
val FallaTextSecondary = Color(0xFF4a2b2b)
val FallaTextMuted = Color(0xFF6b7280)
```

### Bordes

| Color | Hex | Uso |
|-------|-----|-----|
| **Border** | `#e5e7eb` | Bordes de inputs, divisores principales |
| **Border Light** | `#f3f4f6` | Divisores sutiles, separadores de secciones |

```kotlin
val FallaBorder = Color(0xFFe5e7eb)
val FallaBorderLight = Color(0xFFf3f4f6)
```

### Colores Secundarios (Tonos Cálidos)

| Color | Hex | Uso |
|-------|-----|-----|
| **Secondary** | `#ff6f00` | Acciones secundarias, FABs secundarios |
| **Secondary Dark** | `#c43e00` | Hover de secundario |
| **Secondary Light** | `#ffa040` | Backgrounds secundarios |

```kotlin
val FallaSecondary = Color(0xFFff6f00)
val FallaSecondaryDark = Color(0xFFc43e00)
val FallaSecondaryLight = Color(0xFFffa040)
```

### Estados (Error, Success)

| Color | Hex | Uso |
|-------|-----|-----|
| **Error** | `#d32f2f` | Mensajes de error, íconos de alerta |
| **Error Light** | `#ff6659` | Backgrounds de error |
| **Error Dark** | `#9a0007` | Texto sobre error light |
| **Success** | `#2e7d32` | Confirmaciones, estados positivos |
| **Success Light** | `#60ad5e` | Backgrounds de éxito |
| **Success Dark** | `#005005` | Texto sobre success light |

```kotlin
val FallaError = Color(0xFFd32f2f)
val FallaErrorLight = Color(0xFFff6659)
val FallaErrorDark = Color(0xFF9a0007)

val FallaSuccess = Color(0xFF2e7d32)
val FallaSuccessLight = Color(0xFF60ad5e)
val FallaSuccessDark = Color(0xFF005005)
```

---

## 🌓 Esquemas de Color

### Modo Claro (Light Color Scheme)

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = FallaPrimary,                    // #c62828
    onPrimary = Color.White,
    primaryContainer = FallaPrimaryLight,      // #ff5f52
    onPrimaryContainer = FallaTextPrimary,     // #1a0a0a
    
    secondary = FallaSecondary,                // #ff6f00
    onSecondary = Color.White,
    secondaryContainer = FallaSecondaryLight,  // #ffa040
    onSecondaryContainer = FallaTextPrimary,
    
    tertiary = FallaPrimaryDark,               // #9b2222
    onTertiary = Color.White,
    
    error = FallaError,                        // #d32f2f
    onError = Color.White,
    errorContainer = FallaErrorLight,          // #ff6659
    onErrorContainer = FallaErrorDark,         // #9a0007
    
    background = FallaBackgroundLight,         // #fff5f6
    onBackground = FallaTextPrimary,           // #1a0a0a
    
    surface = FallaCardBackground,             // #ffffff
    onSurface = FallaTextPrimary,
    surfaceVariant = FallaSurfaceLight,        // #fef7f7
    onSurfaceVariant = FallaTextSecondary,     // #4a2b2b
    
    outline = FallaBorder,                     // #e5e7eb
    outlineVariant = FallaBorderLight,         // #f3f4f6
    
    scrim = Color.Black
)
```

**Características del modo claro:**
- Background cálido (`#fff5f6`) para suavidad visual
- Textos oscuros con excelente contraste
- Bordes sutiles pero visibles
- Acentos rojos vibrantes para acciones principales

### Modo Oscuro (Dark Color Scheme)

```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = FallaPrimaryLight,               // #ff5f52
    onPrimary = FallaTextPrimary,              // #1a0a0a
    primaryContainer = FallaPrimaryDark,       // #9b2222
    onPrimaryContainer = Color.White,
    
    secondary = FallaSecondaryLight,           // #ffa040
    onSecondary = FallaTextPrimary,
    secondaryContainer = FallaSecondaryDark,   // #c43e00
    onSecondaryContainer = Color.White,
    
    tertiary = FallaPrimary,                   // #c62828
    onTertiary = Color.White,
    
    error = FallaErrorLight,                   // #ff6659
    onError = FallaErrorDark,                  // #9a0007
    errorContainer = FallaError,               // #d32f2f
    onErrorContainer = Color.White,
    
    background = Color(0xFF1a0a0a),            // Casi negro con tinte cálido
    onBackground = Color(0xFFfef7f7),          // Casi blanco con tinte cálido
    
    surface = Color(0xFF2a1a1a),               // Superficie oscura
    onSurface = Color(0xFFfef7f7),
    surfaceVariant = Color(0xFF3a2a2a),        // Variante más clara
    onSurfaceVariant = Color(0xFFe5d5d5),
    
    outline = Color(0xFF5a4a4a),               // Bordes visibles en oscuro
    outlineVariant = Color(0xFF4a3a3a),
    
    scrim = Color.Black
)
```

**Características del modo oscuro:**
- Negros con tintes cálidos (no negro puro)
- Rojos más claros para mantener contraste
- Surfaces con jerarquía visible
- Reducción de fatiga visual en ambientes oscuros

---

## 📦 Estructura de Archivos

```
app/src/main/java/com/fallapp/core/ui/theme/
├── Color.kt        # Definición de todos los colores
├── Theme.kt        # Esquemas de color y composable FallAppTheme
└── Type.kt         # Tipografía (Material 3 Typography)
```

### Color.kt

```kotlin
package com.fallapp.core.ui.theme

import androidx.compose.ui.graphics.Color

// Colores primarios (rojo de las Fallas)
val FallaPrimary = Color(0xFFc62828)
val FallaPrimaryDark = Color(0xFF9b2222)
val FallaPrimaryLight = Color(0xFFff5f52)

// Backgrounds
val FallaBackgroundLight = Color(0xFFfff5f6)
val FallaCardBackground = Color(0xFFffffff)
val FallaSurfaceLight = Color(0xFFfef7f7)

// Texto
val FallaTextPrimary = Color(0xFF1a0a0a)
val FallaTextSecondary = Color(0xFF4a2b2b)
val FallaTextMuted = Color(0xFF6b7280)

// Bordes
val FallaBorder = Color(0xFFe5e7eb)
val FallaBorderLight = Color(0xFFf3f4f6)

// Colores secundarios
val FallaSecondary = Color(0xFFff6f00)
val FallaSecondaryDark = Color(0xFFc43e00)
val FallaSecondaryLight = Color(0xFFffa040)

// Error/Success
val FallaError = Color(0xFFd32f2f)
val FallaErrorLight = Color(0xFFff6659)
val FallaErrorDark = Color(0xFF9a0007)

val FallaSuccess = Color(0xFF2e7d32)
val FallaSuccessLight = Color(0xFF60ad5e)
val FallaSuccessDark = Color(0xFF005005)
```

### Theme.kt

```kotlin
package com.fallapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    // ... (ver arriba)
)

private val DarkColorScheme = darkColorScheme(
    // ... (ver arriba)
)

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
```

### Type.kt

Define la tipografía Material 3 completa (ver archivo para detalles).

---

## 🚀 Uso del Tema

### En MainActivity.kt

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FallAppTheme {  // ← Aplica el tema aquí
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
```

### En Componentes Compose

```kotlin
@Composable
fun MiComponente() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)  // Background de tema
    ) {
        Text(
            text = "Título",
            style = MaterialTheme.typography.headlineMedium,   // Tipografía de tema
            color = MaterialTheme.colorScheme.onBackground     // Color de texto de tema
        )
        
        Button(
            onClick = { /* ... */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,     // Rojo #c62828
                contentColor = MaterialTheme.colorScheme.onPrimary      // Blanco
            )
        ) {
            Text("Acción Principal")
        }
    }
}
```

---

## 🎯 Componentes Comunes

### Botones

```kotlin
// Botón primario (rojo)
Button(onClick = { /* ... */ }) {
    Text("Guardar")  // Usa automáticamente colores del tema
}

// Botón secundario (naranja)
Button(
    onClick = { /* ... */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary
    )
) {
    Text("Cancelar")
}

// Botón de error
Button(
    onClick = { /* ... */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error
    )
) {
    Text("Eliminar")
}
```

### Cards

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,  // Blanco en modo claro
        contentColor = MaterialTheme.colorScheme.onSurface   // Texto negro
    )
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Contenido", style = MaterialTheme.typography.bodyLarge)
    }
}
```

### TextFields

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Email") },
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,      // Rojo al focus
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,    // Gris cuando no está enfocado
        cursorColor = MaterialTheme.colorScheme.primary              // Cursor rojo
    )
)
```

### Iconos

```kotlin
Icon(
    imageVector = Icons.Default.Star,
    contentDescription = "Favorito",
    tint = MaterialTheme.colorScheme.primary  // Rojo
)
```

---

## 📐 Comparación Desktop vs Mobile

| Aspecto | Desktop (CSS) | Mobile (Kotlin) |
|---------|---------------|-----------------|
| **Color Primario** | `#c62828` | `Color(0xFFc62828)` |
| **Background** | `#fff5f6` | `Color(0xFFfff5f6)` |
| **Texto Principal** | `#1a0a0a` | `Color(0xFF1a0a0a)` |
| **Botón Primario** | `background: #c62828` | `ButtonDefaults.buttonColors(primary)` |
| **Hover** | `#9b2222` | Ripple con `primaryDark` |
| **Borde** | `#e5e7eb` | `outline` en theme |

**Resultado:** Identidad visual consistente entre plataformas.

---

## 🎨 Recursos XML (colors.xml)

Aunque el tema se define en Kotlin, `colors.xml` mantiene referencias para compatibilidad:

```xml
<!-- app/src/main/res/values/colors.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Paleta de colores coincidente con 02.desktop -->
    
    <!-- Color primario (rojo de las Fallas) -->
    <color name="primary">#c62828</color>
    <color name="primary_dark">#9b2222</color>
    <color name="primary_light">#ff5f52</color>
    
    <!-- Backgrounds -->
    <color name="background_light">#fff5f6</color>
    <color name="card_background">#ffffff</color>
    
    <!-- Texto -->
    <color name="text_primary">#1a0a0a</color>
    <color name="text_secondary">#4a2b2b</color>
    <color name="text_muted">#6b7280</color>
    
    <!-- Bordes -->
    <color name="border">#e5e7eb</color>
    <color name="border_light">#f3f4f6</color>
    
    <!-- Básicos -->
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    
    <!-- Marcador del mapa (punto rojo) -->
    <color name="map_marker">#c62828</color>
</resources>
```

**Uso:** Principalmente para marcadores del mapa y vistas XML legacy (si las hay).

---

## ✅ Checklist de Implementación

- [x] Crear `Color.kt` con paleta completa
- [x] Crear `Theme.kt` con esquemas claro y oscuro
- [x] Crear `Type.kt` con tipografía Material 3
- [x] Aplicar `FallAppTheme` en `MainActivity`
- [x] Actualizar `colors.xml` para compatibilidad
- [x] Usar `MaterialTheme.colorScheme` en todos los componentes
- [x] Probar en modo claro y oscuro
- [x] Verificar contraste de accesibilidad (WCAG AA)
- [x] Documentar paleta y uso

---

## 🔍 Accesibilidad

### Ratios de Contraste (WCAG 2.1)

| Combinación | Ratio | Estado |
|-------------|-------|--------|
| Primary (#c62828) / White | 5.5:1 | ✅ AA Large Text |
| Text Primary (#1a0a0a) / Background (#fff5f6) | 18.2:1 | ✅ AAA |
| Text Secondary (#4a2b2b) / Background | 11.5:1 | ✅ AAA |
| Text Muted (#6b7280) / Background | 4.9:1 | ✅ AA |
| Error (#d32f2f) / White | 5.2:1 | ✅ AA Large Text |

**Conclusión:** Todos los pares de colores cumplen con WCAG AA o superior para texto.

### Recomendaciones

- ✅ Texto principal siempre usa `onBackground` o `onSurface`
- ✅ Botones primarios con texto blanco sobre rojo cumplen AA para Large Text (14pt bold / 18pt regular)
- ✅ Íconos informativos usan `Text Muted` con ratio 4.9:1 (AA)
- ✅ Estados de error usan combinaciones de alto contraste

---

## 📚 Referencias

- [Material Design 3 - Color System](https://m3.material.io/styles/color/overview)
- [Compose Material 3 - ColorScheme](https://developer.android.com/jetpack/compose/designsystems/material3#color-scheme)
- [WCAG 2.1 - Contrast Guidelines](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)
- [02.desktop/src/renderer/styles/login.css](../../02.desktop/src/renderer/styles/login.css) - Referencia de paleta desktop

---

## 📝 Changelog

### v1.0.0 (2026-02-03)
- **ADDED**: Tema completo de Material 3 con paleta coincidente con desktop
- **ADDED**: Soporte para modo claro y oscuro
- **ADDED**: Colores en `colors.xml` para compatibilidad
- **ADDED**: Documentación completa de uso
- **ADDED**: Validación de accesibilidad WCAG 2.1

---

**Mantenido por:** Equipo FallApp Mobile  
**Última actualización:** 2026-02-03
