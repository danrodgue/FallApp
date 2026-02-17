package com.fallapp.features.eventos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO de evento según respuesta del backend (campos snake_case).
 * El backend usa @JsonProperty para id_evento, id_falla, fecha_evento.
 */
@Serializable
data class EventoDto(
    @SerialName("id_evento") val idEvento: Long,
    @SerialName("id_falla") val idFalla: Long,
    val nombreFalla: String = "",
    val tipo: String = "",
    val nombre: String = "",
    val descripcion: String? = null,
    @SerialName("fecha_evento") val fechaEvento: String,
    val ubicacion: String? = null,
    val participantesEstimado: Int? = null,
    @SerialName("imagenNombre") val imagen: String? = null
)

