package com.fallapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Voto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotoDTO {
    private Long idVoto;
    private Long idUsuario;
    private String nombreUsuario;
    private Long idNinot;
    private String nombreNinot;
    private String tipoVoto;
    private LocalDateTime fechaCreacion;
}
