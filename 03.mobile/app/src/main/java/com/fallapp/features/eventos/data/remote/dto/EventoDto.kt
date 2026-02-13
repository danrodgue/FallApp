package com.fallapp.features.eventos.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO de evento según GUIA.API.FRONTEND.md.
 */
@Serializable
data class EventoDto(
    val idEvento: Long,
    val idFalla: Long,
    val nombreFalla: String,
    val tipo: String,
    val nombre: String,
    val descripcion: String? = null,
    val fechaEvento: String,
    val ubicacion: String? = null,
    val participantesEstimado: Int? = null
)

