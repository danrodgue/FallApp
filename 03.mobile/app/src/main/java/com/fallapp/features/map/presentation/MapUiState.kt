package com.fallapp.features.map.presentation

import com.fallapp.features.fallas.domain.model.Falla

/**
 * Estado de UI para la pantalla de Mapa.
 */
data class MapUiState(
    val fallas: List<Falla> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFalla: Falla? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null
)
