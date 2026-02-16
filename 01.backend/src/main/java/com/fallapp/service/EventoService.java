package com.fallapp.service;

import com.fallapp.dto.EventoDTO;
import com.fallapp.model.Evento;
import com.fallapp.model.Falla;
import com.fallapp.model.Usuario;
import com.fallapp.repository.EventoRepository;
import com.fallapp.repository.FallaRepository;
import com.fallapp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final UsuarioRepository usuarioRepository;
    private final FileUploadService fileUploadService;
    
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
        dto.setCreadoPor(evento.getCreadoPor() != null ? evento.getCreadoPor().getIdUsuario() : null);
        dto.setFechaCreacion(evento.getCreadoEn());
        dto.setImagenNombre(evento.getImagenNombre());
        return dto;
    }

    /**
     * Crear nuevo evento asociado a una falla
     * 
     * Validaciones:
     * - Falla debe existir (relación obligatoria)
     * - Fecha evento: @NotNull validado en DTO
     * - Tipo evento: Enum (planta, crema, ofrenda, infantil, concierto, exposicion, otro)
     * - Participantes estimado: @Min(0) si se proporciona
     * 
     * Campos automáticos:
     * - fecha_creacion: Timestamp actual (default BD)
     * - id: Auto-generado (sequence eventos_id_evento_seq)
     * 
     * @param eventoDTO DTO con datos del evento
     * @return EventoDTO creado con ID y fechas
     * @throws RuntimeException Si la falla no existe
     */
    @Transactional
    public EventoDTO crear(EventoDTO eventoDTO) {
        Falla falla = fallaRepository.findById(eventoDTO.getIdFalla())
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + eventoDTO.getIdFalla()));

        Evento evento = new Evento();
        mapearDTOAEntidad(eventoDTO, evento, falla);
        
        // Asignar usuario creador si se proporciona
        if (eventoDTO.getCreadoPor() != null) {
            Usuario usuario = usuarioRepository.findById(eventoDTO.getCreadoPor())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + eventoDTO.getCreadoPor()));
            evento.setCreadoPor(usuario);
        }
        
        Evento eventoSaved = eventoRepository.save(evento);
        return convertirADTO(eventoSaved);
    }

    /**
     * Actualizar evento existente
     * 
     * Permite cambiar:
     * - Datos del evento (nombre, descripción, fecha, ubicación)
     * - Tipo de evento (enum TipoEvento)
     * - Falla asociada (reasignación permitida)
     * - Participantes estimado
     * 
     * Comportamiento:
     * - Validación de existencia de evento y falla
     * - Sobrescritura completa de campos del DTO
     * 
     * @param id ID del evento a actualizar
     * @param eventoDTO DTO con nuevos valores
     * @return EventoDTO actualizado
     * @throws RuntimeException Si evento o falla no existen
     */
    @Transactional
    public EventoDTO actualizar(Long id, EventoDTO eventoDTO) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));

        Falla falla = fallaRepository.findById(eventoDTO.getIdFalla())
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + eventoDTO.getIdFalla()));

        mapearDTOAEntidad(eventoDTO, evento, falla);
        
        // No cambiar el creadoPor en actualizaciones (solo se asigna en creación)
        
        Evento eventoActualizado = eventoRepository.save(evento);
        return convertirADTO(eventoActualizado);
    }

    /**
     * Eliminar evento del sistema
     * 
     * Restricciones:
     * - Solo rol ADMIN (validado en controller)
     * - No hay relaciones CASCADE críticas (evento es hoja en árbol de dependencias)
     * 
     * @param id ID del evento a eliminar
     * @throws RuntimeException Si el evento no existe
     */
    @Transactional
    public void eliminar(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));
        
        eventoRepository.delete(evento);
    }

    /**
     * Devuelve la entidad Evento completa (incluyendo campos perezosos como la imagen).
     */
    @Transactional(readOnly = true)
    public Evento obtenerEntidadPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));
    }

    /**
     * Actualiza la imagen principal de un evento almacenando la imagen como binario en BD.
     *
     * NOTA: Funcionalidad deshabilitada - la BD no tiene columnas imagen/imagen_content_type.
     * Usar url_imagen en su lugar.
     *
     * - Valida un tamaño máximo razonable (por defecto 5 MB)
     * - Guarda los bytes en campo BYTEA
     * - Guarda el Content-Type para devolver la cabecera adecuada
     */
    /*
    @Transactional
    public void actualizarImagen(Long idEvento, MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new RuntimeException("La imagen del evento no puede estar vacía");
        }

        long maxBytes = 5 * 1024 * 1024; // 5 MB
        if (imagen.getSize() > maxBytes) {
            throw new RuntimeException("La imagen del evento supera el tamaño máximo permitido (5 MB)");
        }

        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + idEvento));

        try {
            evento.setImagen(imagen.getBytes());
        } catch (java.io.IOException e) {
            throw new RuntimeException("No se ha podido leer la imagen del evento");
        }

        String contentType = imagen.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.IMAGE_JPEG_VALUE;
        }
        evento.setImagenContentType(contentType);

        eventoRepository.save(evento);
    }
    */
    /**
     * Mapear DTO a entidad
     */
    private void mapearDTOAEntidad(EventoDTO dto, Evento entidad, Falla falla) {
        entidad.setFalla(falla);
        entidad.setNombre(dto.getNombre());
        entidad.setDescripcion(dto.getDescripcion());
        entidad.setFechaEvento(dto.getFechaEvento());
        entidad.setUbicacion(dto.getUbicacion());
        entidad.setParticipantesEstimado(dto.getParticipantesEstimado());
        
        if (dto.getTipo() != null) {
            entidad.setTipo(Evento.TipoEvento.valueOf(dto.getTipo().toLowerCase()));
        }
    }

    /**
     * Guardar imagen del evento
     * 
     * @param id ID del evento
     * @param imagen Archivo de imagen a guardar
     * @return DTO del evento actualizado
     */
    @Transactional
    public EventoDTO guardarImagen(Long id, MultipartFile imagen) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));

        // Eliminar imagen anterior si existe
        if (evento.getImagenNombre() != null && !evento.getImagenNombre().isBlank()) {
            try {
                fileUploadService.eliminarArchivo(evento.getImagenNombre(), "eventos");
            } catch (Exception e) {
                // Continuar aunque falle la eliminación anterior
            }
        }

        // Guardar nueva imagen
        String nombreArchivo = fileUploadService.guardarArchivo(imagen, "eventos");
        evento.setImagenNombre(nombreArchivo);
        
        eventoRepository.save(evento);
        return convertirADTO(evento);
    }

    /**
     * Obtener imagen del evento
     * 
     * @param id ID del evento
     * @return Bytes de la imagen
     */
    public byte[] obtenerImagen(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));

        if (evento.getImagenNombre() == null || evento.getImagenNombre().isBlank()) {
            throw new RuntimeException("El evento no tiene imagen");
        }

        return fileUploadService.obtenerArchivo(evento.getImagenNombre(), "eventos");
    }

    /**
     * Obtener nombre de la imagen del evento
     * 
     * @param id ID del evento
     * @return Nombre del archivo de imagen
     */
    public String obtenerNombreImagen(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));

        if (evento.getImagenNombre() == null || evento.getImagenNombre().isBlank()) {
            throw new RuntimeException("El evento no tiene imagen");
        }

        return evento.getImagenNombre();
    }

    /**
     * Eliminar imagen del evento
     * 
     * @param id ID del evento
     */
    @Transactional
    public void eliminarImagen(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));

        if (evento.getImagenNombre() != null && !evento.getImagenNombre().isBlank()) {
            fileUploadService.eliminarArchivo(evento.getImagenNombre(), "eventos");
            evento.setImagenNombre(null);
            eventoRepository.save(evento);
        }
    }
}