package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.repository.FallasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Caso de uso para buscar fallas por nombre.
 * 
 * Reglas de negocio:
 * - Requiere mínimo 2 caracteres para buscar
 * - Devuelve lista vacía si la query es muy corta
 */
class SearchFallasUseCase(
    private val repository: FallasRepository
) {
    /**
     * Ejecuta el caso de uso.
     * 
     * @param query texto de búsqueda
     * @return Flow que emite Result con lista de Fallas coincidentes
     */
    operator fun invoke(query: String): Flow<Result<List<Falla>>> {
        val trimmedQuery = query.trim()
        
        if (trimmedQuery.length < 2) {
            return flowOf(Result.Success(emptyList()))
        }
        
        return repository.searchFallas(trimmedQuery)
    }
}
