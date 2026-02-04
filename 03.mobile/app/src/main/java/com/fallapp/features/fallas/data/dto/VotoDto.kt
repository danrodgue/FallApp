package com.fallapp.features.fallas.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO para un voto desde el backend.
 * 
 * Mapea directamente al JSON devuelto por la API.
 * 
 * @property idVoto ID único del voto
 * @property idUsuario ID del usuario que votó
 * @property nombreUsuario Nombre completo del usuario
 * @property idFalla ID de la falla votada
 * @property nombreFalla Nombre de la falla
 * @property tipoVoto Tipo de voto (INGENIOSO, CRITICO, ARTISTICO)
 * @property fechaCreacion Fecha y hora del voto en formato ISO-8601
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Serializable
data class VotoDto(
    val idVoto: Long,
    val idUsuario: Long,
    val nombreUsuario: String,
    val idFalla: Long,
    val nombreFalla: String,
    val tipoVoto: String, // "INGENIOSO", "CRITICO", "ARTISTICO"
    val fechaCreacion: String // ISO-8601 format
)

/**
 * DTO para crear un voto.
 * 
 * @property idNinot ID del ninot a votar (la API vota la falla asociada)
 * @property tipoVoto Tipo de voto ("INGENIOSO", "CRITICO", "ARTISTICO")
 */
@Serializable
data class VotoRequestDto(
    val idNinot: Long,
    val tipoVoto: String
)
