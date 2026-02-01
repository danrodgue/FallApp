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
}
