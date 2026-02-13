package com.fallapp.features.eventos.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    Scaffold(
        modifier = modifier,
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

            Divider(modifier = Modifier.padding(vertical = 8.dp))

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
                    CircularProgressIndicator()
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
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Text(
                text = evento.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = evento.nombreFalla,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = evento.fechaEvento.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            evento.descripcion?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

