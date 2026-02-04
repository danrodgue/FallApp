package com.fallapp.features.fallas.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Categoria
import com.fallapp.features.fallas.domain.usecase.GetFallasByCategoriaUseCase
import com.fallapp.features.fallas.domain.usecase.GetFallasUseCase
import com.fallapp.features.fallas.domain.usecase.SearchFallasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de lista de fallas.
 * 
 * Responsabilidades:
 * - Cargar lista de fallas desde UseCase
 * - Gestionar búsqueda
 * - Filtrar por categoría
 * - Refresh de datos
 */
class FallasListViewModel(
    private val getFallasUseCase: GetFallasUseCase,
    private val searchFallasUseCase: SearchFallasUseCase,
    private val getFallasByCategoriaUseCase: GetFallasByCategoriaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FallasListUiState())
    val uiState: StateFlow<FallasListUiState> = _uiState.asStateFlow()

    init {
        loadFallas()
    }

    /**
     * Carga todas las fallas.
     */
    fun loadFallas(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            getFallasUseCase(forceRefresh).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                fallas = result.data,
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Busca fallas por nombre.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isEmpty()) {
            loadFallas()
        } else {
            viewModelScope.launch {
                searchFallasUseCase(query).collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                        }
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    fallas = result.data,
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
    }

    /**
     * Filtra por categoría.
     */
    fun onCategoriaSelected(categoria: Categoria?) {
        _uiState.update { it.copy(selectedCategoria = categoria) }
        
        if (categoria == null) {
            loadFallas()
        } else {
            viewModelScope.launch {
                getFallasByCategoriaUseCase(categoria).collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                        }
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    fallas = result.data,
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
    }

    /**
     * Pull-to-refresh.
     */
    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadFallas(forceRefresh = true)
    }

    /**
     * Limpia el error.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
