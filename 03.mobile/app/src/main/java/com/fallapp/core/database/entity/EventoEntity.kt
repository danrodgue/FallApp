package com.fallapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fallapp.core.database.TipoEvento
import java.time.LocalDateTime

/**
 * Entidad Room para Eventos.
 * 
 * Representa eventos de fallas (plantà, ofrenda, cremà, etc).
 * Se sincroniza con GET /api/eventos de la API.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Entity(
    tableName = "eventos",
    foreignKeys = [
        ForeignKey(
            entity = FallaEntity::class,
            parentColumns = ["idFalla"],
            childColumns = ["idFalla"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("idFalla"),
        Index("fechaEvento")
    ]
)
data class EventoEntity(
    @PrimaryKey
    val idEvento: Long,
    
    val idFalla: Long,
    val nombreFalla: String,
    
    val tipo: TipoEvento,
    val nombre: String,
    val descripcion: String?,
    
    val fechaEvento: LocalDateTime,
    
    val ubicacion: String?,
    val latitud: Double?,
    val longitud: Double?,
    
    val participantesEstimado: Int?,
    
    val lastSyncTime: LocalDateTime = LocalDateTime.now()
)
