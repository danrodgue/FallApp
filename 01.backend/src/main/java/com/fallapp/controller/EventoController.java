package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.dto.EventoDTO;
import com.fallapp.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    }}