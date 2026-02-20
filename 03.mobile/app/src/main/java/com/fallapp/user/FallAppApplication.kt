package com.fallapp.user

import android.app.Application
import com.fallapp.core.di.appModule
import com.fallapp.core.di.databaseModule
import com.fallapp.core.di.networkModule
import com.fallapp.features.auth.di.authModule
import com.fallapp.features.fallas.di.fallasModule
import com.fallapp.features.map.di.mapModule
import com.fallapp.features.eventos.di.eventosModule
import com.fallapp.features.profile.di.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import com.fallapp.user.BuildConfig

// Aquí arranca la app. Inicializamos Koin para la inyección de dependencias,
// el log y las librerías que usa la app.
class FallAppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Koin
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@FallAppApplication)
            modules(
                networkModule,
                databaseModule,
                appModule,
                authModule,
                fallasModule,
                eventosModule,
                mapModule,
                profileModule
            )
        }
    }
}
