-- Importación con manejo de anyo_fundacion nulo (usa 0 cuando falta)
BEGIN;
CREATE TEMP TABLE raw_import (data jsonb);
COPY raw_import (data) FROM '/tmp/falles-fallas.jsonl';

INSERT INTO fallas (
  nombre,seccion,fallera,presidente,artista,lema,descripcion,anyo_fundacion,
  categoria,distintivo,ubicacion_lat,ubicacion_lon,web_oficial,telefono_contacto,
  email_contacto,experim,datos_json
)
SELECT
  COALESCE(data->>'nombre','Sin nombre'),
  COALESCE(data->>'seccion','?'),
  NULLIF(data->>'fallera',''),
  COALESCE(data->>'presidente','Desconocido'),
  data->>'artista',
  data->>'lema',
  data->>'descripcion',
  COALESCE(NULLIF(data->>'anyo_fundacion','')::int, 0),
  COALESCE(NULLIF(data->>'categoria',''),'sin_categoria')::categoria_falla,
  data->>'distintivo',
  NULLIF(data->>'ubicacion_lat','')::numeric,
  NULLIF(data->>'ubicacion_lon','')::numeric,
  data->>'web_oficial',
  data->>'telefono_contacto',
  data->>'email_contacto',
  (CASE WHEN (data->>'experim') IN ('true','True','1') THEN true ELSE false END),
  data
FROM raw_import
ON CONFLICT (nombre) DO NOTHING;

DROP TABLE raw_import;
COMMIT;

-- Estadísticas
SELECT COUNT(*) AS total_fallas FROM fallas;
SELECT categoria, COUNT(*) AS cantidad FROM fallas GROUP BY categoria ORDER BY cantidad DESC;
