package com.fallapp.features.fallas.presentation.list

import com.fallapp.features.fallas.domain.model.Categoria
import com.fallapp.features.fallas.domain.model.Falla

/**
 * Estado UI para la pantalla de lista de fallas.
 */
data class FallasListUiState(
    val fallas: List<Falla> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedCategoria: Categoria? = null,
    val isRefreshing: Boolean = false
)
