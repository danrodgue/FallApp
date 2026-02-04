# ADR-010: Sistema de Tema Material 3 con Paleta Desktop

**Fecha:** 2026-02-03  
**Estado:** ✅ Aceptado e Implementado  
**Contexto:** Mobile App (03.mobile)

---

## Contexto

FallApp tiene una aplicación desktop (Electron) y una aplicación móvil (Android). Para mantener consistencia de marca y coherencia visual, se necesita un sistema de tema que:
1. Use los mismos colores en ambas plataformas
2. Respete las guías de diseño de Material 3
3. Soporte modo claro y oscuro
4. Sea fácil de mantener y extender

El color principal de la marca es el **rojo de las Fallas valencianas** (`#c62828`).

---

## Decisión

**Se decidió implementar un sistema de tema completo basado en Material 3 con paleta de colores coincidente con la aplicación desktop.**

### Implementación

1. **Crear archivo `Color.kt`** con todas las definiciones de color
2. **Crear archivo `Theme.kt`** con esquemas Light/Dark y composable `FallAppTheme`
3. **Crear archivo `Type.kt`** con tipografía Material 3
4. **Aplicar `FallAppTheme`** en `MainActivity` para toda la app
5. **Actualizar `colors.xml`** para compatibilidad con código no-Compose

---

## Rationale (Razones)

### 1. Consistencia Visual entre Plataformas

**Desktop (Electron/CSS):**
```css
--color-primary: #c62828;
--color-background: #fff5f6;
--color-text: #1a0a0a;
```

**Mobile (Android/Kotlin):**
```kotlin
val FallaPrimary = Color(0xFFc62828)
val FallaBackgroundLight = Color(0xFFfff5f6)
val FallaTextPrimary = Color(0xFF1a0a0a)
```

**Beneficio:** El usuario tiene la misma experiencia visual en desktop y móvil.

### 2. Material 3 Design System

Material 3 ofrece:
- Sistema de color completo (primary, secondary, tertiary, surface, etc.)
- Soporte nativo para modo claro/oscuro
- Componentes con tematización automática (Button, Card, TextField, etc.)
- Accesibilidad integrada (contraste, tamaño de toque, etc.)

**Beneficio:** Desarrollo más rápido, UI profesional, menos código boilerplate.

### 3. Centralización de Colores

Antes de esta decisión:
- ❌ Colores hardcodeados en múltiples archivos
- ❌ Inconsistencias entre componentes
- ❌ Difícil cambiar el tema

Después:
- ✅ Todos los colores en `Color.kt`
- ✅ Tema aplicado globalmente con `FallAppTheme`
- ✅ Cambio de color = editar un solo archivo

### 4. Soporte para Modo Oscuro

Implementación de dos esquemas de color:
- **Light Color Scheme:** Fondo claro, textos oscuros
- **Dark Color Scheme:** Fondo oscuro, textos claros

**Beneficio:** Mejor experiencia en ambientes con poca luz, ahorro de batería en OLED.

### 5. Rojo como Color de Marca

El rojo `#c62828` es el color característico de las Fallas valenciana (ninots, premios, tradición).

**Identidad de marca clara y culturalmente relevante.**

---

## Paleta de Colores

### Colores Principales

| Color | Hex | Uso |
|-------|-----|-----|
| **FallaPrimary** | `#c62828` | Botones principales, FABs, íconos destacados |
| **FallaBackgroundLight** | `#fff5f6` | Fondo principal (modo claro) |
| **FallaTextPrimary** | `#1a0a0a` | Textos principales |
| **FallaSecondary** | `#ff6f00` | Acciones secundarias |
| **FallaError** | `#d32f2f` | Estados de error |
| **FallaSuccess** | `#2e7d32` | Estados de éxito |

### Tonos Complementarios

- **Primary Dark:** `#9b2222` (hover, ripple, sombras)
- **Primary Light:** `#ff5f52` (backgrounds sutiles)
- **Text Secondary:** `#4a2b2b` (textos de soporte)
- **Border:** `#e5e7eb` (bordes, divisores)

---

## Implementación

### Estructura de Archivos

```
app/src/main/java/com/fallapp/core/ui/theme/
├── Color.kt        # Definiciones de colores
├── Theme.kt        # Esquemas y FallAppTheme
└── Type.kt         # Tipografía Material 3
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

// Texto
val FallaTextPrimary = Color(0xFF1a0a0a)
val FallaTextSecondary = Color(0xFF4a2b2b)

// ... (ver Color.kt completo)
```

### Theme.kt

```kotlin
package com.fallapp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = FallaPrimary,
    onPrimary = Color.White,
    background = FallaBackgroundLight,
    onBackground = FallaTextPrimary,
    // ... (ver Theme.kt completo)
)

private val DarkColorScheme = darkColorScheme(
    primary = FallaPrimaryLight,
    onPrimary = FallaTextPrimary,
    // ... (ver Theme.kt completo)
)

@Composable
fun FallAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Aplicación en MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FallAppTheme {  // ← Tema aplicado aquí
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

---

## Consecuencias

### Positivas

- ✅ Consistencia visual entre desktop y móvil
- ✅ Identidad de marca clara (rojo de las Fallas)
- ✅ Modo oscuro funcionando out-of-the-box
- ✅ Menos código boilerplate (componentes usan tema automáticamente)
- ✅ Fácil mantenimiento (colores centralizados)
- ✅ Accesibilidad mejorada (ratios de contraste validados)
- ✅ Componentes Material 3 con tematización automática

### Negativas (mitigadas)

- ⚠️ Requiere refactorizar componentes con colores hardcodeados → HECHO
- ⚠️ Curva de aprendizaje para Material 3 ColorScheme → Documentación creada

### Neutrales

- 🔄 Cambio de paradigma: hardcoded colors → theme-based colors
- 🔄 Más archivos de configuración (Color.kt, Theme.kt, Type.kt)

---

## Alternativas Consideradas

| Alternativa | Pros | Contras | Razón de rechazo |
|-------------|------|---------|------------------|
| **Material 2 + Theme** | Más simple, menos roles de color | Material 2 está deprecated, menos features | Rechazado: Material 3 es el estándar actual |
| **Custom Theme System** | Control total, sin dependencias | Mucho código custom, sin soporte oficial | Rechazado: Reinventar la rueda, más bugs potenciales |
| **Colores hardcodeados** | Simple, directo | Inconsistencias, difícil de mantener, sin modo oscuro | Rechazado: No escalable, mala práctica |
| **Material 3 + FallAppTheme** | **Estándar de industria, features completas, fácil de mantener** | Requiere aprender ColorScheme | **SELECCIONADO** - Mejor balance |

---

## Accesibilidad (WCAG 2.1)

### Ratios de Contraste Validados

| Combinación | Ratio | Estado |
|-------------|-------|--------|
| Primary (#c62828) / White | 5.5:1 | ✅ AA Large Text |
| Text Primary (#1a0a0a) / Background (#fff5f6) | 18.2:1 | ✅ AAA |
| Text Secondary (#4a2b2b) / Background | 11.5:1 | ✅ AAA |
| Text Muted (#6b7280) / Background | 4.9:1 | ✅ AA |
| Error (#d32f2f) / White | 5.2:1 | ✅ AA Large Text |

**Todos los pares de colores cumplen WCAG AA o superior.**

---

## Comparación Desktop vs Mobile

| Aspecto | Desktop (CSS) | Mobile (Kotlin) | Estado |
|---------|---------------|-----------------|--------|
| **Color Primario** | `#c62828` | `Color(0xFFc62828)` | ✅ Coincidente |
| **Background** | `#fff5f6` | `Color(0xFFfff5f6)` | ✅ Coincidente |
| **Texto Principal** | `#1a0a0a` | `Color(0xFF1a0a0a)` | ✅ Coincidente |
| **Borde** | `#e5e7eb` | `Color(0xFFe5e7eb)` | ✅ Coincidente |
| **Modo Oscuro** | Parcial | Completo | ✅ Implementado |

---

## Documentación

- **Guía completa:** [03.mobile/docs/TEMA-COLORES.md](../03.mobile/docs/TEMA-COLORES.md)
- **Código:** `03.mobile/app/src/main/java/com/fallapp/core/ui/theme/`
- **Material 3 Docs:** https://m3.material.io/styles/color/overview

---

## Testing

### Verificación Visual

- ✅ Modo claro: Fondo #fff5f6, texto #1a0a0a
- ✅ Modo oscuro: Fondo oscuro cálido, texto claro
- ✅ Botones primarios: Fondo #c62828, texto blanco
- ✅ Cards: Fondo blanco, bordes sutiles
- ✅ TextFields: Borde gris, focus rojo

### Dispositivos Testeados

- ✅ Pixel 6 (Android 14)
- ✅ Emulador Android Studio (API 34)

---

## Estado de Implementación

- ✅ Color.kt creado con paleta completa
- ✅ Theme.kt creado con Light/Dark schemes
- ✅ Type.kt creado con tipografía Material 3
- ✅ FallAppTheme aplicado en MainActivity
- ✅ colors.xml actualizado para compatibilidad
- ✅ Todos los componentes usando MaterialTheme.colorScheme
- ✅ Modo oscuro testeado
- ✅ Accesibilidad validada (WCAG AA)
- ✅ Documentación creada

---

## Revisión Futura

Este ADR debería revisarse si:
- Se cambia la identidad de marca (nuevo color principal)
- Material 4 se lanza con cambios significativos
- Se necesitan más variantes de color (tertiary, quaternary, etc.)
- Se detectan problemas de accesibilidad en combinaciones específicas

**Fecha próxima revisión:** 2027-02-03 (1 año)

---

## Referencias

- [Material Design 3 - Color System](https://m3.material.io/styles/color/overview)
- [Compose Material 3 - ColorScheme](https://developer.android.com/jetpack/compose/designsystems/material3#color-scheme)
- [WCAG 2.1 - Contrast Guidelines](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)
- [Desktop Palette Reference](../../02.desktop/src/renderer/styles/login.css)

---

**Autor:** Equipo FallApp  
**Aprobado por:** Arquitecto del Proyecto  
**Fecha de decisión:** 2026-02-03  
**Fecha de implementación:** 2026-02-03
