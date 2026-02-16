package com.fallapp.features.map.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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
    modifier: Modifier = Modifier,
    hideBackButton: Boolean = false,
    focusFallaId: Long? = null,
    viewModel: MapViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher para solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

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
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Fallas") },
                navigationIcon = {
                    if (!hideBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Volver")
                        }
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

                        // Guardar referencia
                        mapView = this
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

                    // Si tenemos una falla objetivo, centrar el mapa en ella
                    val targetFalla = focusFallaId?.let { id ->
                        uiState.fallas.firstOrNull { it.idFalla == id }
                    }
                    targetFalla?.let { falla ->
                        val lat = falla.ubicacion.latitud
                        val lng = falla.ubicacion.longitud
                        if (lat != null && lng != null) {
                            val point = GeoPoint(lat, lng)
                            mapView.controller.setZoom(17.0)
                            mapView.controller.setCenter(point)
                            // Marcar esta falla como seleccionada para mostrar la tarjeta inferior
                            viewModel.onFallaSelected(falla)
                        }
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

            // Botón flotante de ubicación (abajo a la derecha)
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        // Obtener ubicación actual
                        getCurrentLocation(context) { lat, lng ->
                            mapView?.let { map ->
                                val userLocation = GeoPoint(lat, lng)

                                // Agregar o actualizar marcador de ubicación del usuario
                                // Remover marcador anterior si existe
                                map.overlays.removeAll { it is Marker && it.id == "user_location" }

                                // Crear marcador azul para ubicación del usuario
                                val userMarker = Marker(map).apply {
                                    position = userLocation
                                    title = "Tu ubicación"
                                    id = "user_location"
                                    icon = createBlueDotMarker(context)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                }

                                map.overlays.add(userMarker)

                                // Zoom más cercano (18.0) y animar
                                map.controller.setZoom(18.0)
                                map.controller.animateTo(userLocation)
                                map.invalidate()
                            }
                        }
                    } else {
                        // Solicitar permisos
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = if (uiState.selectedFalla != null) 120.dp else 0.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Mi ubicación",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Obtiene la ubicación actual del dispositivo usando Google Play Services.
 */
private fun getCurrentLocation(
    context: android.content.Context,
    onLocationReceived: (Double, Double) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(it.latitude, it.longitude)
            }
        }
    } catch (e: SecurityException) {
        // Permisos no otorgados
        e.printStackTrace()
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

/**
 * Crea un marcador de punto azul para la ubicación del usuario.
 */
private fun createBlueDotMarker(context: android.content.Context): Drawable {
    val drawable = android.graphics.drawable.GradientDrawable()
    drawable.shape = android.graphics.drawable.GradientDrawable.OVAL

    // Color azul para ubicación del usuario
    drawable.setColor(android.graphics.Color.parseColor("#0277bd"))

    // Tamaño del punto (24x24 dp - un poco más grande que las fallas)
    val size = (24 * context.resources.displayMetrics.density).toInt()
    drawable.setSize(size, size)

    // Borde blanco grueso para mayor visibilidad
    drawable.setStroke(
        (3 * context.resources.displayMetrics.density).toInt(),
        android.graphics.Color.WHITE
    )

    drawable.setBounds(0, 0, size, size)

    return drawable
}

