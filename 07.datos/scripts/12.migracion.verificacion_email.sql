

BEGIN;


DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'usuarios' AND column_name = 'token_verificacion'
    ) THEN
        ALTER TABLE usuarios
        ADD COLUMN token_verificacion VARCHAR(64) NULL;

        COMMENT ON COLUMN usuarios.token_verificacion IS
        'Token único para verificar email (UUID sin guiones)';
    END IF;


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


CREATE INDEX IF NOT EXISTS idx_usuarios_token_verificacion
ON usuarios(token_verificacion)
WHERE token_verificacion IS NOT NULL;


CREATE INDEX IF NOT EXISTS idx_usuarios_verificado
ON usuarios(verificado);



UPDATE usuarios
SET verificado = true
WHERE verificado = false
  AND fecha_registro < NOW() - INTERVAL '1 day';

COMMIT;


SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'usuarios'
  AND column_name IN ('verificado', 'token_verificacion', 'token_verificacion_expira')
ORDER BY ordinal_position;
