package com.fallapp.dto;

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
    private Long idFalla;
    private String nombreFalla;
    private String tipo;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaEvento;
    private String ubicacion;
    private Integer participantesEstimado;
    private LocalDateTime fechaCreacion;
    private String creadoPor;
}
