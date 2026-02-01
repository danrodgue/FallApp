package com.fallapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un nuevo comentario
 * Debe incluir idFalla o idNinot (no ambos)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearComentarioRequest {
    private Long idFalla;
    private Long idNinot;

    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 3, max = 500, message = "El comentario debe tener entre 3 y 500 caracteres")
    private String contenido;
}
