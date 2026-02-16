package com.fallapp.features.profile.data.repository

import com.fallapp.core.util.Result
import com.fallapp.features.profile.data.mapper.toDomain
import com.fallapp.features.profile.data.remote.ProfileApiService
import com.fallapp.features.profile.data.remote.UpdateProfileRequest
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import com.fallapp.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementación del repositorio de perfil de usuario.
 *
 * Realiza las llamadas a la API para obtener información del perfil.
 * Maneja errores y convierte DTOs a modelos de dominio.
 *
 * @property profileApiService Servicio API para llamadas HTTP
 */
class ProfileRepositoryImpl(
    private val profileApiService: ProfileApiService
) : ProfileRepository {

    /**
     * Obtiene el perfil del usuario desde la API.
     *
     * Flow que:
     * 1. Emite Loading mientras se realiza la llamada
     * 2. Emite Success con UsuarioPerfil si la llamada es exitosa
     * 3. Emite Error si hay un problema
     *
     * @param userId ID del usuario a recuperar
     * @return Flow con Result<UsuarioPerfil>
     */
    override fun getUserProfile(userId: Long): Flow<Result<UsuarioPerfil>> = flow {
        try {
            emit(Result.Loading)

            val response = profileApiService.getUserProfile(userId)

            if (response.exito && response.datos != null) {
                emit(Result.Success(response.datos.toDomain()))
            } else {
                emit(Result.Error(
                    exception = Exception(response.mensaje ?: "Error al cargar perfil"),
                    message = response.mensaje ?: "Error al cargar perfil"
                ))
            }
        } catch (e: Exception) {
            emit(Result.Error(
                exception = e,
                message = e.message ?: "Error desconocido"
            ))
        }
    }

    /**
     * Actualiza el perfil del usuario en la API.
     *
     * @param userId ID del usuario
     * @param nombreCompleto Nombre actualizado
     * @param telefono Teléfono actualizado
     * @param direccion Dirección actualizada
     * @param ciudad Ciudad actualizada
     * @param codigoPostal Código postal actualizado
     * @return Flow con Result<UsuarioPerfil> actualizado
     */
    override fun updateUserProfile(
        userId: Long,
        nombreCompleto: String,
        telefono: String?,
        direccion: String?,
        ciudad: String?,
        codigoPostal: String?
    ): Flow<Result<UsuarioPerfil>> = flow {
        try {
            emit(Result.Loading)

            val request = UpdateProfileRequest(
                nombreCompleto = nombreCompleto,
                telefono = telefono?.takeIf { it.isNotBlank() },
                direccion = direccion?.takeIf { it.isNotBlank() },
                ciudad = ciudad?.takeIf { it.isNotBlank() },
                codigoPostal = codigoPostal?.takeIf { it.isNotBlank() }
            )

            val response = profileApiService.updateUserProfile(userId, request)

            if (response.exito && response.datos != null) {
                emit(Result.Success(response.datos.toDomain()))
            } else {
                emit(Result.Error(
                    exception = Exception(response.mensaje ?: "Error al actualizar perfil"),
                    message = response.mensaje ?: "Error al actualizar perfil"
                ))
            }
        } catch (e: Exception) {
            emit(Result.Error(
                exception = e,
                message = e.message ?: "Error desconocido"
            ))
        }
    }
}


