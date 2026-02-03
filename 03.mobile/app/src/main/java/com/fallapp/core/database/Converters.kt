package com.fallapp.core.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Conversores de tipos para Room Database.
 * 
 * Room no soporta nativamente ciertos tipos complejos (LocalDateTime, List, Enum),
 * así que los convertimos a tipos primitivos que Room entiende.
 * 
 * Conversiones:
 * - LocalDateTime <-> String (ISO 8601)
 * - List<String> <-> String (JSON array)
 * - Enums <-> String (name)
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class Converters {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    // ====== LocalDateTime ======
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            LocalDateTime.parse(it, dateTimeFormatter)
        }
    }
    
    // ====== List<String> ======
    
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toStringList(listString: String?): List<String>? {
        return listString?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // ====== Categoria Enum ======
    
    @TypeConverter
    fun fromCategoria(categoria: Categoria?): String? {
        return categoria?.name
    }
    
    @TypeConverter
    fun toCategoria(categoriaString: String?): Categoria? {
        return categoriaString?.let {
            try {
                Categoria.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
    
    // ====== TipoEvento Enum ======
    
    @TypeConverter
    fun fromTipoEvento(tipo: TipoEvento?): String? {
        return tipo?.name
    }
    
    @TypeConverter
    fun toTipoEvento(tipoString: String?): TipoEvento? {
        return tipoString?.let {
            try {
                TipoEvento.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
    
    // ====== TipoVoto Enum ======
    
    @TypeConverter
    fun fromTipoVoto(tipo: TipoVoto?): String? {
        return tipo?.name
    }
    
    @TypeConverter
    fun toTipoVoto(tipoString: String?): TipoVoto? {
        return tipoString?.let {
            try {
                TipoVoto.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
    
    // ====== Rol Enum ======
    
    @TypeConverter
    fun fromRol(rol: Rol?): String? {
        return rol?.name
    }
    
    @TypeConverter
    fun toRol(rolString: String?): Rol? {
        return rolString?.let {
            try {
                Rol.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

// ====== Enums (definidos aquí para Converters) ======

enum class Categoria {
    ESPECIAL,
    PRIMERA_A,
    PRIMERA_B,
    SEGUNDA_A,
    SEGUNDA_B,
    TERCERA_A,
    TERCERA_B,
    CUARTA,
    QUINTA,
    INFANTIL_ESPECIAL,
    INFANTIL_PRIMERA
}

enum class TipoEvento {
    PLANTA,
    CREMA,
    OFRENDA,
    DESFILE,
    CENA,
    FIESTA,
    MASCLETA,
    CASTILLO,
    PROCLAMACION,
    EXALTACION,
    PAELLA,
    OTRO
}

enum class TipoVoto {
    INGENIOSO,
    CRITICO,
    ARTISTICO
}

enum class Rol {
    FALLERO,
    ADMIN,
    CASAL
}
