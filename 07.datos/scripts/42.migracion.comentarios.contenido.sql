

ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS contenido TEXT;

UPDATE comentarios
SET contenido = texto_comentario
WHERE contenido IS NULL
  AND texto_comentario IS NOT NULL;

