package com.fallapp.service;

import com.fallapp.dto.FallaDTO;
import com.fallapp.dto.PaginatedResponse;
import com.fallapp.model.Falla;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.repository.FallaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de fallas
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FallaService {

    private final FallaRepository fallaRepository;

    /**
     * Obtener falla por ID
     */
    public FallaDTO obtenerPorId(Long id) {
        Falla falla = fallaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Falla", "id", id));
        return convertirADTO(falla);
    }

    /**
     * Listar todas las fallas con paginación
     */
    public PaginatedResponse<FallaDTO> listar(int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("nombre"));
        Page<Falla> page = fallaRepository.findAll(pageable);
        
        return construirRespuestaPaginada(page);
    }

    /**
     * Buscar fallas por texto (full-text search)
     */
    public List<FallaDTO> buscar(String texto) {
        return fallaRepository.buscarPorTexto(texto)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Buscar fallas cercanas a una ubicación
     */
    public List<FallaDTO> buscarCercanas(double latitud, double longitud, double radioKm) {
        return fallaRepository.buscarFallasCercanas(
                java.math.BigDecimal.valueOf(latitud),
                java.math.BigDecimal.valueOf(longitud),
                java.math.BigDecimal.valueOf(radioKm)
        ).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener fallas por sección
     */
    public List<FallaDTO> obtenerPorSeccion(String seccion) {
        return fallaRepository.findBySeccion(seccion, Pageable.unpaged())
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener fallas por categoría
     */
    public PaginatedResponse<FallaDTO> obtenerPorCategoria(String categoria, int pagina, int tamano) {
        Falla.CategoriaFalla cat = Falla.CategoriaFalla.valueOf(categoria.toLowerCase());
        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Falla> page = fallaRepository.findByCategoria(cat, pageable);
        
        return construirRespuestaPaginada(page);
    }

    /**
     * Convertir entidad a DTO
     */
    private FallaDTO convertirADTO(Falla falla) {
        return FallaDTO.builder()
                .idFalla(falla.getIdFalla())
                .nombre(falla.getNombre())
                .seccion(falla.getSeccion())
                .presidente(falla.getPresidente())
                .artista(falla.getArtista())
                .lema(falla.getLema())
                .anyoFundacion(falla.getAnyoFundacion())
                .latitud(falla.getUbicacionLat() != null ? falla.getUbicacionLat().doubleValue() : null)
                .longitud(falla.getUbicacionLon() != null ? falla.getUbicacionLon().doubleValue() : null)
                .categoria(falla.getCategoria() != null ? falla.getCategoria().name() : null)
                .totalEventos(falla.getEventos() != null ? falla.getEventos().size() : 0)
                .totalNinots(falla.getNinots() != null ? falla.getNinots().size() : 0)
                .totalMiembros(falla.getUsuarios() != null ? falla.getUsuarios().size() : 0)
                .fechaCreacion(falla.getCreadoEn())
                .fechaActualizacion(falla.getActualizadoEn())
                .build();
    }

    /**
     * Construir respuesta paginada
     */
    private PaginatedResponse<FallaDTO> construirRespuestaPaginada(Page<Falla> page) {
        List<FallaDTO> contenido = page.getContent()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return PaginatedResponse.<FallaDTO>builder()
                .contenido(contenido)
                .paginaActual(page.getNumber())
                .elementosPorPagina(page.getSize())
                .totalElementos(page.getTotalElements())
                .totalPaginas(page.getTotalPages())
                .esUltimaPagina(page.isLast())
                .build();
    }
}
