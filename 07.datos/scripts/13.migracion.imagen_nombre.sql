-- =============================================================================
-- Migración 13: Añadir columna imagen_nombre a tabla usuarios
-- Autor: Sistema
-- Fecha: 2026-02-16
-- Descripción: Añade la columna imagen_nombre para almacenar el nombre del
--              archivo de imagen del perfil de usuario
-- =============================================================================

-- Añadir columna imagen_nombre
ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS imagen_nombre VARCHAR(255) NULL;

-- Crear índice para búsquedas por imagen (opcional pero útil)
CREATE INDEX IF NOT EXISTS idx_usuarios_imagen_nombre 
ON usuarios(imagen_nombre);

-- Añadir comentario a la columna
COMMENT ON COLUMN usuarios.imagen_nombre IS 'Nombre del archivo de imagen de perfil del usuario';

-- Verificar que la columna se creó correctamente
SELECT column_name, data_type, character_maximum_length, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'usuarios' 
  AND column_name = 'imagen_nombre';
