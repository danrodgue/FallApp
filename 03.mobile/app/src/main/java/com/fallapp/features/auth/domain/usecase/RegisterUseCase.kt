package com.fallapp.features.auth.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.auth.domain.model.User
import com.fallapp.features.auth.domain.repository.AuthRepository

/**
 * Caso de uso: Registrar usuario.
 * 
 * Valida los datos de entrada y llama al repositorio para crear el usuario.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    
    /**
     * Ejecuta el registro con validaciones.
     * @return Result con User o Error con mensaje descriptivo
     */
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        nombre: String,
        apellidos: String
    ): Result<User> {
        // Validación: Email
        if (email.isBlank()) {
            return Result.error(
                exception = IllegalArgumentException("Email requerido"),
                message = "Por favor, introduce tu email"
            )
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.error(
                exception = IllegalArgumentException("Email inválido"),
                message = "El formato del email no es válido"
            )
        }
        
        // Validación: Contraseña
        if (password.isBlank()) {
            return Result.error(
                exception = IllegalArgumentException("Contraseña requerida"),
                message = "Por favor, introduce una contraseña"
            )
        }
        
        if (password.length < 6) {
            return Result.error(
                exception = IllegalArgumentException("Contraseña muy corta"),
                message = "La contraseña debe tener al menos 6 caracteres"
            )
        }
        
        // Validación: Confirmar contraseña
        if (password != confirmPassword) {
            return Result.error(
                exception = IllegalArgumentException("Contraseñas no coinciden"),
                message = "Las contraseñas no coinciden"
            )
        }
        
        // Validación: Nombre
        if (nombre.isBlank()) {
            return Result.error(
                exception = IllegalArgumentException("Nombre requerido"),
                message = "Por favor, introduce tu nombre"
            )
        }
        
        // Validación: Apellidos
        if (apellidos.isBlank()) {
            return Result.error(
                exception = IllegalArgumentException("Apellidos requeridos"),
                message = "Por favor, introduce tus apellidos"
            )
        }
        
        // Llamar al repositorio para registrar
        return authRepository.register(email, password, nombre, apellidos)
    }
}
