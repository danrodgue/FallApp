package com.fallapp.features.auth.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO para la petición de registro.
 * Enviado a POST /api/auth/registro.
 */
@Serializable
data class RegisterRequestDto(
    val email: String,
    val contrasena: String,
    val nombreCompleto: String,
    val idFalla: Long? = null
)

