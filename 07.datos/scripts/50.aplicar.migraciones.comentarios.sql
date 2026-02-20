

ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS sentimiento VARCHAR(20);


ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP WITH TIME ZONE NULL;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'comentarios' AND column_name = 'fecha_edicion'
  ) THEN
    UPDATE comentarios
    SET actualizado_en = fecha_edicion
    WHERE actualizado_en IS NULL AND fecha_edicion IS NOT NULL;
  END IF;
END $$;


ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS contenido TEXT;

UPDATE comentarios
SET contenido = texto_comentario
WHERE contenido IS NULL AND texto_comentario IS NOT NULL;


ALTER TABLE comentarios
ALTER COLUMN texto_comentario DROP NOT NULL;

CREATE OR REPLACE FUNCTION sync_comentarios_texto()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.texto_comentario IS NULL AND NEW.contenido IS NOT NULL THEN
    NEW.texto_comentario = NEW.contenido;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trig_comentarios_sync_texto ON comentarios;
CREATE TRIGGER trig_comentarios_sync_texto
BEFORE INSERT OR UPDATE ON comentarios
FOR EACH ROW
EXECUTE FUNCTION sync_comentarios_texto();
