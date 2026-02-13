package com.fallapp.features.auth.data.mapper

import com.fallapp.features.auth.data.remote.dto.UsuarioDto
import com.fallapp.features.auth.domain.model.User
import com.fallapp.features.auth.domain.model.UserRole

/**
 * Mappers para convertir entre DTOs y modelos de dominio.
 *
 * DTOs -> Domain:
 * - UsuarioDto.toDomain(): User
 */

/**
 * Convierte UsuarioDto a modelo de dominio User.
 *
 * Parsea el nombreCompleto en nombre y apellidos.
 * Ejemplo: "Juan Pérez García" → nombre="Juan", apellidos="Pérez García"
 */
fun UsuarioDto.toDomain(): User {
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

