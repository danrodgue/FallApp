package com.fallapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fallapp.core.database.Categoria
import java.time.LocalDateTime

/**
 * Entidad Room para Fallas.
 * 
 * Representa una falla fallera almacenada localmente.
 * Se sincroniza con GET /api/fallas de la API.
 * 
 * Estrategia offline-first:
 * - Se guarda en local después de obtener de API
 * - Se usa para mostrar datos sin conexión
 * - TTL: Se marca con lastSyncTime para saber si refrescar
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Entity(tableName = "fallas")
data class FallaEntity(
    @PrimaryKey
    val idFalla: Long,
    
    // Información básica
    val nombre: String,
    val seccion: String,
    val categoria: Categoria,
    
    // Cargos
    val presidente: String?,
    val fallera: String?,
    val artista: String?,
    
    // Detalles
    val anyoFundacion: Int?,
    val lema: String?,
    val descripcion: String?,
    val distintivo: String?,
    val experim: Boolean = false,
    
    // Ubicación
    val latitud: Double,
    val longitud: Double,
    
    // Contacto
    val webOficial: String?,
    val telefonoContacto: String?,
    val emailContacto: String?,
    
    // Media
    val urlBoceto: String?,
    
    // Estadísticas
    val totalEventos: Int = 0,
    val totalNinots: Int = 0,
    val totalMiembros: Int = 0,
    
    // Timestamps
    val fechaCreacion: LocalDateTime,
    val fechaActualizacion: LocalDateTime,
    
    // Control de caché
    val lastSyncTime: LocalDateTime = LocalDateTime.now()
)
