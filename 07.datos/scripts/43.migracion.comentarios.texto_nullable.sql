

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

