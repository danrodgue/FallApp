package com.fallapp.features.auth.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO para la petición de login.
 * Enviado a POST /api/auth/login
 * 
 * Formato API:
 * ```json
 * {
 *   "email": "usuario@example.com",
 *   "contrasena": "miPassword123"
 * }
 * ```
 * 
 * @property email Email del usuario
 * @property contrasena Contraseña (nombre del campo según API)
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Serializable
data class LoginRequestDto(
    val email: String,
    val contrasena: String
)
