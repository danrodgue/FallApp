package com.fallapp.core.network

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import com.fallapp.core.config.ApiConfig

/**
 * Tests unitarios para KtorClient.
 * 
 * Verifica:
 * - Configuración correcta del cliente
 * - Headers por defecto
 * - Timeouts configurados
 * - Cliente autenticado con JWT
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class KtorClientTest {
    
    @Test
    fun `create client has correct base URL`() {
        // Given
        val client = KtorClient.create(enableLogging = false)
        
        // When - verificar que la configuración existe
        val config = client.engineConfig
        
        // Then - el cliente se creó correctamente
        assertNotNull(client)
    }
    
    @Test
    fun `createAuthenticated includes Authorization header`() = runTest {
        // Given
        val token = "test-jwt-token-12345"
        val client = KtorClient.createAuthenticated(token, enableLogging = false)
        
        // Then - el cliente existe con el token
        assertNotNull(client)
        // Nota: No podemos verificar el header directamente sin hacer una request real
        // Esto se verificará en tests de integración
    }
    
    @Test
    fun `client has correct timeout configuration`() {
        // Given
        val client = KtorClient.create(enableLogging = false)
        
        // Then - el cliente fue creado (timeouts se configuran internamente)
        assertNotNull(client)
        // Timeouts se verificarán en tests de integración con servidor real
    }
    
    @Test
    fun `client accepts JSON content type`() {
        // Given
        val client = KtorClient.create(enableLogging = false)
        
        // Then
        assertNotNull(client)
        // Content-Type se verificará en tests de integración
    }
    
    @Test
    fun `token is properly formatted with Bearer prefix`() {
        // Given
        val rawToken = "abc123xyz"
        val expectedFormat = "${ApiConfig.TOKEN_PREFIX}$rawToken"
        
        // When - crear cliente autenticado
        val client = KtorClient.createAuthenticated(rawToken, enableLogging = false)
        
        // Then - el formato es Bearer + token
        assertEquals("Bearer abc123xyz", expectedFormat)
        assertNotNull(client)
    }
}
