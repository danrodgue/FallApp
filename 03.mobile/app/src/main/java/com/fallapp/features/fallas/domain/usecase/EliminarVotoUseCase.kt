package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.repository.VotosRepository

/**
 * Use case para eliminar un voto.
 * 
 * Permite al usuario eliminar un voto que haya realizado previamente.
 * Solo el autor del voto puede eliminarlo.
 * 
 * @property repository Repositorio de votos
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class EliminarVotoUseCase(
    private val repository: VotosRepository
) {
    /**
     * Elimina el voto.
     * 
     * @param idVoto ID del voto a eliminar
     * @return Result con confirmación o error
     */
    suspend operator fun invoke(idVoto: Long): Result<Unit> {
        return repository.eliminarVoto(idVoto)
    }
}
