

\echo '========================================='
\echo 'TEST 01: Schema Creation'
\echo '========================================='
\echo ''


\echo 'Test 1.1: Extensiones instaladas'
SELECT
    CASE
        WHEN COUNT(*) >= 2 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Extensiones (uuid-ossp, unaccent)' as test_name,
    COUNT(*) as extensions_count
FROM pg_extension
WHERE extname IN ('uuid-ossp', 'unaccent');


\echo ''
\echo 'Test 1.2: Tablas principales creadas'
SELECT
    CASE
        WHEN COUNT(*) = 5 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Tablas principales' as test_name,
    COUNT(*) as tables_count,
    string_agg(table_name, ', ' ORDER BY table_name) as tables
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
    AND table_name IN ('usuarios', 'fallas', 'eventos', 'votos', 'comentarios');


\echo ''
\echo 'Test 1.3: Tipos ENUM creados'
SELECT
    CASE
        WHEN COUNT(*) = 4 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Tipos ENUM' as test_name,
    COUNT(*) as enum_count,
    string_agg(typname, ', ' ORDER BY typname) as enums
FROM pg_type
WHERE typtype = 'e'
  AND typname IN ('rol_usuario', 'tipo_evento', 'tipo_voto', 'categoria_falla');


\echo ''
\echo 'Test 1.4: Primary Keys definidas'
SELECT
    CASE
        WHEN COUNT(*) = 5 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Primary Keys' as test_name,
    COUNT(*) as pk_count
FROM information_schema.table_constraints
WHERE constraint_type = 'PRIMARY KEY'
  AND table_schema = 'public'
    AND table_name IN ('usuarios', 'fallas', 'eventos', 'votos', 'comentarios');


\echo ''
\echo 'Test 1.5: Foreign Keys definidas'
SELECT
    CASE
        WHEN COUNT(*) >= 8 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Foreign Keys' as test_name,
    COUNT(*) as fk_count
FROM information_schema.table_constraints
WHERE constraint_type = 'FOREIGN KEY'
  AND table_schema = 'public';


\echo ''
\echo 'Test 1.6: Unique Constraints definidas'
SELECT
    CASE
        WHEN COUNT(*) >= 2 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Unique Constraints' as test_name,
    COUNT(*) as unique_count
FROM information_schema.table_constraints
WHERE constraint_type = 'UNIQUE'
  AND table_schema = 'public'
  AND constraint_name LIKE '%email%' OR constraint_name LIKE '%nombre%';


\echo ''
\echo 'Test 1.7: Índices GIN para FTS'
SELECT
    CASE
        WHEN COUNT(*) >= 1 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Índices GIN (Full-Text Search)' as test_name,
    COUNT(*) as gin_indexes
FROM pg_indexes
WHERE indexdef LIKE '%USING gin%'
  AND schemaname = 'public';


\echo ''
\echo 'Test 1.8: Triggers de auditoría'
SELECT
    CASE
        WHEN COUNT(*) = 5 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Triggers de auditoría automática' as test_name,
    COUNT(*) as trigger_count,
    string_agg(trigger_name, ', ' ORDER BY trigger_name) as triggers
FROM information_schema.triggers
WHERE trigger_schema = 'public'
  AND trigger_name LIKE '%actualizar_timestamp%';


\echo ''
\echo 'Test 1.9: Función actualizar_timestamp()'
SELECT
    CASE
        WHEN COUNT(*) = 1 THEN 'PASS'
        ELSE 'FAIL'
    END as result,
    'Función actualizar_timestamp' as test_name,
    COUNT(*) as function_count
FROM pg_proc
WHERE proname = 'actualizar_timestamp';

\echo ''
\echo '========================================='
\echo 'TEST 01 COMPLETADO'
\echo '========================================='
