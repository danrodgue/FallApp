package com.fallapp.core.database.dao

import androidx.room.*
import com.fallapp.core.database.entity.EventoEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO para operaciones con Eventos en Room.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Dao
interface EventoDao {
    
    @Query("SELECT * FROM eventos ORDER BY fechaEvento ASC")
    fun getAllEventos(): Flow<List<EventoEntity>>
    
    @Query("SELECT * FROM eventos WHERE idEvento = :id")
    fun getEventoById(id: Long): Flow<EventoEntity?>
    
    @Query("SELECT * FROM eventos WHERE idFalla = :fallaId ORDER BY fechaEvento ASC")
    fun getEventosByFalla(fallaId: Long): Flow<List<EventoEntity>>
    
    /**
     * Obtiene eventos futuros a partir de ahora.
     */
    @Query("SELECT * FROM eventos WHERE fechaEvento >= :now ORDER BY fechaEvento ASC")
    fun getEventosFuturos(now: String): Flow<List<EventoEntity>>
    
    /**
     * Obtiene próximos N eventos.
     */
    @Query("SELECT * FROM eventos WHERE fechaEvento >= :now ORDER BY fechaEvento ASC LIMIT :limit")
    fun getEventosProximos(now: String, limit: Int): Flow<List<EventoEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: EventoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventos(eventos: List<EventoEntity>)
    
    @Update
    suspend fun updateEvento(evento: EventoEntity)
    
    @Delete
    suspend fun deleteEvento(evento: EventoEntity)
    
    @Query("DELETE FROM eventos")
    suspend fun deleteAllEventos()
    
    @Query("DELETE FROM eventos WHERE lastSyncTime < :threshold")
    suspend fun deleteOldEventos(threshold: String)
}
