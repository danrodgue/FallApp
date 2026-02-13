package com.fallapp.features.auth.presentation.login

import com.fallapp.features.auth.domain.model.User

/**
 * Estado de UI para la pantalla de Login.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: User? = null
)
