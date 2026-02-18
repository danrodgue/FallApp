package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.service.ComentarioService;
import com.fallapp.service.EstadisticasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints de administración para estadísticas de sentimiento.
 *
 * Pensado para ser consumido desde el panel de administración (Electron).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Sentimiento", description = "Estadísticas de sentimiento de comentarios por falla")
public class AdminSentimientoController {

    private final EstadisticasService estadisticasService;
    private final ComentarioService comentarioService;

    /**
     * GET /api/admin/fallas/{idFalla}/sentimiento
     *
     * Devuelve agregados de comentarios por sentimiento para una falla.
     */
    @GetMapping("/fallas/{idFalla}/sentimiento")
    @Operation(summary = "Obtener estadística de sentimiento para una falla")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerSentimientoPorFalla(
            @PathVariable Long idFalla
    ) {
        Map<String, Object> datos = estadisticasService.obtenerSentimientoPorFalla(idFalla);
        return ResponseEntity.ok(ApiResponse.success(datos));
    }

    /**
     * POST /api/admin/comentarios/reanalizar-sentimiento
     *
     * Reanaliza con IA todos los comentarios que tienen sentimiento NULL.
     * Útil cuando el token de Hugging Face no estaba configurado al crear comentarios.
     */
    @PostMapping("/comentarios/reanalizar-sentimiento")
    @Operation(summary = "Reanalizar sentimiento de comentarios pendientes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reanalizarSentimientoPendientes() {
        int encolados = comentarioService.reanalizarSentimientoPendientes();
        Map<String, Object> datos = Map.of(
            "comentariosEncolados", encolados,
            "mensaje", encolados > 0
                ? "Se están reanalizando " + encolados + " comentarios. Espera unos segundos y refresca la estadística."
                : "No hay comentarios pendientes de analizar."
        );
        return ResponseEntity.ok(ApiResponse.success(datos));
    }
}

