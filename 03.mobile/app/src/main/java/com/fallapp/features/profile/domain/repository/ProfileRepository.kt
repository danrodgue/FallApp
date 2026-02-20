package com.fallapp.features.profile.domain.repository

import com.fallapp.core.util.Result
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import kotlinx.coroutines.flow.Flow

// Interfaz del repositorio de perfil. Aquí definimos qué operaciones podemos hacer
interface ProfileRepository {

    fun getUserProfile(userId: Long): Flow<Result<UsuarioPerfil>>

    fun updateUserProfile(
        userId: Long,
        nombreCompleto: String,
        telefono: String?,
        direccion: String?,
        ciudad: String?,
        codigoPostal: String?
    ): Flow<Result<UsuarioPerfil>>

    fun uploadUserImage(
        userId: Long,
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String?
    ): Flow<Result<Unit>>
}

