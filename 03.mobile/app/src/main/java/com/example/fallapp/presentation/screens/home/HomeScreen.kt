package com.example.fallapp.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fallapp.ui.theme.CreamBackground
import com.example.fallapp.ui.theme.DarkText
import com.example.fallapp.ui.theme.DarkTextSecondary
import com.example.fallapp.ui.theme.OrangeAction
import com.example.fallapp.ui.theme.PeachSurface
import com.example.fallapp.ui.theme.RedAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Mapa", "Fallas", "Eventos", "Ajustes")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val showFilters = remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DarkText) {
                tabs.forEachIndexed { index, label ->
                    val selected = pagerState.currentPage == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = {
                            val icon = when (index) {
                                0 -> Icons.Filled.Map
                                1 -> Icons.Filled.Star
                                2 -> Icons.Filled.List
                                else -> Icons.Filled.Settings
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = Color.White
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> MapPage(fallas = state.fallas)
                    1 -> FallasPage(
                        state = state,
                        onSearchChange = viewModel::onSearchQueryChanged,
                        onSearch = { viewModel.search() },
                        onVote = viewModel::onVote
                    )
                    2 -> EventosPage(
                        showFilters = { showFilters.value = true }
                    )
                    3 -> SettingsPage()
                }
            }

            if (showFilters.value) {
                FiltersOverlay(onClose = { showFilters.value = false })
            }
        }
    }
}

@Composable
private fun MapPage(fallas: List<com.example.fallapp.domain.model.Falla>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RedAccent)
    ) {
        // Menú lateral (hamburguesa)
        Icon(
            imageVector = Icons.Filled.Map,
            contentDescription = "Menú",
            tint = DarkText,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Texto vertical "MAPA"
        Text(
            text = "MAPA",
            color = DarkText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .rotate(90f)
        )

        // Lista simple de fallas "anclada" en la parte inferior del mapa
        if (fallas.isNotEmpty()) {
            Surface(
                color = CreamBackground.copy(alpha = 0.9f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(fallas.take(5)) { falla ->
                        Text(
                            text = falla.nombre,
                            color = DarkText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { /* en esta versión simple no hacemos nada */ },
            containerColor = OrangeAction,
            contentColor = CreamBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Ir"
            )
        }
    }
}

@Composable
private fun FallasPage(
    state: HomeUiState,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    onVote: (Long, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Fallas",
            color = DarkText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Carta tipo "Tinder" (una sola falla a la vez) cuando no se está buscando
        if (state.searchQuery.isBlank()) {
            val current = state.fallas.firstOrNull { !state.votedFallas.containsKey(it.id) }

            if (current != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PeachSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = current.nombre,
                            color = DarkText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        current.seccion?.let {
                            Text(text = "Sección: $it", color = DarkTextSecondary, fontSize = 12.sp)
                        }
                        current.categoria?.let {
                            Text(text = "Categoría: $it", color = DarkTextSecondary, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Elige una categoría para votar:",
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VoteChip(
                                text = "Ingenioso",
                                selected = state.votedFallas[current.id] == "INGENIOSO",
                                onClick = { onVote(current.id, "INGENIOSO") }
                            )
                            VoteChip(
                                text = "Crítico",
                                selected = state.votedFallas[current.id] == "CRITICO",
                                onClick = { onVote(current.id, "CRITICO") }
                            )
                            VoteChip(
                                text = "Artístico",
                                selected = state.votedFallas[current.id] == "ARTISTICO",
                                onClick = { onVote(current.id, "ARTISTICO") }
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay más fallas por votar.",
                    color = DarkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buscador (muestra listado cuando hay texto de búsqueda)
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Buscar falla", color = DarkText) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSearch,
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeAction,
                contentColor = Color.White
            )
        ) {
            Text("Buscar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.searchQuery.isNotBlank()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CreamBackground),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.fallas) { falla ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PeachSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = falla.nombre,
                                color = DarkText,
                                fontWeight = FontWeight.SemiBold
                            )
                            falla.seccion?.let {
                                Text(text = "Sección: $it", color = DarkTextSecondary, fontSize = 12.sp)
                            }
                            falla.categoria?.let {
                                Text(text = "Categoría: $it", color = DarkTextSecondary, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val yaVotada = state.votedFallas.containsKey(falla.id)
                            if (yaVotada) {
                                Text(
                                    text = "Ya has votado esta falla (${state.votedFallas[falla.id]})",
                                    color = DarkTextSecondary,
                                    fontSize = 12.sp
                                )
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    VoteChip(
                                        text = "Ingenioso",
                                        selected = false,
                                        onClick = { onVote(falla.id, "INGENIOSO") }
                                    )
                                    VoteChip(
                                        text = "Crítico",
                                        selected = false,
                                        onClick = { onVote(falla.id, "CRITICO") }
                                    )
                                    VoteChip(
                                        text = "Artístico",
                                        selected = false,
                                        onClick = { onVote(falla.id, "ARTISTICO") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoteChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, RedAccent),
        color = if (selected) RedAccent else CreamBackground,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Transparent)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else DarkText,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EventosPage(
    showFilters: () -> Unit
) {
    val dummyEventos = listOf(
        "Mascletà Día Grande - 14:00",
        "Plantà Falla Central - 08:00",
        "Ofrena de Flores - 17:00",
        "Cremà 2026 - 22:00"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header de filtro
        Surface(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, RedAccent),
            color = PeachSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Todos", color = DarkText, fontWeight = FontWeight.Bold)
                    Text(text = "Eventos", color = DarkTextSecondary)
                }
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filtros",
                    tint = DarkText,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CreamBackground)
                        .padding(4.dp)
                        .let { mod ->
                            mod
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Buscador
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CreamBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Escribe...",
                color = DarkText,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dummyEventos) { title ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CreamBackground),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PeachSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = title, color = DarkText, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toca para ver detalle y categoría",
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltersOverlay(
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RedAccent.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PeachSurface,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(260.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar",
                        tint = DarkText,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CreamBackground)
                            .padding(4.dp)
                    )
                    Text(text = "Filtros avanzados", color = DarkText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(0.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                repeat(4) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CreamBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Selector ${it + 1}",
                            color = DarkText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAction,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Aplicar")
                }
            }
        }
    }
}

@Composable
private fun SettingsPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ajustes de cuenta",
            color = DarkText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Desde aquí podrás cerrar sesión o cambiar de cuenta.",
            color = DarkTextSecondary
        )

        Button(
            onClick = {
                // TODO: implementar logout real (borrar token + volver a login)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = RedAccent,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = "Cerrar sesión")
        }
    }
}

