package com.fallapp.features.fallas.data.api

import com.fallapp.core.network.ApiResponse
import com.fallapp.core.network.PaginatedResponse
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
     * Obtiene todas las fallas desde la API con paginación.
     * GET /api/fallas?pagina=0&tamano=100
     * 
     * @param pagina número de página (0-indexed)
     * @param tamano elementos por página
     * @return Lista de FallaDto del contenido paginado
     * @throws Exception si hay error de red o parseo
     */
    suspend fun getAllFallas(pagina: Int = 0, tamano: Int = 100): List<FallaDto> {
        val response: ApiResponse<PaginatedResponse<FallaDto>> = httpClient.get(BASE_PATH) {
            parameter("pagina", pagina)
            parameter("tamano", tamano)
        }.body()
        
        // Extraer el contenido de la respuesta paginada
        return response.datos?.contenido ?: emptyList()
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
            val response: ApiResponse<FallaDto> = httpClient.get("$BASE_PATH/$id").body()
            response.datos
        } catch (e: Exception) {
            null
        }
    }
}
