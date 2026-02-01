package com.fallapp.dto;

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
    private String nombre;
    private String seccion;
    private String presidente;
    private String artista;
    private String lema;
    private Integer anyoFundacion;
    private Double latitud;
    private Double longitud;
    private String categoria;
    private Integer totalEventos;
    private Integer totalNinots;
    private Integer totalMiembros;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
