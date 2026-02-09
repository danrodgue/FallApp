package com.fallapp.features.fallas.domain.model

/**
 * Tipos de voto disponibles para fallas/ninots.
 * 
 * Según la API, los tipos válidos son:
 * - INGENIOSO: Falla/ninot con mensaje ingenioso
 * - CRITICO: Falla/ninot con crítica social
 * - ARTISTICO: Falla/ninot con valor artístico
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
enum class TipoVoto {
    INGENIOSO,
    CRITICO,
    ARTISTICO;
    
    /**
     * Obtiene el nombre para mostrar en UI.
     */
    fun getDisplayName(): String = when (this) {
        INGENIOSO -> "🏆 Mejor Falla"
        CRITICO -> "😄 Ingenio y Gracia"
        ARTISTICO -> "🧪 Mejor Experimental"
    }
    
    /**
     * Obtiene la descripción del tipo de voto.
     */
    fun getDescription(): String = when (this) {
        INGENIOSO -> "Reconoce a la mejor falla en conjunto"
        CRITICO -> "Premia el ingenio y la gracia"
        ARTISTICO -> "Destaca las propuestas más experimentales"
    }
}
