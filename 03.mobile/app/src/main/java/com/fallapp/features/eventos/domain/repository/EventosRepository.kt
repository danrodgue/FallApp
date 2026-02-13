package com.fallapp.features.eventos.domain.repository

import com.fallapp.core.util.Result
import com.fallapp.features.eventos.domain.model.Evento

/**
 * Repositorio de lectura de eventos.
 */
interface EventosRepository {
    suspend fun getEventosByFalla(idFalla: Long): Result<List<Evento>>
    suspend fun getProximosEventos(limit: Int): Result<List<Evento>>
}

