package com.fallapp.features.fallas.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO para la respuesta de la API GET /api/fallas
 * Mapea exactamente la estructura JSON del backend.
 * 
 * Actualizado 2026-02-03 para coincidir con la respuesta real de la API.
 */
@Serializable
data class FallaDto(
    val idFalla: Long,
    val nombre: String,
    val seccion: String,
    val fallera: String? = null,
    val presidente: String? = null,
    val artista: String? = null,
    val lema: String? = null,
    val anyoFundacion: Int? = null,
    val distintivo: String? = null,
    val urlBoceto: String? = null,
    val experim: Boolean? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val descripcion: String? = null,
    val webOficial: String? = null,
    val telefonoContacto: String? = null,
    val emailContacto: String? = null,
    val categoria: String? = null,
    val totalEventos: Int = 0,
    val totalNinots: Int = 0,
    val totalMiembros: Int = 0,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null
)
