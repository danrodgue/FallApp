package com.fallapp.features.votos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.model.TipoVoto
import com.fallapp.features.fallas.domain.model.Voto
import com.fallapp.features.fallas.domain.model.VotoRequest
import com.fallapp.features.fallas.domain.usecase.GetFallasUseCase
import com.fallapp.features.fallas.domain.usecase.GetVotosFallaUseCase
import com.fallapp.features.fallas.domain.usecase.GetVotosUsuarioUseCase
import com.fallapp.features.fallas.domain.usecase.VotarFallaUseCase
import com.fallapp.features.fallas.domain.usecase.EliminarVotoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Votos.
 */
class VotosViewModel(
    private val getFallasUseCase: GetFallasUseCase,
    private val votarFallaUseCase: VotarFallaUseCase,
    private val getVotosUsuarioUseCase: GetVotosUsuarioUseCase,
    private val eliminarVotoUseCase: EliminarVotoUseCase,
    private val getVotosFallaUseCase: GetVotosFallaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VotosUiState())
    val uiState: StateFlow<VotosUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Carga todos los datos: fallas, mis votos, ranking
     */
    private fun loadData() {
        loadFallas()
        loadMisVotos()
        loadRanking()
    }

    /**
     * Carga la lista de fallas para votar.
     */
    private fun loadFallas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getFallasUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                fallas = result.data,
                                fallasParaVotar = buildDeck(result.data, it.misVotos),
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.message ?: "Error al cargar fallas",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    /**
     * Carga los votos del usuario actual.
     * TODO: Obtener ID del usuario desde TokenManager
     */
    private fun loadMisVotos() {
        viewModelScope.launch {
            // El backend obtiene el usuario a partir del token, por lo que el idUsuario es irrelevante.
            val idUsuarioDummy = 0L

            when (val result = getVotosUsuarioUseCase(idUsuarioDummy)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(misVotos = result.data)
                    }
                    // Recalcular mazo de fallas a partir de los votos actuales
                    _uiState.update { state ->
                        state.copy(
                            fallasParaVotar = buildDeck(state.fallas, result.data)
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.message ?: "Error al cargar tus votos")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Carga el ranking de fallas más votadas.
     */
    private fun loadRanking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getFallasUseCase().collect { fallasResult ->
                when (fallasResult) {
                    is Result.Success -> {
                        // Obtener votos para cada falla
                        val fallasConVotos = mutableListOf<Pair<Falla, Int>>()

                        for (falla in fallasResult.data) {
                            when (val votosResult = getVotosFallaUseCase(falla.idFalla)) {
                                is Result.Success -> {
                                    val votos = votosResult.data
                                    val count = if (_uiState.value.rankingFilter != null) {
                                        votos.count { it.tipoVoto == _uiState.value.rankingFilter }
                                    } else {
                                        votos.size
                                    }
                                    if (count > 0) {
                                        fallasConVotos.add(falla to count)
                                    }
                                }
                                else -> {}
                            }
                        }

                        // Ordenar por votos descendente
                        val ranking = fallasConVotos.sortedByDescending { it.second }

                        _uiState.update {
                            it.copy(
                                ranking = ranking,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = fallasResult.message ?: "Error al cargar ranking",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    /**
     * Vota por una falla.
     */
    fun votar(falla: Falla, tipoVoto: TipoVoto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Usar el ID de la falla como idNinot temporal
            // TODO: Obtener el ID del ninot real cuando esté disponible
            val idNinot = falla.idFalla

            val request = VotoRequest(
                idNinot = idNinot,
                tipoVoto = tipoVoto
            )

            when (val result = votarFallaUseCase(request)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            successMessage = "¡Voto registrado!",
                            isLoading = false
                        )
                    }
                    // Recargar datos
                    loadMisVotos()
                    loadRanking()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message ?: "Error al votar",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Elimina un voto.
     */
    fun eliminarVoto(idVoto: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = eliminarVotoUseCase(idVoto)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            successMessage = "Voto eliminado",
                            isLoading = false
                        )
                    }
                    // Recargar datos
                    loadMisVotos()
                    loadRanking()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message ?: "Error al eliminar voto",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Cambia el filtro de ranking por tipo de voto.
     */
    fun setRankingFilter(tipoVoto: TipoVoto?) {
        _uiState.update { it.copy(rankingFilter = tipoVoto) }
        loadRanking()
    }

    /**
     * Limpia los mensajes de éxito/error.
     */
    fun clearMessages() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }

    /**
     * Construye el mazo de fallas para votar:
     * - Excluye las ya votadas por el usuario
     * - Baraja el orden para mostrar una secuencia aleatoria
     */
    private fun buildDeck(fallas: List<Falla>, misVotos: List<Voto>): List<Falla> {
        val votedIds = misVotos.map { it.idFalla }.toSet()
        return fallas
            .filter { it.idFalla !in votedIds }
            .shuffled()
    }
}

/**
 * Estado UI para la pantalla de Votos.
 */
data class VotosUiState(
    val fallas: List<Falla> = emptyList(),
    val fallasParaVotar: List<Falla> = emptyList(),
    val misVotos: List<Voto> = emptyList(),
    val ranking: List<Pair<Falla, Int>> = emptyList(),
    val rankingFilter: TipoVoto? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
