package com.fallapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fallapp.core.database.Rol
import java.time.LocalDateTime

/**
 * Entidad Room para Usuarios.
 * 
 * Guarda información del usuario autenticado localmente.
 * Se sincroniza con POST /api/auth/login de la API.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = FallaEntity::class,
            parentColumns = ["idFalla"],
            childColumns = ["idFalla"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("email", unique = true), Index("idFalla")]
)
data class UsuarioEntity(
    @PrimaryKey
    val idUsuario: Long,
    
    val email: String,
    val nombreCompleto: String,
    val rol: Rol,
    val verificado: Boolean = false,
    
    val idFalla: Long?,
    val nombreFalla: String?,
    
    val ultimoAcceso: LocalDateTime?,
    
    val lastSyncTime: LocalDateTime = LocalDateTime.now()
)
