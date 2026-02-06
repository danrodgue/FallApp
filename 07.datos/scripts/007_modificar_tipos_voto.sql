-- ============================================================================
-- Script: 007_modificar_tipos_voto.sql
-- Descripción: Modifica el enum tipo_voto para soportar las 3 categorías
--              de votación: EXPERIMENTAL, INGENIO_Y_GRACI, MONUMENTO
-- Fecha: 2026-02-06
-- Autor: Sistema
-- ============================================================================

-- Eliminar el enum actual (solo posible porque no hay datos en votos)
-- NOTA: DROP CASCADE elimina la columna tipo_voto y la función obtener_ranking_fallas
DROP TYPE IF EXISTS tipo_voto CASCADE;

-- Recrear el enum con los nuevos valores
CREATE TYPE tipo_voto AS ENUM (
    'EXPERIMENTAL',
    'INGENIO_Y_GRACIA',
    'MONUMENTO'
);

-- Recrear la columna tipo_voto en la tabla votos
ALTER TABLE votos 
    ADD COLUMN tipo_voto tipo_voto NOT NULL DEFAULT 'EXPERIMENTAL'::tipo_voto;

-- Eliminar el default después de crear la columna
ALTER TABLE votos 
    ALTER COLUMN tipo_voto DROP DEFAULT;

-- Recrear índice y constraint única
CREATE INDEX idx_votos_tipo_voto ON votos(tipo_voto);
ALTER TABLE votos 
    ADD CONSTRAINT votos_id_usuario_id_falla_tipo_voto_key 
    UNIQUE (id_usuario, id_falla, tipo_voto);

-- Recrear la función obtener_ranking_fallas con los nuevos tipos
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

-- Verificar que todo está correcto
SELECT 
    enumlabel as tipo_voto_disponible
FROM pg_enum
WHERE enumtypid = 'tipo_voto'::regtype
ORDER BY enumsortorder;

-- Confirmar estructura de la tabla
\d votos

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
