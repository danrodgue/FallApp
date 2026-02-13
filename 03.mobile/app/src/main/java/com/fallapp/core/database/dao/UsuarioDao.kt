package com.fallapp.core.database.dao

import androidx.room.*
import com.fallapp.core.database.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con Usuarios en Room.
 * 
 * En la app móvil, normalmente solo habrá 1 usuario (el autenticado).
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Dao
interface UsuarioDao {
    
    /**
     * Obtiene el usuario actual (debería ser solo 1).
     */
    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getCurrentUser(): Flow<UsuarioEntity?>
    
    /**
     * Obtiene usuario por email.
     */
    @Query("SELECT * FROM usuarios WHERE email = :email")
    suspend fun getUserByEmail(email: String): UsuarioEntity?
    
    /**
     * Inserta o actualiza el usuario autenticado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(usuario: UsuarioEntity)
    
    /**
     * Elimina el usuario actual (logout).
     */
    @Query("DELETE FROM usuarios")
    suspend fun deleteCurrentUser()
}
