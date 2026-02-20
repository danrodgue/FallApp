

\echo '========================================='
\echo 'TEST 02: Data Integrity'
\echo '========================================='
\echo ''


\echo 'Test 2.1: Datos iniciales de usuarios'
SELECT
    CASE
        WHEN COUNT(*) >= 3 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Usuarios seed' as test_name,
    COUNT(*) as usuario_count
FROM usuarios;


\echo ''
\echo 'Test 2.2: Fallas importadas'
SELECT
    CASE
        WHEN COUNT(*) >= 300 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Fallas importadas (>= 300)' as test_name,
    COUNT(*) as falla_count
FROM fallas;


\echo ''
\echo 'Test 2.3: Unicidad de emails'
SELECT
    CASE
        WHEN COUNT(*) = 0 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Emails únicos en usuarios' as test_name,
    COUNT(*) as duplicados
FROM (
    SELECT email, COUNT(*) as cnt
    FROM usuarios
    GROUP BY email
    HAVING COUNT(*) > 1
) duplicados;


\echo ''
\echo 'Test 2.4: Unicidad de nombres de fallas'
SELECT
    CASE
        WHEN COUNT(*) = 0 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Nombres únicos en fallas' as test_name,
    COUNT(*) as duplicados
FROM (
    SELECT nombre, COUNT(*) as cnt
    FROM fallas
    GROUP BY nombre
    HAVING COUNT(*) > 1
) duplicados;


\echo ''
\echo 'Test 2.5: Roles válidos en usuarios'
SELECT
    CASE
        WHEN COUNT(*) = (SELECT COUNT(*) FROM usuarios) THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Roles válidos (admin|casal|usuario)' as test_name,
    COUNT(*) as valid_roles
FROM usuarios
WHERE rol IN ('admin', 'casal', 'usuario');


\echo ''
\echo 'Test 2.6: Integridad referencial usuarios → fallas'
SELECT
    CASE
        WHEN COUNT(*) = 0 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'FK usuarios.id_falla válidos' as test_name,
    COUNT(*) as invalid_fks
FROM usuarios u
WHERE u.id_falla IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM fallas f WHERE f.id_falla = u.id_falla);


\echo ''
\echo 'Test 2.7: Contraseñas hasheadas'
SELECT
    CASE
        WHEN COUNT(*) = (SELECT COUNT(*) FROM usuarios) THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Contraseñas con hash bcrypt' as test_name,
    COUNT(*) as hashed_passwords
FROM usuarios
WHERE LENGTH(contraseña_hash) >= 50;


\echo ''
\echo 'Test 2.8: Coordenadas geográficas válidas'
SELECT
    CASE
        WHEN COUNT(*) = 0 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Coordenadas válidas (lat: -90 a 90, lon: -180 a 180)' as test_name,
    COUNT(*) as invalid_coords
FROM fallas
WHERE (ubicacion_lat IS NOT NULL AND (ubicacion_lat < -90 OR ubicacion_lat > 90))
   OR (ubicacion_lon IS NOT NULL AND (ubicacion_lon < -180 OR ubicacion_lon > 180));


\echo ''
\echo 'Test 2.9: Timestamps automáticos'
SELECT
    CASE
        WHEN COUNT(*) = (SELECT COUNT(*) FROM usuarios) THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Timestamps NOT NULL' as test_name,
    COUNT(*) as valid_timestamps
FROM usuarios
WHERE fecha_registro IS NOT NULL AND actualizado_en IS NOT NULL;


\echo ''
\echo 'Test 2.10: CASCADE DELETE'
BEGIN;

    INSERT INTO fallas (nombre, seccion, presidente, anyo_fundacion, categoria)
    VALUES ('TEST_FALLA_DELETE', 'XT', 'Test Presidente', 2000, 'sin_categoria')
    RETURNING id_falla \gset


    INSERT INTO eventos (id_falla, tipo, nombre, fecha_evento)
    VALUES (:id_falla, 'otro', 'Test Evento', CURRENT_TIMESTAMP);


    SELECT COUNT(*) as eventos_antes FROM eventos WHERE id_falla = :id_falla \gset


    DELETE FROM fallas WHERE id_falla = :id_falla;


    SELECT COUNT(*) as eventos_despues FROM eventos WHERE id_falla = :id_falla \gset


    SELECT
        CASE
            WHEN :eventos_antes = 1 AND :eventos_despues = 0 THEN 'PASS'
            ELSE 'FAIL'
        END as result,
        'CASCADE DELETE funcionando' as test_name,
        :eventos_antes as eventos_antes,
        :eventos_despues as eventos_despues;
ROLLBACK;

\echo ''
\echo '========================================='
\echo 'TEST 02 COMPLETADO'
\echo '========================================='
