

DO $$
BEGIN



    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'usuarios'
          AND column_name = 'foto_perfil'
    ) THEN
        ALTER TABLE usuarios
            ADD COLUMN foto_perfil BYTEA NULL;
    END IF;

IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'usuarios'
          AND column_name = 'foto_perfil_content_type'
    ) THEN
        ALTER TABLE usuarios
            ADD COLUMN foto_perfil_content_type VARCHAR(100) NULL;
    END IF;

IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'eventos'
          AND column_name = 'imagen'
    ) THEN
        ALTER TABLE eventos
            ADD COLUMN imagen BYTEA NULL;
    END IF;

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

