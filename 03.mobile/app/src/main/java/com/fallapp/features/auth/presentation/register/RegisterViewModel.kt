package com.fallapp.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Registro.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }
    
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }
    
    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }
    
    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, errorMessage = null) }
    }
    
    fun onApellidosChange(apellidos: String) {
        _uiState.update { it.copy(apellidos = apellidos, errorMessage = null) }
    }
    
    fun onRegisterClick() {
        val currentState = _uiState.value
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            val result = registerUseCase(
                email = currentState.email.trim(),
                password = currentState.password,
                confirmPassword = currentState.confirmPassword,
                nombre = currentState.nombre.trim(),
                apellidos = currentState.apellidos.trim()
            )
            
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registerSuccess = result.data,
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
                is Result.Loading -> {}
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun resetRegisterSuccess() {
        _uiState.update { it.copy(registerSuccess = null) }
    }
}
