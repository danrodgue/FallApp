package com.example.fallapp.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallapp.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged ->
                _uiState.value = _uiState.value.copy(email = action.value, errorMessage = null)

            is LoginAction.PasswordChanged ->
                _uiState.value = _uiState.value.copy(password = action.value, errorMessage = null)

            LoginAction.Submit -> login()
            LoginAction.NavigateToRegister -> {
                // En una versión más avanzada, navegaríamos a pantalla de registro.
            }

            LoginAction.OnLoginSuccess -> {
                // Esta acción se maneja desde el NavHost, no aquí.
            }
        }
    }

    private fun login() {
        val current = _uiState.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Email y contraseña son obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)
            val result = loginUseCase(current.email, current.password)

            result
                .onSuccess {
                    // Guardado del token se realiza en el propio use case / repositorio.
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Notificamos éxito mediante acción especial que observa el NavHost.
                    onAction(LoginAction.OnLoginSuccess)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error desconocido"
                    )
                }
        }
    }
}

