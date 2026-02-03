package com.fallapp.features.fallas.data.api

import com.fallapp.features.fallas.data.dto.FallaDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

/**
 * Servicio API para operaciones con Fallas.
 * Endpoints: GET /api/fallas, GET /api/fallas/{id}
 */
class FallasApiService(
    private val httpClient: HttpClient
) {
    companion object {
        private const val BASE_PATH = "/api/fallas"
    }

    /**
     * Obtiene todas las fallas desde la API.
     * GET /api/fallas
     * 
     * @return Lista de FallaDto
     * @throws Exception si hay error de red o parseo
     */
    suspend fun getAllFallas(): List<FallaDto> {
        return httpClient.get(BASE_PATH).body()
    }

    /**
     * Obtiene una falla por su ID.
     * GET /api/fallas/{id}
     * 
     * @param id identificador de la falla
     * @return FallaDto o null si no existe
     * @throws Exception si hay error de red
     */
    suspend fun getFallaById(id: Long): FallaDto? {
        return try {
            httpClient.get("$BASE_PATH/$id").body()
        } catch (e: Exception) {
            null
        }
    }
}
