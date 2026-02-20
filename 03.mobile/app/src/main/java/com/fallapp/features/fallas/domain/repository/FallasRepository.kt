package com.fallapp.features.fallas.domain.repository

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Categoria
import com.fallapp.features.fallas.domain.model.Falla
import kotlinx.coroutines.flow.Flow

// Contrato del repositorio de fallas (la capa de datos lo implementa)
interface FallasRepository {
    
    fun getAllFallas(forceRefresh: Boolean = false): Flow<Result<List<Falla>>>
    
    fun getFallaById(id: Long): Flow<Result<Falla?>>
    
    fun searchFallas(query: String): Flow<Result<List<Falla>>>
    
    fun getFallasByCategoria(categoria: Categoria): Flow<Result<List<Falla>>>
    
    fun getNearbyFallas(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Flow<Result<List<Falla>>>
    
    fun getCachedFallas(): Flow<List<Falla>>
}
