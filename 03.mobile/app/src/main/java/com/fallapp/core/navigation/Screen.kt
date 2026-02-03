package com.fallapp.core.navigation

/**
 * Rutas de navegación de la aplicación.
 * 
 * Define todas las pantallas como sealed class para type-safety.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
sealed class Screen(val route: String) {
    
    // Auth
    data object Login : Screen("login")
    data object Register : Screen("register")
    
    // Main
    data object Home : Screen("home")
    
    // Fallas
    data object FallasList : Screen("fallas_list")
    data object FallaDetail : Screen("fallas_detail/{fallaId}") {
        fun createRoute(fallaId: Long) = "fallas_detail/$fallaId"
    }
    
    // Eventos
    data object EventosList : Screen("eventos_list")
    
    // Ninots
    data object NinotsList : Screen("ninots_list")
    
    // Profile
    data object Profile : Screen("profile")
}
