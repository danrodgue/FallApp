-- Script: Actualizar URLs de bocetos de 2025 a 2026
-- Uso: ejecutar con psql contra la BD de producción/local
-- Ejemplo: psql -h host -U usuario -d basedatos -f 04.actualizar_urls_2026.sql

BEGIN;

-- Conteo previo
SELECT COUNT(*) AS filas_con_2025 FROM fallas WHERE url_boceto LIKE '%/2025_%';

-- Actualizar columna url_boceto
UPDATE fallas
SET url_boceto = replace(url_boceto, '/2025_', '/2026_')
WHERE url_boceto LIKE '%/2025_%';

-- Conteo posterior
SELECT COUNT(*) AS filas_con_2025_despues FROM fallas WHERE url_boceto LIKE '%/2025_%';
SELECT COUNT(*) AS filas_con_2026 FROM fallas WHERE url_boceto LIKE '%/2026_%';

COMMIT;
