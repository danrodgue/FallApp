package com.fallapp.core.network

import kotlinx.serialization.Serializable

/**
 * DTO genérico para respuestas paginadas de la API.
 * 
 * Estructura devuelta por endpoints como:
 * - GET /api/fallas
 * - GET /api/eventos
 * - GET /api/ninots
 * 
 * @param T tipo del contenido paginado
 */
@Serializable
data class PaginatedResponse<T>(
    val contenido: List<T>,
    val paginaActual: Int,
    val elementosPorPagina: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val esUltimaPagina: Boolean
)
