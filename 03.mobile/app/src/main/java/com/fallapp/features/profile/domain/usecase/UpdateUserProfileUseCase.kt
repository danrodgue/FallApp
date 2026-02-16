package com.fallapp.features.profile.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import com.fallapp.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case para actualizar el perfil del usuario.
 *
 * Caso de uso que actualiza los datos del perfil del usuario en el servidor.
 *
 * @property profileRepository Repositorio de perfil
 */
class UpdateUserProfileUseCase(
    private val profileRepository: ProfileRepository
) {

    /**
     * Ejecuta la actualización del perfil.
     *
     * @param userId ID del usuario
     * @param nombreCompleto Nombre completo actualizado
     * @param telefono Teléfono actualizado
     * @param direccion Dirección actualizada
     * @param ciudad Ciudad actualizada
     * @param codigoPostal Código postal actualizado
     * @return Flow con Result<UsuarioPerfil> con los datos actualizados
     */
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

