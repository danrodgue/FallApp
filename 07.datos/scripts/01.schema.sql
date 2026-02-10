-- =============================================================================
-- 01.schema.sql
-- Creación de esquema base para FallApp
--
-- Crea:
--   - Extensiones necesarias
--   - Tipos ENUM
--   - Tablas principales
--   - Restricciones y relaciones
--   - Índices de performance
--   - Triggers de auditoría
--
-- Ejecución: Automática al iniciar contenedor PostgreSQL
--
-- ADRs relacionados:
--   - ADR-001: Justificación de PostgreSQL sobre MongoDB
--   - ADR-003: Nomenclatura 01.schema.sql (NN.tipo.sql)
--   - ADR-004: PostGIS deshabilitado por defecto
--
-- =============================================================================

-- =============================================================================
-- 1. EXTENSIONES
-- =============================================================================

-- UUID para identificadores únicos
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Para búsqueda de texto completo en español
CREATE EXTENSION IF NOT EXISTS unaccent;

-- PostGIS para datos geoespaciales (OPCIONAL - comentado por defecto)
-- Ver ADR-004 para justificación: DECIMAL + B-tree es suficiente para 400 fallas
-- Para activar: descomentar la siguiente línea
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- =============================================================================
-- 2. TIPOS ENUM
-- =============================================================================

-- Roles de usuario en la aplicación
CREATE TYPE rol_usuario AS ENUM (
    'admin',           -- Administrador del sistema
    'casal',           -- Representante de falla/casal
    'usuario'          -- Usuario público
);

-- Tipos de eventos/actos falleros
CREATE TYPE tipo_evento AS ENUM (
    'plantà',          -- Plantación del monumento
    'cremà',           -- Quema del monumento
    'ofrenda',         -- Ofrenda floral a la Virgen
    'encuentro',       -- Encuentro de casales/peñas
    'concierto',       -- Actuación musical
    'teatro',          -- Representación teatral
    'otro'             -- Otro acto
);

-- Tipos de votos/puntuaciones
CREATE TYPE tipo_voto AS ENUM (
    'me_gusta',        -- Me gusta general
    'mejor_ninot',     -- Voto a mejor figura/ninot
    'mejor_tema',      -- Voto a mejor tema de la falla
    'rating'           -- Puntuación general (1-5)
);

-- Categorías/distinciones de fallas
CREATE TYPE categoria_falla AS ENUM (
    'brillants',       -- Categoría Brillants
    'fulles',          -- Categoría Fulles
    'argent',          -- Categoría Argent
    'especial',        -- Categoría especial/experimental
    'sin_categoria'    -- Sin categoría asignada
);

-- =============================================================================
-- 3. TABLA: usuarios
-- =============================================================================

CREATE TABLE usuarios (
    id_usuario SERIAL PRIMARY KEY,
    
    -- Autenticación
    email VARCHAR(120) UNIQUE NOT NULL,
    contraseña_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    
    -- Perfil
    rol rol_usuario NOT NULL DEFAULT 'usuario',
    id_falla INTEGER NULL,  -- FK a fallas (casal al que pertenece)
    
    -- Estado
    activo BOOLEAN NOT NULL DEFAULT true,
    verificado BOOLEAN NOT NULL DEFAULT false,
    
    -- Contacto
    telefono VARCHAR(20) NULL,
    
    -- Auditoría
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP WITH TIME ZONE NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_usuarios_id_falla ON usuarios(id_falla);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);

-- =============================================================================
-- 4. TABLA: fallas
-- =============================================================================

CREATE TABLE fallas (
    id_falla SERIAL PRIMARY KEY,
    
    -- Información básica
    nombre VARCHAR(255) UNIQUE NOT NULL,
    seccion VARCHAR(5) NOT NULL,  -- Ej: "1A", "7C", "E"
    
    -- Personas
    fallera VARCHAR(255) NULL,     -- Reina o Infantil
    presidente VARCHAR(255) NOT NULL,
    artista VARCHAR(255) NULL,     -- Arquitecto/constructor
    
    -- Descripción
    lema TEXT NULL,                -- Tema de la falla
    descripcion TEXT NULL,
    anyo_fundacion INTEGER NOT NULL,
    categoria categoria_falla NOT NULL DEFAULT 'sin_categoria',
    distintivo VARCHAR(100) NULL,  -- Año de la distinción
    
    -- Multimedia
    url_boceto VARCHAR(500) NULL,
    url_fotos TEXT NULL,           -- Array de URLs (JSONB o TEXT)
    
    -- Ubicación geográfica
    ubicacion_lat DECIMAL(10, 8) NULL,
    ubicacion_lon DECIMAL(11, 8) NULL,
    
    -- Contacto
    web_oficial VARCHAR(255) NULL,
    telefono_contacto VARCHAR(20) NULL,
    email_contacto VARCHAR(120) NULL,
    
    -- Flags
    experim BOOLEAN NOT NULL DEFAULT false,
    activa BOOLEAN NOT NULL DEFAULT true,
    
    -- Auditoría
    datos_json JSONB NULL,         -- Datos brutos importados (compatibilidad)
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fallas_nombre ON fallas(nombre);
CREATE INDEX idx_fallas_seccion ON fallas(seccion);
CREATE INDEX idx_fallas_anyo_fundacion ON fallas(anyo_fundacion);
CREATE INDEX idx_fallas_categoria ON fallas(categoria);
CREATE INDEX idx_fallas_activa ON fallas(activa);

-- Índice geoespacial (comentado si no usas PostGIS)
-- CREATE INDEX idx_fallas_ubicacion ON fallas USING GIST (
--     ll_to_earth(ubicacion_lat, ubicacion_lon)
-- );

-- Full-text search en español
CREATE INDEX idx_fallas_fts ON fallas USING GIN(
    to_tsvector('spanish', 
        COALESCE(nombre, '') || ' ' || 
        COALESCE(lema, '') || ' ' || 
        COALESCE(artista, '')
    )
);

-- =============================================================================
-- 5. TABLA: eventos
-- =============================================================================

CREATE TABLE eventos (
    id_evento SERIAL PRIMARY KEY,
    id_falla INTEGER NOT NULL,
    
    -- Descripción
    tipo tipo_evento NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT NULL,
    
    -- Ubicación y tiempo
    fecha_evento TIMESTAMP WITH TIME ZONE NOT NULL,
    ubicacion VARCHAR(255) NULL,
    direccion VARCHAR(255) NULL,
    
    -- Detalles
    participantes_estimado INTEGER NULL,
    url_imagen VARCHAR(500) NULL,
    
    -- Auditoría
    creado_por INTEGER NULL,
    actualizado_por INTEGER NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_eventos_id_falla 
        FOREIGN KEY (id_falla) REFERENCES fallas(id_falla) ON DELETE CASCADE,
    
    CONSTRAINT fk_eventos_creado_por 
        FOREIGN KEY (creado_por) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    
    CONSTRAINT fk_eventos_actualizado_por 
        FOREIGN KEY (actualizado_por) REFERENCES usuarios(id_usuario) ON DELETE SET NULL
);

CREATE INDEX idx_eventos_id_falla ON eventos(id_falla);
CREATE INDEX idx_eventos_tipo ON eventos(tipo);
CREATE INDEX idx_eventos_fecha ON eventos(fecha_evento);
CREATE INDEX idx_eventos_creado_por ON eventos(creado_por);

-- =============================================================================
-- 6. TABLA: ninots
-- =============================================================================

CREATE TABLE ninots (
    id_ninot SERIAL PRIMARY KEY,
    id_falla INTEGER NOT NULL,
    
    -- Información
    nombre_ninot VARCHAR(255) NOT NULL,
    titulo_obra VARCHAR(255) NOT NULL,
    descripcion TEXT NULL,
    
    -- Dimensiones
    altura_metros DECIMAL(6, 2) NULL,
    ancho_metros DECIMAL(6, 2) NULL,
    profundidad_metros DECIMAL(6, 2) NULL,
    peso_toneladas DECIMAL(8, 2) NULL,
    
    -- Técnica
    material_principal VARCHAR(100) NULL,
    artista_constructor VARCHAR(255) NULL,
    año_construccion INTEGER NULL,
    
    -- Multimedia
    url_imagen_principal VARCHAR(500) NULL,
    url_imagenes_adicionales TEXT NULL,  -- Array de URLs
    
    -- Premios
    premiado BOOLEAN NOT NULL DEFAULT false,
    categoria_premio VARCHAR(100) NULL,
    año_premio INTEGER NULL,
    
    -- Auditoría
    notas_tecnicas TEXT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ninots_id_falla 
        FOREIGN KEY (id_falla) REFERENCES fallas(id_falla) ON DELETE CASCADE
);

CREATE INDEX idx_ninots_id_falla ON ninots(id_falla);
CREATE INDEX idx_ninots_titulo_obra ON ninots(titulo_obra);
CREATE INDEX idx_ninots_premiado ON ninots(premiado);

-- =============================================================================
-- 7. TABLA: votos
-- =============================================================================

CREATE TABLE votos (
    id_voto SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    id_falla INTEGER NOT NULL,
    
    -- Voto
    tipo_voto tipo_voto NOT NULL,
    -- `valor` indica la presencia del voto; se normaliza a 1 cuando existe
    -- al votar el sistema guarda `valor = 1` (permite cómputo por suma si se desea)
    valor INTEGER NOT NULL DEFAULT 1,
    
    -- Contenido
    comentario TEXT NULL,
    
    -- Auditoría
    ip_origen VARCHAR(45) NULL,  -- IPv4 o IPv6
    fecha_voto TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NULL,
    
    -- Restricción: Un usuario, un voto por tipo por falla
    UNIQUE(id_usuario, id_falla, tipo_voto),
    
    CONSTRAINT fk_votos_id_usuario 
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    
    CONSTRAINT fk_votos_id_falla 
        FOREIGN KEY (id_falla) REFERENCES fallas(id_falla) ON DELETE CASCADE,
    
    -- Restricción: `valor` debe ser 1 (semántica: 1 = voto emitido)
    CONSTRAINT ck_votos_valor CHECK (valor = 1)
);

CREATE INDEX idx_votos_id_usuario ON votos(id_usuario);
CREATE INDEX idx_votos_id_falla ON votos(id_falla);
CREATE INDEX idx_votos_tipo_voto ON votos(tipo_voto);
CREATE INDEX idx_votos_fecha_voto ON votos(fecha_voto);

-- =============================================================================
-- 8. TABLA: comentarios
-- =============================================================================

CREATE TABLE comentarios (
    id_comentario SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    id_falla INTEGER NOT NULL,
    
    -- Contenido
    texto_comentario TEXT NOT NULL,
    rating INTEGER NULL,
    
    -- Relaciones
    id_respuesta_a INTEGER NULL,  -- Para comentarios anidados
    
    -- Estado
    visible BOOLEAN NOT NULL DEFAULT true,
    
    -- Auditoría
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_edicion TIMESTAMP WITH TIME ZONE NULL,
    
    CONSTRAINT fk_comentarios_id_usuario 
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    
    CONSTRAINT fk_comentarios_id_falla 
        FOREIGN KEY (id_falla) REFERENCES fallas(id_falla) ON DELETE CASCADE,
    
    CONSTRAINT fk_comentarios_id_respuesta_a 
        FOREIGN KEY (id_respuesta_a) REFERENCES comentarios(id_comentario) ON DELETE SET NULL,
    
    CONSTRAINT ck_comentarios_rating CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5))
);

CREATE INDEX idx_comentarios_id_usuario ON comentarios(id_usuario);
CREATE INDEX idx_comentarios_id_falla ON comentarios(id_falla);
CREATE INDEX idx_comentarios_visible ON comentarios(visible);
CREATE INDEX idx_comentarios_fecha_creacion ON comentarios(fecha_creacion);
CREATE INDEX idx_comentarios_id_respuesta_a ON comentarios(id_respuesta_a);

-- =============================================================================
-- 9. RESTRICCIONES ADICIONALES (Foreign Keys postergadas)
-- =============================================================================

-- Usuarios: Cada casal/usuario puede pertenecer a una sola falla
ALTER TABLE usuarios 
ADD CONSTRAINT fk_usuarios_id_falla 
    FOREIGN KEY (id_falla) REFERENCES fallas(id_falla) ON DELETE SET NULL;

-- =============================================================================
-- 10. FUNCIONES AUXILIARES
-- =============================================================================

-- Función para actualizar timestamp en actualizado_en
CREATE OR REPLACE FUNCTION actualizar_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- 11. TRIGGERS
-- =============================================================================

-- Actualizar timestamp automáticamente en usuarios
CREATE TRIGGER trig_usuarios_actualizar_timestamp
BEFORE UPDATE ON usuarios
FOR EACH ROW
EXECUTE FUNCTION actualizar_timestamp();

-- Actualizar timestamp automáticamente en fallas
CREATE TRIGGER trig_fallas_actualizar_timestamp
BEFORE UPDATE ON fallas
FOR EACH ROW
EXECUTE FUNCTION actualizar_timestamp();

-- Actualizar timestamp automáticamente en eventos
CREATE TRIGGER trig_eventos_actualizar_timestamp
BEFORE UPDATE ON eventos
FOR EACH ROW
EXECUTE FUNCTION actualizar_timestamp();

-- Actualizar timestamp automáticamente en ninots
CREATE TRIGGER trig_ninots_actualizar_timestamp
BEFORE UPDATE ON ninots
FOR EACH ROW
EXECUTE FUNCTION actualizar_timestamp();

-- Actualizar timestamp automáticamente en comentarios
CREATE TRIGGER trig_comentarios_actualizar_timestamp
BEFORE UPDATE ON comentarios
FOR EACH ROW
EXECUTE FUNCTION actualizar_timestamp();

-- =============================================================================
-- 12. CONFIGURACIÓN FINAL
-- =============================================================================

-- Establecer collation en español para búsquedas correctas
ALTER DATABASE fallapp SET default_text_search_config = 'pg_catalog.spanish';

-- Dar permisos básicos al usuario de aplicación
GRANT CONNECT ON DATABASE fallapp TO fallapp_user;
GRANT USAGE ON SCHEMA public TO fallapp_user;
GRANT CREATE ON SCHEMA public TO fallapp_user;

-- Dar permisos de lectura/escritura a las tablas
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO fallapp_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO fallapp_user;

-- =============================================================================
-- FIN: 01.schema.sql
-- =============================================================================
