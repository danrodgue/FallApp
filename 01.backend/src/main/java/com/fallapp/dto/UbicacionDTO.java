package com.fallapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para datos de ubicación geográfica de una falla
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ubicación geográfica de una falla")
public class UbicacionDTO {
    
    @Schema(description = "ID de la falla", example = "1")
    private Long idFalla;
    
    @Schema(description = "Nombre de la falla", example = "Convento Jerusalén")
    private String nombre;
    
    @Schema(description = "Latitud GPS", example = "39.4699075")
    private Double latitud;
    
    @Schema(description = "Longitud GPS", example = "-0.3763242")
    private Double longitud;
    
    @Schema(description = "Indica si la falla tiene ubicación GPS disponible", example = "true")
    private Boolean tieneUbicacion;
}
