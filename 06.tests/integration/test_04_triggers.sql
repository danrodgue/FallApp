-- =============================================================================
-- test_04_triggers.sql
-- Tests de triggers de auditoría
-- =============================================================================

\echo '========================================='
\echo 'TEST 04: Triggers de Auditoría'
\echo '========================================='
\echo ''

-- Test 4.1: Trigger en usuarios actualiza actualizado_en
\echo 'Test 4.1: Trigger usuarios'
BEGIN;
    -- Crear usuario de prueba
    INSERT INTO usuarios (email, contraseña_hash, nombre_completo, rol)
    VALUES ('test_trigger@test.com', 'hash_test', 'Test Trigger', 'usuario')
    RETURNING id_usuario \gset
    
    -- Guardar timestamp inicial
    SELECT actualizado_en FROM usuarios WHERE id_usuario = :id_usuario \gset timestamp_inicial_
    
    -- Esperar 1 segundo
    SELECT pg_sleep(1);
    
    -- Actualizar usuario
    UPDATE usuarios 
    SET nombre_completo = 'Test Trigger Updated'
    WHERE id_usuario = :id_usuario;
    
    -- Verificar que actualizado_en cambió
    SELECT actualizado_en FROM usuarios WHERE id_usuario = :id_usuario \gset timestamp_final_
    
    SELECT 
        CASE 
            WHEN :'timestamp_final_actualizado_en' > :'timestamp_inicial_actualizado_en' THEN 'PASS'
            ELSE 'FAIL'
        END as result,
        'Trigger usuarios actualiza timestamp' as test_name;
ROLLBACK;

-- Test 4.2: Trigger en fallas actualiza actualizado_en
\echo ''
\echo 'Test 4.2: Trigger fallas'
BEGIN;
    INSERT INTO fallas (nombre, seccion, presidente, anyo_fundacion, categoria)
    VALUES ('TEST_TRIGGER_FALLA', 'XT', 'Test', 2000, 'sin_categoria')
    RETURNING id_falla \gset
    
    SELECT actualizado_en FROM fallas WHERE id_falla = :id_falla \gset ts1_
    SELECT pg_sleep(1);
    
    UPDATE fallas SET presidente = 'Test Updated' WHERE id_falla = :id_falla;
    
    SELECT actualizado_en FROM fallas WHERE id_falla = :id_falla \gset ts2_
    
    SELECT 
        CASE 
            WHEN :'ts2_actualizado_en' > :'ts1_actualizado_en' THEN 'PASS'
            ELSE 'FAIL'
        END as result,
        'Trigger fallas actualiza timestamp' as test_name;
ROLLBACK;

-- Test 4.3: Trigger en eventos
\echo ''
\echo 'Test 4.3: Trigger eventos'
BEGIN;
    -- Necesitamos una falla existente
    SELECT id_falla FROM fallas LIMIT 1 \gset
    
    INSERT INTO eventos (id_falla, tipo, nombre, fecha_evento)
    VALUES (:id_falla, 'otro', 'TEST_TRIGGER_EVENTO', CURRENT_TIMESTAMP)
    RETURNING id_evento \gset
    
    SELECT actualizado_en FROM eventos WHERE id_evento = :id_evento \gset te1_
    SELECT pg_sleep(1);
    
    UPDATE eventos SET nombre = 'TEST_UPDATED' WHERE id_evento = :id_evento;
    
    SELECT actualizado_en FROM eventos WHERE id_evento = :id_evento \gset te2_
    
    SELECT 
        CASE 
            WHEN :'te2_actualizado_en' > :'te1_actualizado_en' THEN 'PASS'
            ELSE 'FAIL'
        END as result,
        'Trigger eventos actualiza timestamp' as test_name;
ROLLBACK;

-- Test 4.4: Trigger en ninots (omitted)
-- La tabla `ninots` fue eliminada; pruebas relacionadas a triggers de ninots se omiten.

-- Test 4.5: Trigger en comentarios
\echo ''
\echo 'Test 4.5: Trigger comentarios'
BEGIN;
    SELECT id_usuario FROM usuarios LIMIT 1 \gset
    SELECT id_falla FROM fallas LIMIT 1 \gset
    
    INSERT INTO comentarios (id_usuario, id_falla, texto_comentario, rating)
    VALUES (:id_usuario, :id_falla, 'TEST_COMENTARIO', 5)
    RETURNING id_comentario \gset
    
    SELECT actualizado_en FROM comentarios WHERE id_comentario = :id_comentario \gset tc1_
    SELECT pg_sleep(1);
    
    UPDATE comentarios SET rating = 4 WHERE id_comentario = :id_comentario;
    
    SELECT actualizado_en FROM comentarios WHERE id_comentario = :id_comentario \gset tc2_
    
    SELECT 
        CASE 
            WHEN :'tc2_actualizado_en' > :'tc1_actualizado_en' THEN 'PASS'
            ELSE 'FAIL'
        END as result,
        'Trigger comentarios actualiza timestamp' as test_name;
ROLLBACK;

\echo ''
\echo '========================================='
\echo 'TEST 04 COMPLETADO'
\echo '========================================='
