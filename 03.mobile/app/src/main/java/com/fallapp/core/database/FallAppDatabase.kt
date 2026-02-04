package com.fallapp.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fallapp.core.database.dao.EventoDao
import com.fallapp.core.database.dao.FallaDao
import com.fallapp.core.database.dao.NinotDao
import com.fallapp.core.database.dao.UsuarioDao
import com.fallapp.core.database.entity.EventoEntity
import com.fallapp.core.database.entity.FallaEntity
import com.fallapp.core.database.entity.NinotEntity
import com.fallapp.core.database.entity.UsuarioEntity

/**
 * Base de datos Room de FallApp.
 * 
 * Almacena datos localmente para estrategia offline-first:
 * - Fallas, Eventos, Ninots se cachean
 * - Usuario autenticado se guarda
 * - Votos y favoritos se sincronizan
 * 
 * Versión: 1
 * - Tablas iniciales: fallas, eventos, ninots, usuarios
 * 
 * Migraciones futuras:
 * - Versión 2: Agregar tabla votos_locales
 * - Versión 3: Agregar tabla favoritos
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Database(
    entities = [
        FallaEntity::class,
        EventoEntity::class,
        NinotEntity::class,
        UsuarioEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FallAppDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun fallaDao(): FallaDao
    abstract fun eventoDao(): EventoDao
    abstract fun ninotDao(): NinotDao
    abstract fun usuarioDao(): UsuarioDao
    
    companion object {
        private const val DATABASE_NAME = "fallapp_database"
        
        @Volatile
        private var INSTANCE: FallAppDatabase? = null
        
        /**
         * Obtiene la instancia de la base de datos (Singleton).
         * 
         * @param context Contexto de la aplicación
         * @return Instancia de FallAppDatabase
         */
        fun getInstance(context: Context): FallAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FallAppDatabase::class.java,
                    DATABASE_NAME
                )
                    // TODO: Agregar migraciones cuando haya cambios de schema
                    // .addMigrations(MIGRATION_1_2)
                    // En desarrollo: destruir y recrear si cambia schema
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Crea una instancia en memoria para tests.
         * 
         * @param context Contexto de test
         * @return Base de datos en memoria
         */
        fun getInMemoryDatabase(context: Context): FallAppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                FallAppDatabase::class.java
            )
                .allowMainThreadQueries() // Solo para tests
                .build()
        }
    }
}
