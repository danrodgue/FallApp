package com.fallapp.core.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Utilidades para formatear y manipular fechas/horas.
 * 
 * Proporciona métodos consistentes para:
 * - Formatear LocalDateTime a strings legibles
 * - Calcular diferencias de tiempo ("hace 2 horas")
 * - Validar si un evento es pasado/futuro
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
object DateTimeUtils {
    
    // Formateadores predefinidos
    private val FORMATTER_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("es", "ES"))
    private val FORMATTER_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))
    private val FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))
    private val FORMATTER_ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    /**
     * Formatea una fecha completa: "01/03/2026 19:00"
     */
    fun formatFull(dateTime: LocalDateTime): String {
        return dateTime.format(FORMATTER_FULL)
    }
    
    /**
     * Formatea solo la fecha: "01/03/2026"
     */
    fun formatDate(dateTime: LocalDateTime): String {
        return dateTime.format(FORMATTER_DATE)
    }
    
    /**
     * Formatea solo la hora: "19:00"
     */
    fun formatTime(dateTime: LocalDateTime): String {
        return dateTime.format(FORMATTER_TIME)
    }
    
    /**
     * Formatea a ISO-8601 para enviar al API: "2026-03-01T19:00:00"
     */
    fun formatIso(dateTime: LocalDateTime): String {
        return dateTime.format(FORMATTER_ISO)
    }
    
    /**
     * Parsea un string ISO-8601 a LocalDateTime.
     * @throws DateTimeParseException si el formato es inválido
     */
    fun parseIso(isoString: String): LocalDateTime {
        return LocalDateTime.parse(isoString, FORMATTER_ISO)
    }
    
    /**
     * Retorna tiempo relativo: "hace 2 horas", "en 3 días", etc.
     * @param dateTime Fecha a comparar con el momento actual
     * @return String legible del tiempo relativo
     */
    fun timeAgo(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        
        return when {
            dateTime.isAfter(now) -> {
                // Fecha futura
                val days = ChronoUnit.DAYS.between(now, dateTime)
                val hours = ChronoUnit.HOURS.between(now, dateTime)
                val minutes = ChronoUnit.MINUTES.between(now, dateTime)
                
                when {
                    days > 0 -> "en $days ${if (days == 1L) "día" else "días"}"
                    hours > 0 -> "en $hours ${if (hours == 1L) "hora" else "horas"}"
                    minutes > 0 -> "en $minutes ${if (minutes == 1L) "minuto" else "minutos"}"
                    else -> "ahora mismo"
                }
            }
            else -> {
                // Fecha pasada
                val days = ChronoUnit.DAYS.between(dateTime, now)
                val hours = ChronoUnit.HOURS.between(dateTime, now)
                val minutes = ChronoUnit.MINUTES.between(dateTime, now)
                
                when {
                    days > 7 -> formatDate(dateTime) // Más de una semana: mostrar fecha
                    days > 0 -> "hace $days ${if (days == 1L) "día" else "días"}"
                    hours > 0 -> "hace $hours ${if (hours == 1L) "hora" else "horas"}"
                    minutes > 0 -> "hace $minutes ${if (minutes == 1L) "minuto" else "minutos"}"
                    else -> "ahora mismo"
                }
            }
        }
    }
    
    /**
     * Verifica si la fecha es pasada.
     */
    fun isPast(dateTime: LocalDateTime): Boolean {
        return dateTime.isBefore(LocalDateTime.now())
    }
    
    /**
     * Verifica si la fecha es futura.
     */
    fun isFuture(dateTime: LocalDateTime): Boolean {
        return dateTime.isAfter(LocalDateTime.now())
    }
    
    /**
     * Verifica si el evento está próximo (dentro de las próximas 24 horas).
     */
    fun isUpcoming(dateTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        val hoursUntil = ChronoUnit.HOURS.between(now, dateTime)
        return hoursUntil in 0..24
    }
}
