package com.fallapp.features.profile.data.repository

import com.fallapp.core.util.Result
import com.fallapp.features.profile.data.mapper.toDomain
import com.fallapp.features.profile.data.remote.ProfileApiService
import com.fallapp.features.profile.data.remote.UpdateProfileRequest
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import com.fallapp.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Aquí hacemos las llamadas reales a la API de perfil y pasamos los datos al dominio
class ProfileRepositoryImpl(
    private val profileApiService: ProfileApiService
) : ProfileRepository {

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

    override fun uploadUserImage(
        userId: Long,
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String?
    ): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            val response = profileApiService.uploadUserImage(
                userId = userId,
                imageBytes = imageBytes,
                fileName = fileName,
                mimeType = mimeType
            )

            if (response.exito) {
                emit(Result.Success(Unit))
            } else {
                emit(
                    Result.Error(
                        exception = Exception(response.mensaje ?: "Error al subir imagen"),
                        message = response.mensaje ?: "Error al subir imagen"
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                Result.Error(
                    exception = e,
                    message = e.message ?: "Error desconocido al subir imagen"
                )
            )
        }
    }
}


