package com.fallapp.features.fallas.domain.model

/**
 * Modelo de dominio para una entrada de ranking de fallas.
 */
data class FallaRanking(
    val idFalla: Long,
    val nombre: String,
    val seccion: String?,
    val votos: Int
)

