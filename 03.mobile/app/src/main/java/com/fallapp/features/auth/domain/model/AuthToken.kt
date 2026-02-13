package com.fallapp.features.auth.domain.model

/**
 * Modelo de dominio para el token de autenticación JWT.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
data class AuthToken(
    val token: String,
    val user: User
)
