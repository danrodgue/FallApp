package com.fallapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Evento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoDTO {
    
    @JsonProperty("id_evento")
    private Long idEvento;
    
    @NotNull(message = "El ID de la falla es obligatorio")
    @JsonProperty("id_falla")
    private Long idFalla;
    
    private String nombreFalla;
    
    @NotBlank(message = "El tipo de evento es obligatorio")
    private String tipo;
    
    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;
    
    private String descripcion;
    
    @NotNull(message = "La fecha del evento es obligatoria")
    @JsonProperty("fecha_evento")
    private LocalDateTime fechaEvento;
    
    private String ubicacion;
    
    private Integer participantesEstimado;
    
    @JsonProperty("creado_por")
    private Long creadoPor;
    
    @JsonProperty("fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    private String imagenNombre;
}    
