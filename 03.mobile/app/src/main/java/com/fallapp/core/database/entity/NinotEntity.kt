package com.fallapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entidad Room para Ninots.
 * 
 * Representa los ninots (figuras artísticas) y sus datos.
 * Se sincroniza con GET /api/ninots de la API.
 * 
 * Sistema de votación:
 * - Los usuarios pueden votar ninots individuales (no fallas completas)
 * - 4 tipos de voto: favorito, ingenioso, critico, artistico
 * - Un usuario puede votar una vez por tipo por ninot
 * - Los contadores se calculan agregando votos del backend
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Entity(
    tableName = "ninots",
    foreignKeys = [
        ForeignKey(
            entity = FallaEntity::class,
            parentColumns = ["idFalla"],
            childColumns = ["idFalla"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idFalla"), Index("premiado")]
)
data class NinotEntity(
    @PrimaryKey
    val idNinot: Long,
    
    // Relación con Falla
    val idFalla: Long,
    val nombreFalla: String,
    
    // Información básica
    val nombreNinot: String,
    val tituloObra: String,
    val descripcion: String?,
    
    // Dimensiones físicas
    val alturaMetros: Double?,
    val anchoMetros: Double?,
    val profundidadMetros: Double?,
    val pesoToneladas: Double?,
    
    // Técnica artística
    val materialPrincipal: String?,
    val artistaConstructor: String?,
    val anyoConstruccion: Int?,
    
    // Multimedia
    val urlImagenPrincipal: String?,
    val imagenes: List<String> = emptyList(),  // URLs adicionales
    
    // Premios
    val premiado: Boolean = false,
    val categoriaPremio: String?,
    val anyoPremio: Int?,
    
    // Estadísticas de votos (agregadas del backend)
    val totalVotos: Int = 0,
    val votosFavorito: Int = 0,
    val votosIngenioso: Int = 0,
    val votosCritico: Int = 0,
    val votosArtistico: Int = 0,
    
    // Auditoría
    val notasTecnicas: String?,
    val fechaCreacion: LocalDateTime,
    val actualizadoEn: LocalDateTime?,
    
    val lastSyncTime: LocalDateTime = LocalDateTime.now()
)
