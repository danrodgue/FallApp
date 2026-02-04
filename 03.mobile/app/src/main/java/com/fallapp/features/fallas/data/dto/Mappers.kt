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
        categoria = Categoria.fromString(categoria ?: "sin_categoria"),
        ubicacion = Ubicacion(
            direccion = null,  // No viene en la API
            ciudad = "Valencia",  // Por defecto Valencia
            provincia = "Valencia",
            codigoPostal = null,
            latitud = latitud,
            longitud = longitud
        ),
        descripcion = descripcion,
        historia = null,  // No viene en la API actual
        imagenes = emptyList(),  // No viene en la API actual
        contacto = if (hasContactInfo()) {
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
        fechaCreacion = fechaCreacion?.let { parseDateTime(it) },
        activa = true  // Por defecto activa
    )
}

// DTO -> Entity (para guardar en Room)
fun FallaDto.toEntity(): FallaEntity {
    val now = LocalDateTime.now()
    return FallaEntity(
        idFalla = idFalla,
        nombre = nombre,
        seccion = seccion,
        categoria = mapDomainCategoriaToEntity(Categoria.fromString(categoria ?: "sin_categoria")),
        presidente = presidente ?: "Desconocido",
        fallera = fallera,
        artista = artista,
        anyoFundacion = anyoFundacion ?: 0,
        lema = lema,
        descripcion = descripcion,
        distintivo = distintivo,
        experim = experim ?: false,
        latitud = latitud ?: 0.0,
        longitud = longitud ?: 0.0,
        webOficial = webOficial,
        telefonoContacto = telefonoContacto,
        emailContacto = emailContacto,
        urlBoceto = urlBoceto,
        totalEventos = totalEventos,
        totalNinots = totalNinots,
        totalMiembros = totalMiembros,
        fechaCreacion = fechaCreacion?.let { parseDateTime(it) } ?: now,
        fechaActualizacion = fechaActualizacion?.let { parseDateTime(it) } ?: now,
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
    return telefonoContacto != null || emailContacto != null || webOficial != null
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

// ========== MAPPERS DE VOTOS ==========

// DTO -> Domain Model
fun VotoDto.toDomain(): com.fallapp.features.fallas.domain.model.Voto {
    return com.fallapp.features.fallas.domain.model.Voto(
        idVoto = idVoto,
        idUsuario = idUsuario,
        nombreUsuario = nombreUsuario,
        idFalla = idFalla,
        nombreFalla = nombreFalla,
        tipoVoto = com.fallapp.features.fallas.domain.model.TipoVoto.valueOf(tipoVoto),
        fechaCreacion = parseDateTime(fechaCreacion)
    )
}

// Domain Model -> DTO Request
fun com.fallapp.features.fallas.domain.model.VotoRequest.toDto(): VotoRequestDto {
    return VotoRequestDto(
        idNinot = idNinot,
        tipoVoto = tipoVoto.name
    )
}

// ========== MAPEO ENTRE ENUMS ==========

private fun mapDomainCategoriaToEntity(domainCategoria: Categoria): EntityCategoria {
    return when (domainCategoria) {
        Categoria.ESPECIAL -> EntityCategoria.ESPECIAL
        Categoria.PRIMERA -> EntityCategoria.PRIMERA_A
        Categoria.SEGUNDA -> EntityCategoria.SEGUNDA_A
        Categoria.TERCERA -> EntityCategoria.TERCERA_A
        Categoria.INFANTIL -> EntityCategoria.INFANTIL_PRIMERA
        Categoria.SIN_CATEGORIA -> EntityCategoria.SIN_CATEGORIA
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
        EntityCategoria.SIN_CATEGORIA -> Categoria.SIN_CATEGORIA
    }
}
