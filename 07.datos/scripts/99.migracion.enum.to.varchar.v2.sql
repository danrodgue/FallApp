-- =====================================================
-- Script de Migración MEJORADO: ENUM → VARCHAR
-- Fecha: 2026-02-01  
-- Propósito: Resolver ADR-008 eliminando TODAS las vistas
-- =====================================================

\echo '🔧 INICIANDO MIGRACIÓN ENUM → VARCHAR'

-- PASO 1: Guardar definiciones de TODAS las vistas
\echo '📦 Guardando definiciones de vistas...'

SELECT table_name, pg_get_viewdef(table_name::regclass, true) 
FROM information_schema.views 
WHERE table_schema = 'public'
ORDER BY table_name;

-- PASO 2: Eliminar TODAS las vistas en cascada
\echo '🗑️  Eliminando vistas...'

DROP VIEW IF EXISTS v_actividad_usuarios CASCADE;
DROP VIEW IF EXISTS v_usuarios_contenido CASCADE;
DROP VIEW IF EXISTS v_busqueda_fallas_fts CASCADE;
DROP VIEW IF EXISTS v_estadisticas_fallas CASCADE;
DROP VIEW IF EXISTS v_eventos_proximos CASCADE;
DROP VIEW IF EXISTS v_fallas_comentarios CASCADE;
DROP VIEW IF EXISTS v_fallas_mas_votadas CASCADE;
DROP VIEW IF EXISTS v_fallas_por_seccion CASCADE;
DROP VIEW IF EXISTS v_ninots_mas_comentados CASCADE;

-- PASO 3: Migrar columna rol
\echo '🔄 Migrando columna rol...'

ALTER TABLE usuarios ALTER COLUMN rol DROP DEFAULT;

ALTER TABLE usuarios 
ALTER COLUMN rol TYPE VARCHAR(20) 
USING rol::text;

ALTER TABLE usuarios 
ADD CONSTRAINT check_rol_values 
CHECK (rol IN ('admin', 'casal', 'usuario'));

ALTER TABLE usuarios 
ALTER COLUMN rol SET DEFAULT 'usuario';

\echo '✅ Columna rol migrada a VARCHAR(20)'

-- PASO 4: Verificar migración
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_name='usuarios' AND column_name='rol';

-- PASO 5: Recrear vistas (copiadas de 30.vistas.consultas.sql)
\echo '🏗️  Recreando vistas...'

-- Vista 1: Estadísticas completas de fallas
CREATE VIEW v_estadisticas_fallas AS
SELECT 
    f.id_falla,
    f.nombre_falla,
    f.seccion,
    COUNT(DISTINCT v.id_voto) as total_votos,
    COUNT(DISTINCT c.id_comentario) as total_comentarios,
    COUNT(DISTINCT n.id_ninot) as total_ninots,
    AVG(CASE WHEN v.tipo_voto = 'rating' THEN v.valor_numerico END) as rating_promedio,
    COUNT(DISTINCT CASE WHEN v.tipo_voto = 'favorito' THEN v.id_voto END) as votos_favorito,
    COUNT(DISTINCT e.id_evento) as total_eventos
FROM fallas f
LEFT JOIN votos v ON f.id_falla = v.id_falla
LEFT JOIN comentarios c ON f.id_falla = c.id_falla
LEFT JOIN ninots n ON f.id_falla = n.id_falla
LEFT JOIN eventos e ON f.id_falla = e.id_falla
GROUP BY f.id_falla, f.nombre_falla, f.seccion;

-- Vista 2: Fallas más votadas
CREATE VIEW v_fallas_mas_votadas AS
SELECT 
    f.id_falla,
    f.nombre_falla,
    f.seccion,
    f.direccion,
    COUNT(DISTINCT v.id_voto) as total_votos,
    COUNT(DISTINCT CASE WHEN v.tipo_voto = 'favorito' THEN v.id_voto END) as votos_favorito,
    AVG(CASE WHEN v.tipo_voto = 'rating' THEN v.valor_numerico END) as rating_promedio
FROM fallas f
LEFT JOIN votos v ON f.id_falla = v.id_falla
GROUP BY f.id_falla, f.nombre_falla, f.seccion, f.direccion
ORDER BY total_votos DESC, votos_favorito DESC;

-- Vista 3: Fallas con comentarios
CREATE VIEW v_fallas_comentarios AS
SELECT 
    f.id_falla,
    f.nombre_falla,
    COUNT(c.id_comentario) as total_comentarios,
    MAX(c.creado_en) as ultimo_comentario
FROM fallas f
LEFT JOIN comentarios c ON f.id_falla = c.id_falla
GROUP BY f.id_falla, f.nombre_falla
HAVING COUNT(c.id_comentario) > 0
ORDER BY total_comentarios DESC;

-- Vista 4: Ninots más comentados
CREATE VIEW v_ninots_mas_comentados AS
SELECT 
    n.id_ninot,
    n.nombre_ninot,
    n.titulo_obra,
    n.artista_constructor,
    f.nombre_falla,
    COUNT(c.id_comentario) as total_comentarios
FROM ninots n
INNER JOIN fallas f ON n.id_falla = f.id_falla
LEFT JOIN comentarios c ON n.id_ninot = c.id_ninot
GROUP BY n.id_ninot, n.nombre_ninot, n.titulo_obra, n.artista_constructor, f.nombre_falla
HAVING COUNT(c.id_comentario) > 0
ORDER BY total_comentarios DESC;

-- Vista 5: Actividad de usuarios
CREATE VIEW v_actividad_usuarios AS
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
GROUP BY u.id_usuario, u.nombre_completo, u.email, u.rol, u.ultimo_acceso, u.fecha_registro
ORDER BY total_votos DESC, total_comentarios DESC;

-- Vista 6: Fallas por sección
CREATE VIEW v_fallas_por_seccion AS
SELECT 
    f.seccion,
    COUNT(f.id_falla) as total_fallas,
    COUNT(DISTINCT n.id_ninot) as total_ninots,
    COUNT(DISTINCT v.id_voto) as total_votos,
    AVG(CASE WHEN v.tipo_voto = 'rating' THEN v.valor_numerico END) as rating_promedio_seccion
FROM fallas f
LEFT JOIN ninots n ON f.id_falla = n.id_falla
LEFT JOIN votos v ON f.id_falla = v.id_falla
GROUP BY f.seccion
ORDER BY total_fallas DESC;

-- Vista 7: Próximos eventos
CREATE VIEW v_eventos_proximos AS
SELECT 
    e.id_evento,
    e.nombre_evento,
    e.tipo_evento,
    e.fecha_inicio,
    e.fecha_fin,
    f.nombre_falla,
    f.seccion,
    e.descripcion
FROM eventos e
LEFT JOIN fallas f ON e.id_falla = f.id_falla
WHERE e.fecha_inicio >= CURRENT_TIMESTAMP
ORDER BY e.fecha_inicio ASC;

-- Vista 8: Usuarios creadores de contenido
CREATE VIEW v_usuarios_contenido AS
SELECT 
    u.id_usuario,
    u.nombre_completo,
    u.email,
    u.rol,
    COUNT(DISTINCT c.id_comentario) as total_comentarios,
    COUNT(DISTINCT v.id_voto) as total_votos,
    (COUNT(DISTINCT c.id_comentario) + COUNT(DISTINCT v.id_voto)) as total_interacciones
FROM usuarios u
LEFT JOIN comentarios c ON u.id_usuario = c.id_usuario
LEFT JOIN votos v ON u.id_usuario = v.id_usuario
WHERE u.activo = true
GROUP BY u.id_usuario, u.nombre_completo, u.email, u.rol
HAVING (COUNT(DISTINCT c.id_comentario) + COUNT(DISTINCT v.id_voto)) > 0
ORDER BY total_interacciones DESC;

-- Vista 9: Búsqueda full-text
CREATE VIEW v_busqueda_fallas_fts AS
SELECT 
    f.id_falla,
    f.nombre_falla,
    f.seccion,
    f.direccion,
    f.lema,
    to_tsvector('spanish', 
        COALESCE(f.nombre_falla, '') || ' ' ||
        COALESCE(f.lema, '') || ' ' ||
        COALESCE(f.descripcion, '') || ' ' ||
        COALESCE(f.seccion, '')
    ) as documento_busqueda
FROM fallas f;

\echo '✅ Todas las vistas recreadas'

-- PASO 6: Test de INSERT/UPDATE
\echo '🧪 Probando INSERT/UPDATE...'

INSERT INTO usuarios (email, contraseña_hash, nombre_completo, rol, telefono)
VALUES ('test_migracion@fallapp.es', '$2a$10$test', 'Test Migración', 'usuario', '600000000')
RETURNING id_usuario, email, rol;

UPDATE usuarios 
SET ultimo_acceso = CURRENT_TIMESTAMP 
WHERE email = 'test_migracion@fallapp.es'
RETURNING email, ultimo_acceso;

DELETE FROM usuarios WHERE email = 'test_migracion@fallapp.es';

\echo '✅ INSERT/UPDATE funcionando correctamente'

-- PASO 7: Verificación final
\echo '📊 Verificación final:'

SELECT 
    'Total usuarios' as metrica, 
    COUNT(*)::text as valor 
FROM usuarios
UNION ALL
SELECT 
    'Total vistas', 
    COUNT(*)::text 
FROM information_schema.views 
WHERE table_schema = 'public'
UNION ALL
SELECT 
    'Tipo columna rol',
    data_type
FROM information_schema.columns 
WHERE table_name='usuarios' AND column_name='rol';

\echo ''
\echo '✅ ========================================='
\echo '✅ MIGRACIÓN COMPLETADA EXITOSAMENTE'
\echo '✅ ========================================='
