package com.fallapp.features.fallas.data.repository

import com.fallapp.core.database.dao.FallaDao
import com.fallapp.core.network.NetworkMonitor
import com.fallapp.core.util.Result
import com.fallapp.features.fallas.data.mapper.toDomain
import com.fallapp.features.fallas.data.mapper.toEntity
import com.fallapp.features.fallas.data.remote.FallasApiService
import com.fallapp.features.fallas.domain.model.Categoria
import com.fallapp.features.fallas.domain.model.Falla
import com.fallapp.features.fallas.domain.repository.FallasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.math.*

/**
 * Implementación del repositorio de Fallas.
 * Coordina entre API (Ktor) y caché local (Room).
 * 
 * Estrategia:
 * 1. Intenta obtener desde API
 * 2. Guarda en caché local
 * 3. Si falla la API, devuelve datos cacheados
 */
class FallasRepositoryImpl(
    private val apiService: FallasApiService,
    private val fallaDao: FallaDao,
    private val networkMonitor: NetworkMonitor
) : FallasRepository {

    override fun getAllFallas(forceRefresh: Boolean): Flow<Result<List<Falla>>> = flow {
        emit(Result.Loading)

        try {
            // Verificar conexión (usar first() en lugar de collect para no bloquearse)
            val isConnected = networkMonitor.isConnected.first()
            
            if (isConnected && (forceRefresh || shouldRefresh())) {
                // Hay conexión, obtener desde API
                val dtos = apiService.getAllFallas()
                
                // Guarda en caché local
                val entities = dtos.map { it.toEntity() }
                fallaDao.insertAll(entities)
                
                // Devuelve datos de la API
                emit(Result.Success(dtos.map { it.toDomain() }))
            } else {
                // Sin conexión o no hace falta refresh: devuelve caché
                val cachedEntities = fallaDao.getAllFallasSync()
                if (cachedEntities.isEmpty() && !isConnected) {
                    emit(Result.Error(Exception("Sin conexión y no hay datos locales")))
                } else {
                    emit(Result.Success(cachedEntities.map { it.toDomain() }))
                }
            }
        } catch (e: Exception) {
            // Si falla la API, intenta devolver caché
            val cachedEntities = fallaDao.getAllFallasSync()
            if (cachedEntities.isNotEmpty()) {
                emit(Result.Success(cachedEntities.map { it.toDomain() }))
            } else {
                emit(Result.Error(e, e.message))
            }
        }
    }.catch { e ->
        emit(Result.Error(e as? Throwable ?: Exception(e.toString()), e.toString()))
    }

    override fun getFallaById(id: Long): Flow<Result<Falla?>> = flow {
        emit(Result.Loading)

        try {
            // Intenta obtener desde API primero
            val dto = apiService.getFallaById(id)
            
            if (dto != null) {
                // Actualiza caché
                fallaDao.insert(dto.toEntity())
                emit(Result.Success(dto.toDomain()))
            } else {
                // Si no está en API, busca en caché
                val cachedEntity = fallaDao.getFallaByIdSync(id)
                emit(Result.Success(cachedEntity?.toDomain()))
            }
        } catch (e: Exception) {
            // Si falla API, devuelve caché
            val cachedEntity = fallaDao.getFallaByIdSync(id)
            if (cachedEntity != null) {
                emit(Result.Success(cachedEntity.toDomain()))
            } else {
                emit(Result.Error(e, e.message))
            }
        }
    }.catch { e ->
        emit(Result.Error(e as? Throwable ?: Exception(e.toString()), e.toString()))
    }

    override fun searchFallas(query: String): Flow<Result<List<Falla>>> = flow {
        emit(Result.Loading)

        try {
            // Búsqueda local en caché (más rápida y funciona offline)
            val results = fallaDao.searchFallasSync(query)
            emit(Result.Success(results.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    override fun getFallasByCategoria(categoria: Categoria): Flow<Result<List<Falla>>> = flow {
        emit(Result.Loading)

        try {
            val results = fallaDao.getFallasByCategoriaSync(categoria.name)
            emit(Result.Success(results.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    override fun getNearbyFallas(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Flow<Result<List<Falla>>> = flow {
        emit(Result.Loading)

        try {
            val allFallas = fallaDao.getAllFallasSync()
            
            // Filtrar fallas cercanas usando fórmula de Haversine
            val nearbyFallas = allFallas.filter { entity ->
                if (entity.latitud != null && entity.longitud != null) {
                    val distance = calculateDistance(
                        latitude, longitude,
                        entity.latitud, entity.longitud
                    )
                    distance <= radiusKm
                } else {
                    false
                }
            }.map { it.toDomain() }
            
            emit(Result.Success(nearbyFallas))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    override fun getCachedFallas(): Flow<List<Falla>> {
        return fallaDao.getAllFallas().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Determina si debería hacer refresh desde la API.
     * Criterio: si han pasado más de 6 horas desde última sincronización.
     */
    private suspend fun shouldRefresh(): Boolean {
        val lastSync = fallaDao.getLastSyncTime()
        if (lastSync == null) return true
        
        val hoursSinceSync = java.time.Duration.between(lastSync, java.time.LocalDateTime.now()).toHours()
        return hoursSinceSync >= 6
    }

    /**
     * Calcula distancia entre dos puntos usando fórmula de Haversine.
     * @return distancia en kilómetros
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
