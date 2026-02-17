package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
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
}

