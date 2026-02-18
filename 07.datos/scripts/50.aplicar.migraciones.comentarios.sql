-- ============================================================================
-- 50.aplicar.migraciones.comentarios.sql
-- ============================================================================
-- Script único para alinear la tabla comentarios con la entidad JPA Comentario
-- en un servidor donde solo se ejecutó el schema inicial (01.schema.sql).
--
-- Soluciona los errores que aparecen en docker logs fallapp-postgres:
--   - column "actualizado_en" of relation "comentarios" does not exist
--   - column c1_0.contenido does not exist
--   - null value in column "texto_comentario" violates not-null constraint
--
-- Incluye: 40 (sentimiento), 41 (actualizado_en), 42 (contenido), 43 (texto nullable + trigger).
-- Ejecutar una sola vez conectado a la base fallapp.
-- ============================================================================

-- 40: Columna sentimiento (análisis IA)
ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS sentimiento VARCHAR(20);

-- 41: Columna actualizado_en
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

-- 42: Columna contenido y copia desde texto_comentario
ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS contenido TEXT;

UPDATE comentarios
SET contenido = texto_comentario
WHERE contenido IS NULL AND texto_comentario IS NOT NULL;

-- 43: texto_comentario nullable + trigger de sincronía
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
