package com.example.fallapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fallapp.presentation.screens.auth.LoginScreen
import com.example.fallapp.presentation.screens.auth.LoginViewModel
import com.example.fallapp.presentation.screens.auth.RegisterScreen
import com.example.fallapp.presentation.screens.auth.RegisterViewModel
import com.example.fallapp.presentation.screens.home.HomeScreen

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Register : AppDestination("register")
    data object Home : AppDestination("home")
    data object Detail : AppDestination("detail")
}

@Composable
fun FallAppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route
    ) {
        composable(AppDestination.Login.route) {
            val vm: LoginViewModel = hiltViewModel()
            val state by vm.uiState.collectAsState()

            LaunchedEffect(state.isLoggedIn) {
                if (state.isLoggedIn) {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                state = state,
                onAction = { action ->
                    vm.onAction(action)
                    if (action is com.example.fallapp.presentation.screens.auth.LoginAction.NavigateToRegister) {
                        navController.navigate(AppDestination.Register.route)
                    }
                }
            )
        }

        composable(AppDestination.Register.route) {
            val vm: RegisterViewModel = hiltViewModel()
            val state by vm.uiState.collectAsState()

            LaunchedEffect(state.isRegistered) {
                if (state.isRegistered) {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                state = state,
                onAction = vm::onAction
            )
        }

        composable(AppDestination.Home.route) {
            HomeScreen()
        }
    }
}

