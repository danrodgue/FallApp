-- ============================================================================
-- 43.migracion.comentarios.texto_nullable.sql
-- ============================================================================
-- Objetivo:
--  - Evitar que el NOT NULL de texto_comentario rompa los inserts de JPA,
--    que ahora solo escribe en la columna `contenido`.
--
-- Problema actual:
--  ERROR: null value in column "texto_comentario" of relation "comentarios" violates not-null constraint
--
-- Solución:
--  1. Hacer texto_comentario NULLABLE.
--  2. Crear un trigger para sincronizar texto_comentario a partir de contenido
--     en nuevos inserts/updates (para compatibilidad con vistas/consultas antiguas).
-- ============================================================================

ALTER TABLE comentarios
ALTER COLUMN texto_comentario DROP NOT NULL;

-- Función para mantener texto_comentario en sincronía con contenido
CREATE OR REPLACE FUNCTION sync_comentarios_texto()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.texto_comentario IS NULL AND NEW.contenido IS NOT NULL THEN
        NEW.texto_comentario = NEW.contenido;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger que se ejecuta en insert/update
DROP TRIGGER IF EXISTS trig_comentarios_sync_texto ON comentarios;

CREATE TRIGGER trig_comentarios_sync_texto
BEFORE INSERT OR UPDATE ON comentarios
FOR EACH ROW
EXECUTE FUNCTION sync_comentarios_texto();

