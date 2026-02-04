package com.fallapp.features.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.usecase.GetFallasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Mapa.
 * 
 * Responsabilidades:
 * - Cargar fallas con ubicación GPS
 * - Gestionar ubicación del usuario
 * - Manejar selección de marcadores
 */
class MapViewModel(
    private val getFallasUseCase: GetFallasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadFallas()
    }

    /**
     * Carga todas las fallas con ubicación GPS.
     */
    fun loadFallas() {
        viewModelScope.launch {
            getFallasUseCase(forceRefresh = false).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Result.Success -> {
                        // Filtrar solo fallas con coordenadas GPS válidas
                        val fallasConUbicacion = result.data.filter { falla ->
                            val lat = falla.ubicacion.latitud
                            val lng = falla.ubicacion.longitud
                            lat != null && lng != null && 
                            lat != 0.0 && lng != 0.0 &&
                            lat in -90.0..90.0 && lng in -180.0..180.0
                        }
                        _uiState.update {
                            it.copy(
                                fallas = fallasConUbicacion,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Selecciona una falla al tocar su marcador.
     */
    fun onFallaSelected(falla: com.fallapp.features.fallas.domain.model.Falla) {
        _uiState.update { it.copy(selectedFalla = falla) }
    }

    /**
     * Deselecciona la falla actual.
     */
    fun onFallaDeselected() {
        _uiState.update { it.copy(selectedFalla = null) }
    }

    /**
     * Actualiza la ubicación del usuario.
     */
    fun onUserLocationUpdate(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                userLatitude = latitude,
                userLongitude = longitude
            )
        }
    }

    /**
     * Limpia el error.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
