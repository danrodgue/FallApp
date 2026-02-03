package com.example.fallapp.presentation.screens.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginAction {
    data class EmailChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object Submit : LoginAction
    data object NavigateToRegister : LoginAction

    /**
     * Acción interna para indicar a la navegación que el login ha ido bien.
     * No viene del usuario, la dispara el propio ViewModel.
     */
    data object OnLoginSuccess : LoginAction
}

