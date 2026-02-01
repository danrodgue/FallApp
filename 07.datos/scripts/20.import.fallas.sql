-- =============================================================================
-- 20.import.fallas.json.sql
-- Importación de datos de Fallas desde JSON
--
-- Origen: /tmp/falles-fallas.json (datos municipales de fallas de Valencia)
-- Destino: Tabla 'fallas' con mapeo de campos
--
-- Estructura esperada del JSON:
-- [
--   {
--     "id": 1,
--     "nombre": "Falla Norte",
--     "seccion": "1A",
--     "fallera": "Fallera Mayor",
--     "presidente": "Nombre Presidente",
--     "artista": "Nombre Artista",
--     "lema": "Tema de la falla",
--     "descripcion": "Descripción...",
--     "anyo_fundacion": 1990,
--     "categoria": "brillants",
--     "ubicacion_lat": 39.4699,
--     "ubicacion_lon": -0.3763,
--     ...
--   }
-- ]
--
-- Ejecución: Tercera fase (después de 01.schema.sql y 10.seed.usuarios.sql)
-- =============================================================================

-- =============================================================================
-- NOTA: Este script asume que el JSON está disponible en el contenedor
-- Si se usa directamente en PostgreSQL, descomentar y ejecutar manualmente:
-- =============================================================================

-- Opción 1: Importar desde archivo en el contenedor (recomendado para Docker)
-- Requiere que el archivo esté en /docker-entrypoint-initdb.d/ o sea accesible
-- COPY fallas(datos_json) FROM '/tmp/falles-fallas.json';

-- Opción 2: Si PostgreSQL está configurado con función plpython3u:
-- Descomentar cuando se pueda ejecutar Python dentro de PostgreSQL

-- =============================================================================
-- IMPORTACIÓN MANUAL - OPCIÓN: SQL GENÉRICO
-- =============================================================================
-- 
-- Este bloque proporciona un INSERT placeholder
-- Será reemplazado por el verdadero JSON en la ejecución real
--

-- Insertar falla de ejemplo para validar estructura
INSERT INTO fallas (
    nombre,
    seccion,
    fallera,
    presidente,
    artista,
    lema,
    descripcion,
    anyo_fundacion,
    categoria,
    distintivo,
    ubicacion_lat,
    ubicacion_lon,
    web_oficial,
    telefono_contacto,
    email_contacto,
    experim,
    activa
) VALUES (
    'Falla Ejemplo - Importación de Prueba',
    'E',
    'Fallera Mayor de Ejemplo',
    'Presidente de Ejemplo',
    'Artista Constructor',
    'Tema: Las Fallas en la Era Digital',
    'Descripción de la falla de prueba para validar la estructura de importación',
    2024,
    'sin_categoria'::categoria_falla,
    NULL,
    39.47694,
    -0.37632,
    'https://example.com/falla',
    '+34-963-000-000',
    'contacto@example.com',
    false,
    true
)
ON CONFLICT (nombre) DO NOTHING;

-- =============================================================================
-- SCRIPT DE IMPORTACIÓN MEJORADA CON PLPYTHON3U
-- =============================================================================
--
-- Para activar importación automática desde JSON:
-- 1. Descomentar el bloque siguiente
-- 2. Asegurar que plpython3u está instalado:
--    CREATE EXTENSION plpython3u;
-- 3. El JSON debe estar accesible en /tmp/falles-fallas.json
--

/*
-- Crear extensión para ejecutar Python (comentada por defecto)
CREATE EXTENSION IF NOT EXISTS plpython3u;

-- Función para importar desde JSON
CREATE OR REPLACE FUNCTION importar_fallas_desde_json()
RETURNS TABLE (importadas INTEGER, errores INTEGER) AS $$
import json
import os
from datetime import datetime

json_file = '/tmp/falles-fallas.json'
importadas = 0
errores = 0

try:
    if os.path.exists(json_file):
        with open(json_file, 'r', encoding='utf-8') as f:
            fallas_data = json.load(f)
            
        for falla in fallas_data:
            try:
                # Mapear campos del JSON a las columnas SQL
                plpy.execute("""
                    INSERT INTO fallas (
                        nombre,
                        seccion,
                        fallera,
                        presidente,
                        artista,
                        lema,
                        descripcion,
                        anyo_fundacion,
                        categoria,
                        distintivo,
                        ubicacion_lat,
                        ubicacion_lon,
                        web_oficial,
                        telefono_contacto,
                        email_contacto,
                        experim,
                        datos_json
                    ) VALUES (
                        %(nombre)s,
                        %(seccion)s,
                        %(fallera)s,
                        %(presidente)s,
                        %(artista)s,
                        %(lema)s,
                        %(descripcion)s,
                        %(anyo)s,
                        %(categoria)s,
                        %(distintivo)s,
                        %(lat)s,
                        %(lon)s,
                        %(web)s,
                        %(telefono)s,
                        %(email)s,
                        %(experim)s,
                        %(datos_raw)s
                    )
                    ON CONFLICT (nombre) DO NOTHING
                """, {
                    'nombre': falla.get('nombre', 'Sin nombre'),
                    'seccion': falla.get('seccion', '?'),
                    'fallera': falla.get('fallera', None),
                    'presidente': falla.get('presidente', 'Desconocido'),
                    'artista': falla.get('artista', None),
                    'lema': falla.get('lema', None),
                    'descripcion': falla.get('descripcion', None),
                    'anyo': int(falla.get('anyo_fundacion', 2000)) if falla.get('anyo_fundacion') else 2000,
                    'categoria': falla.get('categoria', 'sin_categoria'),
                    'distintivo': falla.get('distintivo', None),
                    'lat': float(falla.get('ubicacion_lat')) if falla.get('ubicacion_lat') else None,
                    'lon': float(falla.get('ubicacion_lon')) if falla.get('ubicacion_lon') else None,
                    'web': falla.get('web_oficial', None),
                    'telefono': falla.get('telefono_contacto', None),
                    'email': falla.get('email_contacto', None),
                    'experim': falla.get('experim', False),
                    'datos_raw': json.dumps(falla)
                })
                importadas += 1
                
            except Exception as e:
                plpy.notice(f"Error importando falla {falla.get('nombre', 'desconocida')}: {str(e)}")
                errores += 1
    else:
        plpy.notice(f"Archivo no encontrado: {json_file}")
        errores = 1
        
except Exception as e:
    plpy.notice(f"Error general en importación: {str(e)}")
    errores += 1

return [(importadas, errores)]
$$ LANGUAGE plpython3u;

-- Ejecutar la importación
SELECT * FROM importar_fallas_desde_json();

*/

-- =============================================================================
-- ALTERNATIVA: IMPORTACIÓN VÍA COPY
-- =============================================================================
--
-- Si el JSON está en el contenedor en /docker-entrypoint-initdb.d/:
--

-- COPY fallas(datos_json) FROM '/docker-entrypoint-initdb.d/falles-fallas.json';

-- UPDATE fallas SET
--     nombre = datos_json->>'nombre',
--     seccion = datos_json->>'seccion',
--     fallera = datos_json->>'fallera',
--     presidente = datos_json->>'presidente',
--     artista = datos_json->>'artista',
--     lema = datos_json->>'lema',
--     descripcion = datos_json->>'descripcion',
--     anyo_fundacion = CAST(datos_json->>'anyo_fundacion' AS INTEGER),
--     categoria = CAST(COALESCE(datos_json->>'categoria', 'sin_categoria') AS categoria_falla),
--     ubicacion_lat = CAST(datos_json->>'ubicacion_lat' AS DECIMAL),
--     ubicacion_lon = CAST(datos_json->>'ubicacion_lon' AS DECIMAL),
--     web_oficial = datos_json->>'web_oficial',
--     telefono_contacto = datos_json->>'telefono_contacto',
--     email_contacto = datos_json->>'email_contacto'
-- WHERE datos_json IS NOT NULL;

-- =============================================================================
-- ESTADÍSTICAS Y VALIDACIÓN
-- =============================================================================

-- Verificar fallas importadas
SELECT 
    COUNT(*) as total_fallas,
    COUNT(CASE WHEN activa THEN 1 END) as fallas_activas,
    MIN(anyo_fundacion) as anyo_fundacion_min,
    MAX(anyo_fundacion) as anyo_fundacion_max
FROM fallas;

-- Contar por categoría
SELECT 
    categoria,
    COUNT(*) as cantidad
FROM fallas
GROUP BY categoria
ORDER BY cantidad DESC;

-- Contar por sección
SELECT 
    seccion,
    COUNT(*) as cantidad
FROM fallas
GROUP BY seccion
ORDER BY seccion;

-- =============================================================================
-- NOTAS OPERACIONALES
-- =============================================================================
--
-- 1. PROCEDIMIENTO MANUAL:
--    a. Convertir falles-fallas.json a JSONL (una línea por registro)
--    b. Copiar archivo a /docker-entrypoint-initdb.d/
--    c. Ejecutar COPY ... FROM con el JSONL
--    d. Ejecutar UPDATE para mapear campos
--
-- 2. PROCEDIMIENTO AUTOMÁTICO:
--    a. Activar extensión plpython3u en el contenedor
--    b. Descomentar el bloque de función/importación
--    c. El script ejecutará automáticamente
--
-- 3. MAPEO DE CAMPOS:
--    JSON 'nombre' → SQL 'nombre'
--    JSON 'fallera' → SQL 'fallera'
--    JSON 'presidente' → SQL 'presidente'
--    JSON 'anyo_fundacion' → SQL 'anyo_fundacion'
--    JSON 'ubicacion_lat' → SQL 'ubicacion_lat'
--    JSON 'ubicacion_lon' → SQL 'ubicacion_lon'
--    (Campos adicionales se almacenan en JSONB datos_json)
--
-- 4. PRÓXIMOS PASOS:
--    - Asignar usuarios 'casal' a sus respectivas fallas
--    - Importar eventos, ninots, y comentarios relacionados
--    - Validar integridad referencial
--
-- =============================================================================
-- FIN: 20.import.fallas.json.sql
-- =============================================================================
