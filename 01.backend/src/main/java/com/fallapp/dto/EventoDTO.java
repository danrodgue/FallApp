package com.fallapp.dto;

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
    private Long idEvento;
    
    @NotNull(message = "El ID de la falla es obligatorio")
    private Long idFalla;
    
    private String nombreFalla;
    
    @NotBlank(message = "El tipo de evento es obligatorio")
    private String tipo;
    
    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;
    
    private String descripcion;
    
    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDateTime fechaEvento;
    
    private String ubicacion;
    
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;
    
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String urlImagen;
    
    @Min(value = 0, message = "Los participantes estimados no pueden ser negativos")
    private Integer participantesEstimado;
    
    // Campos de auditoría (solo lectura)
    private UsuarioSimpleDTO creadoPor;
    private LocalDateTime fechaCreacion;
    private LocalDateTime actualizadoEn;
    
    /**
     * DTO simplificado para usuario (para campos de auditoría)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioSimpleDTO {
        private Long id;
        private String nombreCompleto;
        private String email;
    }
}
