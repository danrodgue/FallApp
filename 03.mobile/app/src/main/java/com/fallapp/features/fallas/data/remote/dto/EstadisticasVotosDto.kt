package com.fallapp.features.fallas.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO para la respuesta de GET /api/estadisticas/votos.
 */
@Serializable
data class FallaRankingDto(
    val idFalla: Long,
    val nombre: String,
    val seccion: String? = null,
    val votos: Long
)

@Serializable
data class EstadisticasVotosDto(
    val totalVotos: Long = 0,
    val topFallas: List<FallaRankingDto> = emptyList(),
    val filtroTipoVoto: String? = null
)

