package com.fallapp.core.database.dao

import androidx.room.*
import com.fallapp.core.database.entity.FallaEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO para operaciones con Fallas en Room.
 * 
 * Todas las queries devuelven Flow para observar cambios reactivamente.
 * Operaciones suspend para ejecutar en background (Coroutines).
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Dao
interface FallaDao {
    
    // ====== Queries de lectura ======
    
    /**
     * Obtiene todas las fallas ordenadas por nombre.
     * Flow emite automáticamente cuando hay cambios.
     */
    @Query("SELECT * FROM fallas ORDER BY nombre ASC")
    fun getAllFallas(): Flow<List<FallaEntity>>
    
    /**
     * Obtiene una falla por ID.
     */
    @Query("SELECT * FROM fallas WHERE idFalla = :id")
    fun getFallaById(id: Long): Flow<FallaEntity?>
    
    /**
     * Obtiene una falla por ID (suspendida, para un solo fetch).
     */
    @Query("SELECT * FROM fallas WHERE idFalla = :id")
    suspend fun getFallaByIdOnce(id: Long): FallaEntity?
    
    /**
     * Busca fallas por texto en nombre o descripción.
     */
    @Query("""
        SELECT * FROM fallas 
        WHERE nombre LIKE '%' || :query || '%' 
           OR descripcion LIKE '%' || :query || '%'
        ORDER BY nombre ASC
    """)
    fun searchFallas(query: String): Flow<List<FallaEntity>>
    
    /**
     * Obtiene fallas por sección.
     */
    @Query("SELECT * FROM fallas WHERE seccion = :seccion ORDER BY nombre ASC")
    fun getFallasBySeccion(seccion: String): Flow<List<FallaEntity>>
    
    /**
     * Obtiene fallas por categoría.
     */
    @Query("SELECT * FROM fallas WHERE categoria = :categoria ORDER BY nombre ASC")
    fun getFallasByCategoria(categoria: String): Flow<List<FallaEntity>>
    
    /**
     * Obtiene todas las fallas (suspendida, sin Flow).
     */
    @Query("SELECT * FROM fallas ORDER BY nombre ASC")
    suspend fun getAllFallasSync(): List<FallaEntity>
    
    /**
     * Obtiene una falla por ID (suspendida, sin Flow).
     */
    @Query("SELECT * FROM fallas WHERE idFalla = :id")
    suspend fun getFallaByIdSync(id: Long): FallaEntity?
    
    /**
     * Busca fallas (suspendida, sin Flow).
     */
    @Query("""
        SELECT * FROM fallas 
        WHERE nombre LIKE '%' || :query || '%' 
           OR descripcion LIKE '%' || :query || '%'
        ORDER BY nombre ASC
    """)
    suspend fun searchFallasSync(query: String): List<FallaEntity>
    
    /**
     * Obtiene fallas por categoría (suspendida, sin Flow).
     */
    @Query("SELECT * FROM fallas WHERE categoria = :categoria ORDER BY nombre ASC")
    suspend fun getFallasByCategoriaSync(categoria: String): List<FallaEntity>
    
    /**
     * Obtiene la fecha de última sincronización.
     */
    @Query("SELECT MAX(lastSyncTime) FROM fallas")
    suspend fun getLastSyncTime(): LocalDateTime?
    
    /**
     * Obtiene fallas paginadas.
     */
    @Query("SELECT * FROM fallas ORDER BY nombre ASC LIMIT :limit OFFSET :offset")
    suspend fun getFallasPaginated(limit: Int, offset: Int): List<FallaEntity>
    
    /**
     * Cuenta total de fallas.
     */
    @Query("SELECT COUNT(*) FROM fallas")
    suspend fun getTotalFallas(): Int
    
    // ====== Operaciones de escritura ======
    
    /**
     * Inserta una falla. Si existe, la reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(falla: FallaEntity)
    
    /**
     * Inserta múltiples fallas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fallas: List<FallaEntity>)
    
    /**
     * Actualiza una falla.
     */
    @Update
    suspend fun updateFalla(falla: FallaEntity)
    
    /**
     * Elimina una falla.
     */
    @Delete
    suspend fun deleteFalla(falla: FallaEntity)
    
    /**
     * Elimina todas las fallas (útil para limpiar caché).
     */
    @Query("DELETE FROM fallas")
    suspend fun deleteAllFallas()
    
    /**
     * Elimina fallas con lastSyncTime antiguo (limpieza de caché).
     */
    @Query("DELETE FROM fallas WHERE lastSyncTime < :threshold")
    suspend fun deleteOldFallas(threshold: String)
}
