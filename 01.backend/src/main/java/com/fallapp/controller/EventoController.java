package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.dto.EventoDTO;
import com.fallapp.service.EventoService;
import com.fallapp.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para gestión de Eventos
 * Endpoints: /api/eventos
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {
    
    private final EventoService eventoService;
    private final FileUploadService fileUploadService;
    
    /**
     * GET /api/eventos/futuros - Obtener eventos futuros
     */
    @GetMapping("/futuros")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> obtenerFuturos() {
        List<EventoDTO> eventos = eventoService.obtenerEventosFuturos();
        return ResponseEntity.ok(ApiResponse.success(eventos));
    }
    
    /**
     * GET /api/eventos/proximos - Obtener próximos N eventos
     */
    @GetMapping("/proximos")
    public ResponseEntity<ApiResponse<List<EventoDTO>>> obtenerProximos(
            @RequestParam(defaultValue = "10") int limite) {
        
        limite = Math.min(limite, 50); // Máximo 50
        List<EventoDTO> eventos = eventoService.obtenerProximosEventos(limite);
        return ResponseEntity.ok(ApiResponse.success(eventos));
    }
    
    /**
     * GET /api/eventos/{id} - Obtener evento por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventoDTO>> obtenerPorId(@PathVariable Long id) {
        EventoDTO evento = eventoService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(evento));
    }
    
    /**
     * GET /api/eventos/falla/{idFalla} - Obtener eventos de una falla
     */
    @GetMapping("/falla/{idFalla}")
    public ResponseEntity<ApiResponse<Page<EventoDTO>>> obtenerPorFalla(
            @PathVariable Long idFalla,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaEvento").descending());
        
        Page<EventoDTO> eventos = eventoService.obtenerPorFalla(idFalla, pageable);
        return ResponseEntity.ok(ApiResponse.success(eventos));
    }

    /**
     * POST /api/eventos - Crear nuevo evento
     * Requiere autenticación (admin o usuario de la falla)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EventoDTO>> crear(
            @RequestBody @jakarta.validation.Valid EventoDTO eventoDTO) {
        EventoDTO eventoCreado = eventoService.crear(eventoDTO);
        return ResponseEntity.status(201).body(ApiResponse.success("Evento creado exitosamente", eventoCreado));
    }

    /**
     * PUT /api/eventos/{id} - Actualizar evento existente
     * Requiere autenticación (admin o usuario de la falla)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventoDTO>> actualizar(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid EventoDTO eventoDTO) {
        EventoDTO eventoActualizado = eventoService.actualizar(id, eventoDTO);
        return ResponseEntity.ok(ApiResponse.success("Evento actualizado exitosamente", eventoActualizado));
    }

    /**
     * DELETE /api/eventos/{id} - Eliminar evento
     * Requiere autenticación (admin o usuario de la falla)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        eventoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Evento eliminado exitosamente", null));
    }

    /**
     * POST /api/eventos/{id}/imagen - Subir imagen del evento
     * 
     * Guarda la imagen en el servidor y asocia el nombre del archivo al evento
     */
    @PostMapping(path = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventoDTO>> subirImagen(
            @PathVariable Long id,
            @RequestPart("imagen") MultipartFile imagen) {
        
        try {
            EventoDTO eventoActualizado = eventoService.guardarImagen(id, imagen);
            return ResponseEntity.ok(ApiResponse.success("Imagen del evento subida correctamente", eventoActualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/eventos/{id}/imagen - Descargar imagen del evento
     * 
     * Retorna la imagen en su formato original
     */
    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> descargarImagen(@PathVariable Long id) {
        try {
            byte[] imagenBytes = eventoService.obtenerImagen(id);
            String imagenNombre = eventoService.obtenerNombreImagen(id);
            
            String mimeType = fileUploadService.obtenerMimeType(imagenNombre);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("inline")
                    .filename(imagenNombre)
                    .build());
            
            return new ResponseEntity<>(imagenBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * DELETE /api/eventos/{id}/imagen - Eliminar imagen del evento
     */
    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<ApiResponse<Void>> eliminarImagen(@PathVariable Long id) {
        try {
            eventoService.eliminarImagen(id);
            return ResponseEntity.ok(ApiResponse.success("Imagen del evento eliminada", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/eventos/{id}/imagen - Actualizar imagen principal del evento.
     *
     * NOTA: Endpoint deshabilitado - la BD no tiene columnas imagen/imagen_content_type.
     * Usar url_imagen en su lugar.
     *
     * Formato: multipart/form-data con un campo "imagen" que contiene la imagen.
     * La imagen se almacena como binario (BYTEA) en la base de datos.
     */
    /*
    @PutMapping(path = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> actualizarImagen(
            @PathVariable Long id,
            @RequestPart("imagen") MultipartFile imagen) {

        eventoService.actualizarImagen(id, imagen);
        return ResponseEntity.ok(ApiResponse.success("Imagen del evento actualizada correctamente", null));
    }
    */

    /**
     * GET /api/eventos/{id}/imagen - Obtener imagen principal del evento.
     *
     * Devuelve directamente los bytes de la imagen con el Content-Type original.
     * Si el evento no tiene imagen, devuelve 404.
     * NOTA: Endpoint deshabilitado - la BD no tiene columnas imagen/imagen_content_type.
     * Usar url_imagen en su lugar.
     */
    /*
    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable Long id) {
        var evento = eventoService.obtenerEntidadPorId(id);

        byte[] imagen = evento.getImagen();
        if (imagen == null || imagen.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String contentType = evento.getImagenContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.IMAGE_JPEG_VALUE;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));

        return new ResponseEntity<>(imagen, headers, HttpStatus.OK);
    }
    */
}