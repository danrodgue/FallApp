package com.fallapp.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fallapp.core.util.TokenManager
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

/**
 * Pantalla de Splash que verifica si hay sesión activa.
 *
 * - Si hay token válido: Navega a Main (usuario ya logueado)
 * - Si no hay token: Navega a Login
 *
 * Esta pantalla se muestra brevemente mientras se verifica la autenticación.
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    tokenManager: TokenManager = koinInject()
) {
    LaunchedEffect(Unit) {
        // Verificar si hay token guardado
        val token = tokenManager.authToken.first()

        if (token != null) {
            // Hay sesión activa, ir a Main
            onNavigateToMain()
        } else {
            // No hay sesión, ir a Login
            onNavigateToLogin()
        }
    }

    // UI de splash mientras se verifica
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

