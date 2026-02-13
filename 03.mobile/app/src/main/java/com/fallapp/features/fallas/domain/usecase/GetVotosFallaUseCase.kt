package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Voto
import com.fallapp.features.fallas.domain.repository.VotosRepository

/**
 * Use case para obtener los votos de una falla.
 * 
 * Recupera todos los votos realizados para una falla específica.
 * 
 * @property repository Repositorio de votos
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class GetVotosFallaUseCase(
    private val repository: VotosRepository
) {
    /**
     * Obtiene los votos de la falla.
     * 
     * @param idFalla ID de la falla
     * @return Result con lista de votos o error
     */
    suspend operator fun invoke(idFalla: Long): Result<List<Voto>> {
        return repository.getVotosFalla(idFalla)
    }
}
