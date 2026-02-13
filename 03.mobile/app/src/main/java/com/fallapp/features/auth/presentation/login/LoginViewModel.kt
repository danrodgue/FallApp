package com.fallapp.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Login.
 * 
 * Maneja la lógica de presentación y coordina con el caso de uso de login.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    /**
     * Actualiza el email en el estado.
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }
    
    /**
     * Actualiza la contraseña en el estado.
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }
    
    /**
     * Ejecuta el login.
     */
    fun onLoginClick() {
        val currentState = _uiState.value
        
        // Mostrar loading
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            val result = loginUseCase(
                email = currentState.email.trim(),
                password = currentState.password
            )
            
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginSuccess = result.data.user,
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Error desconocido"
                        )
                    }
                }
                is Result.Loading -> {
                    // Ya está en loading
                }
            }
        }
    }
    
    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Resetea el estado de éxito (para navegación).
     */
    fun resetLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = null) }
    }
}
