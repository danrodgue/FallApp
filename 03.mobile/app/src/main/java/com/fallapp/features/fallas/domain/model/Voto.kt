package com.fallapp.features.fallas.domain.model

import java.time.LocalDateTime

/**
 * Modelo de dominio para un voto.
 * 
 * Los votos se registran en fallas (a través de ninots) y tienen un tipo específico.
 * Un usuario solo puede votar 1 vez por tipo por falla.
 * 
 * @property idVoto ID único del voto
 * @property idUsuario ID del usuario que votó
 * @property nombreUsuario Nombre completo del usuario
 * @property idFalla ID de la falla votada
 * @property nombreFalla Nombre de la falla
 * @property tipoVoto Tipo de voto (INGENIOSO, CRITICO, ARTISTICO)
 * @property fechaCreacion Fecha y hora del voto
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
data class Voto(
    val idVoto: Long,
    val idUsuario: Long,
    val nombreUsuario: String,
    val idFalla: Long,
    val nombreFalla: String,
    val tipoVoto: TipoVoto,
    val fechaCreacion: LocalDateTime? = null
)

/**
 * Request para crear un voto.
 * 
 * @property idNinot ID del ninot a votar (internamente se vota la falla asociada)
 * @property tipoVoto Tipo de voto
 */
data class VotoRequest(
    val idNinot: Long,
    val tipoVoto: TipoVoto
)

/**
 * Estadísticas de votos de una falla.
 * 
 * @property totalVotos Total de votos recibidos
 * @property votosIngenioso Votos de tipo INGENIOSO
 * @property votosCritico Votos de tipo CRITICO
 * @property votosArtistico Votos de tipo ARTISTICO
 */
data class EstadisticasVotos(
    val totalVotos: Int,
    val votosIngenioso: Int,
    val votosCritico: Int,
    val votosArtistico: Int
)
