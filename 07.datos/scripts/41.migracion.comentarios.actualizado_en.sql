-- ============================================================================
-- 41.migracion.comentarios.actualizado_en.sql
-- ============================================================================
-- Añade la columna actualizado_en a la tabla comentarios para alinearla
-- con la entidad JPA com.fallapp.model.Comentario.
--
-- Problema que soluciona:
--  Error interno del servidor:
--    ERROR: column "actualizado_en" of relation "comentarios" does not exist
--    [insert into comentarios (actualizado_en,contenido,fecha_creacion,id_falla,id_usuario) ...]
--
-- Causa:
--  - El schema original define:
--        texto_comentario, fecha_creacion, fecha_edicion
--  - La entidad Comentario usa:
--        contenido, creadoEn (fecha_creacion), actualizadoEn (actualizado_en)
--  - La función actualizar_timestamp() y el código JPA esperan la columna
--    actualizado_en, pero la tabla comentarios no la tenía.
--
-- Este script:
--  1. Crea la columna actualizado_en si no existe.
--  2. Copia datos de fecha_edicion a actualizado_en (si había histórico).
--  3. Deja fecha_edicion como legacy (opcional eliminarla más adelante).
-- ============================================================================

ALTER TABLE comentarios
ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP WITH TIME ZONE NULL;

-- Migrar datos antiguos de fecha_edicion (si existían) a actualizado_en
UPDATE comentarios
SET actualizado_en = fecha_edicion
WHERE actualizado_en IS NULL
  AND fecha_edicion IS NOT NULL;

-- A partir de aquí:
--  - JPA insertará/actualizará actualizado_en.
--  - El trigger trig_comentarios_actualizar_timestamp utilizará
--    la función actualizar_timestamp() para mantener este campo.

