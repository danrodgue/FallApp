package com.fallapp.features.profile.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiResponse
import com.fallapp.features.profile.data.remote.dto.UsuarioPerfilDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val nombreCompleto: String,
    val telefono: String?,
    val direccion: String?,
    val ciudad: String?,
    val codigoPostal: String?
)

/**
 * Servicio API para obtener información de perfil de usuario.
 *
 * Realiza peticiones HTTP a los endpoints de perfil según
 * la documentación de API REST.
 *
 * Endpoints:
 * - GET /api/usuarios/{id} - Obtener datos completos del perfil del usuario
 *
 * La capa data/remote expone solo DTOs y respuestas crudas de la API.
 * El mapeo a modelos de dominio se realiza en los mappers de data/mapper.
 */
class ProfileApiService(
    private val httpClient: HttpClient
) {

    /**
     * GET /api/usuarios/{id}
     *
     * Obtiene toda la información del perfil del usuario autenticado.
     *
     * @param userId ID del usuario a recuperar
     * @return ApiResponse con UsuarioPerfilDto (datos completos del usuario)
     * @throws Exception si la petición falla, el usuario no existe o la red no está disponible
     */
    suspend fun getUserProfile(userId: Long): ApiResponse<UsuarioPerfilDto> {
        return httpClient.get("${ApiConfig.API_URL}/usuarios/$userId").body()
    }

    /**
     * PUT /api/usuarios/{id}
     *
     * Actualiza la información del perfil del usuario.
     *
     * @param userId ID del usuario a actualizar
     * @param request Datos a actualizar
     * @return ApiResponse con UsuarioPerfilDto actualizado
     * @throws Exception si la petición falla o la red no está disponible
     */
    suspend fun updateUserProfile(userId: Long, request: UpdateProfileRequest): ApiResponse<UsuarioPerfilDto> {
        return httpClient.put("${ApiConfig.API_URL}/usuarios/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

