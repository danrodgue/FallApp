package com.fallapp.core.util

/**
 * Extensiones útiles para tipos comunes.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */

// ============================================================
// STRING EXTENSIONS
// ============================================================

/**
 * Valida si un string es un email válido.
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Capitaliza la primera letra de cada palabra.
 * Ejemplo: "falla benimaclet" → "Falla Benimaclet"
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

/**
 * Trunca un string a una longitud máxima añadiendo "..." al final.
 */
fun String.truncate(maxLength: Int): String {
    return if (length <= maxLength) this else "${take(maxLength - 3)}..."
}

// ============================================================
// DOUBLE EXTENSIONS (para coordenadas GPS)
// ============================================================

/**
 * Formatea coordenadas con 6 decimales de precisión.
 * Ejemplo: 39.476776 → "39.476776"
 */
fun Double.toCoordinateString(): String {
    return "%.6f".format(this)
}

// ============================================================
// LIST EXTENSIONS
// ============================================================

/**
 * Retorna una lista inmutable vacía si la lista es null.
 */
fun <T> List<T>?.orEmpty(): List<T> {
    return this ?: emptyList()
}

/**
 * Divide una lista en chunks de tamaño específico.
 * Útil para paginación.
 */
fun <T> List<T>.chunkedSafe(size: Int): List<List<T>> {
    return if (size > 0) chunked(size) else listOf(this)
}
