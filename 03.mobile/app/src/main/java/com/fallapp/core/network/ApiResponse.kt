package com.fallapp.core.network

import kotlinx.serialization.Serializable

/**
 * Formato estándar de respuesta de la API REST de FallApp.
 * 
 * Todas las respuestas del backend siguen este formato:
 * ```json
 * {
 *   "exito": true,
 *   "mensaje": "Operación exitosa",
 *   "datos": { ... },
 *   "timestamp": "2026-02-01T18:30:00"
 * }
 * ```
 * 
 * @param T Tipo de datos contenidos en la respuesta
 * @property exito Indica si la operación fue exitosa
 * @property mensaje Mensaje descriptivo de la operación
 * @property datos Datos de respuesta (null en caso de error)
 * @property timestamp Timestamp ISO 8601 de la respuesta
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
@Serializable
data class ApiResponse<T>(
    val exito: Boolean,
    val mensaje: String? = null,
    val datos: T? = null,
    val timestamp: String
)

/**
 * Verifica si la respuesta fue exitosa y contiene datos.
 * 
 * @return true si exito=true y datos!=null
 */
fun <T> ApiResponse<T>.isSuccessful(): Boolean = exito && datos != null

/**
 * Obtiene los datos o lanza excepción con el mensaje de error.
 * 
 * @return Datos de la respuesta
 * @throws ApiException si la operación falló
 */
fun <T> ApiResponse<T>.getDataOrThrow(): T {
    if (!exito) {
        throw ApiException(mensaje ?: "Error desconocido en la API")
    }
    return datos ?: throw ApiException("Respuesta sin datos")
}

/**
 * Excepción personalizada para errores de API.
 * 
 * @param message Mensaje de error devuelto por la API
 */
class ApiException(message: String) : Exception(message)
