package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Voto
import com.fallapp.features.fallas.domain.model.VotoRequest
import com.fallapp.features.fallas.domain.repository.VotosRepository

/**
 * Use case para votar por una falla.
 * 
 * Registra un voto de un tipo específico (INGENIOSO, CRITICO, ARTISTICO) para una falla.
 * Un usuario solo puede votar 1 vez por tipo por falla.
 * 
 * @property repository Repositorio de votos
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class VotarFallaUseCase(
    private val repository: VotosRepository
) {
    /**
     * Ejecuta el voto.
     * 
     * @param request Datos del voto (idNinot, tipoVoto)
     * @return Result con el voto creado o error
     */
    suspend operator fun invoke(request: VotoRequest): Result<Voto> {
        return repository.crearVoto(request)
    }
}
