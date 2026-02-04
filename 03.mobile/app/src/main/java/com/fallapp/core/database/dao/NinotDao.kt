package com.fallapp.core.database.dao

import androidx.room.*
import com.fallapp.core.database.entity.NinotEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con Ninots en Room.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Dao
interface NinotDao {
    
    @Query("SELECT * FROM ninots ORDER BY nombreNinot ASC")
    fun getAllNinots(): Flow<List<NinotEntity>>
    
    @Query("SELECT * FROM ninots WHERE idNinot = :id")
    fun getNinotById(id: Long): Flow<NinotEntity?>
    
    @Query("SELECT * FROM ninots WHERE idFalla = :fallaId ORDER BY nombreNinot ASC")
    fun getNinotsByFalla(fallaId: Long): Flow<List<NinotEntity>>
    
    @Query("SELECT * FROM ninots WHERE premiado = 1 ORDER BY totalVotos DESC")
    fun getNinotsPremiados(): Flow<List<NinotEntity>>
    
    /**
     * Obtiene ninots ordenados por total de votos.
     */
    @Query("SELECT * FROM ninots ORDER BY totalVotos DESC LIMIT :limit")
    fun getTopNinots(limit: Int): Flow<List<NinotEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNinot(ninot: NinotEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNinots(ninots: List<NinotEntity>)
    
    @Update
    suspend fun updateNinot(ninot: NinotEntity)
    
    @Delete
    suspend fun deleteNinot(ninot: NinotEntity)
    
    @Query("DELETE FROM ninots")
    suspend fun deleteAllNinots()
    
    @Query("DELETE FROM ninots WHERE lastSyncTime < :threshold")
    suspend fun deleteOldNinots(threshold: String)
}
