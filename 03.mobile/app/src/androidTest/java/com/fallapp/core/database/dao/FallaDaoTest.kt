package com.fallapp.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fallapp.core.database.Categoria
import com.fallapp.core.database.FallAppDatabase
import com.fallapp.core.database.entity.FallaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * Tests instrumentados para FallaDao.
 * 
 * Verifica:
 * - Inserción y lectura de fallas
 * - Búsqueda por texto
 * - Filtrado por categoría y sección
 * - Paginación
 * - Operaciones CRUD
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@RunWith(AndroidJUnit4::class)
class FallaDaoTest {
    
    private lateinit var database: FallAppDatabase
    private lateinit var fallaDao: FallaDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FallAppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        fallaDao = database.fallaDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertFalla_andRetrieveById() = runTest {
        // Given
        val falla = createTestFalla(id = 1, nombre = "Falla Test")
        
        // When
        fallaDao.insertFalla(falla)
        val retrieved = fallaDao.getFallaByIdOnce(1)
        
        // Then
        assertNotNull(retrieved)
        assertEquals("Falla Test", retrieved?.nombre)
        assertEquals("1A", retrieved?.seccion)
    }
    
    @Test
    fun insertMultipleFallas_andGetAll() = runTest {
        // Given
        val fallas = listOf(
            createTestFalla(1, "Falla A"),
            createTestFalla(2, "Falla B"),
            createTestFalla(3, "Falla C")
        )
        
        // When
        fallaDao.insertFallas(fallas)
        val allFallas = fallaDao.getAllFallas().first()
        
        // Then
        assertEquals(3, allFallas.size)
        assertTrue(allFallas.any { it.nombre == "Falla A" })
    }
    
    @Test
    fun searchFallas_byName() = runTest {
        // Given
        fallaDao.insertFallas(listOf(
            createTestFalla(1, "Falla Convento"),
            createTestFalla(2, "Falla Ruzafa"),
            createTestFalla(3, "Falla Convento Sur")
        ))
        
        // When
        val results = fallaDao.searchFallas("Convento").first()
        
        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.nombre.contains("Convento") })
    }
    
    @Test
    fun getFallasByCategoria() = runTest {
        // Given
        fallaDao.insertFallas(listOf(
            createTestFalla(1, "Falla 1", categoria = Categoria.ESPECIAL),
            createTestFalla(2, "Falla 2", categoria = Categoria.PRIMERA_A),
            createTestFalla(3, "Falla 3", categoria = Categoria.ESPECIAL)
        ))
        
        // When
        val especiales = fallaDao.getFallasByCategoria("ESPECIAL").first()
        
        // Then
        assertEquals(2, especiales.size)
    }
    
    @Test
    fun getPaginatedFallas() = runTest {
        // Given - 25 fallas
        val fallas = (1..25).map { createTestFalla(it.toLong(), "Falla $it") }
        fallaDao.insertFallas(fallas)
        
        // When - página 0, 10 elementos
        val page0 = fallaDao.getFallasPaginated(limit = 10, offset = 0)
        
        // Then
        assertEquals(10, page0.size)
        
        // When - página 1, 10 elementos
        val page1 = fallaDao.getFallasPaginated(limit = 10, offset = 10)
        
        // Then
        assertEquals(10, page1.size)
        assertNotEquals(page0.first().idFalla, page1.first().idFalla)
    }
    
    @Test
    fun updateFalla() = runTest {
        // Given
        val falla = createTestFalla(1, "Falla Original")
        fallaDao.insertFalla(falla)
        
        // When
        val updated = falla.copy(nombre = "Falla Actualizada")
        fallaDao.updateFalla(updated)
        val retrieved = fallaDao.getFallaByIdOnce(1)
        
        // Then
        assertEquals("Falla Actualizada", retrieved?.nombre)
    }
    
    @Test
    fun deleteFalla() = runTest {
        // Given
        val falla = createTestFalla(1, "Falla A Eliminar")
        fallaDao.insertFalla(falla)
        
        // When
        fallaDao.deleteFalla(falla)
        val retrieved = fallaDao.getFallaByIdOnce(1)
        
        // Then
        assertNull(retrieved)
    }
    
    @Test
    fun getTotalFallas() = runTest {
        // Given
        val fallas = (1..15).map { createTestFalla(it.toLong(), "Falla $it") }
        fallaDao.insertFallas(fallas)
        
        // When
        val total = fallaDao.getTotalFallas()
        
        // Then
        assertEquals(15, total)
    }
    
    // ====== Helper para crear fallas de test ======
    
    private fun createTestFalla(
        id: Long,
        nombre: String,
        categoria: Categoria = Categoria.PRIMERA_A
    ) = FallaEntity(
        idFalla = id,
        nombre = nombre,
        seccion = "1A",
        categoria = categoria,
        presidente = "Test Presidente",
        fallera = "Test Fallera",
        artista = "Test Artista",
        anyoFundacion = 2000,
        lema = "Test Lema",
        descripcion = "Test descripción",
        distintivo = "Test",
        experim = false,
        latitud = 39.4699,
        longitud = -0.3763,
        webOficial = "https://test.com",
        telefonoContacto = "+34961234567",
        emailContacto = "test@test.com",
        urlBoceto = "https://test.com/boceto.jpg",
        totalEventos = 0,
        totalNinots = 0,
        totalMiembros = 0,
        fechaCreacion = LocalDateTime.now(),
        fechaActualizacion = LocalDateTime.now()
    )
}
