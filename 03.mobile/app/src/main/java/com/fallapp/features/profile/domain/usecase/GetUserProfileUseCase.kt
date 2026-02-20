package com.fallapp.features.profile.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import com.fallapp.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

// Use case que pide el perfil del usuario al repositorio
class GetUserProfileUseCase(
    private val profileRepository: ProfileRepository
) {

    // invoke hace que podamos llamar useCase(userId) en vez de useCase.invoke(userId)
    operator fun invoke(userId: Long): Flow<Result<UsuarioPerfil>> {
        return profileRepository.getUserProfile(userId)
    }
}

