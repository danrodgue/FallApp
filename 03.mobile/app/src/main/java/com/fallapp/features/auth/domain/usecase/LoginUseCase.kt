package com.fallapp.features.auth.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.auth.domain.model.AuthToken
import com.fallapp.features.auth.domain.repository.AuthRepository

/**
 * Caso de uso: Iniciar sesión.
 * 
 * Valida los datos de entrada y llama al repositorio para autenticar.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    
    /**
     * Ejecuta el login con validaciones.
     * @param email Email del usuario
     * @param password Contraseña
     * @return Result con AuthToken o Error con mensaje descriptivo
     */
    suspend operator fun invoke(email: String, password: String): Result<AuthToken> {
        // Validación: Email no vacío
        if (email.isBlank()) {
            return Result.error(
                exception = IllegalArgumentException("Email requerido"),
                message = "Por favor, introduce tu email"
            )
        }
        
        // Validación: Formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.error(
                exception = IllegalArgumentException("Email inválido"),
                message = "El formato del email no es válido"
            )
        }
        
        // Validación: Contraseña no vacía
        if (password.isBlank()) {
            return Result.error(
                exception = IllegalArgumentException("Contraseña requerida"),
                message = "Por favor, introduce tu contraseña"
            )
        }
        
        // Validación: Contraseña mínimo 6 caracteres
        if (password.length < 6) {
            return Result.error(
                exception = IllegalArgumentException("Contraseña muy corta"),
                message = "La contraseña debe tener al menos 6 caracteres"
            )
        }
        
        // Llamar al repositorio para autenticar
        return authRepository.login(email, password)
    }
}
