# ADR-009: Migración de Google Maps a OpenStreetMap (osmdroid)

**Fecha:** 2026-02-03  
**Estado:** ✅ Aceptado e Implementado  
**Contexto:** Mobile App (03.mobile)

---

## Contexto

La aplicación móvil FallApp necesita mostrar un mapa interactivo con las ubicaciones GPS de las fallas valencianas. Inicialmente se implementó con Google Maps, pero surgieron consideraciones sobre costos, dependencias y sostenibilidad del proyecto.

---

## Decisión

**Se decidió migrar de Google Maps a OpenStreetMap usando la librería osmdroid.**

### Tecnología Seleccionada

- **Librería:** `osmdroid-android:6.1.18`
- **Tile Source:** OpenStreetMap Mapnik (estilo estándar OSM)
- **Marcadores:** Personalizados con GradientDrawable (puntos rojos circulares)
- **Caché:** Automático con almacenamiento local de teselas

---

## Rationale (Razones)

### Ventajas de OpenStreetMap

1. **Sin API Key requerida**
   - Google Maps requiere API Key con límites de uso gratuito (28,000 cargas de mapa/mes)
   - osmdroid no requiere ninguna clave ni registro
   - Eliminación de configuración compleja

2. **Completamente gratuito**
   - OSM es open source y gratuito sin límites
   - No hay riesgo de costos inesperados si la app se vuelve popular
   - Sostenible a largo plazo

3. **Sin dependencias de Google Play Services**
   - Google Maps requiere `play-services-maps` y `play-services-location` (varios MB)
   - osmdroid es más ligero (~2 MB vs ~10 MB)
   - Funciona en dispositivos sin Google Play Services

4. **Open Source y transparente**
   - Código de osmdroid completamente auditable
   - Datos de mapas de OpenStreetMap, proyecto colaborativo mundial
   - Sin términos de servicio restrictivos

5. **Caché offline**
   - osmdroid cachea automáticamente las teselas visitadas
   - Permite uso parcial sin conexión
   - Mejora la experiencia de usuario

### Desventajas (aceptables)

1. **Menos features "out of the box"**
   - No tiene geocoding integrado (se puede agregar Nominatim si se necesita)
   - No tiene rutas/navegación integrada (se puede agregar GraphHopper si se necesita)
   - **Mitigación:** FallApp solo necesita mostrar marcadores, no necesita estas features

2. **UI menos "pulida" por defecto**
   - Controles de zoom más básicos
   - Estilo de mapa más simple
   - **Mitigación:** Se compensó con marcadores personalizados y coherencia con la paleta de colores

3. **Menor soporte empresarial**
   - osmdroid es mantenido por la comunidad
   - Google Maps tiene soporte oficial de Google
   - **Mitigación:** osmdroid es maduro (10+ años), activamente mantenido, y ampliamente usado

---

## Implementación

### Código Clave

```kotlin
// Dependencia
implementation("org.osmdroid:osmdroid-android:6.1.18")

// Configuración
Configuration.getInstance().userAgentValue = context.packageName

// MapView
MapView(context).apply {
    setTileSource(TileSourceFactory.MAPNIK)
    setMultiTouchControls(true)
    controller.setZoom(12.0)
    controller.setCenter(GeoPoint(39.4699, -0.3763)) // Valencia
}

// Marcador personalizado (punto rojo)
private fun createRedDotMarker(context: Context): Drawable {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.OVAL
    drawable.setColor(Color.parseColor("#c62828"))
    val size = (20 * context.resources.displayMetrics.density).toInt()
    drawable.setSize(size, size)
    drawable.setStroke((2 * context.resources.displayMetrics.density).toInt(), Color.WHITE)
    return drawable
}
```

### Permisos

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
                 android:maxSdkVersion="32" />
```

---

## Consecuencias

### Positivas

- ✅ Eliminado requisito de API Key
- ✅ Reducido tamaño de la app (~8 MB menos)
- ✅ Sin límites de uso
- ✅ Sin riesgo de costos futuros
- ✅ Mayor sostenibilidad del proyecto
- ✅ Coherencia visual mejorada (marcadores rojos #c62828)
- ✅ Funciona en más dispositivos (sin Google Play Services)

### Negativas (mitigadas)

- ⚠️ Requiere implementación de marcadores personalizados → HECHO
- ⚠️ Menor "polish" de UI → Compensado con diseño coherente
- ⚠️ Sin geocoding nativo → No se necesita actualmente

### Neutrales

- 🔄 Diferente API que aprender (osmdroid vs Google Maps)
- 🔄 Cambio en troubleshooting (buscar en osmdroid docs/issues)

---

## Alternativas Consideradas

| Alternativa | Pros | Contras | Razón de rechazo |
|-------------|------|---------|------------------|
| **Google Maps** | UI pulida, features completas, soporte oficial | Requiere API Key, límites de uso, costos potenciales, dependencia de Google Play | Rechazado por requisito de API Key y límites |
| **Mapbox** | Muy customizable, features avanzadas, SDK moderno | Requiere API Key, límites de uso gratuito (50k cargas/mes), costos después | Rechazado por requisito de API Key y límites |
| **MapLibre** | Fork open source de Mapbox, sin API Key | Más complejo de configurar, requiere servidor de teselas propio | Rechazado por complejidad (overkill para necesidades de FallApp) |
| **HERE Maps** | Buenas features, API moderna | Requiere API Key, límites de uso | Rechazado por requisito de API Key |
| **osmdroid (OpenStreetMap)** | **Sin API Key, completamente gratuito, open source, ligero** | Menos features "out of the box" | **SELECCIONADO** - Cumple todas las necesidades sin costos ni límites |

---

## Compatibilidad

### Versiones

- **osmdroid:** 6.1.18
- **Min SDK:** 24 (Android 7.0) - Sin cambios
- **Target SDK:** 34 (Android 14) - Sin cambios

### Dispositivos

- ✅ Dispositivos con Google Play Services
- ✅ Dispositivos **sin** Google Play Services (AOSP, LineageOS, etc.)
- ✅ Emuladores Android Studio

---

## Documentación

- **Guía completa:** [03.mobile/docs/MAPA.OSM.md](../03.mobile/docs/MAPA.OSM.md)
- **Código:** `03.mobile/app/src/main/java/com/fallapp/features/map/presentation/MapScreen.kt`
- **Docs osmdroid:** https://github.com/osmdroid/osmdroid/wiki

---

## Métricas

| Métrica | Google Maps | osmdroid |
|---------|-------------|----------|
| **Tamaño APK (dependencias)** | ~10 MB | ~2 MB |
| **API Key requerida** | Sí | No |
| **Límite gratuito** | 28,000 cargas/mes | Ilimitado |
| **Costo después del límite** | $7 por 1,000 cargas extra | $0 |
| **Configuración (tiempo)** | ~30 min (obtener API Key, configurar GCP, billing) | ~5 min |
| **Dependencias externas** | Google Play Services | Ninguna |

---

## Estado de Implementación

- ✅ osmdroid integrado
- ✅ Google Maps completamente removido
- ✅ Marcadores personalizados (puntos rojos circulares)
- ✅ Caché de teselas configurado
- ✅ Permisos actualizados
- ✅ Documentación creada
- ✅ Testing en dispositivo real

---

## Revisión Futura

Este ADR debería revisarse si:
- Se necesita geocoding (dirección → coordenadas)
- Se necesita navegación paso a paso
- Se requiere vista satelital de alta calidad
- Se necesitan features avanzadas no disponibles en osmdroid

**Fecha próxima revisión:** 2027-02-03 (1 año)

---

**Autor:** Equipo FallApp  
**Aprobado por:** Arquitecto del Proyecto  
**Fecha de decisión:** 2026-02-03  
**Fecha de implementación:** 2026-02-03
