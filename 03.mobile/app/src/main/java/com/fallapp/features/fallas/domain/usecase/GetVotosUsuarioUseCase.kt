package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Voto
import com.fallapp.features.fallas.domain.repository.VotosRepository

/**
 * Use case para obtener los votos de un usuario.
 * 
 * Recupera todos los votos realizados por un usuario específico.
 * 
 * @property repository Repositorio de votos
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class GetVotosUsuarioUseCase(
    private val repository: VotosRepository
) {
    /**
     * Obtiene los votos del usuario.
     * 
     * @param idUsuario ID del usuario
     * @return Result con lista de votos o error
     */
    suspend operator fun invoke(idUsuario: Long): Result<List<Voto>> {
        return repository.getVotosUsuario(idUsuario)
    }
}
