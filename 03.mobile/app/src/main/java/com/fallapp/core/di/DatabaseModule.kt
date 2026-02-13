package com.fallapp.core.di

import com.fallapp.core.database.FallAppDatabase
import com.fallapp.core.database.dao.EventoDao
import com.fallapp.core.database.dao.FallaDao
import com.fallapp.core.database.dao.NinotDao
import com.fallapp.core.database.dao.UsuarioDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Módulo de Koin para inyección de dependencias de base de datos.
 * 
 * Provee:
 * - FallAppDatabase (singleton)
 * - Todos los DAOs (FallaDao, EventoDao, NinotDao, UsuarioDao)
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
val databaseModule = module {
    
    /**
     * Base de datos Room.
     * Singleton que persiste durante toda la vida de la app.
     */
    single<FallAppDatabase> {
        FallAppDatabase.getInstance(androidContext())
    }
    
    /**
     * DAO para operaciones con fallas.
     */
    single<FallaDao> {
        get<FallAppDatabase>().fallaDao()
    }
    
    /**
     * DAO para operaciones con eventos.
     */
    single<EventoDao> {
        get<FallAppDatabase>().eventoDao()
    }
    
    /**
     * DAO para operaciones con ninots.
     */
    single<NinotDao> {
        get<FallAppDatabase>().ninotDao()
    }
    
    /**
     * DAO para operaciones con usuario (login/logout).
     */
    single<UsuarioDao> {
        get<FallAppDatabase>().usuarioDao()
    }
}
