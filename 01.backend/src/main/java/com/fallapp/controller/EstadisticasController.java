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
 * Controlador REST para Estadísticas y Analytics del sistema FallApp
 * 
 * Endpoints: /api/estadisticas
 * 
 * Funcionalidad:
 * - GET /api/estadisticas/resumen - Dashboard general (totales, promedios, actividad)
 * - GET /api/estadisticas/fallas - Analytics por categoría, sección, año fundación
 * - GET /api/estadisticas/votos - Top 10 fallas y ninots más votados
 * - GET /api/estadisticas/usuarios - Distribución por roles, actividad
 * - GET /api/estadisticas/actividad - Stream de actividad reciente (15 días)
 * - GET /api/estadisticas/eventos - Distribución por tipo, participación
 * 
 * Características:
 * - Todos los endpoints son públicos (lectura sin autenticación)
 * - Agregaciones realizadas en capa de servicio (no en BD)
 * - Respuestas tipo Map<String, Object> para flexibilidad JSON
 * - Performance: Queries optimizadas con índices en BD
 * 
 * Estructura de Respuestas:
 * - Resumen: {totalFallas, totalEventos, totalNinots, totalUsuarios, totalVotos, promedioVotosPorFalla, actividadReciente}
 * - Fallas: {porCategoria, porSeccion, promedioAnyoFundacion}
 * - Votos: {top10Fallas: [{nombre, votos, nota}], top10Ninots: [{nombre, votos, nota}]}
 * - Usuarios: {porRol: {admin: X, casal: Y, usuario: Z}, totalActivos}
 * - Actividad: {usuariosNuevos, fallasNuevas, votosRecientes, comentariosRecientes}
 * - Eventos: {porTipo, totalParticipantes, eventosPorFalla}
 * 
 * Casos de Uso:
 * - Dashboard administrativo: obtenerResumen()
 * - Reportes de participación: obtenerEstadisticasVotos()
 * - Analytics de usuarios: obtenerEstadisticasUsuarios()
 * - Timeline de actividad: obtenerActividadReciente()
 * 
 * Seguridad:
 * - No requieren autenticación (datos agregados, no sensibles)
 * - Respuestas siguen ApiResponse<Map<String, Object>> estándar (ADR-007)
 * - Sin paginación (agregaciones pequeñas, <100 elementos)
 * 
 * @see EstadisticasService Business logic y agregaciones
 * @see ADR-007 Formato estándar de respuesta API
 * 
 * @author FallApp Development Team
 * @version 0.4.0
 * @since 2026-02-01
 */
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@Tag(name = "Estadísticas", description = "Analytics y estadísticas del sistema")
public class EstadisticasController {
    
    private final EstadisticasService estadisticasService;
    
    /**
     * GET /api/estadisticas/resumen - Resumen general del sistema
     * 
     * Dashboard principal con métricas clave:
     * - Contadores totales (fallas, eventos, ninots, usuarios, votos)
     * - Promedios (votos por falla, nota promedio)
     * - Actividad reciente (últimos 15 días)
     * 
     * Ejemplo de respuesta:
     * {
     *   "totalFallas": 347,
     *   "totalEventos": 128,
     *   "totalNinots": 215,
     *   "totalUsuarios": 89,
     *   "totalVotos": 1024,
     *   "promedioVotosPorFalla": 2.95,
     *   "actividadReciente": {
     *     "nuevosUsuarios": 12,
     *     "nuevasVotos": 45
     *   }
     * }
     * 
     * @return ApiResponse<Map> con métricas agregadas del sistema
     */
    @GetMapping("/resumen")
    @Operation(summary = "Obtener resumen general de estadísticas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerResumen() {
        Map<String, Object> resumen = estadisticasService.obtenerResumenGeneral();
        return ResponseEntity.ok(ApiResponse.success(resumen));
    }
    
    /**
     * GET /api/estadisticas/fallas - Analytics de fallas
     * 
     * Distribuciones y agregaciones:
     * - Por categoría (especial, primera, segunda, tercera, etc.)
     * - Por sección (Valencia, Russafa, Centro, etc.)
     * - Promedio de año de fundación
     * 
     * Ejemplo de respuesta:
     * {
     *   "porCategoria": {"especial": 15, "primera": 42, "segunda": 78, ...},
     *   "porSeccion": {"Valencia": 120, "Russafa": 87, ...},
     *   "promedioAnyoFundacion": 1978.5
     * }
     * 
     * @return ApiResponse<Map> con distribuciones de fallas
     */
    @GetMapping("/fallas")
    @Operation(summary = "Obtener estadísticas de fallas por categoría y sección")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticasFallas() {
        Map<String, Object> estadisticas = estadisticasService.obtenerEstadisticasFallas();
        return ResponseEntity.ok(ApiResponse.success(estadisticas));
    }
    
    /**
     * GET /api/estadisticas/votos - Estadísticas de votaciones
     */
    @GetMapping("/votos")
    @Operation(summary = "Obtener estadísticas de votos y ninots más votados")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticasVotos(
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String tipoVoto
    ) {
        Map<String, Object> estadisticas = estadisticasService.obtenerEstadisticasVotos(limite, tipoVoto);
        return ResponseEntity.ok(ApiResponse.success(estadisticas));
    }
    
    /**
     * GET /api/estadisticas/usuarios - Estadísticas de usuarios
     */
    @GetMapping("/usuarios")
    @Operation(summary = "Obtener estadísticas de usuarios activos y distribución por rol")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticasUsuarios() {
        Map<String, Object> estadisticas = estadisticasService.obtenerEstadisticasUsuarios();
        return ResponseEntity.ok(ApiResponse.success(estadisticas));
    }
    
    /**
     * GET /api/estadisticas/actividad - Actividad reciente del sistema
     */
    @GetMapping("/actividad")
    @Operation(summary = "Obtener actividad reciente: últimos comentarios, votos, usuarios")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerActividadReciente() {
        Map<String, Object> actividad = estadisticasService.obtenerActividadReciente();
        return ResponseEntity.ok(ApiResponse.success(actividad));
    }
    
    /**
     * GET /api/estadisticas/eventos - Estadísticas de eventos
     */
    @GetMapping("/eventos")
    @Operation(summary = "Obtener estadísticas de eventos por tipo y próximos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticasEventos() {
        Map<String, Object> estadisticas = estadisticasService.obtenerEstadisticasEventos();
        return ResponseEntity.ok(ApiResponse.success(estadisticas));
    }
}
