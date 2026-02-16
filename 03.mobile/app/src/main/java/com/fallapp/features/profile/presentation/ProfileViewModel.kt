package com.fallapp.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import com.fallapp.features.profile.domain.usecase.GetUserProfileUseCase
import com.fallapp.features.profile.domain.usecase.UpdateUserProfileUseCase
import com.fallapp.core.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de perfil del usuario.
 *
 * Gestiona:
 * - Obtención de datos del perfil desde la API
 * - Actualización de datos del perfil
 * - Estados de carga/éxito/error
 * - ID del usuario actual desde el token
 *
 * @property getUserProfileUseCase Use case para obtener perfil
 * @property updateUserProfileUseCase Use case para actualizar perfil
 * @property tokenManager Gestor de tokens para obtener ID del usuario
 */
class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    /**
     * Estado mutable de la UI.
     * Solo el ViewModel puede emitir cambios.
     */
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Carga el perfil del usuario actual.
     *
     * Obtiene el ID del usuario desde TokenManager y realiza
     * la llamada al use case para recuperar los datos.
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // Obtener ID del usuario desde el token
                val userId = getUserIdFromToken()

                if (userId == null) {
                    _uiState.value = ProfileUiState.Error("No se encontró ID de usuario")
                    return@launch
                }

                // Llamar use case y colectar resultados
                getUserProfileUseCase(userId).collect { result ->
                    _uiState.value = when (result) {
                        is Result.Loading -> ProfileUiState.Loading
                        is Result.Success -> ProfileUiState.Success(result.data)
                        is Result.Error -> ProfileUiState.Error(result.message ?: "Error desconocido")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Obtiene el ID del usuario desde el token almacenado.
     *
     * El ID se guarda en TokenManager durante el login junto con el token.
     *
     * @return ID del usuario o null si no está disponible
     */
    private suspend fun getUserIdFromToken(): Long? {
        return tokenManager.getUserId()
    }

    /**
     * Recarga el perfil del usuario.
     * Útil para operaciones de refresco manual.
     */
    fun refreshProfile() {
        loadUserProfile()
    }

    /**
     * Actualiza el perfil del usuario en el servidor.
     *
     * @param nombreCompleto Nombre completo actualizado
     * @param telefono Teléfono actualizado
     * @param direccion Dirección actualizada
     * @param ciudad Ciudad actualizada
     * @param codigoPostal Código postal actualizado
     */
    fun updateProfile(
        nombreCompleto: String,
        telefono: String?,
        direccion: String?,
        ciudad: String?,
        codigoPostal: String?
    ) {
        viewModelScope.launch {
            try {
                val userId = getUserIdFromToken()

                if (userId == null) {
                    _uiState.value = ProfileUiState.Error("No se encontró ID de usuario")
                    return@launch
                }

                updateUserProfileUseCase(
                    userId = userId,
                    nombreCompleto = nombreCompleto,
                    telefono = telefono,
                    direccion = direccion,
                    ciudad = ciudad,
                    codigoPostal = codigoPostal
                ).collect { result ->
                    _uiState.value = when (result) {
                        is Result.Loading -> ProfileUiState.Loading
                        is Result.Success -> ProfileUiState.Success(result.data)
                        is Result.Error -> ProfileUiState.Error(result.message ?: "Error al actualizar")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

/**
 * Estados posibles de la UI de perfil.
 */
sealed class ProfileUiState {
    /** Cargando datos del perfil */
    data object Loading : ProfileUiState()

    /** Perfil cargado exitosamente */
    data class Success(val usuario: UsuarioPerfil) : ProfileUiState()

    /** Error al cargar perfil */
    data class Error(val mensaje: String) : ProfileUiState()
}



