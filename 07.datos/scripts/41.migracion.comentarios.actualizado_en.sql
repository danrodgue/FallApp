

ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP WITH TIME ZONE NULL;


UPDATE comentarios
SET actualizado_en = fecha_edicion
WHERE actualizado_en IS NULL
  AND fecha_edicion IS NOT NULL;

