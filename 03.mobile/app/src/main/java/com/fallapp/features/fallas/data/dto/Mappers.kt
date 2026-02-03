package com.fallapp.features.fallas.data.dto

import com.fallapp.core.database.entity.FallaEntity
import com.fallapp.features.fallas.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.fallapp.core.database.Categoria as EntityCategoria

/**
 * Extensiones para mapear entre DTOs, Entities y Domain Models.
 * Ajustado a la estructura real de FallaEntity.
 */

// DTO -> Domain Model
fun FallaDto.toDomain(): Falla {
    return Falla(
        idFalla = idFalla,
        nombre = nombre,
        seccion = seccion,
        categoria = Categoria.fromString(categoria),
        ubicacion = Ubicacion(
            direccion = direccion,
            ciudad = ciudad,
            provincia = provincia,
            codigoPostal = codigoPostal,
            latitud = latitud,
            longitud = longitud
        ),
        descripcion = descripcion,
        historia = historia,
        imagenes = imagenes ?: emptyList(),
        contacto = if (hasContactInfo()) {
            Contacto(
                telefono = telefono,
                email = email,
                web = web,
                facebook = facebook,
                twitter = twitter,
                instagram = instagram
            )
        } else null,
        estadisticas = if (hasStatistics()) {
            Estadisticas(
                numeroSocios = numeroSocios ?: 0,
                numeroNinots = numeroNinots ?: 0,
                numeroEventos = numeroEventos ?: 0,
                presupuestoTotal = presupuestoTotal,
                anyoFundacion = anyoFundacion
            )
        } else null,
        fechaCreacion = fechaCreacion?.let { parseDateTime(it) },
        activa = activa
    )
}

// DTO -> Entity (para guardar en Room)
fun FallaDto.toEntity(): FallaEntity {
    val now = LocalDateTime.now()
    return FallaEntity(
        idFalla = idFalla,
        nombre = nombre,
        seccion = seccion,
        categoria = mapDomainCategoriaToEntity(Categoria.fromString(categoria)),
        presidente = null,
        fallera = null,
        artista = null,
        anyoFundacion = anyoFundacion,
        lema = null,
        descripcion = descripcion,
        distintivo = null,
        experim = false,
        latitud = latitud ?: 0.0,
        longitud = longitud ?: 0.0,
        webOficial = web,
        telefonoContacto = telefono,
        emailContacto = email,
        urlBoceto = null,
        totalEventos = numeroEventos ?: 0,
        totalNinots = numeroNinots ?: 0,
        totalMiembros = numeroSocios ?: 0,
        fechaCreacion = fechaCreacion?.let { parseDateTime(it) } ?: now,
        fechaActualizacion = now,
        lastSyncTime = now
    )
}

// Entity -> Domain Model
fun FallaEntity.toDomain(): Falla {
    return Falla(
        idFalla = idFalla,
        nombre = nombre,
        seccion = seccion,
        categoria = mapEntityCategoriaToDomain(categoria),
        ubicacion = Ubicacion(
            direccion = null,
            ciudad = seccion,
            provincia = "Valencia",
            codigoPostal = null,
            latitud = latitud,
            longitud = longitud
        ),
        descripcion = descripcion,
        historia = null,
        imagenes = emptyList(),
        contacto = if (telefonoContacto != null || emailContacto != null || webOficial != null) {
            Contacto(
                telefono = telefonoContacto,
                email = emailContacto,
                web = webOficial,
                facebook = null,
                twitter = null,
                instagram = null
            )
        } else null,
        estadisticas = Estadisticas(
            numeroSocios = totalMiembros,
            numeroNinots = totalNinots,
            numeroEventos = totalEventos,
            presupuestoTotal = null,
            anyoFundacion = anyoFundacion
        ),
        fechaCreacion = fechaCreacion,
        activa = true
    )
}

// Helpers privados
private fun FallaDto.hasContactInfo(): Boolean {
    return telefono != null || email != null || web != null ||
            facebook != null || twitter != null || instagram != null
}

private fun FallaDto.hasStatistics(): Boolean {
    return numeroSocios != null || numeroNinots != null ||
            numeroEventos != null || presupuestoTotal != null ||
            anyoFundacion != null
}

private fun parseDateTime(dateString: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }
}

// Mapeo entre enums Domain y Entity
private fun mapDomainCategoriaToEntity(domainCategoria: Categoria): EntityCategoria {
    return when (domainCategoria) {
        Categoria.ESPECIAL -> EntityCategoria.ESPECIAL
        Categoria.PRIMERA -> EntityCategoria.PRIMERA_A
        Categoria.SEGUNDA -> EntityCategoria.SEGUNDA_A
        Categoria.TERCERA -> EntityCategoria.TERCERA_A
        Categoria.INFANTIL -> EntityCategoria.INFANTIL_PRIMERA
    }
}

private fun mapEntityCategoriaToDomain(entityCategoria: EntityCategoria): Categoria {
    return when (entityCategoria) {
        EntityCategoria.ESPECIAL -> Categoria.ESPECIAL
        EntityCategoria.PRIMERA_A, EntityCategoria.PRIMERA_B -> Categoria.PRIMERA
        EntityCategoria.SEGUNDA_A, EntityCategoria.SEGUNDA_B -> Categoria.SEGUNDA
        EntityCategoria.TERCERA_A, EntityCategoria.TERCERA_B -> Categoria.TERCERA
        EntityCategoria.CUARTA, EntityCategoria.QUINTA -> Categoria.TERCERA
        EntityCategoria.INFANTIL_ESPECIAL, EntityCategoria.INFANTIL_PRIMERA -> Categoria.INFANTIL
    }
}
