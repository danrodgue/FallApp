package com.fallapp.features.fallas.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fallapp.features.fallas.domain.model.Categoria
import com.fallapp.features.fallas.domain.model.Falla
import org.koin.androidx.compose.koinViewModel

/**
 * Pantalla de lista de fallas.
 * 
 * Features:
 * - Lista scrollable de fallas
 * - Búsqueda por nombre
 * - Filtro por categoría
 * - Pull-to-refresh
 * - Navegación a detalle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallasListScreen(
    onFallaClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    viewModel: FallasListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fallas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar fallas...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                singleLine = true
            )

            // Filtro de categorías
            CategoriaFilter(
                selectedCategoria = uiState.selectedCategoria,
                onCategoriaSelected = { viewModel.onCategoriaSelected(it) }
            )

            // Error message
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Lista de fallas
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.fallas.isEmpty()) {
                    // Loading inicial
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.fallas.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) {
                                "No se encontraron fallas con \"${uiState.searchQuery}\""
                            } else {
                                "No hay fallas disponibles"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Lista de fallas
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.fallas,
                            key = { it.idFalla }
                        ) { falla ->
                            FallaItem(
                                falla = falla,
                                onClick = { onFallaClick(falla.idFalla) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filtro horizontal de categorías.
 */
@Composable
private fun CategoriaFilter(
    selectedCategoria: Categoria?,
    onCategoriaSelected: (Categoria?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Opción "Todas"
        item {
            FilterChip(
                selected = selectedCategoria == null,
                onClick = { onCategoriaSelected(null) },
                label = { Text("Todas") }
            )
        }

        // Categorías
        items(Categoria.entries.toTypedArray()) { categoria ->
            FilterChip(
                selected = selectedCategoria == categoria,
                onClick = { onCategoriaSelected(categoria) },
                label = { 
                    Text(
                        when (categoria) {
                            Categoria.SIN_CATEGORIA -> "Sin Categoría"
                            else -> categoria.name.lowercase().replaceFirstChar { it.uppercase() }
                        }
                    ) 
                }
            )
        }
    }
}

/**
 * Item de falla en la lista.
 */
@Composable
private fun FallaItem(
    falla: Falla,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nombre y categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = falla.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                AssistChip(
                    onClick = { },
                    label = { Text(falla.categoria.name) },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ubicación
            Text(
                text = "${falla.ubicacion.direccion ?: falla.seccion}, ${falla.ubicacion.ciudad}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Descripción
            falla.descripcion?.let { desc ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Estadísticas
            falla.estadisticas?.let { stats ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (stats.numeroSocios > 0) {
                        Text(
                            text = "👥 ${stats.numeroSocios} socios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (stats.numeroNinots > 0) {
                        Text(
                            text = "🎭 ${stats.numeroNinots} ninots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
