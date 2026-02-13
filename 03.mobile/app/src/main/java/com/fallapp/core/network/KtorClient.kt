package com.fallapp.core.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.fallapp.core.config.ApiConfig

/**
 * Cliente HTTP configurado con Ktor.
 * 
 * Características:
 * - Serialización JSON automática
 * - Logging de requests/responses (solo en DEBUG)
 * - Timeouts configurados
 * - Headers por defecto
 * - Manejo de errores
 * 
 * Uso:
 * ```kotlin
 * val client = KtorClient.create()
 * val response = client.get("${ApiConfig.API_URL}/fallas")
 * ```
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
object KtorClient {
    
    private const val TAG = "KtorClient"
    
    /**
     * Crea una instancia configurada de HttpClient.
     * 
     * @param enableLogging Si true, activa logging detallado (default: BuildConfig.DEBUG)
     * @return HttpClient configurado
     */
    fun create(enableLogging: Boolean = true): HttpClient {
        return HttpClient(Android) {
            
            // ====== JSON Serialization ======
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // Ignora campos extras del backend
                    encodeDefaults = true
                })
            }
            
            // ====== Logging ======
            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d(TAG, message)
                        }
                    }
                    level = LogLevel.ALL // TODO: Cambiar a HEADERS en producción
                }
            }
            
            // ====== Timeouts ======
            install(HttpTimeout) {
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MS
                requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MS
                socketTimeoutMillis = ApiConfig.READ_TIMEOUT_MS
            }
            
            // ====== Headers por defecto ======
            install(DefaultRequest) {
                url(ApiConfig.BASE_URL)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                
                // Headers comunes
                header("Accept-Language", "es-ES")
                header("User-Agent", "FallApp-Android/1.0")
            }
            
            // ====== Configuración del engine Android ======
            engine {
                connectTimeout = ApiConfig.CONNECT_TIMEOUT_MS.toInt()
                socketTimeout = ApiConfig.READ_TIMEOUT_MS.toInt()
            }
        }
    }
    
    /**
     * Crea un cliente con autenticación JWT.
     * 
     * @param token Token JWT del usuario autenticado
     * @param enableLogging Si true, activa logging
     * @return HttpClient con header Authorization configurado
     */
    fun createAuthenticated(token: String, enableLogging: Boolean = true): HttpClient {
        return HttpClient(Android) {
            
            // Reutilizar configuración base
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
            
            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d(TAG, message)
                        }
                    }
                    level = LogLevel.ALL
                }
            }
            
            install(HttpTimeout) {
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MS
                requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MS
                socketTimeoutMillis = ApiConfig.READ_TIMEOUT_MS
            }
            
            install(DefaultRequest) {
                url(ApiConfig.BASE_URL)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                
                // Agregar token JWT
                header(ApiConfig.AUTH_HEADER, "${ApiConfig.TOKEN_PREFIX}$token")
                header("Accept-Language", "es-ES")
                header("User-Agent", "FallApp-Android/1.0")
            }
            
            engine {
                connectTimeout = ApiConfig.CONNECT_TIMEOUT_MS.toInt()
                socketTimeout = ApiConfig.READ_TIMEOUT_MS.toInt()
            }
        }
    }
}
