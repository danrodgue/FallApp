



BEGIN;


SELECT COUNT(*) AS filas_con_2025 FROM fallas WHERE url_boceto LIKE '%/2025_%';


UPDATE fallas
SET url_boceto = replace(url_boceto, '/2025_', '/2026_')
WHERE url_boceto LIKE '%/2025_%';


SELECT COUNT(*) AS filas_con_2025_despues FROM fallas WHERE url_boceto LIKE '%/2025_%';
SELECT COUNT(*) AS filas_con_2026 FROM fallas WHERE url_boceto LIKE '%/2026_%';

COMMIT;
