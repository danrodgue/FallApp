package com.example.fallapp.domain.usecase

import com.example.fallapp.domain.model.Falla
import com.example.fallapp.domain.repository.FallaRepository
import javax.inject.Inject

class GetFallasUseCase @Inject constructor(
    private val repository: FallaRepository
) {
    suspend operator fun invoke(): Result<List<Falla>> = repository.getFallas()
}

