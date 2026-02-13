package com.fallapp.features.fallas.presentation.detail

import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.model.TipoVoto
import com.fallapp.features.fallas.domain.model.Voto

/**
 * Estado UI para la pantalla de detalle de falla con sistema de votación.
 * 
 * @property falla Falla actual mostrada
 * @property isLoading Si está cargando datos
 * @property errorMessage Mensaje de error si hay alguno
 * @property votos Lista de votos de la falla
 * @property votosUsuario Lista de votos del usuario actual en esta falla
 * @property isVoting Si está procesando un voto
 * @property voteSuccess Si el voto se realizó exitosamente
 * @property voteError Mensaje de error al votar
 * @property showVoteDialog Si mostrar el diálogo de votación
 * @property selectedTipoVoto Tipo de voto seleccionado en el diálogo
 */
data class FallaDetailUiState(
    val falla: Falla? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // Sistema de votos
    val votos: List<Voto> = emptyList(),
    val votosUsuario: List<Voto> = emptyList(),
    val isVoting: Boolean = false,
    val voteSuccess: Boolean = false,
    val voteError: String? = null,
    val showVoteDialog: Boolean = false,
    val selectedTipoVoto: TipoVoto? = null
) {
    /**
     * Verifica si el usuario ya votó con un tipo específico.
     */
    fun hasVotedWith(tipoVoto: TipoVoto): Boolean {
        return votosUsuario.any { it.tipoVoto == tipoVoto }
    }
    
    /**
     * Obtiene el voto del usuario para un tipo específico.
     */
    fun getVotoFor(tipoVoto: TipoVoto): Voto? {
        return votosUsuario.firstOrNull { it.tipoVoto == tipoVoto }
    }
    
    /**
     * Cuenta los votos por tipo.
     */
    fun countVotosBy(tipoVoto: TipoVoto): Int {
        return votos.count { it.tipoVoto == tipoVoto }
    }
}
