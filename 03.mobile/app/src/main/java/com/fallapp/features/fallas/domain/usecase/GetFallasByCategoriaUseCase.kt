package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Categoria
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.repository.FallasRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para filtrar fallas por categoría.
 * 
 * Útil para mostrar clasificaciones (Especial, Primera, Segunda, etc.)
 */
class GetFallasByCategoriaUseCase(
    private val repository: FallasRepository
) {
    /**
     * Ejecuta el caso de uso.
     * 
     * @param categoria categoría para filtrar
     * @return Flow que emite Result con lista de Fallas de esa categoría
     */
    operator fun invoke(categoria: Categoria): Flow<Result<List<Falla>>> {
        return repository.getFallasByCategoria(categoria)
    }
}
