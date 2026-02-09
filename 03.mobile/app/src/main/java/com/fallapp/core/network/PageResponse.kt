package com.fallapp.core.network

import kotlinx.serialization.Serializable

/**
 * DTO genérico para respuestas paginadas basadas en Spring Data (`content`, `empty`, etc.).
 *
 * Ejemplo típico:
 * {
 *   "content": [ ... ],
 *   "empty": true,
 *   "first": true,
 *   "last": true,
 *   "number": 0,
 *   "numberOfElements": 0,
 *   "size": 20,
 *   "totalElements": 0,
 *   "totalPages": 0
 * }
 */
@Serializable
data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val empty: Boolean = true,
    val first: Boolean = true,
    val last: Boolean = true,
    val number: Int = 0,
    val numberOfElements: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0
)

