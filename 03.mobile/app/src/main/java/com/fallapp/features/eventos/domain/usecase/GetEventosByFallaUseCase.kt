package com.fallapp.features.eventos.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.eventos.domain.model.Evento
import com.fallapp.features.eventos.domain.repository.EventosRepository

class GetEventosByFallaUseCase(
    private val repository: EventosRepository
) {
    suspend operator fun invoke(idFalla: Long): Result<List<Evento>> =
        repository.getEventosByFalla(idFalla)
}

