package com.example.fallapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FallaDto(
    @SerialName("idFalla") val id: Long,
    val nombre: String,
    val seccion: String? = null,
    val presidente: String? = null,
    val lema: String? = null,
    val categoria: String? = null,
    val urlBoceto: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

@Serializable
data class FallasPageDto(
    val contenido: List<FallaDto> = emptyList(),
    val totalElementos: Int? = null
)

