package com.fallapp.features.auth.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.auth.domain.repository.AuthRepository

/**
 * Caso de uso: Cerrar sesión.
 * 
 * Elimina el token local y cierra la sesión del usuario.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    
    /**
     * Ejecuta el logout.
     * @return Result<Unit> indicando éxito o error
     */
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
