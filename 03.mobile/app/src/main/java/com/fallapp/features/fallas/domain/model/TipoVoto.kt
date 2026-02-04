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
        INGENIOSO -> "😄 Ingenioso"
        CRITICO -> "💭 Crítico"
        ARTISTICO -> "🎨 Artístico"
    }
    
    /**
     * Obtiene la descripción del tipo de voto.
     */
    fun getDescription(): String = when (this) {
        INGENIOSO -> "Mensaje ingenioso y creativo"
        CRITICO -> "Crítica social relevante"
        ARTISTICO -> "Gran valor artístico"
    }
}
