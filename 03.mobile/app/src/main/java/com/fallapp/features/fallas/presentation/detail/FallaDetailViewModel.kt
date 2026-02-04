package com.fallapp.features.fallas.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.TipoVoto
import com.fallapp.features.fallas.domain.model.VotoRequest
import com.fallapp.features.fallas.domain.usecase.EliminarVotoUseCase
import com.fallapp.features.fallas.domain.usecase.GetFallaByIdUseCase
import com.fallapp.features.fallas.domain.usecase.GetVotosFallaUseCase
import com.fallapp.features.fallas.domain.usecase.VotarFallaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de falla con sistema de votación.
 * 
 * @property getFallaByIdUseCase Use case para obtener falla
 * @property votarFallaUseCase Use case para votar
 * @property getVotosFallaUseCase Use case para obtener votos de la falla
 * @property eliminarVotoUseCase Use case para eliminar voto
 */
class FallaDetailViewModel(
    private val getFallaByIdUseCase: GetFallaByIdUseCase,
    private val votarFallaUseCase: VotarFallaUseCase,
    private val getVotosFallaUseCase: GetVotosFallaUseCase,
    private val eliminarVotoUseCase: EliminarVotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FallaDetailUiState())
    val uiState: StateFlow<FallaDetailUiState> = _uiState.asStateFlow()

    /**
     * Carga la falla y sus votos.
     */
    fun loadFalla(id: Long) {
        viewModelScope.launch {
            getFallaByIdUseCase(id).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                falla = result.data,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        // Cargar votos de la falla
                        loadVotos(id)
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
     * Carga los votos de la falla.
     */
    private fun loadVotos(idFalla: Long) {
        viewModelScope.launch {
            when (val result = getVotosFallaUseCase(idFalla)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            votos = result.data,
                            // TODO: Filtrar votosUsuario por idUsuario actual del TokenManager
                            votosUsuario = emptyList() // Por ahora vacío, necesitaremos TokenManager
                        )
                    }
                }
                is Result.Error -> {
                    // Error al cargar votos, no es crítico
                    _uiState.update { it.copy(voteError = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    /**
     * Muestra el diálogo de votación para un tipo específico.
     */
    fun showVoteDialog(tipoVoto: TipoVoto) {
        _uiState.update {
            it.copy(
                showVoteDialog = true,
                selectedTipoVoto = tipoVoto,
                voteError = null,
                voteSuccess = false
            )
        }
    }
    
    /**
     * Cierra el diálogo de votación.
     */
    fun dismissVoteDialog() {
        _uiState.update {
            it.copy(
                showVoteDialog = false,
                selectedTipoVoto = null,
                voteError = null,
                voteSuccess = false
            )
        }
    }
    
    /**
     * Ejecuta el voto.
     * 
     * Nota: idNinot debe ser el ID de un ninot de esta falla.
     * Para simplificar, usaremos el idFalla como idNinot temporalmente
     * (en producción deberías obtener el primer ninot de la falla).
     */
    fun vote(tipoVoto: TipoVoto, idNinot: Long) {
        val currentState = _uiState.value
        
        // Verificar si ya votó con este tipo
        if (currentState.hasVotedWith(tipoVoto)) {
            _uiState.update {
                it.copy(voteError = "Ya has votado ${tipoVoto.getDisplayName()} en esta falla")
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isVoting = true, voteError = null) }
            
            val request = VotoRequest(
                idNinot = idNinot,
                tipoVoto = tipoVoto
            )
            
            when (val result = votarFallaUseCase(request)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isVoting = false,
                            voteSuccess = true,
                            voteError = null,
                            showVoteDialog = false
                        )
                    }
                    // Recargar votos
                    currentState.falla?.let { falla ->
                        loadVotos(falla.idFalla)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isVoting = false,
                            voteSuccess = false,
                            voteError = result.message ?: "Error al votar"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    /**
     * Elimina un voto existente.
     */
    fun removeVote(idVoto: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVoting = true) }
            
            when (val result = eliminarVotoUseCase(idVoto)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isVoting = false,
                            voteSuccess = true
                        )
                    }
                    // Recargar votos
                    _uiState.value.falla?.let { falla ->
                        loadVotos(falla.idFalla)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isVoting = false,
                            voteError = result.message ?: "Error al eliminar voto"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    /**
     * Resetea el estado de éxito del voto.
     */
    fun resetVoteSuccess() {
        _uiState.update { it.copy(voteSuccess = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
