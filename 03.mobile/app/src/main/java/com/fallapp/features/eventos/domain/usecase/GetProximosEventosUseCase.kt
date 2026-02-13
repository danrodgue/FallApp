package com.fallapp.features.eventos.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.eventos.domain.model.Evento
import com.fallapp.features.eventos.domain.repository.EventosRepository

class GetProximosEventosUseCase(
    private val repository: EventosRepository
) {
    suspend operator fun invoke(limit: Int): Result<List<Evento>> =
        repository.getProximosEventos(limit)
}

