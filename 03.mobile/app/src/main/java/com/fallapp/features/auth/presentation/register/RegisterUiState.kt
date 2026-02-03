package com.fallapp.features.auth.presentation.register

import com.fallapp.features.auth.domain.model.User

/**
 * Estado de UI para la pantalla de Registro.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: User? = null
)
