package com.fallapp.features.profile.data.mapper

import com.fallapp.features.profile.data.remote.dto.UsuarioPerfilDto
import com.fallapp.features.profile.domain.model.UsuarioPerfil

/**
 * Mappers para convertir entre DTOs de API y modelos de dominio.
 *
 * Responsabilidades:
 * - Convertir DTO → Domain (de API)
 * - Filtrar datos sensibles según especificación
 */

/**
 * Convierte DTO de API a modelo de dominio.
 *
 * Filtración de datos:
 * - NO incluye: idFalla, nombreFalla, activo, fechaActualizacion, idUsuario
 * - Campos mostrados: email, nombreCompleto, rol, teléfono, dirección, ciudad, código postal, fechaCreacion
 *
 * @return UsuarioPerfil con solo campos relevantes para UI
 */
fun UsuarioPerfilDto.toDomain(): UsuarioPerfil {
    return UsuarioPerfil(
        idUsuario = this.idUsuario,
        email = this.email,
        nombreCompleto = this.nombreCompleto,
        rol = this.rol,
        telefono = this.telefono,
        direccion = this.direccion,
        ciudad = this.ciudad,
        codigoPostal = this.codigoPostal,
        fechaCreacion = this.fechaCreacion
    )
}

