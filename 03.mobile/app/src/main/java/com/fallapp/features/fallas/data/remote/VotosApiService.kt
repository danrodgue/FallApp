package com.fallapp.features.fallas.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiException
import com.fallapp.core.network.ApiResponse
import com.fallapp.core.network.PageResponse
import com.fallapp.core.util.TokenManager
import com.fallapp.features.fallas.data.remote.dto.VotoDto
import com.fallapp.features.fallas.data.remote.dto.VotoRequestDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.json.JsonElement

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

    /**
     * Crear voto.
     *
     * Aunque la guía documenta que `datos` devuelve un `VotoDTO` completo,
     * en la práctica el backend puede devolver estructuras diferentes o incluso null.
     * Por eso modelamos `datos` como `JsonElement` y dejamos que
     * la capa de repositorio simplemente use el flag `exito`/`mensaje`.
     */
    suspend fun crearVoto(request: VotoRequestDto): ApiResponse<JsonElement> =
        client.post(ApiConfig.Endpoints.VOTOS) {
            contentType(ContentType.Application.Json)
            attachAuthHeader()
            // request now includes `idFalla` (server expects idFalla)
            setBody(request)
        }.body()

    /**
     * Obtiene los votos del usuario actual a partir del token JWT.
     *
     * El backend devuelve ApiResponse<List<VotoDTO>> (no paginado),
     * así que aquí modelamos `datos` como una lista plana.
     */
    suspend fun getMisVotos(): ApiResponse<List<VotoDto>> =
        client.get(ApiConfig.Endpoints.MIS_VOTOS) {
            attachAuthHeader()
        }.body()

    suspend fun eliminarVoto(idVoto: Long): ApiResponse<Unit> =
        client.delete("${ApiConfig.Endpoints.VOTOS}/$idVoto") {
            attachAuthHeader()
        }.body()

    /**
     * Obtiene los votos asociados a una falla concreta.
     *
     * El backend devuelve ApiResponse<List<VotoDTO>> (no paginado).
     */
    suspend fun getVotosFalla(idFalla: Long): ApiResponse<List<VotoDto>> =
        client.get("${ApiConfig.API_PATH}/votos/falla/$idFalla") {
            attachAuthHeader()
        }.body()
}

