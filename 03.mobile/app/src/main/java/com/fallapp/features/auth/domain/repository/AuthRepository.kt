package com.fallapp.features.auth.domain.repository

import com.fallapp.core.util.Result
import com.fallapp.features.auth.domain.model.AuthToken
import com.fallapp.features.auth.domain.model.User

/**
 * Repositorio de autenticación (interfaz de dominio).
 * 
 * Define las operaciones de autenticación que la capa de datos debe implementar.
 * Esta es la "puerta" entre el dominio y los datos.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
interface AuthRepository {
    
    /**
     * Inicia sesión con email y contraseña.
     * @param email Email del usuario
     * @param password Contraseña
     * @return Result con AuthToken si es exitoso, Error si falla
     */
    suspend fun login(email: String, password: String): Result<AuthToken>
    
    /**
     * Registra un nuevo usuario.
     * @param email Email del usuario
     * @param password Contraseña
     * @param nombre Nombre
     * @param apellidos Apellidos
     * @return Result con User si es exitoso, Error si falla
     */
    suspend fun register(
        email: String,
        password: String,
        nombre: String,
        apellidos: String
    ): Result<User>
    
    /**
     * Cierra la sesión actual.
     * Elimina el token local y notifica al servidor.
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Obtiene el usuario actualmente logueado desde el almacenamiento local.
     * @return User si hay sesión activa, null si no
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Verifica si hay una sesión activa.
     */
    suspend fun isLoggedIn(): Boolean
}
