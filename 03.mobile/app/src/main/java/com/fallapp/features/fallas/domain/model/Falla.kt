package com.fallapp.features.fallas.domain.model

import java.time.LocalDateTime

/**
 * Modelo de dominio para una Falla.
 * Representa los datos esenciales de una falla desde la perspectiva del negocio.
 */
data class Falla(
    val idFalla: Long,
    val nombre: String,
    val seccion: String,
    val categoria: Categoria,
    val ubicacion: Ubicacion,
    val descripcion: String?,
    val historia: String?,
    val imagenes: List<String>,
    val contacto: Contacto?,
    val estadisticas: Estadisticas?,
    val fechaCreacion: LocalDateTime?,
    val activa: Boolean
)

data class Ubicacion(
    val direccion: String?,
    val ciudad: String,
    val provincia: String,
    val codigoPostal: String?,
    val latitud: Double?,
    val longitud: Double?
)

data class Contacto(
    val telefono: String?,
    val email: String?,
    val web: String?,
    val facebook: String?,
    val twitter: String?,
    val instagram: String?
)

data class Estadisticas(
    val numeroSocios: Int,
    val numeroNinots: Int,
    val numeroEventos: Int,
    val presupuestoTotal: Double?,
    val anyoFundacion: Int?
)

enum class Categoria {
    ESPECIAL,
    PRIMERA,
    SEGUNDA,
    TERCERA,
    INFANTIL;

    companion object {
        fun fromString(value: String?): Categoria {
            return when (value?.uppercase()) {
                "ESPECIAL" -> ESPECIAL
                "PRIMERA", "PRIMERA_A", "PRIMERA_B" -> PRIMERA
                "SEGUNDA", "SEGUNDA_A", "SEGUNDA_B" -> SEGUNDA
                "TERCERA", "TERCERA_A", "TERCERA_B" -> TERCERA
                "INFANTIL" -> INFANTIL
                else -> TERCERA
            }
        }
    }
}
