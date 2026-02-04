package com.example.fallapp.domain.usecase

import com.example.fallapp.domain.model.Falla
import com.example.fallapp.domain.repository.FallaRepository
import javax.inject.Inject

class SearchFallasUseCase @Inject constructor(
    private val repository: FallaRepository
) {
    suspend operator fun invoke(texto: String): Result<List<Falla>> =
        repository.searchFallas(texto)
}

