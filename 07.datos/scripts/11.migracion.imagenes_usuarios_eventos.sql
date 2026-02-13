-- =============================================================================
-- 11.migracion.imagenes_usuarios_eventos.sql
-- Añadir soporte de imágenes binarias para usuarios y eventos
--
-- Cambios:
--   - usuarios.foto_perfil BYTEA
--   - usuarios.foto_perfil_content_type VARCHAR(100)
--   - eventos.imagen BYTEA
--   - eventos.imagen_content_type VARCHAR(100)
--
-- Esta migración es idempotente: comprueba la existencia de columnas antes
-- de crearlas, para evitar errores en entornos donde ya se hayan aplicado
-- cambios manuales.
-- =============================================================================

DO $$
BEGIN
    -- -------------------------------------------------------------------------
    -- 1. Campo foto_perfil en usuarios (BYTEA)
    -- -------------------------------------------------------------------------
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'usuarios'
          AND column_name = 'foto_perfil'
    ) THEN
        ALTER TABLE usuarios
            ADD COLUMN foto_perfil BYTEA NULL;
    END IF;

    -- -------------------------------------------------------------------------
    -- 2. Campo foto_perfil_content_type en usuarios (MIME type)
    -- -------------------------------------------------------------------------
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'usuarios'
          AND column_name = 'foto_perfil_content_type'
    ) THEN
        ALTER TABLE usuarios
            ADD COLUMN foto_perfil_content_type VARCHAR(100) NULL;
    END IF;

    -- -------------------------------------------------------------------------
    -- 3. Campo imagen en eventos (BYTEA)
    -- -------------------------------------------------------------------------
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'eventos'
          AND column_name = 'imagen'
    ) THEN
        ALTER TABLE eventos
            ADD COLUMN imagen BYTEA NULL;
    END IF;

    -- -------------------------------------------------------------------------
    -- 4. Campo imagen_content_type en eventos (MIME type)
    -- -------------------------------------------------------------------------
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'eventos'
          AND column_name = 'imagen_content_type'
    ) THEN
        ALTER TABLE eventos
            ADD COLUMN imagen_content_type VARCHAR(100) NULL;
    END IF;
END;
$$ LANGUAGE plpgsql;

