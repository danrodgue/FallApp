package com.fallapp.features.fallas.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO para un voto desde el backend.
 */
@Serializable
data class VotoDto(
    val idVoto: Long,
    val idUsuario: Long,
    val nombreUsuario: String,
    val idFalla: Long,
    val nombreFalla: String,
    val tipoVoto: String,
    val fechaCreacion: String
)

/**
 * DTO para crear un voto.
 */
@Serializable
data class VotoRequestDto(
    val idFalla: Long,
    val tipoVoto: String
)

