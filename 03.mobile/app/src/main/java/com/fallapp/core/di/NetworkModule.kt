package com.fallapp.core.di

import com.fallapp.core.network.KtorClient
import com.fallapp.core.network.NetworkMonitor
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Módulo de Koin para inyección de dependencias relacionadas con red.
 * 
 * Provee:
 * - HttpClient sin autenticación (para login/registro)
 * - NetworkMonitor para verificar conectividad
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
val networkModule = module {
    
    /**
     * HttpClient público (sin token).
     * Usado para endpoints públicos como /api/auth/login
     */
    single<HttpClient> {
        KtorClient.create()
    }
    
    /**
     * NetworkMonitor para verificar estado de conexión.
     * Singleton que observa cambios de red.
     */
    single<NetworkMonitor> {
        NetworkMonitor(androidContext())
    }
}
