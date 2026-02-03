package com.fallapp.features.fallas.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fallapp.features.fallas.domain.model.Falla
import org.koin.androidx.compose.koinViewModel

/**
 * Pantalla de detalle de una falla.
 * 
 * Muestra:
 * - Información completa de la falla
 * - Mapa de ubicación (placeholder por ahora)
 * - Contacto y redes sociales
 * - Estadísticas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallaDetailScreen(
    fallaId: Long,
    onBackClick: () -> Unit,
    viewModel: FallaDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(fallaId) {
        viewModel.loadFalla(fallaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.falla?.nombre ?: "Detalle de Falla") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadFalla(fallaId) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            uiState.falla != null -> {
                FallaDetailContent(
                    falla = uiState.falla!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
private fun FallaDetailContent(
    falla: Falla,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        // Cabecera
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = falla.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = falla.seccion,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text("Categoría: ${falla.categoria.name}") }
                )
            }
        }

        // Ubicación
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = falla.ubicacion.direccion ?: "Dirección no disponible")
                Text(text = "${falla.ubicacion.ciudad}, ${falla.ubicacion.provincia}")
                falla.ubicacion.codigoPostal?.let {
                    Text(text = "CP: $it")
                }
                
                // Coordenadas (para futuro mapa)
                if (falla.ubicacion.latitud != null && falla.ubicacion.longitud != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📍 ${falla.ubicacion.latitud}, ${falla.ubicacion.longitud}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Descripción
        falla.descripcion?.let { desc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = desc)
                }
            }
        }

        // Historia
        falla.historia?.let { historia ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Historia",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = historia)
                }
            }
        }

        // Estadísticas
        falla.estadisticas?.let { stats ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Estadísticas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (stats.numeroSocios > 0) {
                        StatRow(icon = "👥", label = "Socios", value = "${stats.numeroSocios}")
                    }
                    if (stats.numeroNinots > 0) {
                        StatRow(icon = "🎭", label = "Ninots", value = "${stats.numeroNinots}")
                    }
                    if (stats.numeroEventos > 0) {
                        StatRow(icon = "📅", label = "Eventos", value = "${stats.numeroEventos}")
                    }
                    stats.anyoFundacion?.let {
                        StatRow(icon = "📆", label = "Fundación", value = "$it")
                    }
                    stats.presupuestoTotal?.let {
                        StatRow(icon = "💰", label = "Presupuesto", value = "${it}€")
                    }
                }
            }
        }

        // Contacto
        falla.contacto?.let { contacto ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Contacto",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    contacto.telefono?.let {
                        ContactRow(icon = Icons.Default.Phone, text = it)
                    }
                    contacto.email?.let {
                        ContactRow(icon = Icons.Default.Email, text = it)
                    }
                    contacto.web?.let {
                        ContactRow(icon = Icons.Default.Language, text = it)
                    }
                    
                    // Redes sociales
                    if (contacto.facebook != null || contacto.twitter != null || contacto.instagram != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Redes Sociales",
                            style = MaterialTheme.typography.labelLarge
                        )
                        contacto.facebook?.let {
                            Text("📘 Facebook: $it")
                        }
                        contacto.twitter?.let {
                            Text("🐦 Twitter: $it")
                        }
                        contacto.instagram?.let {
                            Text("📷 Instagram: $it")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}
