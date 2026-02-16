package com.fallapp.features.auth.data.repository

import com.fallapp.core.database.Rol
import com.fallapp.core.database.dao.UsuarioDao
import com.fallapp.core.database.entity.UsuarioEntity
import com.fallapp.core.util.Result
import com.fallapp.core.util.TokenManager
import com.fallapp.features.auth.data.mapper.toDomain
import com.fallapp.features.auth.data.remote.AuthApiService
import com.fallapp.features.auth.domain.model.AuthToken
import com.fallapp.features.auth.domain.model.User
import com.fallapp.features.auth.domain.model.UserRole
import com.fallapp.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime


/**
 * Implementación del repositorio de autenticación.
 * 
 * Coordina entre:
 * - AuthApiService (llamadas HTTP)
 * - TokenManager (persistencia de token)
 * - UsuarioDao (caché local del usuario)
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val usuarioDao: UsuarioDao
) : AuthRepository {
    
    override suspend fun login(email: String, password: String): Result<AuthToken> {
        return try {
            // Llamar al API (devuelve ApiResponse<LoginResponseDto>)
            val apiResponse = authApiService.login(email, password)
            
            // Verificar éxito y extraer datos
            if (!apiResponse.exito || apiResponse.datos == null) {
                return Result.error(
                    exception = Exception(apiResponse.mensaje ?: "Error desconocido"),
                    message = apiResponse.mensaje ?: "Login falló sin mensaje"
                )
            }
            
            val loginData = apiResponse.datos
            
            // Guardar token con email e ID del usuario
            tokenManager.saveToken(loginData.token, loginData.usuario.email, loginData.usuario.idUsuario)

            // Mapear a dominio
            val user = loginData.usuario.toDomain()
            
            // Guardar usuario en base de datos local
            usuarioDao.insertUser(
                UsuarioEntity(
                    idUsuario = user.idUsuario,
                    email = user.email,
                    nombreCompleto = loginData.usuario.nombreCompleto,
                    rol = Rol.valueOf(user.rol.name.uppercase()),
                    verificado = user.verificado,
                    idFalla = user.idFalla,
                    nombreFalla = loginData.usuario.nombreFalla,
                    ultimoAcceso = LocalDateTime.now()
                )
            )
            
            Result.success(AuthToken(loginData.token, user))
        } catch (e: Exception) {
            Result.error(
                exception = e,
                message = "Error al iniciar sesión: ${e.message ?: "Desconocido"}"
            )
        }
    }
    
    override suspend fun register(
        email: String,
        password: String,
        nombre: String,
        apellidos: String
    ): Result<User> {
        return try {
            // Combinar nombre y apellidos en nombreCompleto
            val nombreCompleto = "$nombre $apellidos".trim()
            
            // Llamar al API (devuelve ApiResponse<LoginResponseDto>)
            val apiResponse = authApiService.register(email, password, nombreCompleto, idFalla = null)
            
            // Verificar éxito y extraer datos
            if (!apiResponse.exito || apiResponse.datos == null) {
                return Result.error(
                    exception = Exception(apiResponse.mensaje ?: "Error desconocido"),
                    message = apiResponse.mensaje ?: "Registro falló sin mensaje"
                )
            }
            
            // Mapear a dominio
            val user = apiResponse.datos.usuario.toDomain()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.error(
                exception = e,
                message = "Error al registrar: ${e.message ?: "Desconocido"}"
            )
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            // Notificar al backend (opcional)
            authApiService.logout()
            
            // Eliminar token local
            tokenManager.clearToken()
            
            // Eliminar usuario de BD local
            usuarioDao.deleteCurrentUser()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Incluso si falla la petición, limpiamos localmente
            tokenManager.clearToken()
            usuarioDao.deleteCurrentUser()
            Result.success(Unit)
        }
    }
    
    override suspend fun getCurrentUser(): User? {
        val usuarioEntity = usuarioDao.getCurrentUser().first() ?: return null
        
        // Parsear nombreCompleto en nombre y apellidos
        val parts = usuarioEntity.nombreCompleto.split(" ", limit = 2)
        val nombre = parts.getOrNull(0) ?: ""
        val apellidos = parts.getOrNull(1) ?: ""
        
        return User(
            idUsuario = usuarioEntity.idUsuario,
            email = usuarioEntity.email,
            nombre = nombre,
            apellidos = apellidos,
            rol = UserRole.fromString(usuarioEntity.rol.name),
            verificado = usuarioEntity.verificado,
            idFalla = usuarioEntity.idFalla
        )
    }
    
    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasActiveSession()
    }
}
