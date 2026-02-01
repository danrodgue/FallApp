package com.fallapp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferencia de datos de Ninot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NinotDTO {
    private Long idNinot;
    
    @NotNull(message = "El ID de la falla es obligatorio")
    private Long idFalla;
    
    private String nombreFalla;
    
    @NotBlank(message = "El nombre del ninot es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombreNinot;
    
    private String tituloObra;
    
    @DecimalMin(value = "0.1", message = "La altura debe ser mayor que 0")
    private Double altura;
    
    @DecimalMin(value = "0.1", message = "El ancho debe ser mayor que 0")
    private Double ancho;
    
    private List<String> imagenes;
    private Boolean premiado;
    private Integer totalVotos;
    private Integer votosIngenioso;
    private Integer votosCritico;
    private Integer votosArtistico;
    private LocalDateTime fechaCreacion;
}
