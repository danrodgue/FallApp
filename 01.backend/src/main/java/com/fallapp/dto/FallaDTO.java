package com.fallapp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Falla
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FallaDTO {
    private Long idFalla;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;
    
    @NotBlank(message = "La sección es obligatoria")
    @Size(max = 5, message = "La sección no puede exceder 5 caracteres")
    private String seccion;
    
    private String fallera;
    
    @NotBlank(message = "El presidente es obligatorio")
    @Size(max = 255, message = "El nombre del presidente no puede exceder 255 caracteres")
    private String presidente;
    
    private String artista;
    private String lema;
    
    @NotNull(message = "El año de fundación es obligatorio")
    @Min(value = 1900, message = "El año de fundación debe ser posterior a 1900")
    private Integer anyoFundacion;
    
    private String distintivo;
    private String urlBoceto;
    private Boolean experim;
    
    @DecimalMin(value = "-90.0", message = "Latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "Latitud debe estar entre -90 y 90")
    private Double latitud;
    
    @DecimalMin(value = "-180.0", message = "Longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "Longitud debe estar entre -180 y 180")
    private Double longitud;
    
    private String descripcion;
    private String webOficial;
    private String telefonoContacto;
    
    @Email(message = "Email de contacto debe ser válido")
    private String emailContacto;
    
    private String categoria;
    private Integer totalEventos;
    private Integer totalNinots;
    private Integer totalMiembros;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
