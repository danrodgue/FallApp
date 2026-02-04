package com.example.fallapp.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallapp.domain.model.Falla
import com.example.fallapp.domain.usecase.GetFallasUseCase
import com.example.fallapp.domain.usecase.SearchFallasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val fallas: List<Falla> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val votedFallas: Map<Long, String> = emptyMap()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFallas: GetFallasUseCase,
    private val searchFallas: SearchFallasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadFallas()
    }

    fun loadFallas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = getFallas()
            result
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        fallas = list
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun search() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            loadFallas()
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = searchFallas(query)
            result
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        fallas = list
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
        }
    }

    fun onVote(fallaId: Long, categoria: String) {
        // Un voto por falla (se sobrescribe si repite categoría)
        val updated = _uiState.value.votedFallas.toMutableMap()
        if (!updated.containsKey(fallaId)) {
            updated[fallaId] = categoria
            _uiState.value = _uiState.value.copy(votedFallas = updated)
        }
    }
}

