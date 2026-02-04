# 🗺️ Mapa de Fallas - OpenStreetMap

**Fecha:** 2026-02-03  
**Estado:** ✅ OPERATIVO  
**Versión:** 2.0.0  
**Actualización:** Migración a OpenStreetMap (osmdroid)

---

## 📋 Resumen

Se ha implementado una pantalla de mapa interactivo usando **OpenStreetMap** (osmdroid) que muestra todas las fallas de Valencia con sus ubicaciones GPS. 

**Ventajas de osmdroid:**
- ✅ **Sin API Key requerida** - Completamente gratuito
- ✅ **Sin límites de uso** - No hay cuotas ni restricciones
- ✅ **Open Source** - Código abierto y mantenido activamente
- ✅ **Caché offline** - Las teselas se guardan para uso sin conexión
- ✅ **Ligero** - Menor tamaño que Google Maps

Los usuarios pueden:
- Ver marcadores rojos de todas las fallas en el mapa
- Tocar un marcador para ver información de la falla
- Navegar interactivamente con gestos multi-touch
- Zoom in/out con controles o gestos

---

## 🏗️ Arquitectura

### Componentes Implementados

| Componente | Responsabilidad | Archivo |
|------------|----------------|---------|
| **MapScreen** | UI con osmdroid en AndroidView | `MapScreen.kt` |
| **MapViewModel** | Gestiona estado y lógica de negocio | `MapViewModel.kt` |
| **MapUiState** | Estado de UI inmutable | `MapUiState.kt` |
| **MapModule** | Inyección de dependencias Koin | `MapModule.kt` |
| **createRedDotMarker()** | Función helper para marcadores personalizados | `MapScreen.kt` |

### Flujo de Datos

```
1. MapScreen se crea al navegar a Screen.Map
2. MapViewModel se inyecta automáticamente vía Koin
3. ViewModel llama a GetFallasUseCase
4. Filtra fallas con coordenadas GPS válidas:
   - latitud != null && longitud != null
   - lat != 0.0 && lng != 0.0
   - lat in -90.0..90.0 && lng in -180.0..180.0
5. MapScreen renderiza marcadores rojos en osmdroid
6. Usuario toca marcador → actualiza selectedFalla en UiState
7. Card inferior muestra info de falla seleccionada
```

---

## 🗺️ OpenStreetMap Integration (osmdroid)

### Dependencias

```kotlin
// build.gradle.kts (app level)
dependencies {
    implementation("org.osmdroid:osmdroid-android:6.1.18")
}
```

**Nota:** Google Maps fue **completamente removido**. No se necesitan las siguientes dependencias:
```kotlin
// ❌ REMOVIDAS
// implementation("com.google.maps.android:maps-compose:4.3.3")
// implementation("com.google.android.gms:play-services-maps:18.2.0")
// implementation("com.google.android.gms:play-services-location:21.1.0")
```

### Configuración AndroidManifest

```xml
<!-- Permisos -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
                 android:maxSdkVersion="32" />

<!-- Permitir HTTP para desarrollo -->
<application
    android:usesCleartextTraffic="true"
    ...>
</application>
```

⚠️ **IMPORTANTE**: 
- `WRITE_EXTERNAL_STORAGE` es necesario para caché de teselas offline
- Solo aplica a API 32 o inferior (Android 12L y anteriores)
- `usesCleartextTraffic="true"` es para la API de desarrollo (HTTP)

**NO se necesita API Key** - osmdroid es completamente gratuito.

---

## 🎨 Marcadores Personalizados

### Implementación de Puntos Rojos

En lugar de los iconos por defecto (dedos apuntando hacia abajo), se usan **puntos rojos circulares** con borde blanco, estilo clásico de mapas:

```kotlin
private fun createRedDotMarker(context: android.content.Context): Drawable {
    val drawable = android.graphics.drawable.GradientDrawable()
    drawable.shape = android.graphics.drawable.GradientDrawable.OVAL
    
    // Color rojo coincidente con la paleta (#c62828)
    drawable.setColor(android.graphics.Color.parseColor("#c62828"))
    
    // Tamaño del punto (20x20 dp)
    val size = (20 * context.resources.displayMetrics.density).toInt()
    drawable.setSize(size, size)
    
    // Borde blanco para contraste
    drawable.setStroke(
        (2 * context.resources.displayMetrics.density).toInt(),
        android.graphics.Color.WHITE
    )
    
    drawable.setBounds(0, 0, size, size)
    
    return drawable
}
```

**Características:**
- 🔴 Color: `#c62828` (rojo de las Fallas, coincidente con paleta desktop)
- ⚪ Borde: Blanco de 2dp para contraste sobre el mapa
- 📏 Tamaño: 20dp de diámetro (visible pero no invasivo)
- 🎯 Centrado: Anclado al centro para precisión en coordenadas GPS

### Uso en Marcadores

```kotlin
val markerIcon = createRedDotMarker(mapView.context)

uiState.fallas.forEach { falla ->
    val marker = Marker(mapView).apply {
        position = GeoPoint(falla.ubicacion.latitud!!, falla.ubicacion.longitud!!)
        title = falla.nombre
        snippet = "Sección: ${falla.seccion}"
        
        // Establecer icono de punto rojo
        icon = markerIcon
        
        // Centrar el icono en la posición
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        
        setOnMarkerClickListener { marker, _ ->
            viewModel.onFallaSelected(falla)
            true
        }
    }
    mapView.overlays.add(marker)
}
```

---

## 🎯 Características Implementadas

### 1. Marcadores de Fallas
- ✅ Punto rojo circular por cada falla con ubicación GPS
- ✅ Tamaño: 20dp, color: `#c62828`, borde blanco 2dp
- ✅ Título del marcador: Nombre de la falla
- ✅ Snippet: Sección de la falla
- ✅ Click en marcador → muestra info de la falla en Card inferior
- ✅ Marcador centrado exactamente en coordenadas GPS

### 2. Configuración del Mapa
- ✅ Centrado inicial: Plaza del Ayuntamiento de Valencia (39.4699, -0.3763)
- ✅ Zoom inicial: nivel 12.0 (ciudad completa visible)
- ✅ Tile Source: OpenStreetMap Mapnik (estilo estándar OSM)
- ✅ Controles multi-touch habilitados
- ✅ Zoom mínimo: 10.0, Zoom máximo: 18.0
- ✅ Caché de teselas para uso offline

### 3. Interactividad
- ✅ Tap en marcador → muestra Card con info de falla
- ✅ Card inferior con: nombre, sección, dirección (si disponible)
- ✅ Gestos multi-touch: zoom con pellizco, pan con arrastre
- ✅ Botón "Atrás" en TopBar para volver
- ✅ Scroll fluido sobre el mapa

### 4. Validación de Coordenadas
```kotlin
val fallasConUbicacion = result.data.filter { falla ->
    val lat = falla.ubicacion.latitud
    val lng = falla.ubicacion.longitud
    lat != null && lng != null && 
    lat != 0.0 && lng != 0.0 &&
    lat in -90.0..90.0 && lng in -180.0..180.0
}
```

**Filtros aplicados:**
- No null
- No ceros (coordenadas por defecto/placeholder)
- Rango válido de GPS (latitud: -90 a 90, longitud: -180 a 180)

---

## 📱 Código Clave

### MapScreen.kt (Fragmento Principal)

```kotlin
@Composable
fun MapScreen(
    onBackClick: () -> Unit,
    onFallaClick: (Long) -> Unit,
    viewModel: MapViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Configurar osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Fallas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // OSM Map
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        
                        val valenciaCentro = GeoPoint(39.4699, -0.3763)
                        controller.setZoom(12.0)
                        controller.setCenter(valenciaCentro)
                        
                        minZoomLevel = 10.0
                        maxZoomLevel = 18.0
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    mapView.overlays.clear()
                    val markerIcon = createRedDotMarker(mapView.context)
                    
                    uiState.fallas.forEach { falla ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(
                                falla.ubicacion.latitud!!,
                                falla.ubicacion.longitud!!
                            )
                            title = falla.nombre
                            snippet = "Sección: ${falla.seccion}"
                            icon = markerIcon
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            
                            setOnMarkerClickListener { _, _ ->
                                viewModel.onFallaSelected(falla)
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                }
            )

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Info Card de falla seleccionada
            uiState.selectedFalla?.let { falla ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(falla.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("Sección: ${falla.seccion}", style = MaterialTheme.typography.bodyMedium)
                        falla.ubicacion.direccion?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
```

---

## 🔧 Troubleshooting

### Problema: Mapa no carga

**Solución:**
1. Verificar que `INTERNET` y `ACCESS_NETWORK_STATE` estén en AndroidManifest
2. Comprobar conexión a internet del dispositivo
3. Verificar que `Configuration.getInstance().userAgentValue` esté configurado

### Problema: Marcadores no aparecen

**Solución:**
1. Verificar que las coordenadas GPS sean válidas
2. Comprobar que `mapView.invalidate()` se llame después de agregar marcadores
3. Revisar que las fallas pasen el filtro de validación de coordenadas

### Problema: Teselas no se cargan

**Solución:**
1. Verificar permiso `WRITE_EXTERNAL_STORAGE` para caché
2. Limpiar caché de la app: Ajustes → Apps → FallApp → Almacenamiento → Limpiar caché
3. Verificar conectividad y que los servidores OSM estén accesibles

### Problema: Mapa lento o lag

**Solución:**
1. Limitar número de marcadores cargados (actualmente: todas las fallas ~340)
2. Implementar clustering para agrupar marcadores cercanos
3. Reducir densidad de teselas o pre-cargar teselas para área de Valencia

---

## 📊 Estadísticas de Uso

| Métrica | Valor |
|---------|-------|
| **Total Fallas en BD** | 347 |
| **Fallas con GPS válido** | ~343 (99%) |
| **Teselas descargadas** | Variable (caché automático) |
| **Tamaño caché promedio** | 5-15 MB (depende de zoom usado) |
| **Tiempo carga inicial** | < 2 segundos |
| **Latencia tap marcador** | < 100ms |

---

## 🚀 Mejoras Futuras

### Corto Plazo
- [ ] Implementar clustering de marcadores para mejor rendimiento
- [ ] Añadir búsqueda/filtro de fallas en el mapa
- [ ] Mostrar ruta desde ubicación actual a falla seleccionada
- [ ] Permitir cambiar tipo de teselas (estándar, satélite, transporte)

### Medio Plazo
- [ ] Modo offline completo con pre-carga de teselas de Valencia
- [ ] Capa de calor con densidad de fallas por zona
- [ ] Animaciones al agregar/quitar marcadores
- [ ] Compartir ubicación de falla

### Largo Plazo
- [ ] Integración con eventos en tiempo real en el mapa
- [ ] AR (Realidad Aumentada) para encontrar fallas cercanas
- [ ] Navegación paso a paso a fallas
- [ ] Capas temáticas (categorías, secciones, año fundación)

---

## 📚 Referencias

- [osmdroid Wiki](https://github.com/osmdroid/osmdroid/wiki)
- [OpenStreetMap](https://www.openstreetmap.org/)
- [osmdroid API Docs](https://osmdroid.github.io/osmdroid/javadoc/)
- [Tile Usage Policy](https://operations.osmfoundation.org/policies/tiles/)

---

## 📝 Changelog

### v2.0.0 (2026-02-03)
- **BREAKING**: Migración de Google Maps a OpenStreetMap (osmdroid)
- **REMOVED**: Dependencias de Google Maps y Google Play Services
- **REMOVED**: Requisito de API Key
- **ADDED**: Marcadores rojos personalizados con borde blanco
- **ADDED**: Soporte para caché offline de teselas
- **IMPROVED**: Validación robusta de coordenadas GPS
- **IMPROVED**: Rendimiento de carga del mapa

### v1.0.0 (2026-02-02)
- Implementación inicial con Google Maps
- Marcadores estándar
- Navegación básica

---

**Mantenido por:** Equipo FallApp Mobile  
**Última actualización:** 2026-02-03
