package com.fallapp.user

import android.app.Application
import com.fallapp.core.di.appModule
import com.fallapp.core.di.databaseModule
import com.fallapp.core.di.networkModule
import com.fallapp.features.auth.di.authModule
import com.fallapp.features.fallas.di.fallasModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Clase principal de la aplicación FallApp User.
 * 
 * Responsabilidades:
 * - Inicializar Koin (Dependency Injection)
 * - Configurar logging global
 * - Inicializar librerías de terceros
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class FallAppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Koin
        startKoin {
            // Log level según build type
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            
            // Contexto de Android
            androidContext(this@FallAppApplication)
            
            // Módulos de DI
            modules(
                networkModule,
                databaseModule,
                appModule,
                authModule,    // ← Auth feature module
                fallasModule   // ← Fallas feature module
            )
        }
    }
}
