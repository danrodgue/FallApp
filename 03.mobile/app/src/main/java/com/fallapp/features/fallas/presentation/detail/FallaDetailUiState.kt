package com.fallapp.features.fallas.presentation.detail

import com.fallapp.features.fallas.domain.model.Falla

/**
 * Estado UI para la pantalla de detalle de falla.
 */
data class FallaDetailUiState(
    val falla: Falla? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
