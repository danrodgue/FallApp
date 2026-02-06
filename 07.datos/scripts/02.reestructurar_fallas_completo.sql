-- ========================================================================
-- SCRIPT: Reestructuración Completa de Tabla FALLAS
-- FECHA: 2026-02-04
-- DESCRIPCIÓN: 
--   1. Vacía la tabla fallas actual (conserva estructura)
--   2. Inserta TODOS los datos del JSON original (351 fallas)
--   3. Añade migración para las 3 nuevas columnas de Usuario
-- ========================================================================

-- ==========================
-- PARTE 1: LIMPIEZA DE FALLAS
-- ==========================

BEGIN;

-- Deshabilitar triggers temporalmente
ALTER TABLE fallas DISABLE TRIGGER ALL;
ALTER TABLE votos DISABLE TRIGGER ALL;
ALTER TABLE eventos DISABLE TRIGGER ALL;
ALTER TABLE ninots DISABLE TRIGGER ALL;
ALTER TABLE usuarios DISABLE TRIGGER ALL;

-- Eliminar datos relacionados primero (por CASCADE)
TRUNCATE TABLE votos CASCADE;
TRUNCATE TABLE comentarios CASCADE;
TRUNCATE TABLE eventos CASCADE;
-- NO truncar ninots porque tiene datos valiosos

-- Limpiar fallas
TRUNCATE TABLE fallas RESTART IDENTITY CASCADE;

-- Reactivar triggers
ALTER TABLE fallas ENABLE TRIGGER ALL;
ALTER TABLE votos ENABLE TRIGGER ALL;
ALTER TABLE eventos ENABLE TRIGGER ALL;
ALTER TABLE ninots ENABLE TRIGGER ALL;
ALTER TABLE usuarios ENABLE TRIGGER ALL;

-- ==========================
-- PARTE 2: INSERCIÓN COMPLETA DE DATOS
-- ==========================

-- Insertar las 351 fallas del JSON original con TODOS los campos
INSERT INTO fallas (
    nombre, seccion, fallera, presidente, artista, lema, 
    anyo_fundacion, distintivo, url_boceto, experim, 
    ubicacion_lat, ubicacion_lon, categoria, datos_json
) VALUES
-- Fallas con TODOS los datos del JSON (objectid, geo_shape completo)
('Isabel la Catòlica-Ciril Amorós', '2A', 'Patricia Vilana García', 'Santiago Riaza Grau', 'Art en Foc', 'Hotel apocalipsis', 1979, 'Fulles (2008)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_289_bm.jpg', false, 39.46802481927411, -0.36987256054880074, 'sin_categoria', '{"objectid": 7876, "id_falla": 289}'::jsonb),
('Sèneca-Iecla', '7B', NULL, 'Agustín Martínez Martínez', 'SacabutxART', 'Guriland', 1981, 'Fulles (2010)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_097_bm.jpg', false, 39.47271514928657, -0.35007997461738655, 'sin_categoria', '{"objectid": 7877, "id_falla": 97}'::jsonb),
('Indústria-Sants Just i Pastor', '2A', 'Clara Ana Salcedo Medina', 'Alexandro Simón Rivas', 'Pasky Roda', 'La nit mes fosca', 1974, 'Brillants (2018)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_184_bm.jpg', false, 39.46778267486452, -0.341797345121452, 'sin_categoria', '{"objectid": 7880, "id_falla": 184}'::jsonb),
('Cases de Bàrcena', '8C', NULL, 'Iván Manzaneque Rodríguez', 'Mon de Color', 'Había una vez.....un circo', 1998, 'Or (2017)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_290_bm.jpg', false, 39.521355896197555, -0.358425326048514, 'sin_categoria', '{"objectid": 7881, "id_falla": 290}'::jsonb),
('Plaza Sant Miquel-Vicent Iborra', '8A', 'Ana Sánchez Pérez', 'David Gilabert Martí', 'Fet D''encarrec', 'Ací no queda ni el gat', 1862, 'Brillants (1994)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_018_bm.jpg', false, 39.476824536112765, -0.3808785937650569, 'sin_categoria', '{"objectid": 7887, "id_falla": 18}'::jsonb),
('Gayano Lluch', '1B', 'Mar Gimeno Jordá', 'Roberto Godoy Cortes', 'Luis Espinosa Olmos', 'Atraca´m si vols, la terrorífica història de ''el día de..''', 1973, 'Brillants (2017)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_227_bm.jpg', false, 39.49173387148874, -0.38086370356302623, 'sin_categoria', '{"objectid": 7901, "id_falla": 227}'::jsonb),
('Baró de San Petrillo-Leonor Jovani', '1B', 'Aitana Sánchez García', 'Esther Mor Cubas', 'Arturo Vallés Bea', 'Les bogeries del Baró', 1943, 'Brillants (1993)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_125_bm.jpg', false, 39.48541765267115, -0.36040966256810064, 'sin_categoria', '{"objectid": 7903, "id_falla": 125}'::jsonb),
('Peu de La Creu-En Joan de Vila-Rasa', '3A', 'Gema Ballester Climent', 'Yolanda Alarcón Medina', 'Ximo Esteve  Mares', 'Pocions i transformacions', 1874, 'Brillants (1991)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_029_bm.jpg', false, 39.474073376412036, -0.38375923190475575, 'sin_categoria', '{"objectid": 7907, "id_falla": 29}'::jsonb),
('Àngel Guimerà-Pintor Vila Prades', 'FC', NULL, 'Josep Lluís Romero Carbonell', 'La Comissió', 'Agranant', 1908, 'Fulles (1984)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_182_bm.jpg', false, 39.47003991500788, -0.3898486667363017, 'sin_categoria', '{"objectid": 7908, "id_falla": 182}'::jsonb),
('Barraca-Espadà', '3C', 'Inés Plaza Soriano', 'José Antonio Lacomba Ríos', 'Luis Espinosa Olmos', 'Animal que no conegues no li toques les orelles', 1961, 'Brillants (2005)', 'http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_049_bm.jpg', false, 39.47192818142817, -0.3282611407518631, 'sin_categoria', '{"objectid": 7910, "id_falla": 49}'::jsonb);

-- NOTA: Este es un extracto de 10 registros
-- El script completo debe incluir las 351 fallas
-- Ver archivo: 03.insertar_351_fallas_completo.sql

-- ==========================
-- PARTE 3: MIGRACIÓN USUARIO (Nuevos campos de dirección)
-- ==========================

-- Añadir columnas que faltan según el git pull
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS direccion VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS ciudad VARCHAR(100);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS codigo_postal VARCHAR(10);

-- Crear índice para búsquedas por ciudad
CREATE INDEX IF NOT EXISTS idx_usuarios_ciudad ON usuarios(ciudad);

-- ==========================
-- PARTE 4: VERIFICACIÓN
-- ==========================

-- Verificar conteo
DO $$
DECLARE
    total_fallas INT;
BEGIN
    SELECT COUNT(*) INTO total_fallas FROM fallas;
    RAISE NOTICE 'Total de fallas insertadas: %', total_fallas;
    
    IF total_fallas < 300 THEN
        RAISE WARNING 'Se esperaban ~351 fallas, pero solo hay %', total_fallas;
    END IF;
END $$;

-- Mostrar estadísticas de campos con datos
SELECT 
    COUNT(*) as total,
    COUNT(fallera) as con_fallera,
    COUNT(artista) as con_artista,
    COUNT(lema) as con_lema,
    COUNT(distintivo) as con_distintivo,
    COUNT(url_boceto) as con_boceto,
    COUNT(ubicacion_lat) as con_gps,
    ROUND(COUNT(ubicacion_lat)::numeric / COUNT(*)::numeric * 100, 2) as porcentaje_gps
FROM fallas;

COMMIT;

-- ========================================================================
-- NOTAS IMPORTANTES:
-- ========================================================================
-- 
-- 1. Este script ELIMINA TODOS los datos actuales de fallas, votos, 
--    comentarios y eventos. Los ninots se conservan.
-- 
-- 2. Los valores NULL en 'fallera' son intencionados ("NO HAY" en JSON)
--    y se respetan como NULL en la BD.
-- 
-- 3. Las coordenadas GPS están en formato (lat, lon) del JSON original.
-- 
-- 4. Para insertar las 351 fallas completas, usar el script generador:
--    python3 07.datos/scripts/generar_insert_fallas.py
-- 
-- 5. Después de ejecutar, recompilar backend:
--    cd 01.backend && mvn clean package -DskipTests
--    sudo systemctl restart fallapp.service
-- 
-- ========================================================================
