package com.fallapp.core.di

import com.fallapp.core.util.TokenManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Módulo de Koin para utilidades generales de la aplicación.
 * 
 * Provee:
 * - TokenManager: Gestión de JWT tokens
 * - DateTimeUtils: Utilidades de fecha/hora (object, no necesita DI)
 * - Extensions: Extensiones de tipos (top-level functions, no necesita DI)
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
val appModule = module {
    
    /**
     * TokenManager para gestionar tokens JWT.
     * Singleton que persiste el token en DataStore.
     */
    single<TokenManager> {
        TokenManager(androidContext())
    }
}
