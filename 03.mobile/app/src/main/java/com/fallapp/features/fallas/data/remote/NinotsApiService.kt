package com.fallapp.features.fallas.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiResponse
import com.fallapp.core.network.PageResponse
import com.fallapp.features.fallas.data.remote.dto.NinotDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Servicio API para operaciones con ninots, usado por la lógica de votos.
 */
class NinotsApiService(
    private val client: HttpClient
) {
    /**
     * Obtiene la lista de ninots asociados a una falla.
     * Utiliza el endpoint documentado: GET /api/ninots/falla/{idFalla}
     * 
     * En el backend actual, la respuesta va envuelta en:
     * ApiResponse<PageResponse<NinotDto>>
     * donde `datos` tiene la forma:
     * {
     *   "content": [ ... ],
     *   "empty": true,
     *   ...
     * }
     */
    suspend fun getNinotsByFalla(idFalla: Long): List<NinotDto> {
        val response: ApiResponse<PageResponse<NinotDto>> =
            client.get(ApiConfig.Endpoints.ninotsByFalla(idFalla)).body()

        val page = response.datos
        return page?.content ?: emptyList()
    }
}

