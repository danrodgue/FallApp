package com.example.fallapp.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallapp.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.EmailChanged ->
                _uiState.value = _uiState.value.copy(email = action.value, errorMessage = null)

            is RegisterAction.PasswordChanged ->
                _uiState.value = _uiState.value.copy(password = action.value, errorMessage = null)

            is RegisterAction.NombreCompletoChanged ->
                _uiState.value = _uiState.value.copy(nombreCompleto = action.value, errorMessage = null)

            is RegisterAction.IdFallaChanged ->
                _uiState.value = _uiState.value.copy(idFalla = action.value, errorMessage = null)

            RegisterAction.Submit -> register()
        }
    }

    private fun register() {
        val current = _uiState.value
        if (current.email.isBlank() || current.password.isBlank() || current.nombreCompleto.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Todos los campos son obligatorios salvo Falla")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)
            val idFallaLong = current.idFalla.toLongOrNull()
            val result = registerUseCase(
                email = current.email,
                password = current.password,
                nombreCompleto = current.nombreCompleto,
                idFalla = idFallaLong
            )

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistered = true
                    )
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

