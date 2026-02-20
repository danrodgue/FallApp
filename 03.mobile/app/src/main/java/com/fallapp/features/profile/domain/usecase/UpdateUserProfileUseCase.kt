package com.fallapp.features.profile.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import com.fallapp.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

// Use case que manda al servidor los datos actualizados del perfil
class UpdateUserProfileUseCase(
    private val profileRepository: ProfileRepository
) {

    operator fun invoke(
        userId: Long,
        nombreCompleto: String,
        telefono: String?,
        direccion: String?,
        ciudad: String?,
        codigoPostal: String?
    ): Flow<Result<UsuarioPerfil>> {
        return profileRepository.updateUserProfile(
            userId = userId,
            nombreCompleto = nombreCompleto,
            telefono = telefono,
            direccion = direccion,
            ciudad = ciudad,
            codigoPostal = codigoPostal
        )
    }
}

