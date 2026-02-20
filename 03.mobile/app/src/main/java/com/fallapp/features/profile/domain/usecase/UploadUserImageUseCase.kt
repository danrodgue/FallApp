package com.fallapp.features.profile.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

// Use case para subir la foto de perfil
class UploadUserImageUseCase(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(
        userId: Long,
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String?
    ): Flow<Result<Unit>> {
        return profileRepository.uploadUserImage(
            userId = userId,
            imageBytes = imageBytes,
            fileName = fileName,
            mimeType = mimeType
        )
    }
}
