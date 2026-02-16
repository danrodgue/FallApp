package com.fallapp.features.profile.domain.repository

import com.fallapp.core.util.Result
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de perfil de usuario.
 *
 * Define las operaciones disponibles para obtener y gestionar
 * información del perfil del usuario autenticado.
 *
 * Patrón: Repository para abstraer la fuente de datos (API, BD local, etc.)
 */
interface ProfileRepository {

    /**
     * Obtiene el perfil completo del usuario desde la API.
     *
     * Realiza una llamada a GET /api/usuarios/{id} y devuelve
     * un Flow con el resultado envuelto en Result.
     *
     * @param userId ID del usuario a recuperar
     * @return Flow con Result<UsuarioPerfil> que emite el estado de la operación
     */
    fun getUserProfile(userId: Long): Flow<Result<UsuarioPerfil>>

    /**
     * Actualiza el perfil del usuario en la API.
     *
     * Realiza una llamada a PUT /api/usuarios/{id} con los datos actualizados.
     *
     * @param userId ID del usuario a actualizar
     * @param nombreCompleto Nombre completo actualizado
     * @param telefono Teléfono actualizado
     * @param direccion Dirección actualizada
     * @param ciudad Ciudad actualizada
     * @param codigoPostal Código postal actualizado
     * @return Flow con Result<UsuarioPerfil> con los datos actualizados
     */
    fun updateUserProfile(
        userId: Long,
        nombreCompleto: String,
        telefono: String?,
        direccion: String?,
        ciudad: String?,
        codigoPostal: String?
    ): Flow<Result<UsuarioPerfil>>
}

