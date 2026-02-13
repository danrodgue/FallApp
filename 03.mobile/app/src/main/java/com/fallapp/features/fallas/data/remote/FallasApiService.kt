package com.fallapp.features.fallas.data.remote

import com.fallapp.core.network.ApiResponse
import com.fallapp.core.network.PaginatedResponse
import com.fallapp.features.fallas.data.remote.dto.FallaDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

/**
 * Servicio API para operaciones con Fallas.
 * Endpoints: GET /api/fallas, GET /api/fallas/{id}
 *
 * Forma parte de la capa data/remote.
 */
class FallasApiService(
    private val httpClient: HttpClient
) {
    companion object {
        private const val BASE_PATH = "/api/fallas"
    }

    /**
     * Obtiene todas las fallas desde la API con paginación.
     * GET /api/fallas?pagina=0&tamano=100
     */
    suspend fun getAllFallas(pagina: Int = 0, tamano: Int = 100): List<FallaDto> {
        val response: ApiResponse<PaginatedResponse<FallaDto>> = httpClient.get(BASE_PATH) {
            parameter("pagina", pagina)
            parameter("tamano", tamano)
        }.body()

        return response.datos?.contenido ?: emptyList()
    }

    /**
     * Obtiene una falla por su ID.
     * GET /api/fallas/{id}
     */
    suspend fun getFallaById(id: Long): FallaDto? {
        return try {
            val response: ApiResponse<FallaDto> = httpClient.get("$BASE_PATH/$id").body()
            response.datos
        } catch (e: Exception) {
            null
        }
    }
}

