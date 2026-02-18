package com.fallapp.features.auth.domain.model

/**
 * Modelo de dominio para un Usuario autenticado.
 * 
 * Representa la información del usuario después de un login exitoso.
 * Este modelo se usa en toda la capa de dominio y presentación.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
data class User(
    val idUsuario: Long,
    val email: String,
    val nombre: String,
    val apellidos: String,
    val rol: UserRole,
    val verificado: Boolean = false,
    val idFalla: Long? = null  // null si es usuario regular sin falla asignada
)

/**
 * Roles de usuario en el sistema.
 */
enum class UserRole {
    ADMIN,      // Administrador del sistema
    CASAL,      // Gestiona una falla
    USUARIO;    // Usuario regular

    companion object {
        fun fromString(value: String): UserRole {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: USUARIO
        }
    }
}
