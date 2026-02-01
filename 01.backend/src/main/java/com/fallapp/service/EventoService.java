package com.fallapp.service;

import com.fallapp.dto.EventoDTO;
import com.fallapp.model.Evento;
import com.fallapp.model.Falla;
import com.fallapp.repository.EventoRepository;
import com.fallapp.repository.FallaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Eventos
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventoService {
    
    private final EventoRepository eventoRepository;
    private final FallaRepository fallaRepository;
    
    /**
     * Obtener eventos futuros
     */
    public List<EventoDTO> obtenerEventosFuturos() {
        List<Evento> eventos = eventoRepository.findEventosFuturos(java.time.LocalDateTime.now());
        return eventos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener próximos eventos (limitado)
     */
    public List<EventoDTO> obtenerProximosEventos(int limite) {
        Pageable pageable = Pageable.ofSize(limite);
        List<Evento> eventos = eventoRepository.findProximosEventos(pageable);
        return eventos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener eventos por falla
     */
    public Page<EventoDTO> obtenerPorFalla(Long idFalla, Pageable pageable) {
        Falla falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + idFalla));
        Page<Evento> eventos = eventoRepository.findByFalla(falla, pageable);
        List<EventoDTO> dtos = eventos.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, eventos.getTotalElements());
    }
    
    /**
     * Obtener evento por ID
     */
    public EventoDTO obtenerPorId(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));
        return convertirADTO(evento);
    }
    
    /**
     * Convertir entidad Evento a DTO
     */
    private EventoDTO convertirADTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setIdEvento(evento.getIdEvento());
        dto.setIdFalla(evento.getFalla() != null ? evento.getFalla().getIdFalla() : null);
        dto.setNombreFalla(evento.getFalla() != null ? evento.getFalla().getNombre() : null);
        dto.setTipo(evento.getTipo() != null ? evento.getTipo().name() : null);
        dto.setNombre(evento.getNombre());
        dto.setDescripcion(evento.getDescripcion());
        dto.setFechaEvento(evento.getFechaEvento());
        dto.setUbicacion(evento.getUbicacion());
        dto.setParticipantesEstimado(evento.getParticipantesEstimado());
        dto.setFechaCreacion(evento.getCreadoEn());
        dto.setCreadoPor(evento.getCreadoPor() != null ? evento.getCreadoPor().getNombreCompleto() : null);
        return dto;
    }
}
