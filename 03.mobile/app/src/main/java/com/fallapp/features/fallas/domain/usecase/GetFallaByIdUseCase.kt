package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.repository.FallasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Caso de uso para obtener una falla por su ID.
 * 
 * Reglas de negocio:
 * - Valida que el ID sea válido (mayor que 0)
 * - Devuelve error si no se encuentra la falla
 */
class GetFallaByIdUseCase(
    private val repository: FallasRepository
) {
    /**
     * Ejecuta el caso de uso.
     * 
     * @param id identificador de la falla
     * @return Flow que emite Result con la Falla o Error si no existe
     */
    operator fun invoke(id: Long): Flow<Result<Falla>> {
        if (id <= 0) {
            return kotlinx.coroutines.flow.flowOf(
                Result.Error(IllegalArgumentException("ID de falla inválido"))
            )
        }
        
        return repository.getFallaById(id).map { result ->
            when (result) {
                is Result.Success -> {
                    if (result.data != null) {
                        Result.Success(result.data)
                    } else {
                        Result.Error(Exception("Falla no encontrada"))
                    }
                }
                is Result.Error -> result
                is Result.Loading -> result
            }
        }
    }
}
