package com.fallapp.features.votos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.model.FallaRanking
import com.fallapp.features.fallas.domain.model.TipoVoto
import com.fallapp.features.fallas.domain.model.Voto
import com.fallapp.features.fallas.domain.model.VotoRequest
import com.fallapp.features.fallas.domain.usecase.GetFallasUseCase
import com.fallapp.features.fallas.domain.usecase.GetVotosFallaUseCase
import com.fallapp.features.fallas.domain.usecase.GetMisVotosUseCase
import com.fallapp.features.fallas.domain.usecase.VotarFallaUseCase
import com.fallapp.features.fallas.domain.usecase.EliminarVotoUseCase
import com.fallapp.features.fallas.domain.usecase.CrearComentarioUseCase
import com.fallapp.features.fallas.domain.usecase.GetRankingUseCase
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
    private val getMisVotosUseCase: GetMisVotosUseCase,
    private val eliminarVotoUseCase: EliminarVotoUseCase,
    private val getVotosFallaUseCase: GetVotosFallaUseCase,
    private val getRankingUseCase: GetRankingUseCase,
    private val crearComentarioUseCase: CrearComentarioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VotosUiState())
    val uiState: StateFlow<VotosUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refreshData() {
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

            // Forzamos refresh para asegurarnos de tener todas las fallas (≈260)
            getFallasUseCase(forceRefresh = true).collect { result ->
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
     */
    private fun loadMisVotos() {
        viewModelScope.launch {
            when (val result = getMisVotosUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(misVotos = result.data)
                    }
                    _uiState.update { state ->
                        state.copy(
                            fallasParaVotar = buildDeck(state.fallas, result.data)
                        )
                    }
                }
                is Result.Error -> {
                    val msg = result.message ?: ""
                    val userFriendly = when {
                        msg.contains("no está respondiendo correctamente", ignoreCase = true) ||
                        msg.contains("en mantenimiento", ignoreCase = true) ->
                            "El servidor está en mantenimiento. Intenta en unos minutos."
                        msg.contains("Request method 'GET' is not supported", ignoreCase = true) ->
                            "El servidor está en mantenimiento. Intenta en unos minutos."
                        msg.contains("nn not found", ignoreCase = true) ->
                            "Error al sincronizar tus votos. Por favor recarga la pantalla."
                        msg.contains("Sesión expirada", ignoreCase = true) ->
                            "Tu sesión ha expirado. Por favor inicia sesión de nuevo."
                        else -> msg.ifBlank { "Error al cargar tus votos" }
                    }
                    _uiState.update {
                        it.copy(errorMessage = userFriendly)
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

            // Mapear filtro actual a valor de API
            val tipoApi: String? = when (_uiState.value.rankingFilter) {
                null -> null
                TipoVoto.INGENIOSO -> "MONUMENTO"          // Mejor falla
                TipoVoto.CRITICO -> "INGENIO_Y_GRACIA"
                TipoVoto.ARTISTICO -> "EXPERIMENTAL"
            }

            when (val result = getRankingUseCase(tipoVotoApi = tipoApi, limite = 10)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            ranking = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message ?: "Error al cargar ranking",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // No-op; ya hemos marcado isLoading arriba
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

            val request = VotoRequest(
                idFalla = falla.idFalla,
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
                    val raw = result.message ?: ""
                    val userFriendly = when {
                        raw.contains("JDBC exception executing", ignoreCase = true) ||
                        raw.contains("operator does not exist: tipo_voto", ignoreCase = true) ->
                            "No se ha podido registrar tu voto por un problema en el servidor. Inténtalo más tarde."
                        raw.contains("nn not found", ignoreCase = true) ->
                            "Error al procesar tu voto. Por favor intenta de nuevo."
                        raw.contains("Sesión expirada", ignoreCase = true) ->
                            "Tu sesión ha expirado. Por favor inicia sesión de nuevo."
                        else -> raw.ifBlank { "Error al votar" }
                    }
                    _uiState.update {
                        it.copy(
                            errorMessage = userFriendly,
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
     * Envía un comentario para una falla.
     * El backend analiza el sentimiento con IA automáticamente.
     */
    fun enviarComentario(falla: Falla, contenido: String) {
        viewModelScope.launch {
            when (val result = crearComentarioUseCase(falla.idFalla, contenido)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "¡Comentario enviado! Se analizará con IA.")
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.message ?: "Error al enviar comentario")
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
    val ranking: List<FallaRanking> = emptyList(),
    val rankingFilter: TipoVoto? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
