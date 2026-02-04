package com.fallapp.features.auth.data.dto

import com.fallapp.features.auth.domain.model.User
import com.fallapp.features.auth.domain.model.UserRole

/**
 * Mappers para convertir entre DTOs y modelos de dominio.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */

/**
 * Convierte UsuarioDto a modelo de dominio User.
 * 
 * Parsea el nombreCompleto en nombre y apellidos.
 * Ejemplo: "Juan Pérez García" → nombre="Juan", apellidos="Pérez García"
 */
fun UsuarioDto.toDomain(): User {
    // Parsear nombreCompleto en nombre y apellidos
    val parts = nombreCompleto.split(" ", limit = 2)
    val nombre = parts.getOrNull(0) ?: ""
    val apellidos = parts.getOrNull(1) ?: ""
    
    return User(
        idUsuario = idUsuario,
        email = email,
        nombre = nombre,
        apellidos = apellidos,
        rol = UserRole.fromString(rol),
        idFalla = idFalla
    )
}
