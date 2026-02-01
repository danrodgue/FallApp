package com.fallapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un nuevo voto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearVotoRequest {
    @NotNull(message = "El ID del ninot es obligatorio")
    private Long idNinot;

    @NotBlank(message = "El tipo de voto es obligatorio")
    @Pattern(regexp = "favorito|ingenioso|critico|artistico|rating", 
             message = "Tipo de voto debe ser: favorito, ingenioso, critico, artistico o rating")
    private String tipoVoto;
}
