package com.fallapp.features.votos.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fallapp.user.R
import coil.compose.AsyncImage
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.model.FallaRanking
import com.fallapp.features.fallas.domain.model.TipoVoto
import com.fallapp.features.fallas.domain.model.Voto
import kotlinx.coroutines.launch
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
    var selectedTab by remember { mutableIntStateOf(1) } // Iniciar en tab 1 (Votar)
    val snackbarHostState = remember { SnackbarHostState() }
    var showInfo by remember { mutableStateOf(false) }
    var selectedFalla by remember { mutableStateOf<Falla?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }

    fun openFallaInVoteCard(idFalla: Long) {
        val falla = uiState.fallas.firstOrNull { it.idFalla == idFalla }
            ?: uiState.fallasParaVotar.firstOrNull { it.idFalla == idFalla }

        if (falla != null) {
            selectedFalla = falla
            selectedTab = 1
            showSearchField = false
            searchQuery = ""
        }
    }

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

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedTab == 1) { // Solo mostrar lupa en tab Votar
                                IconButton(onClick = { showSearchField = !showSearchField }) {
                                    Icon(
                                        imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = if (showSearchField) "Cerrar búsqueda" else "Buscar falla",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Text(
                                "Votos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showInfo = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Información del sistema de votos"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                // Campo de búsqueda debajo del título (solo en tab Votar)
                if (selectedTab == 1 && showSearchField) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                // Buscar falla que coincida
                                selectedFalla = if (query.isBlank()) {
                                    null
                                } else {
                                    uiState.fallasParaVotar.firstOrNull { falla ->
                                        falla.nombre.contains(query, ignoreCase = true) ||
                                        falla.seccion.contains(query, ignoreCase = true)
                                    }
                                }
                            },
                            placeholder = { Text("Buscar falla...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = MaterialTheme.shapes.small,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        selectedFalla = null
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar"
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ranking") },
                        icon = { Icon(Icons.Default.DateRange, null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Votar") },
                        icon = { Icon(Icons.Default.Whatshot, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Mis Votos") },
                        icon = { Icon(Icons.Default.Favorite, null) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (showInfo) {
            AlertDialog(
                onDismissRequest = { showInfo = false },
                title = {
                    Text(
                        text = "Cómo funcionan los votos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "• Cada usuario puede votar una falla una sola vez por categoría.\n" +
                                    "• Pulsa uno de los 3 botones de icono para votar por categoría.\n" +
                                    "• Tus votos se ligan a tu usuario y se usan para construir el ranking general.\n" +
                                    "• Puedes ver y gestionar tus votos en la pestaña «Mis Votos».",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        HorizontalDivider()

                        Text(
                            text = "Leyenda de iconos:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        VoteLegendRow(
                            iconRes = R.drawable.trofeo,
                            label = "Mejor Falla (Monumento)"
                        )
                        VoteLegendRow(
                            iconRes = R.drawable.risa,
                            label = "Ingenio y Gracia"
                        )
                        VoteLegendRow(
                            iconRes = R.drawable.experimental,
                            label = "Mejor Experimental"
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfo = false }) {
                        Text("Entendido")
                    }
                }
            )
        }

        when (selectedTab) {
            0 -> RankingTab(
                ranking = uiState.ranking,
                isLoading = uiState.isLoading,
                selectedTipoVoto = uiState.rankingFilter,
                onFilterChange = { tipoVoto ->
                    viewModel.setRankingFilter(tipoVoto)
                },
                onFallaClick = { idFalla ->
                    openFallaInVoteCard(idFalla)
                },
                modifier = Modifier.padding(padding)
            )
            1 -> VotarTab(
                fallas = uiState.fallasParaVotar,
                selectedFalla = selectedFalla,
                isLoading = uiState.isLoading,
                onVoteClick = { falla, tipoVoto ->
                    viewModel.votar(falla, tipoVoto)
                },
                onFallaClick = onFallaClick,
                onCommentSubmit = { falla, contenido ->
                    viewModel.enviarComentario(falla, contenido)
                },
                modifier = Modifier.padding(padding)
            )
            2 -> MisVotosTab(
                votos = uiState.misVotos,
                fallas = uiState.fallasParaVotar,
                isLoading = uiState.isLoading,
                onDeleteVote = { idVoto ->
                    viewModel.eliminarVoto(idVoto)
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
    selectedFalla: Falla?,
    isLoading: Boolean,
    onVoteClick: (Falla, TipoVoto) -> Unit,
    onFallaClick: (Long) -> Unit,
    onCommentSubmit: (Falla, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
            if (selectedFalla != null) {
                // Mostrar falla específica seleccionada del dropdown
                SwipeFallaCard(
                    falla = selectedFalla,
                    onVoteClick = { tipoVoto -> onVoteClick(selectedFalla, tipoVoto) },
                    onFallaClick = { onFallaClick(selectedFalla.idFalla) },
                    onCommentSubmit = { contenido -> onCommentSubmit(selectedFalla, contenido) }
                )
            } else {
                // Modo mazo aleatorio normal
                StackedCardDeck(
                    fallas = fallas,
                    onVoteClick = onVoteClick,
                    onFallaClick = onFallaClick,
                    onCommentSubmit = onCommentSubmit
                )
            }
        }
    }
}

/**
 * Pila de cartas tipo "Tinder" con swipe horizontal.
 *
 * Implementación simplificada y fluida:
 * - Solo hay UNA carta en el árbol de nodos.
 * - Al hacer swipe suficiente, la carta actual sale por un lado y la siguiente entra suavemente desde el lado contrario.
 */
@Composable
private fun StackedCardDeck(
    fallas: List<Falla>,
    onVoteClick: (Falla, TipoVoto) -> Unit,
    onFallaClick: (Long) -> Unit,
    onCommentSubmit: (Falla, String) -> Unit = { _, _ -> }
) {
    if (fallas.isEmpty()) return

    val scope = rememberCoroutineScope()

    var currentIndex by remember { mutableIntStateOf(0) }

    val activeOffsetX = remember { Animatable(0f) }
    val activeScale = remember { Animatable(1f) }

    val swipeThreshold = 150f
    val screenWidth = 800f // valor aproximado para animación de salida

    fun computeNextIndex(direction: Float): Int =
        if (direction >= 0f) {
            // Swipe a la derecha → siguiente
            (currentIndex + 1) % fallas.size
        } else {
            // Swipe a la izquierda → anterior
            (currentIndex - 1 + fallas.size) % fallas.size
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val activeFalla = fallas[currentIndex]
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = activeOffsetX.value
                    scaleX = activeScale.value
                    scaleY = activeScale.value
                }
                .zIndex(10f)
                .pointerInput(fallas, currentIndex) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                activeOffsetX.snapTo(activeOffsetX.value + dragAmount)
                                val normalized = (activeOffsetX.value / screenWidth).coerceIn(-1f, 1f)
                                activeScale.snapTo(1f - 0.05f * kotlin.math.abs(normalized))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                val shouldSwipe = kotlin.math.abs(activeOffsetX.value) > swipeThreshold
                                val direction = if (activeOffsetX.value >= 0f) 1f else -1f

                                if (shouldSwipe) {
                                    // 1) Animar carta actual hacia afuera
                                    activeOffsetX.animateTo(
                                        targetValue = direction * screenWidth,
                                        animationSpec = tween(
                                            durationMillis = 260,
                                            easing = FastOutSlowInEasing
                                        )
                                    )

                                    // 2) Cambiar índice a la siguiente carta
                                    currentIndex = computeNextIndex(direction)

                                    // 3) Preparar entrada de la nueva carta desde el lado opuesto
                                    activeOffsetX.snapTo(-direction * screenWidth * 0.5f)
                                    activeScale.snapTo(0.9f)

                                    // 4) Animar nueva carta al centro
                                    activeOffsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = 280,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    activeScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 280,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                } else {
                                    // Volver al centro sin cambiar de carta
                                    activeOffsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = 220,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    activeScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 220,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
        ) {
            SwipeFallaCard(
                falla = activeFalla,
                onVoteClick = { tipoVoto -> onVoteClick(activeFalla, tipoVoto) },
                onFallaClick = { onFallaClick(activeFalla.idFalla) },
                onCommentSubmit = { contenido -> onCommentSubmit(activeFalla, contenido) }
            )
        }

        // Contador de cartas (igual estilo que te gustaba: posición / total)
        Text(
            text = "${currentIndex + 1} / ${fallas.size}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Carta tipo "Tinder" para votar una falla.
 */
@Composable
private fun SwipeFallaCard(
    falla: Falla,
    onVoteClick: (TipoVoto) -> Unit,
    onFallaClick: () -> Unit,
    onCommentSubmit: (String) -> Unit = {}
) {
    var showVoteDialog by remember { mutableStateOf(false) }
    var selectedTipoVoto by remember { mutableStateOf<TipoVoto?>(null) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var showCommentBox by remember { mutableStateOf(false) }
    var comentarioTexto by remember { mutableStateOf("") }
    val imageUrl = falla.imagenes.firstOrNull()

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

    // Dialog de imagen en pantalla completa
    if (showFullScreenImage && imageUrl != null) {
        Dialog(
            onDismissRequest = { showFullScreenImage = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showFullScreenImage = false }
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen de ${falla.nombre}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Botón X con fondo circular semitransparente
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    IconButton(
                        onClick = { showFullScreenImage = false }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Imagen principal de la falla (clickeable para pantalla completa)
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen de ${falla.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { showFullScreenImage = true },
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
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onFallaClick)
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

            // Fila: Ver mapa + botón para desplegar comentarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onFallaClick) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver en el mapa")
                }

                TextButton(
                    onClick = { showCommentBox = !showCommentBox }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentar"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Comentar")
                }
            }

            // Desplegable de comentario
            if (showCommentBox) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = comentarioTexto,
                            onValueChange = { comentarioTexto = it },
                            placeholder = {
                                Text(
                                    text = "Escribe tu comentario...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 4
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showCommentBox = false
                                comentarioTexto = ""
                            }) {
                                Text("Cancelar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val texto = comentarioTexto.trim()
                                    if (texto.length >= 3) {
                                        onCommentSubmit(texto)
                                        comentarioTexto = ""
                                        showCommentBox = false
                                    }
                                },
                                enabled = comentarioTexto.trim().length >= 3
                            ) {
                                Text("Enviar")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botones de votación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoVoto.entries.forEach { tipoVoto ->
                    val voteColors = when (tipoVoto) {
                        TipoVoto.INGENIOSO -> Triple(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.onPrimary,
                            R.drawable.trofeo
                        )
                        TipoVoto.CRITICO -> Triple(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.onSecondary,
                            R.drawable.risa
                        )
                        TipoVoto.ARTISTICO -> Triple(
                            Color(0xFFFFE3A1),
                            MaterialTheme.colorScheme.onSurface,
                            R.drawable.experimental
                        )
                    }

                    Button(
                        onClick = {
                            selectedTipoVoto = tipoVoto
                            showVoteDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 64.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp,
                            hoveredElevation = 5.dp
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = voteColors.first,
                            contentColor = voteColors.second,
                            disabledContainerColor = voteColors.first.copy(alpha = 0.5f),
                            disabledContentColor = voteColors.second.copy(alpha = 0.75f)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val iconSize = if (tipoVoto == TipoVoto.CRITICO) 36.dp else 28.dp

                            Image(
                                painter = painterResource(id = voteColors.third),
                                contentDescription = when (tipoVoto) {
                                    TipoVoto.INGENIOSO -> "Votar Mejor Falla"
                                    TipoVoto.CRITICO -> "Votar Ingenio y Gracia"
                                    TipoVoto.ARTISTICO -> "Votar Mejor Experimental"
                                },
                                modifier = Modifier.size(iconSize),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoteLegendRow(
    iconRes: Int,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Tab de mis votos.
 */
@Composable
private fun MisVotosTab(
    votos: List<Voto>,
    fallas: List<Falla>,
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
                    val falla = fallas.firstOrNull { it.idFalla == voto.idFalla }
                    MiVotoCard(
                        voto = voto,
                        falla = falla,
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
    falla: Falla?,
    onDeleteClick: () -> Unit,
    onFallaClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    val imageUrl = falla?.imagenes?.firstOrNull()

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

    // Dialog de imagen en pantalla completa
    if (showFullScreenImage && imageUrl != null) {
        Dialog(
            onDismissRequest = { showFullScreenImage = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showFullScreenImage = false }
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen de ${voto.nombreFalla}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Botón X con fondo circular semitransparente
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    IconButton(
                        onClick = { showFullScreenImage = false }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = {
                    if (imageUrl != null) {
                        showFullScreenImage = true
                    }
                })
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = when (voto.tipoVoto) {
                                TipoVoto.INGENIOSO -> R.drawable.trofeo
                                TipoVoto.CRITICO -> R.drawable.risa
                                TipoVoto.ARTISTICO -> R.drawable.experimental
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = when (voto.tipoVoto) {
                            TipoVoto.INGENIOSO -> "Mejor Falla"
                            TipoVoto.CRITICO -> "Ingenio y Gracia"
                            TipoVoto.ARTISTICO -> "Mejor Experimental"
                        },
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
    ranking: List<FallaRanking>,
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
                val label = when (tipo) {
                    TipoVoto.INGENIOSO -> "Mejor Falla"
                    TipoVoto.CRITICO -> "Ingenio y Gracia"
                    TipoVoto.ARTISTICO -> "Mejor Experimental"
                }
                val iconRes = when (tipo) {
                    TipoVoto.INGENIOSO -> R.drawable.trofeo
                    TipoVoto.CRITICO -> R.drawable.risa
                    TipoVoto.ARTISTICO -> R.drawable.experimental
                }
                Tab(
                    selected = selectedTipoVoto == tipo,
                    onClick = { onFilterChange(tipo) },
                    text = { Text(label) },
                    icon = {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Unspecified
                        )
                    }
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
                        // Icono de fuego cuando no hay ranking
                        imageVector = Icons.Default.Whatshot,
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
                    items(ranking.take(20).withIndex().toList()) { indexed ->
                        val position = indexed.index + 1
                        val item = indexed.value
                        RankingCard(
                            position = position,
                            item = item,
                            selectedTipoVoto = selectedTipoVoto,
                            onFallaClick = { onFallaClick(item.idFalla) }
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
    item: FallaRanking,
    selectedTipoVoto: TipoVoto?,
    onFallaClick: () -> Unit
) {
    // Color del badge de votos según la categoría seleccionada en el filtro
    val chipColor = when (selectedTipoVoto) {
        null -> MaterialTheme.colorScheme.primaryContainer
        TipoVoto.INGENIOSO -> MaterialTheme.colorScheme.primary      // Mejor Falla
        TipoVoto.CRITICO -> MaterialTheme.colorScheme.secondary      // Ingenio y Gracia
        TipoVoto.ARTISTICO -> Color(0xFFFFE3A1)                      // Experimental (mismo color que el botón de votos)
    }
    val chipContentColor = when (selectedTipoVoto) {
        null -> MaterialTheme.colorScheme.onPrimaryContainer
        TipoVoto.ARTISTICO -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onPrimary
    }
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
                    text = item.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.seccion ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Número de votos
            Surface(
                shape = MaterialTheme.shapes.small,
                color = chipColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        // Icono de fuego en el badge de votos
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = chipContentColor
                    )
                    Text(
                        text = item.votos.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = chipContentColor
                    )
                }
            }
        }
    }
}
