package com.fallapp.features.profile.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import com.fallapp.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case para obtener el perfil del usuario.
 *
 * Encapsula la lógica de negocio para recuperar información del usuario
 * desde el repositorio.
 *
 * @property profileRepository Repositorio de perfil
 */
class GetUserProfileUseCase(
    private val profileRepository: ProfileRepository
) {

    /**
     * Obtiene el perfil del usuario especificado.
     *
     * Patrón: Operador invoke permite usar la clase como función:
     * ```
     * val useCase = GetUserProfileUseCase(repo)
     * val result = useCase(userId)  // En lugar de useCase.invoke(userId)
     * ```
     *
     * @param userId ID del usuario a recuperar
     * @return Flow con Result<UsuarioPerfil>
     */
    operator fun invoke(userId: Long): Flow<Result<UsuarioPerfil>> {
        return profileRepository.getUserProfile(userId)
    }
}

