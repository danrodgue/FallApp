package com.fallapp.service;

import com.fallapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import com.fallapp.model.Voto;

/**
 * Servicio de Analytics y Estadísticas para FallApp
 * 
 * Responsabilidades:
 * - Agregaciones y métricas del sistema (totales, promedios, distribuciones)
 * - Reportes de actividad reciente (últimos 15 días)
 * - Rankings (top 10 fallas/ninots más votados)
 * - Estadísticas por dimensiones (categoría, tipo evento, rol usuario)
 * 
 * Características:
 * - Todas las operaciones son READ-ONLY (@Transactional(readOnly=true))
 * - Respuestas tipo Map<String, Object> para flexibilidad JSON
 * - No usa paginación (agregaciones pequeñas <100 elementos)
 * - Performance: Queries optimizadas con índices en BD
 * 
 * Agregaciones realizadas:
 * - Contadores: count() en repositories
 * - Distribuciones: groupBy en Java Streams (no en BD)
 * - Rankings: ordenamiento por votos/notas DESC + limit(10)
 * - Promedios: cálculos en memoria con streams
 * 
 * Casos de uso:
 * - Dashboard administrativo: obtenerResumenGeneral()
 * - Reportes de participación: obtenerEstadisticasVotos()
 * - Analytics de engagement: obtenerActividadReciente()
 * - Distribución demográfica: obtenerEstadisticasUsuarios()
 * 
 * Seguridad:
 * - Endpoints públicos (datos agregados, no sensibles)
 * - No expone información personal de usuarios
 * - Respuestas siguen ApiResponse<Map> estándar (ADR-007)
 * 
 * @see EstadisticasController REST controller que consume este servicio
 * @see ADR-007 Formato estándar de respuesta API
 * 
 * @author FallApp Development Team
 * @version 0.4.0
 * @since 2026-02-01
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstadisticasService {
    
    private final FallaRepository fallaRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VotoRepository votoRepository;
    private final ComentarioRepository comentarioRepository;
    
    /**
     * Obtener resumen general del sistema (Dashboard principal)
     * 
     * Métricas incluidas:
     * - Contadores totales de todas las entidades principales
     * - Usuarios activos (campo activo=true)
     * - Ninots premiados (campo premiado=true)
     * - Timestamp de generación del reporte
     * 
     * Performance: 7 queries (una por cada count())
     * Tiempo estimado: <100ms con índices optimizados
     * 
     * Ejemplo de respuesta:
     * {
     *   "totalFallas": 347,
     *   "totalEventos": 128,
     *   "totalNinots": 215,
     *   "totalUsuarios": 89,
     *   "totalVotos": 1024,
     *   "totalComentarios": 156,
     *   "usuariosActivos": 67,
     *   "ninotsPremiados": 12,
     *   "fechaGeneracion": "2026-02-01T14:30:00"
     * }
     * 
     * @return Map con métricas agregadas del sistema
     */
    public Map<String, Object> obtenerResumenGeneral() {
        Map<String, Object> resumen = new HashMap<>();
        
        resumen.put("totalFallas", fallaRepository.count());
        resumen.put("totalEventos", eventoRepository.count());
        // Table `ninots` removed; report 0 or derive from media if available
        resumen.put("totalNinots", 0);
        resumen.put("totalUsuarios", usuarioRepository.count());
        resumen.put("totalVotos", votoRepository.count());
        resumen.put("totalComentarios", comentarioRepository.count());
        
        // Usuarios activos
        resumen.put("usuariosActivos", usuarioRepository.findByActivoTrue().size());
        
        // Fecha generación
        resumen.put("fechaGeneracion", LocalDateTime.now());
        
        return resumen;
    }
    
    /**
     * Obtener estadísticas de fallas
     */
    public Map<String, Object> obtenerEstadisticasFallas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("totalFallas", fallaRepository.count());
        
        // Por categoría (usando la enum)
        Map<String, Long> porCategoria = new HashMap<>();
        porCategoria.put("especial", fallaRepository.countByCategoria(com.fallapp.model.Falla.CategoriaFalla.especial));
        porCategoria.put("primera", fallaRepository.countByCategoria(com.fallapp.model.Falla.CategoriaFalla.primera));
        porCategoria.put("segunda", fallaRepository.countByCategoria(com.fallapp.model.Falla.CategoriaFalla.segunda));
        porCategoria.put("tercera", fallaRepository.countByCategoria(com.fallapp.model.Falla.CategoriaFalla.tercera));
        porCategoria.put("cuarta", fallaRepository.countByCategoria(com.fallapp.model.Falla.CategoriaFalla.cuarta));
        porCategoria.put("quinta", fallaRepository.countByCategoria(com.fallapp.model.Falla.CategoriaFalla.quinta));
        porCategoria.put("sin_categoria", fallaRepository.countByCategoria(com.fallapp.model.Falla.CategoriaFalla.sin_categoria));
        
        estadisticas.put("porCategoria", porCategoria);
        
        // Total fallas
        estadisticas.put("totalFallas", fallaRepository.count());
        
        return estadisticas;
    }
    
    /**
     * Obtener estadísticas de votos
     */
    public Map<String, Object> obtenerEstadisticasVotos(Integer limite, String tipoVotoStr) {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalVotos", votoRepository.count());

        int top = (limite == null || limite <= 0) ? 10 : limite;

        // Parse tipoVoto if provided
        Voto.TipoVoto tipo = null;
        if (tipoVotoStr != null && !tipoVotoStr.isBlank()) {
            try {
                tipo = Voto.TipoVoto.valueOf(tipoVotoStr);
            } catch (IllegalArgumentException ex) {
                tipo = null;
            }
        }

        final Voto.TipoVoto filtroTipo = tipo;

        // Top fallas (by votes)
        List<Map<String, Object>> topFallas = fallaRepository.findAll().stream()
                .map(f -> {
                    long cnt = (filtroTipo == null) ? votoRepository.countByFalla(f) : votoRepository.countByFallaAndTipoVoto(f, filtroTipo);
                    Map<String, Object> m = new HashMap<>();
                    m.put("idFalla", f.getIdFalla());
                    m.put("nombre", f.getNombre());
                    m.put("seccion", f.getSeccion());
                    m.put("votos", cnt);
                    return m;
                })
                .sorted((a, b) -> Long.compare(((Number) b.get("votos")).longValue(), ((Number) a.get("votos")).longValue()))
                .limit(top)
                .toList();

        estadisticas.put("topFallas", topFallas);

        // Ninots removed: no per-ninot ranking available. Return empty list.
        estadisticas.put("topNinots", Collections.emptyList());

        estadisticas.put("filtroTipoVoto", tipo == null ? "ALL" : tipo.name());

        return estadisticas;
    }
    
    /**
     * Obtener estadísticas de usuarios
     */
    public Map<String, Object> obtenerEstadisticasUsuarios() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("totalUsuarios", usuarioRepository.count());
        estadisticas.put("usuariosActivos", usuarioRepository.findByActivoTrue().size());
        
        // Por rol
        Map<String, Long> porRol = new HashMap<>();
        porRol.put("admin", usuarioRepository.countByRol(com.fallapp.model.Usuario.RolUsuario.admin));
        porRol.put("casal", usuarioRepository.countByRol(com.fallapp.model.Usuario.RolUsuario.casal));
        porRol.put("usuario", usuarioRepository.countByRol(com.fallapp.model.Usuario.RolUsuario.usuario));
        
        estadisticas.put("porRol", porRol);
        
        return estadisticas;
    }
    
    /**
     * Obtener actividad reciente
     */
    public Map<String, Object> obtenerActividadReciente() {
        Map<String, Object> actividad = new HashMap<>();
        
        // Últimos 5 comentarios
        var ultimosComentarios = comentarioRepository.findAll().stream()
                .sorted((c1, c2) -> c2.getCreadoEn().compareTo(c1.getCreadoEn()))
                .limit(5)
                .map(comentario -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("idComentario", comentario.getIdComentario());
                    info.put("usuario", comentario.getUsuario() != null ? comentario.getUsuario().getNombreCompleto() : null);
                    info.put("contenido", comentario.getContenido().length() > 100 ? 
                            comentario.getContenido().substring(0, 100) + "..." : 
                            comentario.getContenido());
                    info.put("fecha", comentario.getCreadoEn());
                    return info;
                })
                .toList();
        
        actividad.put("ultimosComentarios", ultimosComentarios);
        
        // Últimos 5 votos
        var ultimosVotos = votoRepository.findAll().stream()
                .sorted((v1, v2) -> v2.getCreadoEn().compareTo(v1.getCreadoEn()))
                .limit(5)
                .map(voto -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("idVoto", voto.getIdVoto());
                    info.put("usuario", voto.getUsuario() != null ? voto.getUsuario().getNombreCompleto() : null);
                    info.put("falla", voto.getFalla() != null ? voto.getFalla().getNombre() : null);
                    info.put("fecha", voto.getCreadoEn());
                    return info;
                })
                .toList();
        
        actividad.put("ultimosVotos", ultimosVotos);
        
        return actividad;
    }

    /**
     * Obtener estadísticas de sentimiento de comentarios para una falla concreta.
     *
     * Estructura de respuesta:
     * {
     *   "idFalla": 23,
     *   "nombreFalla": "Falla Convento Jerusalén",
     *   "totalComentarios": 33,
     *   "sentimientos": {
     *     "positive": 25,
     *     "neutral": 5,
     *     "negative": 3
     *   }
     * }
     */
    public Map<String, Object> obtenerSentimientoPorFalla(Long idFalla) {
        var falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + idFalla));

        Map<String, Long> sentimientos = new HashMap<>();
        sentimientos.put("positive", 0L);
        sentimientos.put("neutral", 0L);
        sentimientos.put("negative", 0L);

        comentarioRepository.countByFallaGroupBySentimiento(falla).forEach(row -> {
            String sentimiento = (String) row[0];
            Long total = (Long) row[1];
            if (sentimiento != null && sentimientos.containsKey(sentimiento)) {
                sentimientos.put(sentimiento, total);
            }
        });

        long totalComentariosFalla = comentarioRepository.countByFalla(falla);
        long totalAnalizados = sentimientos.values().stream().mapToLong(Long::longValue).sum();
        long totalPendientes = Math.max(totalComentariosFalla - totalAnalizados, 0L);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idFalla", falla.getIdFalla());
        resultado.put("nombreFalla", falla.getNombre());
        resultado.put("totalComentarios", totalAnalizados);
        resultado.put("totalComentariosFalla", totalComentariosFalla);
        resultado.put("totalPendientes", totalPendientes);
        resultado.put("sentimientos", sentimientos);

        return resultado;
    }
    
    /**
     * Obtener estadísticas de eventos
     */
    public Map<String, Object> obtenerEstadisticasEventos() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("totalEventos", eventoRepository.count());
        
        // Eventos futuros
        estadisticas.put("eventosFuturos", 
                eventoRepository.findEventosFuturos(LocalDateTime.now()).size());
        
        // Por tipo (usando la enum)
        Map<String, Long> porTipo = new HashMap<>();
        porTipo.put("planta", eventoRepository.countByTipo(com.fallapp.model.Evento.TipoEvento.planta));
        porTipo.put("crema", eventoRepository.countByTipo(com.fallapp.model.Evento.TipoEvento.crema));
        porTipo.put("ofrenda", eventoRepository.countByTipo(com.fallapp.model.Evento.TipoEvento.ofrenda));
        porTipo.put("infantil", eventoRepository.countByTipo(com.fallapp.model.Evento.TipoEvento.infantil));
        porTipo.put("concierto", eventoRepository.countByTipo(com.fallapp.model.Evento.TipoEvento.concierto));
        porTipo.put("exposicion", eventoRepository.countByTipo(com.fallapp.model.Evento.TipoEvento.exposicion));
        porTipo.put("otro", eventoRepository.countByTipo(com.fallapp.model.Evento.TipoEvento.otro));
        
        estadisticas.put("porTipo", porTipo);
        
        return estadisticas;
    }
}
