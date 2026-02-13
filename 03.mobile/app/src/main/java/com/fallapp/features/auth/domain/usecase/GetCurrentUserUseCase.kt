package com.fallapp.features.auth.domain.usecase

import com.fallapp.features.auth.domain.model.User
import com.fallapp.features.auth.domain.repository.AuthRepository

/**
 * Caso de uso para obtener el usuario actualmente autenticado
 * desde el almacenamiento local (Room + DataStore).
 */
class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}

