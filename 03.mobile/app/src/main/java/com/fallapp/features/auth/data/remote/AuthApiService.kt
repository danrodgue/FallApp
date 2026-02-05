package com.fallapp.features.auth.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiResponse
import com.fallapp.features.auth.data.remote.dto.LoginRequestDto
import com.fallapp.features.auth.data.remote.dto.LoginResponseDto
import com.fallapp.features.auth.data.remote.dto.RegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Servicio API para autenticación.
 *
 * Realiza las peticiones HTTP a los endpoints de auth según
 * la documentación en GUIA.API.FRONTEND.md.
 *
 * Endpoints:
 * - POST /api/auth/login - Iniciar sesión
 * - POST /api/auth/registro - Crear cuenta
 *
 * La capa data/remote expone sólo DTOs y respuestas crudas de la API.
 * El mapeo a modelos de dominio se realiza en los mappers de data/mapper.
 */
class AuthApiService(
    private val httpClient: HttpClient
) {

    /**
     * POST /api/auth/login
     *
     * Inicia sesión con email y contraseña.
     *
     * @param email Email del usuario
     * @param password Contraseña (se envía como "contrasena" a la API)
     * @return ApiResponse con LoginResponseDto (token + usuario)
     * @throws Exception si la petición falla o la red no está disponible
     */
    suspend fun login(email: String, password: String): ApiResponse<LoginResponseDto> {
        return httpClient.post("${ApiConfig.API_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, password))
        }.body()
    }

    /**
     * POST /api/auth/registro
     *
     * Crea una nueva cuenta de usuario.
     *
     * @param email Email del usuario (debe ser único)
     * @param password Contraseña (mínimo 6 caracteres)
     * @param nombreCompleto Nombre completo del usuario (3-200 caracteres)
     * @param idFalla ID de la falla asociada (opcional)
     * @return ApiResponse con LoginResponseDto (token + usuario creado)
     * @throws Exception si la petición falla o el email ya existe
     */
    suspend fun register(
        email: String,
        password: String,
        nombreCompleto: String,
        idFalla: Long? = null
    ): ApiResponse<LoginResponseDto> {
        return httpClient.post("${ApiConfig.API_URL}/auth/registro") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email, password, nombreCompleto, idFalla))
        }.body()
    }

    /**
     * POST /api/auth/logout
     *
     * El logout es principalmente local (eliminar token del storage).
     * El backend no requiere notificación de logout ya que JWT es stateless.
     */
    suspend fun logout() {
        // Logout es local: eliminar token de TokenManager
        // No se requiere llamada al backend
    }
}

