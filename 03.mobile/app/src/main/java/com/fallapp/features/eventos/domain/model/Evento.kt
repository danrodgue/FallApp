package com.fallapp.features.eventos.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Modelo de dominio para eventos de falla.
 */
@Serializable
data class Evento(
    @SerialName("id_evento")
    val idEvento: Long,

    @SerialName("id_falla")
    val idFalla: Long,

    @SerialName("nombreFalla")
    val nombreFalla: String,

    @SerialName("tipo")
    val tipo: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String? = null,

    @SerialName("fecha_evento")
    val fechaEventoString: String,

    @SerialName("ubicacion")
    val ubicacion: String? = null,

    @SerialName("participantesEstimado")
    val participantesEstimado: Int? = null,

    @SerialName("imagenNombre")
    val imagen: String? = null,

    @SerialName("creado_por")
    val creadoPor: Long? = null,

    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null
) {
    val fechaEvento: LocalDateTime
        get() {
            return try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                LocalDateTime.parse(fechaEventoString, formatter)
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        }
}

