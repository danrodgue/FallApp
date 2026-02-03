package com.fallapp.features.map.presentation

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Pantalla de Mapa mostrando todas las fallas de Valencia usando OpenStreetMap.
 * 
 * Características:
 * - Marcadores para cada falla con ubicación GPS
 * - Info al tocar marcador
 * - Centrado en Valencia por defecto
 * - Controles de zoom
 * - SIN necesidad de API Key (usa OpenStreetMap)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit,
    onFallaClick: (Long) -> Unit,
    viewModel: MapViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Configurar osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Mostrar error en Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // OSM Map
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        
                        // Valencia centro (Plaza del Ayuntamiento)
                        val valenciaCentro = GeoPoint(39.4699, -0.3763)
                        controller.setZoom(12.0)
                        controller.setCenter(valenciaCentro)
                        
                        // Habilitar zoom con botones
                        setBuiltInZoomControls(false)
                        minZoomLevel = 10.0
                        maxZoomLevel = 18.0
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    // Limpiar marcadores existentes
                    mapView.overlays.clear()
                    
                    // Crear icono de marcador rojo personalizado
                    val markerIcon = createRedDotMarker(mapView.context)
                    
                    // Agregar marcadores para cada falla
                    uiState.fallas.forEach { falla ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(
                                falla.ubicacion.latitud!!,
                                falla.ubicacion.longitud!!
                            )
                            title = falla.nombre
                            snippet = "Sección: ${falla.seccion}"
                            
                            // Establecer icono de punto rojo
                            icon = markerIcon
                            
                            // Centrar el icono en la posición (offset para que el punto esté exactamente en las coordenadas)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            
                            setOnMarkerClickListener { marker, _ ->
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Card con info de falla seleccionada
            uiState.selectedFalla?.let { falla ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = falla.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Sección: ${falla.seccion}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        falla.ubicacion.direccion?.let { dir ->
                            Text(
                                text = dir,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Crea un marcador de punto rojo personalizado para el mapa.
 * Coincide con el estilo visual típico de mapas y la paleta de colores de la app.
 */
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
