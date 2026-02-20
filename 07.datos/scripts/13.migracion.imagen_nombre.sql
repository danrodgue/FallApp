

ALTER TABLE usuarios
ADD COLUMN IF NOT EXISTS imagen_nombre VARCHAR(255) NULL;


CREATE INDEX IF NOT EXISTS idx_usuarios_imagen_nombre
ON usuarios(imagen_nombre);


COMMENT ON COLUMN usuarios.imagen_nombre IS 'Nombre del archivo de imagen de perfil del usuario';


SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_name = 'usuarios'
  AND column_name = 'imagen_nombre';
