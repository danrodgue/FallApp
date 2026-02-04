package com.example.fallapp.presentation.screens.auth

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val nombreCompleto: String = "",
    val idFalla: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
)

sealed interface RegisterAction {
    data class EmailChanged(val value: String) : RegisterAction
    data class PasswordChanged(val value: String) : RegisterAction
    data class NombreCompletoChanged(val value: String) : RegisterAction
    data class IdFallaChanged(val value: String) : RegisterAction
    data object Submit : RegisterAction
}

