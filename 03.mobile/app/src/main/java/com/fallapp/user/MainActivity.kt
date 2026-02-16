package com.fallapp.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fallapp.core.navigation.NavGraph
import com.fallapp.core.ui.theme.FallAppTheme

/**
 * Actividad principal de FallApp User.
 * 
 * Configura la navegación y el tema de la aplicación con la paleta de colores
 * coincidente con 02.desktop (rojo #c62828).
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FallAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

