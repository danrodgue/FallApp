package com.fallapp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Ninot (Versión Simplificada v2.0)
 * 
 * @version 2.0
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NinotDTO {
    
    // Identificación
    private Long idNinot;
    
    @NotNull(message = "El ID de la falla es obligatorio")
    private Long idFalla;
    
    private String nombreFalla;  // Incluido para respuestas
    
    // Información básica
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;
    
    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Pattern(
        regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
        message = "URL de imagen inválida"
    )
    private String urlImagen;
    
    // Auditoría
    private LocalDateTime fechaCreacion;
}
