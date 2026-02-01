package com.fallapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper para respuestas paginadas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> contenido;
    private Integer paginaActual;
    private Integer elementosPorPagina;
    private Long totalElementos;
    private Integer totalPaginas;
    private Boolean esUltimaPagina;
}
