

ALTER TABLE votos DROP CONSTRAINT IF EXISTS votos_id_usuario_id_falla_tipo_voto_key;
DROP FUNCTION IF EXISTS obtener_ranking_fallas(INTEGER, tipo_voto);


DROP TYPE IF EXISTS tipo_voto CASCADE;



ALTER TABLE votos
    ADD COLUMN tipo_voto VARCHAR(30) NOT NULL DEFAULT 'EXPERIMENTAL';

ALTER TABLE votos
    ALTER COLUMN tipo_voto DROP DEFAULT;


ALTER TABLE votos
    ADD CONSTRAINT ck_votos_tipo_voto
    CHECK (tipo_voto IN ('EXPERIMENTAL', 'INGENIO_Y_GRACIA', 'MONUMENTO'));



ALTER TABLE votos DROP CONSTRAINT IF EXISTS ck_votos_valor;



DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='votos' AND column_name='valor' AND data_type='boolean'
    ) THEN
        ALTER TABLE votos ALTER COLUMN valor TYPE INTEGER USING (CASE WHEN valor IS TRUE THEN 1 WHEN valor IS FALSE THEN 0 ELSE NULL END);
    END IF;
END$$;


UPDATE votos SET valor = 1 WHERE valor IS NOT NULL;


ALTER TABLE votos ALTER COLUMN valor SET DEFAULT 1;
ALTER TABLE votos ALTER COLUMN valor SET NOT NULL;
ALTER TABLE votos ADD CONSTRAINT ck_votos_valor CHECK (valor = 1);


CREATE INDEX idx_votos_tipo_voto ON votos(tipo_voto);
ALTER TABLE votos
    ADD CONSTRAINT votos_id_usuario_id_falla_tipo_voto_key
    UNIQUE (id_usuario, id_falla, tipo_voto);



CREATE TYPE tipo_voto AS ENUM (
    'EXPERIMENTAL',
    'INGENIO_Y_GRACIA',
    'MONUMENTO'
);


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

SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'votos'
ORDER BY ordinal_position;

\d votos

