-- Añadir columna de sentimiento a la tabla comentarios
-- Ejecutar este script en la base de datos de FallApp.

ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS sentimiento VARCHAR(20);

