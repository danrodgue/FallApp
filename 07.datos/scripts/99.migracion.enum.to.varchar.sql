-- =====================================================
-- Script de Migración: ENUM → VARCHAR
-- Archivo: 99.migracion.enum.to.varchar.sql
-- Fecha: 2026-02-01
-- Propósito: Resolver incompatibilidad JPA con PostgreSQL ENUMs
-- Relacionado: ADR-008
-- =====================================================

-- IMPORTANTE: Ejecutar en orden, no saltarse pasos

-- =====================================================
-- PASO 1: Backup de datos críticos
-- =====================================================
BEGIN;

-- Guardar definiciones de vistas para recreación
SELECT 'v_actividad_usuarios' as vista, 
       pg_get_viewdef('v_actividad_usuarios', true) as definicion
INTO TEMP TABLE backup_vistas;

-- Verificar datos actuales
SELECT 'BACKUP: ' || COUNT(*)::text || ' usuarios antes de migración' as info
FROM usuarios;

-- =====================================================
-- PASO 2: Migrar columna 'rol' en usuarios
-- =====================================================

-- 2.1: Eliminar vista dependiente temporalmente
DROP VIEW IF EXISTS v_actividad_usuarios CASCADE;

-- 2.2: Eliminar default ENUM
ALTER TABLE usuarios ALTER COLUMN rol DROP DEFAULT;

-- 2.3: Convertir ENUM a VARCHAR
ALTER TABLE usuarios 
ALTER COLUMN rol TYPE VARCHAR(20) 
USING rol::text;

-- 2.4: Añadir constraint CHECK para validación
ALTER TABLE usuarios 
ADD CONSTRAINT check_rol_values 
CHECK (rol IN ('admin', 'casal', 'usuario'));

-- 2.5: Restaurar default VARCHAR
ALTER TABLE usuarios 
ALTER COLUMN rol SET DEFAULT 'usuario';

-- 2.6: Verificar migración
SELECT 'VERIFICACIÓN: Columna rol migrada a VARCHAR' as info;
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_name='usuarios' AND column_name='rol';

-- =====================================================
-- PASO 3: Recrear vista v_actividad_usuarios
-- =====================================================

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

-- =====================================================
-- PASO 4: Migrar columna 'año_construccion' en ninots
-- =====================================================

-- Verificar si existe el problema
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ninots' 
        AND column_name = 'anyo_construccion'
    ) THEN
        -- Renombrar columna si existe con 'y'
        ALTER TABLE ninots RENAME COLUMN anyo_construccion TO año_construccion;
        RAISE NOTICE 'Columna anyo_construccion renombrada a año_construccion';
    ELSE
        RAISE NOTICE 'Columna año_construccion ya existe correctamente';
    END IF;
END $$;

-- =====================================================
-- PASO 5: Verificaciones finales
-- =====================================================

-- Verificar estructura de usuarios
SELECT 'VERIFICACIÓN FINAL usuarios:' as info;
\d usuarios

-- Verificar estructura de ninots  
SELECT 'VERIFICACIÓN FINAL ninots:' as info;
\d ninots

-- Verificar vistas recreadas
SELECT 'Vistas recreadas:' as info;
SELECT table_name 
FROM information_schema.views 
WHERE table_schema = 'public' 
AND table_name = 'v_actividad_usuarios';

-- Verificar constraint CHECK
SELECT 'Constraints CHECK:' as info;
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'usuarios'::regclass
AND contype = 'c';

-- Test de INSERT con nuevo valor
INSERT INTO usuarios (email, contraseña_hash, nombre_completo, rol, telefono)
VALUES ('test_migracion@fallapp.es', '$2a$10$test', 'Usuario Test Migración', 'usuario', '600000000')
RETURNING id_usuario, email, rol;

-- Limpiar usuario de test
DELETE FROM usuarios WHERE email = 'test_migracion@fallapp.es';

-- Verificar datos preservados
SELECT COUNT(*) as total_usuarios, 
       COUNT(DISTINCT rol) as roles_distintos,
       string_agg(DISTINCT rol, ', ') as roles
FROM usuarios;

COMMIT;

-- =====================================================
-- Resultado esperado:
-- ✅ Columna 'rol' migrada de ENUM → VARCHAR(20)
-- ✅ Constraint CHECK añadido para validación
-- ✅ Vista v_actividad_usuarios recreada
-- ✅ Columna año_construcción corregida
-- ✅ Datos preservados sin pérdida
-- ✅ JPA puede hacer INSERT/UPDATE sin errores
-- =====================================================

SELECT '✅ MIGRACIÓN COMPLETADA EXITOSAMENTE' as resultado;
