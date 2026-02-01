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

    /**
     * Crear nueva falla en el sistema
     * 
     * Validaciones de negocio:
     * - Nombre único: No pueden existir dos fallas con el mismo nombre
     * - Coordenadas: Latitud y longitud convertidas a BigDecimal
     * - Categoría: Enum validado (especial, primera, segunda, tercera, cuarta, quinta)
     * - Año fundación: Validado en DTO con @Min(1900)
     * 
     * Flujo de creación:
     * 1. Verificar unicidad del nombre
     * 2. Mapear DTO → Entidad (conversión de tipos)
     * 3. Persistir en BD (JPA genera ID auto-incremental)
     * 4. Convertir entidad guardada → DTO para respuesta
     * 
     * Transaccionalidad:
     * @Transactional garantiza rollback si falla algún paso
     * 
     * @param fallaDTO DTO con datos validados por Bean Validation (@NotBlank, @Size, etc.)
     * @return FallaDTO con ID generado y fechas de auditoría
     * @throws IllegalArgumentException Si ya existe falla con ese nombre
     * @see ADR-007 Formato estándar ApiResponse para respuestas
     */
    @Transactional
    public FallaDTO crear(FallaDTO fallaDTO) {
        // Verificar si ya existe una falla con ese nombre
        if (fallaRepository.existsByNombre(fallaDTO.getNombre())) {
            throw new IllegalArgumentException("Ya existe una falla con el nombre: " + fallaDTO.getNombre());
        }

        Falla falla = new Falla();
        mapearDTOAEntidad(fallaDTO, falla);
        
        Falla fallaSaved = fallaRepository.save(falla);
        return convertirADTO(fallaSaved);
    }

    /**
     * Actualizar falla existente
     */
    @Transactional
    public FallaDTO actualizar(Long id, FallaDTO fallaDTO) {
        Falla falla = fallaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Falla", "id", id));

        // Verificar si el nuevo nombre ya existe (y no es el mismo)
        if (!falla.getNombre().equals(fallaDTO.getNombre()) && 
            fallaRepository.existsByNombre(fallaDTO.getNombre())) {
            throw new IllegalArgumentException("Ya existe una falla con el nombre: " + fallaDTO.getNombre());
        }

        mapearDTOAEntidad(fallaDTO, falla);
        
        Falla fallaActualizada = fallaRepository.save(falla);
        return convertirADTO(fallaActualizada);
    }

    /**
     * Eliminar falla del sistema (soft delete o hard delete según configuración)
     * 
     * IMPORTANTE - Efectos Cascada:
     * - Eventos asociados: Eliminados (ON DELETE CASCADE)
     * - Ninots asociados: Eliminados (ON DELETE CASCADE)
     * - Votos de ninots: Eliminados (ON DELETE CASCADE)
     * - Comentarios: Eliminados (ON DELETE CASCADE)
     * - Usuarios asignados: Relación eliminada (usuarios no se borran)
     * 
     * Restricciones:
     * - Solo usuarios con rol ADMIN pueden ejecutar (validado en Controller)
     * - Operación irreversible (no hay papelera de reciclaje)
     * 
     * Performance:
     * - Puede ser lento si la falla tiene muchos eventos/ninots (>100)
     * - PostgreSQL maneja cascadas en BD (más eficiente que JPA)
     * 
     * @param id ID de la falla a eliminar
     * @throws RuntimeException Si la falla no existe
     * @see FallaController#eliminar Endpoint con @PreAuthorize("hasRole('ROLE_ADMIN')")
     */
    @Transactional
    public void eliminar(Long id) {
        Falla falla = fallaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Falla", "id", id));
        
        fallaRepository.delete(falla);
    }

    /**
     * Mapear DTO a entidad
     */
    private void mapearDTOAEntidad(FallaDTO dto, Falla entidad) {
        entidad.setNombre(dto.getNombre());
        entidad.setSeccion(dto.getSeccion());
        entidad.setFallera(dto.getFallera());
        entidad.setPresidente(dto.getPresidente());
        entidad.setArtista(dto.getArtista());
        entidad.setLema(dto.getLema());
        entidad.setAnyoFundacion(dto.getAnyoFundacion());
        entidad.setDistintivo(dto.getDistintivo());
        entidad.setUrlBoceto(dto.getUrlBoceto());
        entidad.setExperim(dto.getExperim() != null ? dto.getExperim() : false);
        
        if (dto.getLatitud() != null) {
            entidad.setUbicacionLat(java.math.BigDecimal.valueOf(dto.getLatitud()));
        }
        if (dto.getLongitud() != null) {
            entidad.setUbicacionLon(java.math.BigDecimal.valueOf(dto.getLongitud()));
        }
        
        entidad.setDescripcion(dto.getDescripcion());
        entidad.setWebOficial(dto.getWebOficial());
        entidad.setTelefonoContacto(dto.getTelefonoContacto());
        entidad.setEmailContacto(dto.getEmailContacto());
        
        if (dto.getCategoria() != null) {
            entidad.setCategoria(Falla.CategoriaFalla.valueOf(dto.getCategoria().toLowerCase()));
        }
    }}