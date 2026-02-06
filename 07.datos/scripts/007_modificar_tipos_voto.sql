-- ============================================================================
-- Script: 007_modificar_tipos_voto.sql
-- Descripción: Modifica el sistema de votación para soportar 3 categorías:
--              EXPERIMENTAL, INGENIO_Y_GRACIA, MONUMENTO
-- Fecha: 2026-02-06
-- Autor: Sistema
-- Versión: 2.0 (final - compatible con Hibernate)
-- ============================================================================

-- PASO 1: Eliminar constraint única anterior y función que depende del enum
ALTER TABLE votos DROP CONSTRAINT IF EXISTS votos_id_usuario_id_falla_tipo_voto_key;
DROP FUNCTION IF EXISTS obtener_ranking_fallas(INTEGER, tipo_voto);

-- PASO 2: Eliminar el enum tipo_voto (cascade elimina la columna)
DROP TYPE IF EXISTS tipo_voto CASCADE;

-- PASO 3: Recrear la columna tipo_voto como VARCHAR con CHECK constraint
--         (Compatible con Hibernate @Enumerated(EnumType.STRING))
ALTER TABLE votos 
    ADD COLUMN tipo_voto VARCHAR(30) NOT NULL DEFAULT 'EXPERIMENTAL';

ALTER TABLE votos 
    ALTER COLUMN tipo_voto DROP DEFAULT;

-- PASO 4: Agregar constraint de validación para los 3 tipos
ALTER TABLE votos 
    ADD CONSTRAINT ck_votos_tipo_voto 
    CHECK (tipo_voto IN ('EXPERIMENTAL', 'INGENIO_Y_GRACIA', 'MONUMENTO'));

-- PASO 5: Hacer columna 'valor' opcional (no todos los votos necesitan rating)
ALTER TABLE votos ALTER COLUMN valor DROP NOT NULL;

-- PASO 6: Recrear índice y constraint única
CREATE INDEX idx_votos_tipo_voto ON votos(tipo_voto);
ALTER TABLE votos 
    ADD CONSTRAINT votos_id_usuario_id_falla_tipo_voto_key 
    UNIQUE (id_usuario, id_falla, tipo_voto);

-- PASO 7: Recrear tipo enum solo para la función obtener_ranking_fallas
--         (La tabla ya no lo usa, solo la función lo necesita)
CREATE TYPE tipo_voto AS ENUM (
    'EXPERIMENTAL',
    'INGENIO_Y_GRACIA',
    'MONUMENTO'
);

-- PASO 8: Recrear función obtener_ranking_fallas
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
        AND (p_tipo_voto IS NULL OR v.tipo_voto::tipo_voto = p_tipo_voto)
    WHERE f.activa = true
    GROUP BY f.id_falla, f.nombre, f.seccion
    ORDER BY COUNT(v.id_voto) DESC
    LIMIT p_limite;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- VERIFICACIONES
-- ============================================================================
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'votos'
ORDER BY ordinal_position;

\d votos

-- ============================================================================
-- RESUMEN DE CAMBIOS:
-- - tipo_voto: Cambiado de enum PostgreSQL a VARCHAR(30) con CHECK constraint
-- - Valores: EXPERIMENTAL, INGENIO_Y_GRACIA, MONUMENTO
-- - Compatibilidad: Ahora compatible con Hibernate @Enumerated(EnumType.STRING)
-- - valor: Columna opcional (NULL permitido)
-- - Constraint única: Mantiene 1 voto por usuario por falla por tipo
-- ============================================================================
