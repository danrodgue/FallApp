#!/bin/bash


echo "=== Aplicando migración de verificación de email ==="

docker exec -i fallapp-postgres psql -U fallapp_user -d fallapp <<'EOSQL'
-- Añadir columna token_verificacion
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS token_verificacion VARCHAR(64) NULL;

-- Añadir columna token_verificacion_expira
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS token_verificacion_expira TIMESTAMP WITH TIME ZONE NULL;

-- Crear índice para mejorar rendimiento en búsquedas por token
CREATE INDEX IF NOT EXISTS idx_usuarios_token_verificacion ON usuarios(token_verificacion);

-- Marcar usuarios existentes como verificados (así no los molestamos con verificación retroactiva)
UPDATE usuarios SET verificado = true WHERE fecha_registro < NOW() - INTERVAL '1 day';

-- Verificar estructura
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'usuarios'
  AND column_name IN ('verificado', 'token_verificacion', 'token_verificacion_expira')
ORDER BY ordinal_position;
EOSQL

echo "=== Migración completada ==="
