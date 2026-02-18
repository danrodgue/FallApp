package com.fallapp.features.eventos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.fallapp.features.eventos.domain.model.Evento
import com.fallapp.features.fallas.domain.model.Falla
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(
    onFallaClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventosViewModel = koinViewModel()
 ) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar error en Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Recargar eventos cada vez que se entra a la pantalla
    LaunchedEffect(Unit) {
        viewModel.refreshEventos()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Eventos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar eventos"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Buscador de fallas (igual espíritu que en Votos)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar falla") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val trimmedQuery = searchQuery.trim()
            val fallasFiltradas: List<Falla> =
                if (trimmedQuery.isEmpty()) emptyList()
                else uiState.fallas.filter { falla ->
                    falla.nombre.contains(trimmedQuery, ignoreCase = true) ||
                            falla.seccion.contains(trimmedQuery, ignoreCase = true)
                }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (uiState.isLoading && uiState.fallas.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (trimmedQuery.isNotEmpty() && fallasFiltradas.isEmpty()) {
                    Text(
                        text = "No se han encontrado fallas para \"$trimmedQuery\"",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (fallasFiltradas.isNotEmpty()) {
                    // Cogemos la primera coincidencia como falla seleccionada
                    val fallaSeleccionada = fallasFiltradas.first()
                    LaunchedEffect(fallaSeleccionada.idFalla) {
                        viewModel.buscarEventosDeFalla(fallaSeleccionada)
                    }

                    FallaConEventosSection(
                        falla = fallaSeleccionada,
                        eventos = uiState.eventosDeFalla,
                        onFallaClick = onFallaClick
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Listado general de eventos (equivalente al listado general de fallas, pero de eventos)
            Text(
                text = "Próximos eventos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )

            if (uiState.eventosProximos.isEmpty() && uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Cargando eventos...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (uiState.eventosProximos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos próximos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.eventosProximos) { evento ->
                        EventoRow(
                            evento = evento,
                            onClick = { onFallaClick(evento.idFalla) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FallaConEventosSection(
    falla: Falla,
    eventos: List<Evento>,
    onFallaClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = falla.nombre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = falla.seccion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (eventos.isEmpty()) {
            Text(
                text = "Esta falla no tiene eventos registrados.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            eventos.forEach { evento ->
                EventoRow(
                    evento = evento,
                    onClick = { onFallaClick(falla.idFalla) }
                )
            }
        }
    }
}

@Composable
private fun EventoRow(
    evento: Evento,
    onClick: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails) {
        EventoDetailsDialog(
            evento = evento,
            onDismiss = { showDetails = false }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = { showDetails = true }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen a la izquierda (con placeholder mientras carga)
            if (evento.imagen != null) {
                SubcomposeAsyncImage(
                    model = evento.imagen,
                    contentDescription = "Imagen de ${evento.nombre}",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    content = { SubcomposeAsyncImageContent() }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (evento.tipo.lowercase()) {
                            "oficial" -> "🎭"
                            "cultural" -> "🎨"
                            "infantil" -> "👶"
                            "deportivo" -> "⚽"
                            "musical" -> "🎵"
                            "exposicion" -> "🎪"
                            "concierto" -> "🎵"
                            else -> "📅"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Información a la derecha
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = evento.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = evento.nombreFalla,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatEventDate(evento.fechaEvento),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Flecha indicadora
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ver detalles",
                modifier = Modifier.padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EventoDetailsDialog(
    evento: Evento,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header con título y botón cerrar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalles del Evento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Imagen del evento (si existe), con placeholder mientras carga
                if (evento.imagen != null) {
                    SubcomposeAsyncImage(
                        model = evento.imagen,
                        contentDescription = "Imagen de ${evento.nombre}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        content = { SubcomposeAsyncImageContent() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Contenido
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nombre del evento
                    item {
                        Column {
                            Text(
                                text = "Nombre",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = evento.nombre,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Falla
                    item {
                        Column {
                            Text(
                                text = "Falla",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = evento.nombreFalla,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Fecha
                    item {
                        Column {
                            Text(
                                text = "Fecha",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatEventDate(evento.fechaEvento),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Ubicación
                    if (evento.ubicacion != null) {
                        item {
                            Column {
                                Text(
                                    text = "Ubicación",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = evento.ubicacion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Descripción
                    if (evento.descripcion != null) {
                        item {
                            Column {
                                Text(
                                    text = "Descripción",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = evento.descripcion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Participantes estimados
                    if (evento.participantesEstimado != null && evento.participantesEstimado > 0) {
                        item {
                            Column {
                                Text(
                                    text = "Participantes Estimados",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${evento.participantesEstimado} personas",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Tipo de evento
                    item {
                        Column {
                            Text(
                                text = "Tipo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (evento.tipo.lowercase()) {
                                        "oficial" -> "🎭"
                                        "cultural" -> "🎨"
                                        "infantil" -> "👶"
                                        "deportivo" -> "⚽"
                                        "musical" -> "🎵"
                                        "exposicion" -> "🎪"
                                        "concierto" -> "🎵"
                                        else -> "📅"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = evento.tipo.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Botón cerrar
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

/**
 * Formatea la fecha del evento de manera legible.
 */
private fun formatEventDate(dateTime: java.time.LocalDateTime): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return dateTime.format(formatter)
}

