package com.fallapp.features.fallas.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiException
import com.fallapp.core.network.ApiResponse
import com.fallapp.core.util.TokenManager
import com.fallapp.features.fallas.data.remote.dto.ComentarioRequestDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonElement

/**
 * Servicio API para comentarios en fallas.
 * Requiere autenticación JWT.
 * Tras crear, el backend analiza sentimiento con IA (HuggingFace).
 */
class ComentariosApiService(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) {

    private suspend fun HttpRequestBuilder.attachAuthHeader() {
        val token = tokenManager.getToken()
            ?: throw ApiException("Inicia sesión para poder comentar.")
        header(ApiConfig.AUTH_HEADER, "${ApiConfig.TOKEN_PREFIX}$token")
    }

    /**
     * POST /api/comentarios - Crear comentario en una falla.
     * El backend analiza automáticamente el sentimiento con IA.
     */
    suspend fun crearComentario(request: ComentarioRequestDto): ApiResponse<JsonElement> =
        client.post(ApiConfig.Endpoints.COMENTARIOS) {
            contentType(ContentType.Application.Json)
            attachAuthHeader()
            setBody(request)
        }.body()
}
