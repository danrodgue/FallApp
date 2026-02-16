package com.fallapp.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fallapp.features.auth.presentation.login.LoginScreen
import com.fallapp.features.auth.presentation.register.RegisterScreen
import com.fallapp.features.fallas.presentation.detail.FallaDetailScreen
import com.fallapp.features.fallas.presentation.list.FallasListScreen
import com.fallapp.features.map.presentation.MapScreen

/**
 * Grafo de navegación principal de la aplicación.
 * 
 * Define todas las rutas y transiciones entre pantallas.
 * 
 * @param navController Controlador de navegación
 * @param startDestination Pantalla inicial (login o home si ya está logueado)
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen - Verificación de sesión
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth Flow
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navegar a la pantalla principal con bottom navigation
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Después del registro, volver al login
                    navController.popBackStack()
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        // Main Screen con Bottom Navigation
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        
        // Home (placeholder por ahora)
        composable(Screen.Home.route) {
            HomeScreenPlaceholder(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onViewFallas = {
                    navController.navigate(Screen.FallasList.route)
                },
                onViewMap = {
                    navController.navigate(Screen.Map.route)
                }
            )
        }
        
        // Fallas List
        composable(Screen.FallasList.route) {
            FallasListScreen(
                onFallaClick = { fallaId ->
                    navController.navigate(Screen.FallaDetail.createRoute(fallaId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Falla Detail
        composable(Screen.FallaDetail.route) { backStackEntry ->
            val fallaId = backStackEntry.arguments?.getString("fallaId")?.toLongOrNull()
            if (fallaId != null) {
                FallaDetailScreen(
                    fallaId = fallaId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Mapa de Fallas
        composable(Screen.Map.route) {
            MapScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onFallaClick = { fallaId ->
                    navController.navigate(Screen.FallaDetail.createRoute(fallaId))
                }
            )
        }

        // Mapa centrado en una falla concreta
        composable(Screen.MapFocus.route) { backStackEntry ->
            val fallaId = backStackEntry.arguments?.getString("fallaId")?.toLongOrNull()
            MapScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onFallaClick = { id ->
                    navController.navigate(Screen.FallaDetail.createRoute(id))
                },
                focusFallaId = fallaId
            )
        }
    }
}

/**
 * Pantalla placeholder para Home (se implementará después).
 */
@Composable
private fun HomeScreenPlaceholder(
    onLogout: () -> Unit,
    onViewFallas: () -> Unit,
    onViewMap: () -> Unit
) {
    Scaffold { padding ->
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
                    text = "🎉 Login Exitoso!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Bienvenido a FallApp",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = onViewFallas) {
                    Text("Ver Fallas")
                }
                
                Button(onClick = onViewMap) {
                    Text("Ver Mapa")
                }
                
                Button(onClick = onLogout) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}
