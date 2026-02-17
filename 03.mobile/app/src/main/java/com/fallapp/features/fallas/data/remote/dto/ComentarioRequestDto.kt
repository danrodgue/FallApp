package com.fallapp.features.fallas.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO para crear un comentario.
 * El backend espera: idUsuario, idFalla, contenido (3-500 caracteres).
 */
@Serializable
data class ComentarioRequestDto(
    val idUsuario: Long,
    val idFalla: Long,
    val contenido: String
)
