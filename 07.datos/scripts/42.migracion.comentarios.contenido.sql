-- ============================================================================
-- 42.migracion.comentarios.contenido.sql
-- ============================================================================
-- Alinea la tabla comentarios con la entidad JPA com.fallapp.model.Comentario.
--
-- Problema actual:
--  - La entidad usa columna `contenido`
--  - El schema original creó `texto_comentario`
--  - Hibernate intenta ejecutar:
--      select c1_0.id_comentario, c1_0.actualizado_en, c1_0.contenido, ...
--    y falla con:
--      ERROR: column c1_0.contenido does not exist
--
-- Este script:
--  1. Crea la columna `contenido` si no existe.
--  2. Copia los datos desde `texto_comentario` si hay registros previos.
--
-- Después de ejecutarlo, los SELECT/INSERT de JPA funcionarán correctamente.
-- ============================================================================

ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS contenido TEXT;

UPDATE comentarios
SET contenido = texto_comentario
WHERE contenido IS NULL
  AND texto_comentario IS NOT NULL;

