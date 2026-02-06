package com.fallapp.features.votos.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import coil.compose.AsyncImage
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.model.TipoVoto
import com.fallapp.features.fallas.domain.model.Voto
import org.koin.androidx.compose.koinViewModel

/**
 * Pantalla principal de Votos con 3 tabs:
 * - Votar: Lista de fallas para votar
 * - Mis Votos: Votos del usuario
 * - Ranking: Fallas más votadas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotosScreen(
    onFallaClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VotosViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Votos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Votar") },
                        icon = { Icon(Icons.Default.Star, null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Mis Votos") },
                        icon = { Icon(Icons.Default.Favorite, null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Ranking") },
                        icon = { Icon(Icons.Default.DateRange, null) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (selectedTab) {
            0 -> VotarTab(
                fallas = uiState.fallas,
                isLoading = uiState.isLoading,
                onVoteClick = { falla, tipoVoto ->
                    viewModel.votar(falla, tipoVoto)
                },
                onFallaClick = onFallaClick,
                modifier = Modifier.padding(padding)
            )
            1 -> MisVotosTab(
                votos = uiState.misVotos,
                isLoading = uiState.isLoading,
                onDeleteVote = { idVoto ->
                    viewModel.eliminarVoto(idVoto)
                },
                onFallaClick = onFallaClick,
                modifier = Modifier.padding(padding)
            )
            2 -> RankingTab(
                ranking = uiState.ranking,
                isLoading = uiState.isLoading,
                selectedTipoVoto = uiState.rankingFilter,
                onFilterChange = { tipoVoto ->
                    viewModel.setRankingFilter(tipoVoto)
                },
                onFallaClick = onFallaClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

/**
 * Tab para votar fallas.
 */
@Composable
private fun VotarTab(
    fallas: List<Falla>,
    isLoading: Boolean,
    onVoteClick: (Falla, TipoVoto) -> Unit,
    onFallaClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && fallas.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (fallas.isEmpty()) {
            Text(
                text = "No hay fallas disponibles",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { fallas.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) { page ->
                val falla = fallas[page]
                SwipeFallaCard(
                    falla = falla,
                    onVoteClick = { tipoVoto -> onVoteClick(falla, tipoVoto) },
                    onFallaClick = { onFallaClick(falla.idFalla) }
                )
            }

            if (fallas.isNotEmpty()) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${fallas.size}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Carta tipo "Tinder" para votar una falla.
 */
@Composable
private fun SwipeFallaCard(
    falla: Falla,
    onVoteClick: (TipoVoto) -> Unit,
    onFallaClick: () -> Unit
) {
    var showVoteDialog by remember { mutableStateOf(false) }
    var selectedTipoVoto by remember { mutableStateOf<TipoVoto?>(null) }

    if (showVoteDialog && selectedTipoVoto != null) {
        AlertDialog(
            onDismissRequest = { showVoteDialog = false },
            title = { Text("Confirmar Voto") },
            text = {
                Text("¿Votar ${selectedTipoVoto!!.getDisplayName()} para ${falla.nombre}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onVoteClick(selectedTipoVoto!!)
                        showVoteDialog = false
                    }
                ) {
                    Text("Votar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxSize(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onFallaClick)
                .padding(16.dp)
        ) {
            // Imagen principal de la falla
            val imageUrl = falla.imagenes.firstOrNull()

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen de ${falla.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin imagen disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de la falla
            Text(
                text = falla.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = falla.seccion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            falla.descripcion?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enlace al mapa
            TextButton(
                onClick = onFallaClick
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ver en el mapa")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones de votación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoVoto.entries.forEach { tipoVoto ->
                    Button(
                        onClick = {
                            selectedTipoVoto = tipoVoto
                            showVoteDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (tipoVoto) {
                                TipoVoto.INGENIOSO -> MaterialTheme.colorScheme.primary
                                TipoVoto.CRITICO -> MaterialTheme.colorScheme.secondary
                                TipoVoto.ARTISTICO -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (tipoVoto) {
                                    TipoVoto.INGENIOSO -> "😄"
                                    TipoVoto.CRITICO -> "💭"
                                    TipoVoto.ARTISTICO -> "🎨"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = when (tipoVoto) {
                                    TipoVoto.INGENIOSO -> "Ingenioso"
                                    TipoVoto.CRITICO -> "Crítico"
                                    TipoVoto.ARTISTICO -> "Artístico"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab de mis votos.
 */
@Composable
private fun MisVotosTab(
    votos: List<Voto>,
    isLoading: Boolean,
    onDeleteVote: (Long) -> Unit,
    onFallaClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && votos.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (votos.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No has votado aún",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(votos) { voto ->
                    MiVotoCard(
                        voto = voto,
                        onDeleteClick = { onDeleteVote(voto.idVoto) },
                        onFallaClick = { onFallaClick(voto.idFalla) }
                    )
                }
            }
        }
    }
}

/**
 * Card de un voto del usuario.
 */
@Composable
private fun MiVotoCard(
    voto: Voto,
    onDeleteClick: () -> Unit,
    onFallaClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Voto") },
            text = { Text("¿Estás seguro de que quieres eliminar este voto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onFallaClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (voto.tipoVoto) {
                            TipoVoto.INGENIOSO -> "😄"
                            TipoVoto.CRITICO -> "💭"
                            TipoVoto.ARTISTICO -> "🎨"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = voto.tipoVoto.getDisplayName().replace("😄 ", "").replace("💭 ", "").replace("🎨 ", ""),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = voto.nombreFalla,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                voto.fechaCreacion?.let {
                    Text(
                        text = it.toString().substringBefore('T'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, "Eliminar voto")
            }
        }
    }
}

/**
 * Tab de ranking de votos.
 */
@Composable
private fun RankingTab(
    ranking: List<Pair<Falla, Int>>,
    isLoading: Boolean,
    selectedTipoVoto: TipoVoto?,
    onFilterChange: (TipoVoto?) -> Unit,
    onFallaClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Filtro de tipo de voto
        ScrollableTabRow(
            selectedTabIndex = when (selectedTipoVoto) {
                null -> 0
                TipoVoto.INGENIOSO -> 1
                TipoVoto.CRITICO -> 2
                TipoVoto.ARTISTICO -> 3
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTipoVoto == null,
                onClick = { onFilterChange(null) },
                text = { Text("Todos") }
            )
            TipoVoto.entries.forEach { tipo ->
                Tab(
                    selected = selectedTipoVoto == tipo,
                    onClick = { onFilterChange(tipo) },
                    text = { Text(tipo.getDisplayName()) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && ranking.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (ranking.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay votos aún",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ranking.take(20)) { (falla, votos) ->
                        RankingCard(
                            position = ranking.indexOf(falla to votos) + 1,
                            falla = falla,
                            votos = votos,
                            onFallaClick = { onFallaClick(falla.idFalla) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card de ranking de una falla.
 */
@Composable
private fun RankingCard(
    position: Int,
    falla: Falla,
    votos: Int,
    onFallaClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (position <= 3) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onFallaClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = when (position) {
                    1 -> MaterialTheme.colorScheme.primary
                    2 -> MaterialTheme.colorScheme.secondary
                    3 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (position <= 3) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Información de la falla
            Column(modifier = Modifier.weight(1f)) {
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
            }

            // Número de votos
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = votos.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
