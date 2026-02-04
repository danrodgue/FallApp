-- =============================================================================
-- test_05_ubicaciones_gps.sql
-- Test de Integridad: Validar datos de ubicaciones GPS en fallas
-- =============================================================================

\echo ''
\echo '========================================='
\echo 'TEST INTEGRACIÓN: Ubicaciones GPS'
\echo '========================================='
\echo ''

-- Test 1: Verificar que las columnas de ubicación existen
\echo 'Test 1: Verificar columnas ubicacion_lat y ubicacion_lon'
SELECT 
    CASE 
        WHEN COUNT(*) = 2 THEN 'PASS | Columnas ubicacion_lat y ubicacion_lon existen'
        ELSE 'FAIL | Columnas no encontradas'
    END as resultado
FROM information_schema.columns
WHERE table_name = 'fallas' 
  AND column_name IN ('ubicacion_lat', 'ubicacion_lon');

-- Test 2: Verificar cobertura de ubicaciones (>= 99%)
\echo ''
\echo 'Test 2: Verificar cobertura de ubicaciones >= 99%'
SELECT 
    CASE 
        WHEN ROUND(COUNT(ubicacion_lat)::numeric / COUNT(*)::numeric * 100, 2) >= 99.0 
        THEN 'PASS | Cobertura: ' || ROUND(COUNT(ubicacion_lat)::numeric / COUNT(*)::numeric * 100, 2) || '% (>= 99%)'
        ELSE 'FAIL | Cobertura: ' || ROUND(COUNT(ubicacion_lat)::numeric / COUNT(*)::numeric * 100, 2) || '% (< 99%)'
    END as resultado,
    COUNT(*) as total_fallas,
    COUNT(ubicacion_lat) as con_ubicacion
FROM fallas;

-- Test 3: Validar rango de latitudes (Valencia: 38-40)
\echo ''
\echo 'Test 3: Validar rango de latitudes (38° a 40°)'
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN 'PASS | Todas las latitudes en rango válido (38-40)'
        ELSE 'FAIL | ' || COUNT(*) || ' fallas con latitud fuera de rango'
    END as resultado
FROM fallas
WHERE ubicacion_lat IS NOT NULL 
  AND (ubicacion_lat < 38 OR ubicacion_lat > 40);

-- Test 4: Validar rango de longitudes (Valencia: -1 a 0)
\echo ''
\echo 'Test 4: Validar rango de longitudes (-1° a 0°)'
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN 'PASS | Todas las longitudes en rango válido (-1 a 0)'
        ELSE 'FAIL | ' || COUNT(*) || ' fallas con longitud fuera de rango'
    END as resultado
FROM fallas
WHERE ubicacion_lon IS NOT NULL 
  AND (ubicacion_lon < -1 OR ubicacion_lon > 0);

-- Test 5: Verificar precisión decimal (mínimo 6 decimales)
\echo ''
\echo 'Test 5: Verificar precisión decimal en coordenadas'
SELECT 
    CASE 
        WHEN MIN(LENGTH(ubicacion_lat::text) - POSITION('.' IN ubicacion_lat::text)) >= 6 
        THEN 'PASS | Latitud con precisión >= 6 decimales'
        ELSE 'FAIL | Latitud con precisión insuficiente'
    END as resultado_lat,
    CASE 
        WHEN MIN(LENGTH(ubicacion_lon::text) - POSITION('.' IN ubicacion_lon::text)) >= 6 
        THEN 'PASS | Longitud con precisión >= 6 decimales'
        ELSE 'FAIL | Longitud con precisión insuficiente'
    END as resultado_lon
FROM fallas
WHERE ubicacion_lat IS NOT NULL 
  AND ubicacion_lon IS NOT NULL
LIMIT 1;

-- Test 6: Verificar consistencia (si tiene lat, debe tener lon y viceversa)
\echo ''
\echo 'Test 6: Verificar consistencia de coordenadas'
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN 'PASS | Todas las fallas tienen ambas coordenadas o ninguna'
        ELSE 'FAIL | ' || COUNT(*) || ' fallas con solo una coordenada'
    END as resultado
FROM fallas
WHERE (ubicacion_lat IS NULL AND ubicacion_lon IS NOT NULL)
   OR (ubicacion_lat IS NOT NULL AND ubicacion_lon IS NULL);

-- Test 7: Verificar índices de ubicación (si existen)
\echo ''
\echo 'Test 7: Verificar índices en columnas de ubicación'
SELECT 
    CASE 
        WHEN COUNT(*) >= 0 THEN 'INFO | ' || COUNT(*) || ' índices encontrados en columnas de ubicación'
        ELSE 'INFO | Sin índices específicos de ubicación'
    END as resultado
FROM pg_indexes
WHERE tablename = 'fallas' 
  AND (indexdef LIKE '%ubicacion_lat%' OR indexdef LIKE '%ubicacion_lon%');

-- Test 8: Estadísticas generales
\echo ''
\echo 'Test 8: Estadísticas generales de ubicaciones'
SELECT 
    'INFO | Estadísticas GPS' as tipo,
    COUNT(*) as total_fallas,
    COUNT(ubicacion_lat) as con_ubicacion,
    COUNT(*) - COUNT(ubicacion_lat) as sin_ubicacion,
    ROUND(AVG(ubicacion_lat), 6) as lat_promedio,
    ROUND(AVG(ubicacion_lon), 6) as lon_promedio,
    ROUND(MIN(ubicacion_lat), 6) as lat_minima,
    ROUND(MAX(ubicacion_lat), 6) as lat_maxima,
    ROUND(MIN(ubicacion_lon), 6) as lon_minima,
    ROUND(MAX(ubicacion_lon), 6) as lon_maxima
FROM fallas;

-- Test 9: Listar fallas sin ubicación
\echo ''
\echo 'Test 9: Listar fallas sin ubicación GPS'
SELECT 
    CASE 
        WHEN COUNT(*) <= 1 THEN 'PASS | Solo ' || COUNT(*) || ' falla(s) sin ubicación'
        ELSE 'WARN | ' || COUNT(*) || ' fallas sin ubicación'
    END as resultado
FROM fallas
WHERE ubicacion_lat IS NULL;

\echo ''
SELECT 
    id_falla,
    nombre,
    seccion,
    'Sin ubicación GPS' as estado
FROM fallas
WHERE ubicacion_lat IS NULL
ORDER BY id_falla;

\echo ''
\echo '========================================='
\echo 'FIN TEST: Ubicaciones GPS'
\echo '========================================='
