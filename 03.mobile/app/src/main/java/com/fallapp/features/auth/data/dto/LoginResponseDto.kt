package com.fallapp.features.auth.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO para los datos de respuesta de login/registro.
 * 
 * La API devuelve un wrapper ApiResponse con estos datos dentro.
 * 
 * Formato completo de la API:
 * ```json
 * {
 *   "exito": true,
 *   "mensaje": "Login exitoso",
 *   "datos": {
 *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *     "tipo": "Bearer",
 *     "expiraEn": 86400,
 *     "usuario": { ... }
 *   },
 *   "timestamp": "2026-02-01T18:30:00"
 * }
 * ```
 * 
 * @property token JWT token de autenticación
 * @property tipo Tipo de token (siempre "Bearer")
 * @property expiraEn Segundos hasta expiración (24h = 86400s)
 * @property usuario Datos del usuario autenticado
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEn: Int,
    val usuario: UsuarioDto
)

/**
 * DTO del usuario devuelto en login/registro.
 * 
 * @property idUsuario ID único del usuario
 * @property email Email de la cuenta
 * @property nombreCompleto Nombre completo del usuario
 * @property rol Rol del usuario (FALLERO, ADMIN, CASAL)
 * @property verificado Estado de verificación de email
 * @property idFalla ID de la falla asociada (null si no tiene)
 * @property nombreFalla Nombre de la falla asociada (null si no tiene)
 * @property ultimoAcceso Timestamp del último acceso (opcional)
 */
@Serializable
data class UsuarioDto(
    val idUsuario: Long,
    val email: String,
    val nombreCompleto: String,
    val rol: String,
    val verificado: Boolean = false,
    val idFalla: Long? = null,
    val nombreFalla: String? = null,
    val ultimoAcceso: String? = null
)
