package com.fallapp.features.auth.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO para los datos de respuesta de login/registro.
 *
 * La API devuelve un wrapper ApiResponse con estos datos dentro.
 * Estos DTOs viven en data/remote/dto y se mapean a modelos
 * de dominio en la capa data/mapper.
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
 */
@Serializable
data class UsuarioDto(
    val idUsuario: Long,
    val email: String,
    val nombreCompleto: String,
    val rol: String,
    val verificado: Boolean = false,  // Estado de verificación de email
    val idFalla: Long? = null,
    val nombreFalla: String? = null,
    val ultimoAcceso: String? = null
)

