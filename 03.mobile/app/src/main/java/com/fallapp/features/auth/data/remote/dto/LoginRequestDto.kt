package com.fallapp.features.auth.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO para la petición de login.
 * Enviado a POST /api/auth/login
 *
 * Forma parte de la capa data/remote/dto y nunca se usa
 * directamente en domain o presentation.
 */
@Serializable
data class LoginRequestDto(
    val email: String,
    val contrasena: String
)

