package com.fallapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Comentario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDTO {
    private Long idComentario;
    private Long idUsuario;
    private String nombreUsuario;
    private Long idFalla;
    private String nombreFalla;
    private Long idNinot;
    private String nombreNinot;
    private String contenido;
    private LocalDateTime fechaCreacion;
}
