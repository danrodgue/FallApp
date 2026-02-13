-- =============================================================================
-- 30.vistas.consultas.sql
-- Vistas y consultas útiles para reportes y análisis de datos
--
-- Crea:
--   - 9 vistas especializadas para consultas complejas
--   - 2 funciones SQL parametrizadas y reutilizables
--
-- Beneficios:
--   - Reutilización: Misma lógica en múltiples endpoints
--   - Performance: Optimización por PostgreSQL
--   - DRY: Sin duplicación de JOINs en backend
--
-- ADRs relacionados:
--   - ADR-005: Justificación de Vistas SQL vs Queries en Backend
--
-- Ejecución: Después de 01.schema.sql y datos (usuarios/fallas importados)
-- =============================================================================

-- =============================================================================
-- 1. VISTA: Estadísticas generales de fallas
-- =============================================================================

CREATE OR REPLACE VIEW v_estadisticas_fallas AS
SELECT 
    f.id_falla,
    f.nombre,
    f.seccion,
    f.anyo_fundacion,
    f.categoria,
    f.artista,
    f.presidente,
    COUNT(DISTINCT e.id_evento) as total_eventos,
    COUNT(DISTINCT n.id_ninot) as total_ninots,
    COUNT(DISTINCT v.id_voto) as total_votos,
    AVG(CASE WHEN v.tipo_voto = 'rating' THEN v.valor END) as promedio_rating,
    COUNT(DISTINCT c.id_comentario) as total_comentarios,
    MAX(c.fecha_creacion) as ultimo_comentario
FROM fallas f
LEFT JOIN eventos e ON f.id_falla = e.id_falla
LEFT JOIN ninots n ON f.id_falla = n.id_falla
LEFT JOIN votos v ON f.id_falla = v.id_falla
LEFT JOIN comentarios c ON f.id_falla = c.id_falla
GROUP BY 
    f.id_falla, f.nombre, f.seccion, f.anyo_fundacion, 
    f.categoria, f.artista, f.presidente
ORDER BY f.nombre;

-- =============================================================================
-- 2. VISTA: Fallas más votadas
-- =============================================================================

CREATE OR REPLACE VIEW v_fallas_mas_votadas AS
SELECT 
    f.id_falla,
    f.nombre,
    f.seccion,
    COUNT(v.id_voto) as total_votos,
    AVG(CASE WHEN v.tipo_voto = 'rating' THEN v.valor END) as rating_promedio,
    COUNT(DISTINCT CASE WHEN v.tipo_voto = 'me_gusta' THEN v.id_usuario END) as me_gustas,
    COUNT(DISTINCT CASE WHEN v.tipo_voto = 'mejor_ninot' THEN v.id_usuario END) as votos_ninot,
    COUNT(DISTINCT CASE WHEN v.tipo_voto = 'mejor_tema' THEN v.id_usuario END) as votos_tema,
    RANK() OVER (ORDER BY COUNT(v.id_voto) DESC) as ranking_votos
FROM fallas f
LEFT JOIN votos v ON f.id_falla = v.id_falla
WHERE f.activa = true
GROUP BY f.id_falla, f.nombre, f.seccion
HAVING COUNT(v.id_voto) > 0
ORDER BY total_votos DESC;

-- =============================================================================
-- 3. VISTA: Fallas con comentarios moderados
-- =============================================================================

CREATE OR REPLACE VIEW v_fallas_comentarios AS
SELECT 
    f.id_falla,
    f.nombre,
    f.seccion,
    COUNT(c.id_comentario) as total_comentarios,
    COUNT(CASE WHEN c.visible = true THEN 1 END) as comentarios_visibles,
    COUNT(CASE WHEN c.visible = false THEN 1 END) as comentarios_ocultos,
    ROUND(AVG(CASE WHEN c.rating IS NOT NULL THEN c.rating END)::numeric, 2) as rating_promedio,
    MAX(c.fecha_creacion) as ultimo_comentario
FROM fallas f
LEFT JOIN comentarios c ON f.id_falla = c.id_falla
WHERE f.activa = true
GROUP BY f.id_falla, f.nombre, f.seccion
HAVING COUNT(c.id_comentario) > 0
ORDER BY total_comentarios DESC;

-- =============================================================================
-- 4. VISTA: Top Ninots más comentados
-- =============================================================================

CREATE OR REPLACE VIEW v_ninots_mas_comentados AS
SELECT 
    n.id_ninot,
    n.nombre_ninot,
    n.titulo_obra,
    f.nombre as nombre_falla,
    f.seccion,
    COUNT(v.id_voto) as votos_ninot,
    n.premiado,
    n.categoria_premio,
    RANK() OVER (ORDER BY COUNT(v.id_voto) DESC) as ranking
FROM ninots n
JOIN fallas f ON n.id_falla = f.id_falla
LEFT JOIN votos v ON f.id_falla = v.id_falla 
    AND v.tipo_voto = 'mejor_ninot'
GROUP BY n.id_ninot, n.nombre_ninot, n.titulo_obra, 
         f.nombre, f.seccion, n.premiado, n.categoria_premio
HAVING COUNT(v.id_voto) > 0
ORDER BY votos_ninot DESC;

-- =============================================================================
-- 5. VISTA: Actividad de usuarios
-- =============================================================================

CREATE OR REPLACE VIEW v_actividad_usuarios AS
SELECT 
    u.id_usuario,
    u.nombre_completo,
    u.email,
    u.rol,
    COUNT(DISTINCT v.id_voto) as total_votos,
    COUNT(DISTINCT c.id_comentario) as total_comentarios,
    MAX(u.ultimo_acceso) as ultimo_acceso,
    u.fecha_registro,
    CASE 
        WHEN u.ultimo_acceso >= CURRENT_TIMESTAMP - INTERVAL '7 days' THEN 'Activo'
        WHEN u.ultimo_acceso >= CURRENT_TIMESTAMP - INTERVAL '30 days' THEN 'Moderadamente activo'
        WHEN u.ultimo_acceso >= CURRENT_TIMESTAMP - INTERVAL '90 days' THEN 'Inactivo reciente'
        ELSE 'Sin actividad'
    END as estado_actividad
FROM usuarios u
LEFT JOIN votos v ON u.id_usuario = v.id_usuario
LEFT JOIN comentarios c ON u.id_usuario = c.id_usuario
WHERE u.activo = true
GROUP BY u.id_usuario, u.nombre_completo, u.email, u.rol, 
         u.ultimo_acceso, u.fecha_registro
ORDER BY total_votos DESC, total_comentarios DESC;

-- =============================================================================
-- 6. VISTA: Fallas por sección con métricas
-- =============================================================================

CREATE OR REPLACE VIEW v_fallas_por_seccion AS
SELECT 
    f.seccion,
    COUNT(*) as total_fallas_seccion,
    COUNT(CASE WHEN f.activa = true THEN 1 END) as fallas_activas,
    ROUND(AVG(f.anyo_fundacion)::numeric, 0) as anyo_promedio_fundacion,
    COUNT(DISTINCT e.id_evento) as total_eventos,
    COUNT(DISTINCT v.id_voto) as total_votos,
    ROUND(AVG(CASE WHEN v.tipo_voto = 'rating' THEN v.valor END)::numeric, 2) as rating_promedio
FROM fallas f
LEFT JOIN eventos e ON f.id_falla = e.id_falla
LEFT JOIN votos v ON f.id_falla = v.id_falla
GROUP BY f.seccion
ORDER BY f.seccion;

-- =============================================================================
-- 7. VISTA: Eventos próximos (ordenados por fecha)
-- =============================================================================

CREATE OR REPLACE VIEW v_eventos_proximos AS
SELECT 
    e.id_evento,
    e.nombre,
    e.tipo,
    e.fecha_evento,
    f.nombre as nombre_falla,
    f.seccion,
    e.ubicacion,
    e.participantes_estimado,
    CASE 
        WHEN e.fecha_evento < CURRENT_TIMESTAMP THEN 'Pasado'
        WHEN e.fecha_evento <= CURRENT_TIMESTAMP + INTERVAL '7 days' THEN 'Esta semana'
        WHEN e.fecha_evento <= CURRENT_TIMESTAMP + INTERVAL '30 days' THEN 'Este mes'
        ELSE 'Próximo'
    END as proximidad
FROM eventos e
JOIN fallas f ON e.id_falla = f.id_falla
WHERE f.activa = true
ORDER BY e.fecha_evento ASC;

-- =============================================================================
-- 8. VISTA: Usuarios creadores de contenido
-- =============================================================================

CREATE OR REPLACE VIEW v_usuarios_contenido AS
SELECT 
    u.id_usuario,
    u.nombre_completo,
    u.email,
    u.rol,
    COUNT(DISTINCT e.id_evento) as eventos_creados,
    COUNT(DISTINCT c.id_comentario) as comentarios_escritos,
    COUNT(DISTINCT CASE WHEN c.visible = true THEN c.id_comentario END) as comentarios_activos,
    ROUND(AVG(c.rating)::numeric, 2) as rating_comentarios_promedio
FROM usuarios u
LEFT JOIN eventos e ON u.id_usuario = e.creado_por
LEFT JOIN comentarios c ON u.id_usuario = c.id_usuario
WHERE u.activo = true
GROUP BY u.id_usuario, u.nombre_completo, u.email, u.rol
HAVING COUNT(DISTINCT e.id_evento) > 0 OR COUNT(DISTINCT c.id_comentario) > 0
ORDER BY comentarios_escritos DESC, eventos_creados DESC;

-- =============================================================================
-- 9. VISTA: Búsqueda full-text de fallas (helper)
-- =============================================================================

CREATE OR REPLACE VIEW v_busqueda_fallas_fts AS
SELECT 
    id_falla,
    nombre,
    seccion,
    artista,
    lema,
    CAST(
        to_tsvector('spanish', 
            COALESCE(nombre, '') || ' ' || 
            COALESCE(lema, '') || ' ' || 
            COALESCE(artista, '')
        ) 
    AS TEXT) as vector_busqueda
FROM fallas
WHERE activa = true;

-- =============================================================================
-- 10. FUNCIÓN: Búsqueda de fallas por texto
-- =============================================================================

CREATE OR REPLACE FUNCTION buscar_fallas(p_texto VARCHAR)
RETURNS TABLE (
    id_falla INTEGER,
    nombre VARCHAR,
    seccion VARCHAR,
    artista VARCHAR,
    lema TEXT,
    relevancia REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        f.id_falla,
        f.nombre,
        f.seccion,
        f.artista,
        f.lema,
        ts_rank(
            to_tsvector('spanish', 
                COALESCE(f.nombre, '') || ' ' || 
                COALESCE(f.lema, '') || ' ' || 
                COALESCE(f.artista, '')
            ),
            to_tsquery('spanish', p_texto)
        ) as relevancia
    FROM fallas f
    WHERE f.activa = true
        AND to_tsvector('spanish', 
            COALESCE(f.nombre, '') || ' ' || 
            COALESCE(f.lema, '') || ' ' || 
            COALESCE(f.artista, '')
        ) @@ to_tsquery('spanish', p_texto)
    ORDER BY relevancia DESC;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- 11. FUNCIÓN: Obtener ranking de fallas por votos
-- =============================================================================

CREATE OR REPLACE FUNCTION obtener_ranking_fallas(
    p_limite INTEGER DEFAULT 10,
    p_tipo_voto tipo_voto DEFAULT 'EXPERIMENTAL'
)
RETURNS TABLE (
    posicion INTEGER,
    id_falla INTEGER,
    nombre VARCHAR,
    seccion VARCHAR,
    votos_count INTEGER,
    rating_promedio NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ROW_NUMBER() OVER (ORDER BY COUNT(v.id_voto) DESC) as posicion,
        f.id_falla,
        f.nombre,
        f.seccion,
        COUNT(v.id_voto)::INTEGER as votos_count,
        ROUND(AVG(v.valor)::numeric, 2) as rating_promedio
    FROM fallas f
    LEFT JOIN votos v ON f.id_falla = v.id_falla 
        AND (p_tipo_voto IS NULL OR v.tipo_voto = p_tipo_voto)
    WHERE f.activa = true
    GROUP BY f.id_falla, f.nombre, f.seccion
    ORDER BY COUNT(v.id_voto) DESC
    LIMIT p_limite;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- 12. PERMISOS PARA VISTAS
-- =============================================================================

-- Permitir lectura de vistas a usuarios de aplicación
GRANT SELECT ON ALL TABLES IN SCHEMA public TO fallapp_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO fallapp_user;

-- =============================================================================
-- FIN: 30.vistas.consultas.sql
-- =============================================================================
