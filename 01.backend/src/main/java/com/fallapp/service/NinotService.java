package com.fallapp.service;

import com.fallapp.dto.NinotDTO;
import com.fallapp.model.Falla;
import com.fallapp.model.Ninot;
import com.fallapp.repository.FallaRepository;
import com.fallapp.repository.NinotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Ninots
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NinotService {
    
    private final NinotRepository ninotRepository;
    private final FallaRepository fallaRepository;
    
    /**
     * Obtener todos los ninots con paginación
     */
    public Page<NinotDTO> obtenerTodos(Pageable pageable) {
        Page<Ninot> ninots = ninotRepository.findAll(pageable);
        List<NinotDTO> dtos = ninots.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, ninots.getTotalElements());
    }
    
    /**
     * Obtener ninot por ID
     */
    public NinotDTO obtenerPorId(Long id) {
        Ninot ninot = ninotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ninot no encontrado con ID: " + id));
        return convertirADTO(ninot);
    }
    
    /**
     * Obtener ninots por falla
     */
    public Page<NinotDTO> obtenerPorFalla(Long idFalla, Pageable pageable) {
        Falla falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + idFalla));
        Page<Ninot> ninots = ninotRepository.findByFalla(falla, pageable);
        List<NinotDTO> dtos = ninots.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, ninots.getTotalElements());
    }
    
    /**
     * Obtener ninots premiados
     */
    public Page<NinotDTO> obtenerPremiados(Pageable pageable) {
        Page<Ninot> ninots = ninotRepository.findByPremiadoTrue(pageable);
        List<NinotDTO> dtos = ninots.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, ninots.getTotalElements());
    }
    
    /**
     * Convertir entidad Ninot a DTO
     */
    private NinotDTO convertirADTO(Ninot ninot) {
        NinotDTO dto = new NinotDTO();
        dto.setIdNinot(ninot.getIdNinot());
        dto.setIdFalla(ninot.getFalla() != null ? ninot.getFalla().getIdFalla() : null);
        dto.setNombreFalla(ninot.getFalla() != null ? ninot.getFalla().getNombre() : null);
        dto.setNombreNinot(ninot.getNombreNinot());
        dto.setTituloObra(ninot.getTituloObra());
        dto.setAltura(ninot.getAlturaMetros() != null ? ninot.getAlturaMetros().doubleValue() : null);
        dto.setAncho(ninot.getAnchoMetros() != null ? ninot.getAnchoMetros().doubleValue() : null);
        
        // Convertir array de imágenes a lista
        java.util.List<String> imagenes = new java.util.ArrayList<>();
        if (ninot.getUrlImagenPrincipal() != null) {
            imagenes.add(ninot.getUrlImagenPrincipal());
        }
        if (ninot.getUrlImagenesAdicionales() != null) {
            imagenes.addAll(java.util.Arrays.asList(ninot.getUrlImagenesAdicionales()));
        }
        dto.setImagenes(imagenes.isEmpty() ? null : imagenes);
        
        dto.setPremiado(ninot.getPremiado());
        dto.setFechaCreacion(ninot.getCreadoEn());
        
        // Campos calculados
        dto.setTotalVotos(ninot.getVotos() != null ? ninot.getVotos().size() : 0);
        
        return dto;
    }

    /**
     * Crear nuevo ninot asociado a una falla
     * 
     * Validaciones:
     * - Falla debe existir (relación obligatoria)
     * - Nombre: @NotBlank validado en DTO
     * - Dimensiones: @DecimalMin(0.1) para altura y ancho (en metros)
     * - Imágenes: Array de URLs (opcional)
     * 
     * Conversiones:
     * - Double → BigDecimal para altura, ancho, profundidad (precisión decimal)
     * - String[] → String (join con comas) para imágenes
     * 
     * @param ninotDTO DTO con datos del ninot
     * @return NinotDTO creado con ID y totalVotos=0
     * @throws RuntimeException Si la falla no existe
     */
    @Transactional
    public NinotDTO crear(NinotDTO ninotDTO) {
        Falla falla = fallaRepository.findById(ninotDTO.getIdFalla())
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + ninotDTO.getIdFalla()));

        Ninot ninot = new Ninot();
        mapearDTOAEntidad(ninotDTO, ninot, falla);
        
        Ninot ninotSaved = ninotRepository.save(ninot);
        return convertirADTO(ninotSaved);
    }

    /**
     * Actualizar ninot existente
     * 
     * Permite modificar:
     * - Datos básicos (nombre, artista, descripción)
     * - Dimensiones (altura, ancho, profundidad)
     * - Estado premiado (boolean)
     * - Tipo premio y año premio (si premiado=true)
     * - Array de imágenes (URLs)
     * - Falla asociada (reasignación permitida)
     * 
     * Nota: Los votos NO se modifican aquí (usar endpoint /api/votos)
     * 
     * @param id ID del ninot a actualizar
     * @param ninotDTO DTO con nuevos valores
     * @return NinotDTO actualizado con totalVotos calculado
     * @throws RuntimeException Si ninot o falla no existen
     */
    @Transactional
    public NinotDTO actualizar(Long id, NinotDTO ninotDTO) {
        Ninot ninot = ninotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ninot no encontrado con ID: " + id));

        Falla falla = fallaRepository.findById(ninotDTO.getIdFalla())
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + ninotDTO.getIdFalla()));

        mapearDTOAEntidad(ninotDTO, ninot, falla);
        
        Ninot ninotActualizado = ninotRepository.save(ninot);
        return convertirADTO(ninotActualizado);
    }

    /**
     * Eliminar ninot del sistema
     * 
     * IMPORTANTE - Efectos Cascada:
     * - Votos asociados: Eliminados (ON DELETE CASCADE)
     * - Comentarios: Eliminados (ON DELETE CASCADE)
     * 
     * Restricciones:
     * - Solo rol ADMIN (validado en controller)
     * - Operación irreversible
     * 
     * @param id ID del ninot a eliminar
     * @throws RuntimeException Si el ninot no existe
     */
    @Transactional
    public void eliminar(Long id) {
        Ninot ninot = ninotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ninot no encontrado con ID: " + id));
        
        ninotRepository.delete(ninot);
    }

    /**
     * Mapear DTO a entidad
     */
    private void mapearDTOAEntidad(NinotDTO dto, Ninot entidad, Falla falla) {
        entidad.setFalla(falla);
        entidad.setNombreNinot(dto.getNombreNinot());
        entidad.setTituloObra(dto.getTituloObra());
        
        if (dto.getAltura() != null) {
            entidad.setAlturaMetros(java.math.BigDecimal.valueOf(dto.getAltura()));
        }
        if (dto.getAncho() != null) {
            entidad.setAnchoMetros(java.math.BigDecimal.valueOf(dto.getAncho()));
        }
        
        // Manejar imágenes
        if (dto.getImagenes() != null && !dto.getImagenes().isEmpty()) {
            entidad.setUrlImagenPrincipal(dto.getImagenes().get(0));
            if (dto.getImagenes().size() > 1) {
                String[] adicionales = dto.getImagenes().subList(1, dto.getImagenes().size()).toArray(new String[0]);
                entidad.setUrlImagenesAdicionales(adicionales);
            }
        }
        
        entidad.setPremiado(dto.getPremiado() != null ? dto.getPremiado() : false);
    }}