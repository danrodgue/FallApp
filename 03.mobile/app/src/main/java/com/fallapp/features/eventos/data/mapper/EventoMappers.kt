package com.fallapp.features.eventos.data.mapper

import com.fallapp.core.database.TipoEvento
import com.fallapp.features.eventos.data.remote.dto.EventoDto
import com.fallapp.features.eventos.domain.model.Evento
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Mapeos entre DTO y modelo de dominio de eventos.
 */

fun EventoDto.toDomain(): Evento {
    return Evento(
        idEvento = idEvento,
        idFalla = idFalla,
        nombreFalla = nombreFalla,
        tipo = mapTipoEvento(tipo),
        nombre = nombre,
        descripcion = descripcion,
        fechaEvento = parseDateTime(fechaEvento),
        ubicacion = ubicacion,
        participantesEstimado = participantesEstimado
    )
}

private fun mapTipoEvento(tipo: String): TipoEvento {
    return try {
        TipoEvento.valueOf(tipo.uppercase())
    } catch (e: IllegalArgumentException) {
        TipoEvento.OTRO
    }
}

private fun parseDateTime(dateString: String): LocalDateTime {
    return try {
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}

