package com.fallapp.features.eventos.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiResponse
import com.fallapp.core.network.PageResponse
import com.fallapp.features.eventos.data.remote.dto.EventoDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Servicio remoto para consumir los endpoints de eventos.
 *
 * No requiere autenticación.
 */
class EventosApiService(
    private val client: HttpClient
) {

    /**
     * Próximos N eventos (lista plana).
     */
    suspend fun getProximosEventos(limit: Int): ApiResponse<List<EventoDto>> {
        return client.get(ApiConfig.Endpoints.EVENTOS_PROXIMOS) {
            parameter("limite", limit)
        }.body()
    }

    /**
     * Eventos de una falla (paginado estilo Spring: datos -> PageResponse).
     */
    suspend fun getEventosByFalla(fallaId: Long): ApiResponse<PageResponse<EventoDto>> {
        return client.get(ApiConfig.Endpoints.eventosByFalla(fallaId)).body()
    }
}

