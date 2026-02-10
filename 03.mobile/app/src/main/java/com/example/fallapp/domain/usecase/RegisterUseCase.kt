package com.example.fallapp.domain.usecase

import com.example.fallapp.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        nombreCompleto: String,
        idFalla: Long?
    ): Result<Unit> {
        return authRepository.register(email, password, nombreCompleto, idFalla)
    }
}

