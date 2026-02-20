package com.fallapp.features.profile.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiResponse
import com.fallapp.core.util.TokenManager
import com.fallapp.features.profile.data.remote.dto.UsuarioPerfilDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.setBody
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UpdateProfileRequest(
    val nombreCompleto: String,
    val telefono: String?,
    val direccion: String?,
    val ciudad: String?,
    val codigoPostal: String?
)

// Servicio que llama a la API de perfil (GET/PUT usuarios, subir imagen)
class ProfileApiService(
    private val httpClient: HttpClient,
    private val tokenManager: TokenManager
) {

    private suspend fun authHeaderValue(): String? {
        val token = tokenManager.getValidToken() ?: return null
        return "${ApiConfig.TOKEN_PREFIX}$token"
    }

    suspend fun getUserProfile(userId: Long): ApiResponse<UsuarioPerfilDto> {
        return httpClient.get("${ApiConfig.API_URL}/usuarios/$userId") {
            authHeaderValue()?.let { header(ApiConfig.AUTH_HEADER, it) }
        }.body()
    }

    suspend fun updateUserProfile(userId: Long, request: UpdateProfileRequest): ApiResponse<UsuarioPerfilDto> {
        return httpClient.put("${ApiConfig.API_URL}/usuarios/$userId") {
            authHeaderValue()?.let { header(ApiConfig.AUTH_HEADER, it) }
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun uploadUserImage(
        userId: Long,
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String?
    ): ApiResponse<JsonElement> {
        val multipartBody = MultiPartFormDataContent(
            formData {
                append(
                    key = "imagen",
                    value = imageBytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, mimeType ?: "image/jpeg")
                    }
                )
            }
        )

        return httpClient.post("${ApiConfig.API_URL}/usuarios/$userId/imagen") {
            authHeaderValue()?.let { header(ApiConfig.AUTH_HEADER, it) }
            setBody(multipartBody)
        }.body()
    }
}






