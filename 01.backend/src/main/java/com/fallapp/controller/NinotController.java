package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.dto.NinotDTO;
import com.fallapp.service.NinotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de Ninots
 * Endpoints: /api/ninots
 */
@RestController
@RequestMapping("/api/ninots")
@RequiredArgsConstructor
public class NinotController {
    
    private final NinotService ninotService;
    
    /**
     * GET /api/ninots - Obtener todos los ninots
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NinotDTO>>> obtenerTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sort) {
        
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        
        Page<NinotDTO> ninots = ninotService.obtenerTodos(pageable);
        return ResponseEntity.ok(ApiResponse.success(ninots));
    }
    
    /**
     * GET /api/ninots/{id} - Obtener ninot por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NinotDTO>> obtenerPorId(@PathVariable Long id) {
        NinotDTO ninot = ninotService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(ninot));
    }
    
    /**
     * GET /api/ninots/falla/{idFalla} - Obtener ninots de una falla
     */
    @GetMapping("/falla/{idFalla}")
    public ResponseEntity<ApiResponse<Page<NinotDTO>>> obtenerPorFalla(
            @PathVariable Long idFalla,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<NinotDTO> ninots = ninotService.obtenerPorFalla(idFalla, pageable);
        return ResponseEntity.ok(ApiResponse.success(ninots));
    }
    
    /**
     * GET /api/ninots/premiados - Obtener ninots premiados
     */
    @GetMapping("/premiados")
    public ResponseEntity<ApiResponse<Page<NinotDTO>>> obtenerPremiados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        
        Page<NinotDTO> ninots = ninotService.obtenerPremiados(pageable);
        return ResponseEntity.ok(ApiResponse.success(ninots));
    }

    /**
     * POST /api/ninots - Crear nuevo ninot
     * Requiere autenticación (admin o usuario de la falla)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NinotDTO>> crear(
            @RequestBody @jakarta.validation.Valid NinotDTO ninotDTO) {
        NinotDTO ninotCreado = ninotService.crear(ninotDTO);
        return ResponseEntity.status(201).body(ApiResponse.success("Ninot creado exitosamente", ninotCreado));
    }

    /**
     * PUT /api/ninots/{id} - Actualizar ninot existente
     * Requiere autenticación (admin o usuario de la falla)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NinotDTO>> actualizar(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid NinotDTO ninotDTO) {
        NinotDTO ninotActualizado = ninotService.actualizar(id, ninotDTO);
        return ResponseEntity.ok(ApiResponse.success("Ninot actualizado exitosamente", ninotActualizado));
    }

    /**
     * DELETE /api/ninots/{id} - Eliminar ninot
     * Requiere autenticación (admin o usuario de la falla)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        ninotService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Ninot eliminado exitosamente", null));
    }}