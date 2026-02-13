package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.dto.FallaDTO;
import com.fallapp.dto.PaginatedResponse;
import com.fallapp.dto.UbicacionDTO;
import com.fallapp.service.FallaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de fallas
 */
@RestController
@RequestMapping("/api/fallas")
@RequiredArgsConstructor
@Tag(name = "Fallas", description = "Gestión de fallas valencianas")
public class FallaController {

    private final FallaService fallaService;

    @GetMapping
    @Operation(summary = "Listar fallas con paginación")
    public ResponseEntity<ApiResponse<PaginatedResponse<FallaDTO>>> listar(
            @Parameter(description = "Página (0-indexed)") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Elementos por página") @RequestParam(defaultValue = "20") int tamano) {
        PaginatedResponse<FallaDTO> resultado = fallaService.listar(pagina, tamano);
        return ResponseEntity.ok(ApiResponse.success(resultado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener falla por ID")
    public ResponseEntity<ApiResponse<FallaDTO>> obtenerPorId(@PathVariable Long id) {
        FallaDTO falla = fallaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(falla));
    }

    @GetMapping("/{id}/ubicacion")
    @Operation(summary = "Obtener ubicación GPS de una falla por ID", 
               description = "Retorna las coordenadas GPS (latitud, longitud) de una falla específica. Útil para mapas y geolocalización.")
    public ResponseEntity<ApiResponse<UbicacionDTO>> obtenerUbicacion(
            @Parameter(description = "ID de la falla") @PathVariable Long id) {
        UbicacionDTO ubicacion = fallaService.obtenerUbicacion(id);
        return ResponseEntity.ok(ApiResponse.success(ubicacion));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar fallas por texto (full-text search)")
    public ResponseEntity<ApiResponse<List<FallaDTO>>> buscar(
            @Parameter(description = "Texto a buscar") @RequestParam String texto) {
        List<FallaDTO> resultados = fallaService.buscar(texto);
        return ResponseEntity.ok(ApiResponse.success(resultados));
    }

    @GetMapping("/cercanas")
    @Operation(summary = "Buscar fallas cercanas a una ubicación")
    public ResponseEntity<ApiResponse<List<FallaDTO>>> buscarCercanas(
            @Parameter(description = "Latitud") @RequestParam double latitud,
            @Parameter(description = "Longitud") @RequestParam double longitud,
            @Parameter(description = "Radio en kilómetros") @RequestParam(defaultValue = "5.0") double radio) {
        List<FallaDTO> resultados = fallaService.buscarCercanas(latitud, longitud, radio);
        return ResponseEntity.ok(ApiResponse.success(resultados));
    }

    @GetMapping("/seccion/{seccion}")
    @Operation(summary = "Obtener fallas por sección")
    public ResponseEntity<ApiResponse<List<FallaDTO>>> obtenerPorSeccion(@PathVariable String seccion) {
        List<FallaDTO> resultados = fallaService.obtenerPorSeccion(seccion);
        return ResponseEntity.ok(ApiResponse.success(resultados));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Obtener fallas por categoría")
    public ResponseEntity<ApiResponse<PaginatedResponse<FallaDTO>>> obtenerPorCategoria(
            @PathVariable String categoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        PaginatedResponse<FallaDTO> resultado = fallaService.obtenerPorCategoria(categoria, pagina, tamano);
        return ResponseEntity.ok(ApiResponse.success(resultado));
    }

    /**
     * Crear nueva falla
     * @param fallaDTO Datos de la nueva falla
     * @return Respuesta con falla creada
     */
    @PostMapping
    @Operation(summary = "Crear nueva falla", description = "Requiere autenticación. Solo administradores o usuarios con rol 'casal' pueden crear fallas.")
    public ResponseEntity<ApiResponse<FallaDTO>> crear(@RequestBody @jakarta.validation.Valid FallaDTO fallaDTO) {
        FallaDTO fallaCreada = fallaService.crear(fallaDTO);
        return ResponseEntity.status(201).body(ApiResponse.success("Falla creada exitosamente", fallaCreada));
    }

    /**
     * Actualizar falla existente
     * @param id ID de la falla
     * @param fallaDTO Datos actualizados
     * @return Respuesta con falla actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar falla existente", description = "Requiere autenticación. Solo administradores o usuarios pertenecientes a la falla pueden actualizarla.")
    public ResponseEntity<ApiResponse<FallaDTO>> actualizar(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid FallaDTO fallaDTO) {
        FallaDTO fallaActualizada = fallaService.actualizar(id, fallaDTO);
        return ResponseEntity.ok(ApiResponse.success("Falla actualizada exitosamente", fallaActualizada));
    }

    /**
     * Eliminar falla
     * @param id ID de la falla
     * @return Respuesta de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar falla", description = "Requiere autenticación. Solo administradores pueden eliminar fallas. ATENCIÓN: Esto eliminará también eventos, ninots y votos asociados.")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        fallaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Falla eliminada exitosamente", null));
    }
}
