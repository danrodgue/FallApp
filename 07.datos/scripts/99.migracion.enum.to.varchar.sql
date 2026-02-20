

BEGIN;


SELECT 'v_actividad_usuarios' as vista,
       pg_get_viewdef('v_actividad_usuarios', true) as definicion
INTO TEMP TABLE backup_vistas;


SELECT 'BACKUP: ' || COUNT(*)::text || ' usuarios antes de migración' as info
FROM usuarios;

DROP VIEW IF EXISTS v_actividad_usuarios CASCADE;


ALTER TABLE usuarios ALTER COLUMN rol DROP DEFAULT;


ALTER TABLE usuarios
ALTER COLUMN rol TYPE VARCHAR(20)
USING rol::text;


ALTER TABLE usuarios
ADD CONSTRAINT check_rol_values
CHECK (rol IN ('admin', 'casal', 'usuario'));


ALTER TABLE usuarios
ALTER COLUMN rol SET DEFAULT 'usuario';


SELECT 'VERIFICACIÓN: Columna rol migrada a VARCHAR' as info;
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name='usuarios' AND column_name='rol';

CREATE OR REPLACE VIEW v_actividad_usuarios AS
SELECT u.id_usuario,
    u.nombre_completo,
    u.email,
    u.rol,
    count(DISTINCT v.id_voto) AS total_votos,
    count(DISTINCT c.id_comentario) AS total_comentarios,
    max(u.ultimo_acceso) AS ultimo_acceso,
    u.fecha_registro,
    CASE
        WHEN u.ultimo_acceso >= (CURRENT_TIMESTAMP - '7 days'::interval) THEN 'Activo'::text
        WHEN u.ultimo_acceso >= (CURRENT_TIMESTAMP - '30 days'::interval) THEN 'Moderadamente activo'::text
        WHEN u.ultimo_acceso >= (CURRENT_TIMESTAMP - '90 days'::interval) THEN 'Inactivo reciente'::text
        ELSE 'Sin actividad'::text
    END AS estado_actividad
FROM usuarios u
LEFT JOIN votos v ON u.id_usuario = v.id_usuario
LEFT JOIN comentarios c ON u.id_usuario = c.id_usuario
WHERE u.activo = true
GROUP BY u.id_usuario, u.nombre_completo, u.email, u.rol, u.ultimo_acceso, u.fecha_registro
ORDER BY (count(DISTINCT v.id_voto)) DESC, (count(DISTINCT c.id_comentario)) DESC;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'ninots'
        AND column_name = 'anyo_construccion'
    ) THEN

        ALTER TABLE ninots RENAME COLUMN anyo_construccion TO año_construccion;
        RAISE NOTICE 'Columna anyo_construccion renombrada a año_construccion';
    ELSE
        RAISE NOTICE 'Columna año_construccion ya existe correctamente';
    END IF;
END $$;

SELECT 'VERIFICACIÓN FINAL usuarios:' as info;
\d usuarios


SELECT 'VERIFICACIÓN FINAL ninots:' as info;
\d ninots


SELECT 'Vistas recreadas:' as info;
SELECT table_name
FROM information_schema.views
WHERE table_schema = 'public'
AND table_name = 'v_actividad_usuarios';


SELECT 'Constraints CHECK:' as info;
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'usuarios'::regclass
AND contype = 'c';


INSERT INTO usuarios (email, contraseña_hash, nombre_completo, rol, telefono)
VALUES ('test_migracion@fallapp.es', '$2a$10$test', 'Usuario Test Migración', 'usuario', '600000000')
RETURNING id_usuario, email, rol;


DELETE FROM usuarios WHERE email = 'test_migracion@fallapp.es';


SELECT COUNT(*) as total_usuarios,
       COUNT(DISTINCT rol) as roles_distintos,
       string_agg(DISTINCT rol, ', ') as roles
FROM usuarios;

COMMIT;

SELECT '✅ MIGRACIÓN COMPLETADA EXITOSAMENTE' as resultado;
