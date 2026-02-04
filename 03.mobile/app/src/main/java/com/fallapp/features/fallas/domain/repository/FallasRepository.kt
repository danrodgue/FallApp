package com.fallapp.features.fallas.domain.repository

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Categoria
import com.fallapp.features.fallas.domain.model.Falla
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de dominio para gestionar operaciones con Fallas.
 * Define el contrato que la capa de datos debe implementar.
 */
interface FallasRepository {
    
    /**
     * Obtiene todas las fallas.
     * Intenta obtener desde la API y guarda en caché local.
     * Si no hay conexión, devuelve datos cacheados.
     * 
     * @param forceRefresh fuerza la actualización desde la API ignorando caché
     * @return Flow que emite Result con lista de Fallas
     */
    fun getAllFallas(forceRefresh: Boolean = false): Flow<Result<List<Falla>>>
    
    /**
     * Obtiene una falla por su ID.
     * 
     * @param id identificador de la falla
     * @return Flow que emite Result con la Falla o null si no existe
     */
    fun getFallaById(id: Long): Flow<Result<Falla?>>
    
    /**
     * Busca fallas por nombre.
     * 
     * @param query texto de búsqueda
     * @return Flow que emite Result con lista de Fallas que coinciden
     */
    fun searchFallas(query: String): Flow<Result<List<Falla>>>
    
    /**
     * Filtra fallas por categoría.
     * 
     * @param categoria categoría para filtrar
     * @return Flow que emite Result con lista de Fallas de esa categoría
     */
    fun getFallasByCategoria(categoria: Categoria): Flow<Result<List<Falla>>>
    
    /**
     * Obtiene fallas cercanas a una ubicación.
     * 
     * @param latitude latitud de referencia
     * @param longitude longitud de referencia
     * @param radiusKm radio de búsqueda en kilómetros
     * @return Flow que emite Result con lista de Fallas cercanas
     */
    fun getNearbyFallas(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Flow<Result<List<Falla>>>
    
    /**
     * Obtiene fallas desde la caché local (Room).
     * No hace llamadas a la API.
     * 
     * @return Flow que emite lista de Fallas cacheadas
     */
    fun getCachedFallas(): Flow<List<Falla>>
}
