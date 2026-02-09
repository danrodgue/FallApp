package com.fallapp.features.fallas.data.mapper

import com.fallapp.core.database.entity.FallaEntity
import com.fallapp.features.fallas.data.remote.dto.FallaDto
import com.fallapp.features.fallas.data.remote.dto.VotoDto
import com.fallapp.features.fallas.data.remote.dto.VotoRequestDto
import com.fallapp.features.fallas.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.fallapp.core.database.Categoria as EntityCategoria

/**
 * Extensiones para mapear entre DTOs, Entities y Domain Models.
 * Ubicadas en data/mapper para mantener separado DTO → Domain.
 */

// DTO -> Domain Model
fun FallaDto.toDomain(): Falla {
    return Falla(
        idFalla = idFalla,
        nombre = nombre,
        seccion = seccion,
        categoria = Categoria.fromString(categoria ?: "sin_categoria"),
        ubicacion = Ubicacion(
            direccion = null,
            ciudad = "Valencia",
            provincia = "Valencia",
            codigoPostal = null,
            latitud = latitud,
            longitud = longitud
        ),
        descripcion = descripcion,
        historia = null,
        imagenes = listOfNotNull(urlBoceto),
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
        activa = true
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
        imagenes = listOfNotNull(urlBoceto),
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

// ========== MAPPERS DE VOTOS ==========

// DTO -> Domain Model
fun VotoDto.toDomain(): Voto {
    return Voto(
        idVoto = idVoto,
        idUsuario = idUsuario,
        nombreUsuario = nombreUsuario,
        idFalla = idFalla,
        nombreFalla = nombreFalla,
        tipoVoto = when (tipoVoto.uppercase()) {
            "MONUMENTO" -> TipoVoto.INGENIOSO          // Mejor Falla
            "INGENIO_Y_GRACIA" -> TipoVoto.CRITICO     // Ingenio y Gracia
            "EXPERIMENTAL" -> TipoVoto.ARTISTICO       // Mejor Experimental
            else -> TipoVoto.INGENIOSO
        },
        fechaCreacion = parseDateTime(fechaCreacion)
    )
}

// Domain Model -> DTO Request
fun VotoRequest.toDto(): VotoRequestDto {
    return VotoRequestDto(
        idNinot = idNinot,
        tipoVoto = when (tipoVoto) {
            // Mapear nombres de dominio a constantes de la API
            TipoVoto.INGENIOSO -> "MONUMENTO"
            TipoVoto.CRITICO -> "INGENIO_Y_GRACIA"
            TipoVoto.ARTISTICO -> "EXPERIMENTAL"
        }
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

