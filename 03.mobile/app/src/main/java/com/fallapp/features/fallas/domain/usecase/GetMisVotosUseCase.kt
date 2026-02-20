package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Voto
import com.fallapp.features.fallas.domain.repository.VotosRepository

class GetMisVotosUseCase(
    private val repository: VotosRepository
) {
    suspend operator fun invoke(): Result<List<Voto>> = repository.getMisVotos()
}
