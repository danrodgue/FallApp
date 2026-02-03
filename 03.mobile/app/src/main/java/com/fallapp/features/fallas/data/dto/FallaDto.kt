package com.fallapp.features.fallas.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO para la respuesta de la API GET /api/fallas
 * Mapea exactamente la estructura JSON del backend.
 */
@Serializable
data class FallaDto(
    val idFalla: Long,
    val nombre: String,
    val seccion: String,
    val categoria: String?,
    val direccion: String?,
    val ciudad: String,
    val provincia: String,
    val codigoPostal: String?,
    val latitud: Double?,
    val longitud: Double?,
    val telefono: String?,
    val email: String?,
    val web: String?,
    val facebook: String?,
    val twitter: String?,
    val instagram: String?,
    val descripcion: String?,
    val historia: String?,
    val imagenes: List<String>? = null,
    val numeroSocios: Int? = null,
    val numeroNinots: Int? = null,
    val numeroEventos: Int? = null,
    val presupuestoTotal: Double? = null,
    val anyoFundacion: Int? = null,
    val fechaCreacion: String?,
    val activa: Boolean = true
)
