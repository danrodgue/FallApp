-- Script para convertir la columna categoria de tipo enum a varchar
-- Ejecutar UNA VEZ en la base de datos PostgreSQL si aparece el error:
-- "column categoria is of type categoria_falla but expression is of type character varying"
--
-- Conecta a tu BD y ejecuta:
-- psql -U tu_usuario -d tu_base_datos -f fix-categoria-enum.sql

ALTER TABLE fallas 
  ALTER COLUMN categoria TYPE varchar(50) 
  USING categoria::text;
