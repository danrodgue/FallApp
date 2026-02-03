package com.fallapp.features.auth.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO para la petición de registro.
 * Enviado a POST /api/auth/registro
 * 
 * Formato API:
 * ```json
 * {
 *   "email": "usuario@example.com",
 *   "contrasena": "miPassword123",
 *   "nombreCompleto": "Juan Pérez García",
 *   "idFalla": 1
 * }
 * ```
 * 
 * @property email Email del usuario (debe ser único)
 * @property contrasena Contraseña (mínimo 6 caracteres)
 * @property nombreCompleto Nombre completo del usuario (3-200 caracteres)
 * @property idFalla ID de la falla a la que pertenece (opcional)
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Serializable
data class RegisterRequestDto(
    val email: String,
    val contrasena: String,
    val nombreCompleto: String,
    val idFalla: Long? = null
)
