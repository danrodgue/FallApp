-- =============================================================================
-- 12.migracion.verificacion_email.sql
-- Agrega campos para verificación de email con tokens
--
-- Cambios:
--   - token_verificacion: Token único para verificar email
--   - token_verificacion_expira: Fecha de expiración del token (24h)
--
-- Usuario existente: Los usuarios existentes tendrán verificado=true por defecto
-- Usuarios nuevos: verificado=false hasta que verifiquen email
--
-- Ejecutar: psql -U fallapp_user -d fallapp -f 12.migracion.verificacion_email.sql
-- =============================================================================

BEGIN;

-- Agregar columnas si no existen (idempotente)
DO $$ 
BEGIN
    -- Token de verificación (VARCHAR 64 para UUID sin guiones)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'usuarios' AND column_name = 'token_verificacion'
    ) THEN
        ALTER TABLE usuarios 
        ADD COLUMN token_verificacion VARCHAR(64) NULL;
        
        COMMENT ON COLUMN usuarios.token_verificacion IS 
        'Token único para verificar email (UUID sin guiones)';
    END IF;

    -- Fecha de expiración del token (24 horas desde generación)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'usuarios' AND column_name = 'token_verificacion_expira'
    ) THEN
        ALTER TABLE usuarios 
        ADD COLUMN token_verificacion_expira TIMESTAMP WITH TIME ZONE NULL;
        
        COMMENT ON COLUMN usuarios.token_verificacion_expira IS 
        'Fecha de expiración del token de verificación';
    END IF;
END $$;

-- Índice para búsqueda rápida por token
CREATE INDEX IF NOT EXISTS idx_usuarios_token_verificacion 
ON usuarios(token_verificacion) 
WHERE token_verificacion IS NOT NULL;

-- Índice para verificado (búsquedas por usuarios verificados/no verificados)
CREATE INDEX IF NOT EXISTS idx_usuarios_verificado 
ON usuarios(verificado);

-- MIGRACIÓN DE DATOS: Usuarios existentes se marcan como verificados
-- (asumimos que ya están validados)
UPDATE usuarios 
SET verificado = true 
WHERE verificado = false 
  AND fecha_registro < NOW() - INTERVAL '1 day';

COMMIT;

-- Verificar cambios
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'usuarios' 
  AND column_name IN ('verificado', 'token_verificacion', 'token_verificacion_expira')
ORDER BY ordinal_position;
