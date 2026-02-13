package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.dto.ComentarioDTO;
import com.fallapp.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de Comentarios en fallas y ninots
 * 
 * Endpoints: /api/comentarios
 * 
 * Funcionalidad:
 * - GET /api/comentarios?idFalla={id} - Listar comentarios de una falla (público)
 * - GET /api/comentarios/{id} - Obtener comentario por ID (público)
 * - POST /api/comentarios - Crear comentario (requiere autenticación JWT)
 * - PUT /api/comentarios/{id} - Actualizar comentario (requiere ser autor o admin)
 * - DELETE /api/comentarios/{id} - Eliminar comentario (requiere ser autor o admin)
 * 
 * Validaciones:
 * - Cada comentario debe tener idFalla O idNinot (no ambos, no ninguno)
 * - Contenido: mínimo 3 caracteres, máximo 500 caracteres (@Size)
 * - Solo el autor del comentario o admin puede editar/eliminar
 * - Usuario autenticado se extrae del JWT token (@AuthenticationPrincipal)
 * 
 * Seguridad:
 * - POST/PUT/DELETE requieren header: Authorization: Bearer {token}
 * - Token JWT validado por JwtAuthenticationFilter (ADR-006)
 * - Respuestas siguen ApiResponse<T> estándar (ADR-007)
 * 
 * @see ComentarioService Business logic y validaciones
 * @see ComentarioDTO DTO con validaciones Bean Validation
 * @see ADR-006 Autenticación JWT implementada
 * @see ADR-007 Formato estándar de respuesta API
 * 
 * @author FallApp Development Team
 * @version 0.4.0
 * @since 2026-02-01
 */
@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
@Tag(name = "Comentarios", description = "Gestión de comentarios en fallas y ninots")
public class ComentarioController {
    
    private final ComentarioService comentarioService;
    
    /**
     * GET /api/comentarios - Obtener comentarios filtrados
     * 
     * Permite filtrar comentarios por falla o ninot usando query parameters.
     * Si no se proporciona ningún filtro, retorna todos los comentarios.
     * 
     * Ejemplos:
     * - GET /api/comentarios?idFalla=1 → Comentarios de falla con ID 1
     * - GET /api/comentarios?idNinot=5 → Comentarios de ninot con ID 5
     * - GET /api/comentarios → Todos los comentarios
     * 
     * Ordenamiento: Descendente por fecha de creación (más recientes primero)
     * 
    * @param idFalla ID de la falla (opcional)
     * @return ApiResponse con lista de ComentarioDTO ordenados por fecha
     * @throws ResourceNotFoundException Si idFalla o idNinot no existen
     */
    @GetMapping
    @Operation(summary = "Obtener comentarios", description = "Obtener comentarios filtrados por falla")
    public ResponseEntity<ApiResponse<List<ComentarioDTO>>> obtener(
            @RequestParam(required = false) Long idFalla) {
        
        List<ComentarioDTO> comentarios;
        
        if (idFalla != null) {
            comentarios = comentarioService.obtenerPorFalla(idFalla);
        } else {
            comentarios = comentarioService.obtenerTodos();
        }
        
        return ResponseEntity.ok(ApiResponse.success(comentarios));
    }
    
    /**
     * GET /api/comentarios/{id} - Obtener comentario por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener comentario por ID")
    public ResponseEntity<ApiResponse<ComentarioDTO>> obtenerPorId(@PathVariable Long id) {
        ComentarioDTO comentario = comentarioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(comentario));
    }
    
    /**
     * POST /api/comentarios - Crear nuevo comentario
     * Requiere autenticación
     */
    @PostMapping
    @Operation(summary = "Crear nuevo comentario", description = "Requiere autenticación. Debe especificar idFalla.")
    public ResponseEntity<ApiResponse<ComentarioDTO>> crear(
            @RequestBody @jakarta.validation.Valid ComentarioDTO comentarioDTO) {
        ComentarioDTO comentarioCreado = comentarioService.crear(comentarioDTO);
        return ResponseEntity.status(201).body(ApiResponse.success("Comentario creado exitosamente", comentarioCreado));
    }
    
    /**
     * PUT /api/comentarios/{id} - Actualizar comentario existente
     * Requiere autenticación (solo el autor o admin)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comentario", description = "Solo el autor del comentario o un admin pueden actualizarlo")
    public ResponseEntity<ApiResponse<ComentarioDTO>> actualizar(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid ComentarioDTO comentarioDTO) {
        ComentarioDTO comentarioActualizado = comentarioService.actualizar(id, comentarioDTO);
        return ResponseEntity.ok(ApiResponse.success("Comentario actualizado exitosamente", comentarioActualizado));
    }
    
    /**
     * DELETE /api/comentarios/{id} - Eliminar comentario
     * Requiere autenticación (solo el autor o admin)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comentario", description = "Solo el autor del comentario o un admin pueden eliminarlo")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        comentarioService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Comentario eliminado exitosamente", null));
    }
}
