

BEGIN;


ALTER TABLE fallas DISABLE TRIGGER ALL;
ALTER TABLE votos DISABLE TRIGGER ALL;
ALTER TABLE eventos DISABLE TRIGGER ALL;
ALTER TABLE ninots DISABLE TRIGGER ALL;
ALTER TABLE usuarios DISABLE TRIGGER ALL;


TRUNCATE TABLE votos CASCADE;
TRUNCATE TABLE comentarios CASCADE;
TRUNCATE TABLE eventos CASCADE;



TRUNCATE TABLE fallas RESTART IDENTITY CASCADE;


ALTER TABLE fallas ENABLE TRIGGER ALL;
ALTER TABLE votos ENABLE TRIGGER ALL;
ALTER TABLE eventos ENABLE TRIGGER ALL;
ALTER TABLE ninots ENABLE TRIGGER ALL;
ALTER TABLE usuarios ENABLE TRIGGER ALL;

INSERT INTO fallas (
    nombre, seccion, fallera, presidente, artista, lema,
    anyo_fundacion, distintivo, url_boceto, experim,
    ubicacion_lat, ubicacion_lon, categoria, datos_json
) VALUES

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

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS direccion VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS ciudad VARCHAR(100);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS codigo_postal VARCHAR(10);


CREATE INDEX IF NOT EXISTS idx_usuarios_ciudad ON usuarios(ciudad);

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

