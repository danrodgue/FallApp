-- =============================================================================
-- test_03_views_functions.sql
-- Tests de vistas y funciones SQL
-- =============================================================================

\echo '========================================='
\echo 'TEST 03: Views and Functions'
\echo '========================================='
\echo ''

-- Test 3.1: Verificar que existen las 9 vistas
\echo 'Test 3.1: Vistas creadas'
SELECT 
    CASE 
        WHEN COUNT(*) = 9 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Vistas especializadas' as test_name,
    COUNT(*) as view_count,
    string_agg(table_name, ', ' ORDER BY table_name) as views
FROM information_schema.views
WHERE table_schema = 'public'
  AND table_name LIKE 'v_%';

-- Test 3.2: Verificar v_estadisticas_fallas retorna datos
\echo ''
\echo 'Test 3.2: Vista v_estadisticas_fallas'
SELECT 
    CASE 
        WHEN COUNT(*) >= 300 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'v_estadisticas_fallas con datos' as test_name,
    COUNT(*) as row_count
FROM v_estadisticas_fallas;

-- Test 3.3: Verificar v_fallas_mas_votadas tiene estructura correcta
\echo ''
\echo 'Test 3.3: Vista v_fallas_mas_votadas (estructura)'
SELECT 
    CASE 
        WHEN COUNT(*) = 4 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'v_fallas_mas_votadas columnas' as test_name,
    COUNT(*) as column_count
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'v_fallas_mas_votadas'
  AND column_name IN ('id_falla', 'nombre', 'total_votos', 'rating_promedio');

-- Test 3.4: Verificar v_busqueda_fallas_fts tiene campo searchable
\echo ''
\echo 'Test 3.4: Vista v_busqueda_fallas_fts (FTS ready)'
SELECT 
    CASE 
        WHEN COUNT(*) = 1 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'v_busqueda_fallas_fts con searchable' as test_name,
    COUNT(*) as has_searchable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'v_busqueda_fallas_fts'
  AND column_name = 'searchable';

-- Test 3.5: Verificar función buscar_fallas existe
\echo ''
\echo 'Test 3.5: Función buscar_fallas()'
SELECT 
    CASE 
        WHEN COUNT(*) = 1 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Función buscar_fallas' as test_name,
    COUNT(*) as function_count
FROM pg_proc
WHERE proname = 'buscar_fallas';

-- Test 3.6: Verificar función obtener_ranking_fallas existe
\echo ''
\echo 'Test 3.6: Función obtener_ranking_fallas()'
SELECT 
    CASE 
        WHEN COUNT(*) = 1 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Función obtener_ranking_fallas' as test_name,
    COUNT(*) as function_count
FROM pg_proc
WHERE proname = 'obtener_ranking_fallas';

-- Test 3.7: Test funcional de buscar_fallas (si hay fallas con "valencia")
\echo ''
\echo 'Test 3.7: Prueba funcional buscar_fallas()'
DO $$
DECLARE
    result_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO result_count FROM buscar_fallas('valencia');
    
    IF result_count >= 0 THEN
        RAISE NOTICE 'PASS | buscar_fallas() ejecutable | resultados: %', result_count;
    ELSE
        RAISE NOTICE 'FAIL | buscar_fallas() no ejecutable';
    END IF;
END $$;

-- Test 3.8: Test funcional de obtener_ranking_fallas
\echo ''
\echo 'Test 3.8: Prueba funcional obtener_ranking_fallas()'
DO $$
DECLARE
    result_count INTEGER;
BEGIN
    -- Intentar obtener top 10 (puede retornar 0 si no hay votos aún)
    SELECT COUNT(*) INTO result_count FROM obtener_ranking_fallas(10, 'rating');
    
    IF result_count >= 0 THEN
        RAISE NOTICE 'PASS | obtener_ranking_fallas() ejecutable | resultados: %', result_count;
    ELSE
        RAISE NOTICE 'FAIL | obtener_ranking_fallas() no ejecutable';
    END IF;
END $$;

-- Test 3.9: Verificar v_eventos_proximos ordena por fecha
\echo ''
\echo 'Test 3.9: Vista v_eventos_proximos ordenada'
SELECT 
    CASE 
        WHEN COUNT(*) = 0 OR (
            SELECT bool_and(fecha_evento <= LEAD(fecha_evento) OVER ())
            FROM v_eventos_proximos
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'v_eventos_proximos ordenada por fecha' as test_name,
    COUNT(*) as evento_count
FROM v_eventos_proximos;

-- Test 3.10: Verificar vistas no tienen errores de sintaxis
\echo ''
\echo 'Test 3.10: Todas las vistas accesibles (sin errores)'
DO $$
DECLARE
    vista RECORD;
    error_count INTEGER := 0;
BEGIN
    FOR vista IN 
        SELECT table_name 
        FROM information_schema.views 
        WHERE table_schema = 'public' AND table_name LIKE 'v_%'
    LOOP
        BEGIN
            EXECUTE 'SELECT 1 FROM ' || vista.table_name || ' LIMIT 1';
        EXCEPTION WHEN OTHERS THEN
            error_count := error_count + 1;
            RAISE NOTICE 'ERROR en vista: %', vista.table_name;
        END;
    END LOOP;
    
    IF error_count = 0 THEN
        RAISE NOTICE 'PASS | Todas las vistas sin errores';
    ELSE
        RAISE NOTICE 'FAIL | % vistas con errores', error_count;
    END IF;
END $$;

\echo ''
\echo '========================================='
\echo 'TEST 03 COMPLETADO'
\echo '========================================='
