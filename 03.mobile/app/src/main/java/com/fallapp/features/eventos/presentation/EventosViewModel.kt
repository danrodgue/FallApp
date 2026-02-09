package com.fallapp.features.eventos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.eventos.domain.model.Evento
import com.fallapp.features.eventos.domain.usecase.GetEventosByFallaUseCase
import com.fallapp.features.eventos.domain.usecase.GetProximosEventosUseCase
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.usecase.GetFallasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventosUiState(
    val fallas: List<Falla> = emptyList(),
    val eventosDeFalla: List<Evento> = emptyList(),
    val eventosProximos: List<Evento> = emptyList(),
    val selectedFalla: Falla? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EventosViewModel(
    private val getFallasUseCase: GetFallasUseCase,
    private val getEventosByFallaUseCase: GetEventosByFallaUseCase,
    private val getProximosEventosUseCase: GetProximosEventosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventosUiState())
    val uiState: StateFlow<EventosUiState> = _uiState.asStateFlow()

    init {
        loadFallas()
        loadProximosEventos()
    }

    private fun loadFallas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getFallasUseCase(forceRefresh = false).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(fallas = result.data, isLoading = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(errorMessage = result.message, isLoading = false)
                    }
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun loadProximosEventos(limit: Int = 50) {
        viewModelScope.launch {
            when (val result = getProximosEventosUseCase(limit)) {
                is Result.Success -> _uiState.update {
                    it.copy(eventosProximos = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(errorMessage = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun buscarEventosDeFalla(falla: Falla) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedFalla = falla) }
            when (val result = getEventosByFallaUseCase(falla.idFalla)) {
                is Result.Success -> _uiState.update {
                    it.copy(eventosDeFalla = result.data, isLoading = false)
                }
                is Result.Error -> _uiState.update {
                    it.copy(errorMessage = result.message, isLoading = false)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

