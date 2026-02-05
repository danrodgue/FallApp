package com.fallapp.features.fallas.data.remote

import com.fallapp.core.network.ApiResponse
import com.fallapp.features.fallas.data.remote.dto.VotoDto
import com.fallapp.features.fallas.data.remote.dto.VotoRequestDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Servicio API para gestión de votos.
 *
 * Capa data/remote: sólo conoce DTOs y endpoints HTTP.
 */
class VotosApiService(
    private val client: HttpClient
) {

    suspend fun crearVoto(request: VotoRequestDto): ApiResponse<VotoDto> {
        return client.post("/api/votos") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getVotosUsuario(idUsuario: Long): ApiResponse<List<VotoDto>> {
        return client.get("/api/votos/usuario/$idUsuario").body()
    }

    suspend fun eliminarVoto(idVoto: Long): ApiResponse<Unit> {
        return client.delete("/api/votos/$idVoto").body()
    }

    suspend fun getVotosFalla(idFalla: Long): ApiResponse<List<VotoDto>> {
        return client.get("/api/votos/falla/$idFalla").body()
    }
}

