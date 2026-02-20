

\echo '============================================================================='
\echo 'INICIO: Migración Ninots v1.0 → v2.0'
\echo '============================================================================='

BEGIN;

\echo ''
\echo '[PASO 1] Analizando datos actuales...'


SELECT
    'Ninots en tabla actual' as metrica,
    COUNT(*) as total,
    COUNT(url_imagen_principal) as con_imagen,
    COUNT(altura_metros) as con_altura,
    COUNT(material_principal) as con_material,
    COUNT(artista_constructor) as con_artista
FROM ninots;


SELECT
    'ALERTA: Ninots sin imagen' as alerta,
    COUNT(*) as total
FROM ninots
WHERE url_imagen_principal IS NULL;

\echo ''
\echo '[PASO 2] Creando backup de tabla actual...'

CREATE TABLE ninots_backup_20260202 AS
SELECT * FROM ninots;

\echo 'Backup creado: ninots_backup_20260202'

\echo ''
\echo '[PASO 3] Creando tabla con estructura simplificada...'

CREATE TABLE ninots_new (
    id_ninot SERIAL PRIMARY KEY,
    id_falla INTEGER NOT NULL,
    nombre VARCHAR(255) NULL,
    url_imagen VARCHAR(500) NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ninots_new_id_falla
        FOREIGN KEY (id_falla) REFERENCES fallas(id_falla) ON DELETE CASCADE
);


COMMENT ON TABLE ninots_new IS 'Ninots simplificados v2.0 - Solo URLs de imágenes con datos reales';
COMMENT ON COLUMN ninots_new.id_ninot IS 'ID único del ninot';
COMMENT ON COLUMN ninots_new.id_falla IS 'Falla a la que pertenece el ninot';
COMMENT ON COLUMN ninots_new.nombre IS 'Nombre opcional para identificar el ninot (ej: Boceto 2026)';
COMMENT ON COLUMN ninots_new.url_imagen IS 'URL obligatoria de la imagen del ninot o boceto';
COMMENT ON COLUMN ninots_new.fecha_creacion IS 'Fecha de creación del registro';

\echo ''
\echo '[PASO 4] Migrando datos a nueva estructura...'

INSERT INTO ninots_new (id_ninot, id_falla, nombre, url_imagen, fecha_creacion)
SELECT
    n.id_ninot,
    n.id_falla,
    n.nombre_ninot,
    n.url_imagen_principal,
    n.fecha_creacion
FROM ninots n
WHERE n.url_imagen_principal IS NOT NULL
  AND n.url_imagen_principal != '';


SELECT
    'Registros migrados' as descripcion,
    COUNT(*) as total,
    MIN(id_ninot) as primer_id,
    MAX(id_ninot) as ultimo_id
FROM ninots_new;

\echo ''
\echo '[PASO 5] Actualizando foreign keys en tablas dependientes...'


\echo '  → Actualizando foreign key en tabla votos...'
ALTER TABLE votos DROP CONSTRAINT IF EXISTS fk_votos_id_ninot;
ALTER TABLE votos DROP CONSTRAINT IF EXISTS fk_votos_ninot;


DO $$
DECLARE
    missing_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO missing_count
    FROM votos v
    LEFT JOIN ninots_new n ON v.id_ninot = n.id_ninot
    WHERE v.id_ninot IS NOT NULL AND n.id_ninot IS NULL;

    IF missing_count > 0 THEN
        RAISE WARNING 'ATENCIÓN: % votos referencian ninots que no se migraron', missing_count;
        RAISE WARNING 'Estos votos quedarán huérfanos. Considere eliminarlos o migrar ninots faltantes.';
    END IF;
END $$;

ALTER TABLE votos ADD CONSTRAINT fk_votos_id_ninot
    FOREIGN KEY (id_ninot) REFERENCES ninots_new(id_ninot) ON DELETE CASCADE;


\echo '  → Actualizando foreign key en tabla comentarios...'
ALTER TABLE comentarios DROP CONSTRAINT IF EXISTS fk_comentarios_id_ninot;
ALTER TABLE comentarios DROP CONSTRAINT IF EXISTS fk_comentarios_ninot;


DO $$
DECLARE
    missing_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO missing_count
    FROM comentarios c
    LEFT JOIN ninots_new n ON c.id_ninot = n.id_ninot
    WHERE c.id_ninot IS NOT NULL AND n.id_ninot IS NULL;

    IF missing_count > 0 THEN
        RAISE WARNING 'ATENCIÓN: % comentarios referencian ninots que no se migraron', missing_count;
    END IF;
END $$;

ALTER TABLE comentarios ADD CONSTRAINT fk_comentarios_id_ninot
    FOREIGN KEY (id_ninot) REFERENCES ninots_new(id_ninot) ON DELETE CASCADE;

\echo ''
\echo '[PASO 6] Reemplazando tabla antigua con nueva estructura...'

DROP TABLE ninots CASCADE;
ALTER TABLE ninots_new RENAME TO ninots;


ALTER TABLE ninots RENAME CONSTRAINT fk_ninots_new_id_falla TO fk_ninots_id_falla;

\echo ''
\echo '[PASO 7] Creando índices optimizados...'

CREATE INDEX idx_ninots_id_falla ON ninots(id_falla);
CREATE INDEX idx_ninots_fecha_creacion ON ninots(fecha_creacion);


CREATE INDEX idx_ninots_nombre ON ninots(nombre) WHERE nombre IS NOT NULL;


ANALYZE ninots;

\echo ''
\echo '[PASO 8] Actualizando secuencia de IDs...'

SELECT setval('ninots_id_ninot_seq', COALESCE((SELECT MAX(id_ninot) FROM ninots), 1));

\echo ''
\echo '[PASO 9] Verificando integridad de datos...'


\d ninots


\echo ''
\echo '
SELECT
    'Total ninots' as metrica,
    COUNT(*) as valor
FROM ninots
UNION ALL
SELECT
    'Ninots con nombre',
    COUNT(*)
FROM ninots
WHERE nombre IS NOT NULL
UNION ALL
SELECT
    'Fallas con ninots',
    COUNT(DISTINCT id_falla)
FROM ninots
UNION ALL
SELECT
    'Votos asociados',
    COUNT(*)
FROM votos
WHERE id_ninot IN (SELECT id_ninot FROM ninots)
UNION ALL
SELECT
    'Comentarios asociados',
    COUNT(*)
FROM comentarios
WHERE id_ninot IN (SELECT id_ninot FROM ninots);


\echo ''
\echo '
DO $$
DECLARE
    orphan_votos INTEGER;
    orphan_comentarios INTEGER;
BEGIN

    SELECT COUNT(*) INTO orphan_votos
    FROM votos v
    LEFT JOIN ninots n ON v.id_ninot = n.id_ninot
    WHERE v.id_ninot IS NOT NULL AND n.id_ninot IS NULL;


    SELECT COUNT(*) INTO orphan_comentarios
    FROM comentarios c
    LEFT JOIN ninots n ON c.id_ninot = n.id_ninot
    WHERE c.id_ninot IS NOT NULL AND n.id_ninot IS NULL;

    IF orphan_votos = 0 AND orphan_comentarios = 0 THEN
        RAISE NOTICE '✅ Integridad referencial OK';
        RAISE NOTICE '✅ No hay registros huérfanos';
    ELSE
        IF orphan_votos > 0 THEN
            RAISE WARNING '⚠️  % votos huérfanos detectados', orphan_votos;
        END IF;
        IF orphan_comentarios > 0 THEN
            RAISE WARNING '⚠️  % comentarios huérfanos detectados', orphan_comentarios;
        END IF;
    END IF;
END $$;

\echo ''
\echo '============================================================================='
\echo '✅ MIGRACIÓN COMPLETADA EXITOSAMENTE'
\echo '============================================================================='
\echo ''
\echo 'Acciones post-migración:'
\echo '  1. Verificar que la aplicación funciona correctamente'
\echo '  2. Si hay problemas, hacer ROLLBACK y restaurar desde ninots_backup_20260202'
\echo '  3. Si todo está OK, hacer COMMIT'
\echo '  4. Actualizar código backend (Ninot.java, NinotDTO.java, etc.)'
\echo ''
\echo 'Backup disponible en: ninots_backup_20260202'
\echo ''

\echo 'Esperando decisión: COMMIT o ROLLBACK...'
