package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.repository.FallasRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para obtener todas las fallas.
 * 
 * Reglas de negocio:
 * - Intenta obtener desde API primero
 * - Si falla, devuelve datos cacheados
 * - Permite forzar refresh desde API
 */
class GetFallasUseCase(
    private val repository: FallasRepository
) {
    /**
     * Ejecuta el caso de uso.
     * 
     * @param forceRefresh si es true, ignora caché y obtiene desde API
     * @return Flow que emite Result con lista de Fallas
     */
    operator fun invoke(forceRefresh: Boolean = false): Flow<Result<List<Falla>>> {
        return repository.getAllFallas(forceRefresh)
    }
}
