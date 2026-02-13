package com.fallapp.features.eventos.data.repository

import com.fallapp.core.util.Result
import com.fallapp.features.eventos.data.mapper.toDomain
import com.fallapp.features.eventos.data.remote.EventosApiService
import com.fallapp.features.eventos.domain.model.Evento
import com.fallapp.features.eventos.domain.repository.EventosRepository

/**
 * Implementación simple basada solo en red para eventos.
 */
class EventosRepositoryImpl(
    private val apiService: EventosApiService
) : EventosRepository {

    override suspend fun getEventosByFalla(idFalla: Long): Result<List<Evento>> {
        return try {
            val response = apiService.getEventosByFalla(idFalla)
            if (response.exito && response.datos != null) {
                val eventosDto = response.datos.content
                Result.Success(eventosDto.map { it.toDomain() })
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al obtener eventos de la falla"),
                    message = response.mensaje
                )
            }
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "Error de conexión al obtener eventos de la falla"
            )
        }
    }

    override suspend fun getProximosEventos(limit: Int): Result<List<Evento>> {
        return try {
            val response = apiService.getProximosEventos(limit)
            if (response.exito && response.datos != null) {
                Result.Success(response.datos.map { it.toDomain() })
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al obtener eventos"),
                    message = response.mensaje
                )
            }
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "Error de conexión al obtener eventos"
            )
        }
    }
}

