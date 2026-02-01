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
}
