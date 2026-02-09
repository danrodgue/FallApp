package com.fallapp.features.eventos.domain.model

import com.fallapp.core.database.TipoEvento
import java.time.LocalDateTime

/**
 * Modelo de dominio para eventos de falla.
 */
data class Evento(
    val idEvento: Long,
    val idFalla: Long,
    val nombreFalla: String,
    val tipo: TipoEvento,
    val nombre: String,
    val descripcion: String?,
    val fechaEvento: LocalDateTime,
    val ubicacion: String?,
    val participantesEstimado: Int?
)

