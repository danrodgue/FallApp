package com.fallapp.features.fallas.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiException
import com.fallapp.core.network.ApiResponse
import com.fallapp.core.util.TokenManager
import com.fallapp.features.fallas.data.remote.dto.VotoDto
import com.fallapp.features.fallas.data.remote.dto.VotoRequestDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*

/**
 * Servicio API para gestión de votos.
 *
 * Capa data/remote: sólo conoce DTOs y endpoints HTTP.
 * Requiere autenticación JWT para todas las operaciones.
 */
class VotosApiService(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) {

    private suspend fun HttpRequestBuilder.attachAuthHeader() {
        val token = tokenManager.getToken()
            ?: throw ApiException("No hay sesión activa. Inicia sesión para poder votar.")
        header(ApiConfig.AUTH_HEADER, "${ApiConfig.TOKEN_PREFIX}$token")
    }

    suspend fun crearVoto(request: VotoRequestDto): ApiResponse<VotoDto> {
        return client.post(ApiConfig.Endpoints.VOTOS) {
            contentType(ContentType.Application.Json)
            attachAuthHeader()
            setBody(request)
        }.body()
    }

    /**
     * Obtiene los votos del usuario actual a partir del token JWT.
     * No necesita idUsuario porque el backend lo infiere del token.
     */
    suspend fun getMisVotos(): ApiResponse<List<VotoDto>> {
        return client.get(ApiConfig.Endpoints.MIS_VOTOS) {
            attachAuthHeader()
        }.body()
    }

    suspend fun eliminarVoto(idVoto: Long): ApiResponse<Unit> {
        return client.delete("${ApiConfig.Endpoints.VOTOS}/$idVoto") {
            attachAuthHeader()
        }.body()
    }

    suspend fun getVotosFalla(idFalla: Long): ApiResponse<List<VotoDto>> {
        return client.get("${ApiConfig.Endpoints.VOTOS}/falla/$idFalla") {
            attachAuthHeader()
        }.body()
    }
}

