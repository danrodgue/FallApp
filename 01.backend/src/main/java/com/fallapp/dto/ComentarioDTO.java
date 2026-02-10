package com.fallapp.dto;

import jakarta.validation.constraints.*;
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
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long idUsuario;
    
    private String nombreUsuario;
    
    private Long idFalla;
    private String nombreFalla;
    
    // `ninots` eliminado: comentarios se asocian solo a `idFalla`
    
    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 3, max = 500, message = "El comentario debe tener entre 3 y 500 caracteres")
    private String contenido;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;}