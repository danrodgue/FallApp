package com.fallapp.features.eventos.data.mapper

import com.fallapp.core.config.ApiConfig
import com.fallapp.features.eventos.data.remote.dto.EventoDto
import com.fallapp.features.eventos.domain.model.Evento
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Mapeos entre DTO y modelo de dominio de eventos.
 */

fun EventoDto.toDomain(): Evento {
    // Construir URL completa de la imagen si tenemos nombre de archivo
    val imagenUrl = imagen?.let {
        "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.EVENTOS}/$idEvento/imagen"
    }

    return Evento(
        idEvento = idEvento,
        idFalla = idFalla,
        nombreFalla = nombreFalla,
        tipo = tipo,
        nombre = nombre,
        descripcion = descripcion,
        fechaEventoString = fechaEvento,
        ubicacion = ubicacion,
        participantesEstimado = participantesEstimado,
        imagen = imagenUrl
    )
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

