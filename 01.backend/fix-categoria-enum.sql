-- Script para corregir columnas con incompatibilidad Hibernate/PostgreSQL
-- Ejecutar UNA VEZ en la base de datos PostgreSQL
--
-- psql -U tu_usuario -d tu_base_datos -f fix-categoria-enum.sql

-- 1. categoria: enum -> varchar (para actualizar fallas desde desktop)
ALTER TABLE fallas 
  ALTER COLUMN categoria TYPE varchar(50) 
  USING categoria::text;

-- 2. datos_json: jsonb -> text (si sigue fallando con "expression is of type character varying")
ALTER TABLE fallas 
  ALTER COLUMN datos_json TYPE text 
  USING datos_json::text;
