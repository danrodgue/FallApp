package com.fallapp.dto;

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
    private Long idFalla;
    private String nombreFalla;
    private String nombreNinot;
    private String tituloObra;
    private Double altura;
    private Double ancho;
    private List<String> imagenes;
    private Boolean premiado;
    private Integer totalVotos;
    private Integer votosIngenioso;
    private Integer votosCritico;
    private Integer votosArtistico;
    private LocalDateTime fechaCreacion;
}
