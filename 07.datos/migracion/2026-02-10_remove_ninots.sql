-- Migration: Remove ninots table and create backup
-- Date: 2026-02-10
-- WARNING: Run on production only after verifying backups and maintenance window.

-- 1) Create a backup table with all ninots content
CREATE TABLE IF NOT EXISTS ninots_backup_20260210 AS
SELECT * FROM ninots;

-- 2) Verify backup size (optional)
-- SELECT count(*) FROM ninots_backup_20260210;

-- 3) Drop foreign keys that might reference ninots (if any)
-- (If your DB has FK constraints from other tables to ninots, drop them first.)

-- 4) Drop the ninots table (cascade to remove dependent objects)
DROP TABLE IF EXISTS ninots CASCADE;

-- 5) Notes:
-- - This file intentionally performs a full backup into `ninots_backup_20260210` before dropping.
-- - After running this migration, application code must already be updated to not rely on ninots.
-- - To rollback: restore from backup table (see comments below).

-- Rollback example (restore table and data):
-- CREATE TABLE ninots (
--   id_ninot SERIAL PRIMARY KEY,
--   id_falla INTEGER NOT NULL,
--   nombre VARCHAR(255),
--   url_imagen VARCHAR(500) NOT NULL,
--   fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );
-- INSERT INTO ninots (id_ninot, id_falla, nombre, url_imagen, fecha_creacion)
-- SELECT id_ninot, id_falla, nombre, url_imagen, fecha_creacion FROM ninots_backup_20260210;

-- End of migration
