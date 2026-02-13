package com.fallapp.core.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.fallapp.core.database.FallAppDatabase
import com.fallapp.core.database.dao.EventoDao
import com.fallapp.core.database.dao.FallaDao
import com.fallapp.core.database.dao.NinotDao
import com.fallapp.core.database.dao.UsuarioDao
import com.fallapp.core.network.NetworkMonitor
import io.ktor.client.HttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull

/**
 * Tests para verificar que los módulos de Koin se cargan correctamente
 * y todas las dependencias se resuelven sin errores.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner::class)
class KoinModulesTest : KoinTest {
    
    @Before
    fun setup() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext<Context>())
            modules(networkModule, databaseModule, appModule)
        }
    }
    
    @After
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `networkModule provee HttpClient`() {
        val httpClient: HttpClient by inject()
        assertNotNull(httpClient, "HttpClient debería estar disponible")
    }
    
    @Test
    fun `networkModule provee NetworkMonitor`() {
        val networkMonitor: NetworkMonitor by inject()
        assertNotNull(networkMonitor, "NetworkMonitor debería estar disponible")
    }
    
    @Test
    fun `databaseModule provee FallAppDatabase`() {
        val database: FallAppDatabase by inject()
        assertNotNull(database, "FallAppDatabase debería estar disponible")
    }
    
    @Test
    fun `databaseModule provee FallaDao`() {
        val fallaDao: FallaDao by inject()
        assertNotNull(fallaDao, "FallaDao debería estar disponible")
    }
    
    @Test
    fun `databaseModule provee EventoDao`() {
        val eventoDao: EventoDao by inject()
        assertNotNull(eventoDao, "EventoDao debería estar disponible")
    }
    
    @Test
    fun `databaseModule provee NinotDao`() {
        val ninotDao: NinotDao by inject()
        assertNotNull(ninotDao, "NinotDao debería estar disponible")
    }
    
    @Test
    fun `databaseModule provee UsuarioDao`() {
        val usuarioDao: UsuarioDao by inject()
        assertNotNull(usuarioDao, "UsuarioDao debería estar disponible")
    }
    
    @Test
    fun `todos los módulos se cargan sin conflictos`() {
        // Si llegamos aquí sin excepciones, todos los módulos se cargaron correctamente
        val httpClient: HttpClient by inject()
        val database: FallAppDatabase by inject()
        val networkMonitor: NetworkMonitor by inject()
        
        assertNotNull(httpClient)
        assertNotNull(database)
        assertNotNull(networkMonitor)
    }
}
