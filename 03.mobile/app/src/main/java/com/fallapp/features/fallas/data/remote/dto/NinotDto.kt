package com.fallapp.features.fallas.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO mínimo para ninots, usado solo para obtener idNinot desde la API.
 * El backend puede devolver muchos más campos, que se ignoran gracias a ignoreUnknownKeys.
 */
@Serializable
data class NinotDto(
    val idNinot: Long
)

