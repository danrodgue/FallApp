package com.fallapp.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.fallapp.features.fallas.presentation.list.FallasListScreen
import com.fallapp.features.map.presentation.MapScreen
import com.fallapp.features.votos.presentation.VotosScreen

/**
 * Pantalla principal con Bottom Navigation Bar.
 * 
 * Contiene las principales secciones de la app:
 * - Mapa
 * - Lista de Fallas
 * - Home
 * - Perfil
 */
@Composable
fun MainScreen(
    navController: NavHostController
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = 0) { 4 }
    val scope = rememberCoroutineScope()
    
    val items = listOf(
        BottomNavItem("Mapa", Icons.Default.LocationOn),
        BottomNavItem("Fallas", Icons.Default.List),
        BottomNavItem("Votos", Icons.Default.Star),
        BottomNavItem("Perfil", Icons.Default.Person)
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        LaunchedEffect(pagerState.currentPage) {
            selectedItem = pagerState.currentPage
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding)
        ) { page ->
            when (page) {
                0 -> MapScreen(
                    onBackClick = { /* No hacer nada, estamos en pantalla principal */ },
                    onFallaClick = { fallaId ->
                        navController.navigate(Screen.FallaDetail.createRoute(fallaId))
                    },
                    modifier = Modifier.fillMaxSize(),
                    hideBackButton = true
                )
                1 -> FallasListScreen(
                    onFallaClick = { fallaId ->
                        navController.navigate(Screen.FallaDetail.createRoute(fallaId))
                    },
                    onBackClick = { /* No hacer nada, estamos en pantalla principal */ },
                    modifier = Modifier.fillMaxSize(),
                    hideBackButton = true
                )
                2 -> VotosScreen(
                    onFallaClick = { fallaId ->
                        // Navegar al mapa centrado en esta falla
                        navController.navigate(Screen.MapFocus.createRoute(fallaId))
                    },
                    modifier = Modifier.fillMaxSize()
                )
                3 -> ProfileTab(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Item de Bottom Navigation.
 */
private data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

/**
 * Tab de Perfil (placeholder).
 */
@Composable
private fun ProfileTab(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Perfil de Usuario",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Información del usuario aparecerá aquí",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onLogout) {
                Text("Cerrar Sesión")
            }
        }
    }
}
