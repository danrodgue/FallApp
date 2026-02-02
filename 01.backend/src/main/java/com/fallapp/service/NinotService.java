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
     * Convertir entidad Ninot a DTO
     */
    private NinotDTO convertirADTO(Ninot ninot) {
        return NinotDTO.builder()
                .idNinot(ninot.getIdNinot())
                .idFalla(ninot.getFalla() != null ? ninot.getFalla().getIdFalla() : null)
                .nombreFalla(ninot.getFalla() != null ? ninot.getFalla().getNombre() : null)
                .nombre(ninot.getNombre())
                .urlImagen(ninot.getUrlImagen())
                .fechaCreacion(ninot.getFechaCreacion())
                .build();
    }

    /**
     * Crear nuevo ninot asociado a una falla
     * 
     * @param ninotDTO DTO con datos del ninot
     * @return NinotDTO creado con ID
     * @throws RuntimeException Si la falla no existe
     */
    @Transactional
    public NinotDTO crear(NinotDTO ninotDTO) {
        Falla falla = fallaRepository.findById(ninotDTO.getIdFalla())
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + ninotDTO.getIdFalla()));

        Ninot ninot = new Ninot();
        ninot.setFalla(falla);
        ninot.setNombre(ninotDTO.getNombre());
        ninot.setUrlImagen(ninotDTO.getUrlImagen());
        
        Ninot ninotSaved = ninotRepository.save(ninot);
        return convertirADTO(ninotSaved);
    }

    /**
     * Actualizar ninot existente
     * 
     * @param id ID del ninot a actualizar
     * @param ninotDTO DTO con nuevos valores
     * @return NinotDTO actualizado
     * @throws RuntimeException Si ninot o falla no existen
     */
    @Transactional
    public NinotDTO actualizar(Long id, NinotDTO ninotDTO) {
        Ninot ninot = ninotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ninot no encontrado con ID: " + id));

        Falla falla = fallaRepository.findById(ninotDTO.getIdFalla())
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + ninotDTO.getIdFalla()));

        ninot.setFalla(falla);
        ninot.setNombre(ninotDTO.getNombre());
        ninot.setUrlImagen(ninotDTO.getUrlImagen());
        
        Ninot ninotActualizado = ninotRepository.save(ninot);
        return convertirADTO(ninotActualizado);
    }

    /**
     * Eliminar ninot del sistema
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
}