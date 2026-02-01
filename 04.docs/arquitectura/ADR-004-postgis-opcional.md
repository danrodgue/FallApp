# ADR-004: PostGIS Opcional (Deshabilitado por Defecto)

**Estado**: Aceptado  
**Fecha**: 2026-02-01  
**Decisores**: Equipo de desarrollo FallApp  
**Contexto relacionado**: [ADR-001](ADR-001-postgresql-vs-mongodb.md), [03.BASE-DATOS.md](../especificaciones/03.BASE-DATOS.md)

---

## Contexto y Problema

FallApp gestiona fallas de Valencia con coordenadas geográficas (latitud, longitud). Necesitamos:
1. Almacenar ubicaciones de fallas
2. Buscar fallas cercanas a una ubicación
3. Calcular distancias entre fallas
4. Mostrar fallas en mapa (frontend)

**Problema**: ¿Usar PostGIS (extensión geoespacial avanzada) o tipos básicos de PostgreSQL?

**Datos del problema**:
- ~400 fallas en Valencia (volumen pequeño)
- Búsquedas geoespaciales no son core del MVP
- Equipo tiene experiencia limitada con PostGIS
- Timeline de 4 semanas (proyecto académico)

**Alternativas consideradas**:
- PostGIS (extensión completa)
- DECIMAL(lat, lon) + índices básicos
- Geometry/Geography nativo PostgreSQL (sin PostGIS)

---

## Factores de Decisión

| Factor | Peso | PostGIS | DECIMAL + B-tree | Geometry nativo |
|--------|------|---------|------------------|-----------------|
| **Funcionalidad geoespacial** | Media | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| **Performance búsquedas** | Media | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Simplicidad** | Alta | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Curva aprendizaje** | Alta | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Suficiente para MVP** | Alta | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Volumen de datos** | Baja | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Reversibilidad** | Media | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

---

## Decisión

**Usar DECIMAL(lat, lon) con índices B-tree, dejando PostGIS como extensión opcional (comentada en el código).**

### Implementación

```sql
-- En 01.schema.sql

-- PostGIS para datos geoespaciales (OPCIONAL - comentado por defecto)
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- Tabla fallas con coordenadas DECIMAL
CREATE TABLE fallas (
    -- ... otros campos
    ubicacion_lat NUMERIC(10,8) NULL,  -- Ejemplo: 39.47391700
    ubicacion_lon NUMERIC(11,8) NULL,  -- Ejemplo: -0.37628400
    -- ...
);

-- Índices B-tree (suficientes para ~400 registros)
CREATE INDEX idx_fallas_ubicacion_lat ON fallas(ubicacion_lat);
CREATE INDEX idx_fallas_ubicacion_lon ON fallas(ubicacion_lon);
```

### Justificación

1. **Suficiente para el MVP**
   - Búsqueda por bounding box: `WHERE lat BETWEEN x1 AND x2 AND lon BETWEEN y1 AND y2`
   - Distancia Haversine implementable en aplicación
   - 400 fallas = queries en milisegundos con índices B-tree

2. **Simplicidad sobre poder**
   - DECIMAL es universalmente entendido
   - Sin dependencias externas
   - Sin curva de aprendizaje de PostGIS

3. **Timeline corto** (4 semanas)
   - PostGIS requiere aprendizaje de funciones espaciales
   - PostGIS añade complejidad de debugging
   - DECIMAL "simplemente funciona"

4. **Fácilmente reversible**
   - PostGIS está comentado, no eliminado
   - Migración futura: descomentar 1 línea + migración de datos
   - Sin reescritura arquitectónica

---

## Por qué NO PostGIS (por ahora)

### Complejidad innecesaria para MVP

**Con PostGIS** (más complejo):
```sql
-- Crear extensión
CREATE EXTENSION postgis;

-- Tipo de dato especial
ALTER TABLE fallas ADD COLUMN ubicacion GEOGRAPHY(POINT, 4326);

-- Indexación GiST
CREATE INDEX idx_fallas_ubicacion ON fallas USING GIST(ubicacion);

-- Búsqueda (sintaxis especial)
SELECT * FROM fallas
WHERE ST_DWithin(
    ubicacion,
    ST_MakePoint(-0.376284, 39.473917)::geography,
    5000  -- 5km
);
```

**Sin PostGIS** (más simple):
```sql
-- Búsqueda por bounding box (aproximación suficiente)
SELECT * FROM fallas
WHERE ubicacion_lat BETWEEN 39.42 AND 39.52
  AND ubicacion_lon BETWEEN -0.42 AND -0.32;

-- Distancia calculada en aplicación con Haversine
```

### Curva de aprendizaje

PostGIS tiene 100+ funciones espaciales:
- `ST_DWithin`, `ST_Distance`, `ST_MakePoint`
- `ST_Buffer`, `ST_Intersects`, `ST_Contains`
- Tipos: `POINT`, `LINESTRING`, `POLYGON`, `GEOGRAPHY`

**Tiempo de aprendizaje**: 1-2 semanas  
**Tiempo disponible**: 4 semanas totales

**Decisión**: No justificado para MVP.

### Performance no crítica

**Volumen de datos**: ~400 fallas  
**Queries esperadas**: 10-100 por segundo (muy optimista)

```sql
-- Benchmark aproximado con índices B-tree
EXPLAIN ANALYZE
SELECT * FROM fallas
WHERE ubicacion_lat BETWEEN 39.42 AND 39.52
  AND ubicacion_lon BETWEEN -0.42 AND -0.32;

-- Resultado esperado: <5ms con 400 registros
```

PostGIS optimiza para **millones de registros**. No lo necesitamos.

---

## Por qué SÍ dejar PostGIS comentado

1. **Documentación de intención**
   ```sql
   -- PostGIS para datos geoespaciales (OPCIONAL - comentado por defecto)
   -- CREATE EXTENSION IF NOT EXISTS postgis;
   ```
   Comunica que sabemos que existe y por qué no la usamos.

2. **Fácil activación futura**
   ```sql
   -- Descomentar y migrar datos
   CREATE EXTENSION IF NOT EXISTS postgis;
   
   ALTER TABLE fallas ADD COLUMN ubicacion_postgis GEOGRAPHY(POINT, 4326);
   
   UPDATE fallas
   SET ubicacion_postgis = ST_MakePoint(ubicacion_lon, ubicacion_lat)::geography
   WHERE ubicacion_lat IS NOT NULL;
   ```

3. **Sin overhead**
   - Extensión no instalada = 0 impacto en memoria/performance
   - Comentario = documentación gratis

---

## Alternativa Considerada: Geometry Nativo

PostgreSQL tiene tipos `POINT` sin PostGIS:
```sql
ubicacion POINT  -- Sin PostGIS
```

**Desventajas**:
- Sintaxis menos intuitiva que DECIMAL
- Funciones limitadas sin PostGIS
- No añade valor vs DECIMAL para nuestro caso

**Decisión**: DECIMAL es más simple y universal.

---

## Implementación de Búsquedas

### Búsqueda por Bounding Box (Implementado)

```sql
-- Buscar fallas en área rectangular
SELECT id_falla, nombre, ubicacion_lat, ubicacion_lon
FROM fallas
WHERE ubicacion_lat BETWEEN :lat_min AND :lat_max
  AND ubicacion_lon BETWEEN :lon_min AND :lon_max
  AND activa = true;
```

**Performance**: <5ms con 400 registros e índices B-tree.

### Búsqueda por Distancia (Aplicación)

Cálculo de distancia Haversine en Java:
```java
public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371; // Radio de la Tierra en km
    
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
    return R * c; // Distancia en km
}
```

**Estrategia**:
1. Filtrar con bounding box en SQL (rápido)
2. Calcular distancia exacta en aplicación (suficiente)
3. Ordenar y limitar resultados

---

## Consecuencias

### Positivas
- ✅ Simplicidad: DECIMAL es universalmente entendido
- ✅ Sin curva de aprendizaje de PostGIS
- ✅ Performance suficiente para 400 registros
- ✅ Fácilmente reversible (PostGIS comentado, no eliminado)
- ✅ Menor superficie de ataque de bugs geoespaciales
- ✅ Menor tamaño de imagen Docker (~50MB menos)

### Negativas
- ⚠️ Distancia calculada en aplicación (no en BD)
- ⚠️ Índices B-tree menos óptimos que GiST (no crítico)
- ⚠️ Sin funciones espaciales avanzadas (buffer, intersección)

### Neutrales
- 🔄 Si escalamos a 10,000+ fallas, considerar PostGIS
- 🔄 Si añadimos polígonos o líneas, PostGIS será necesario
- 🔄 Frontend usa leaflet/mapbox (agnóstico a backend)

---

## Criterios para Activar PostGIS

Considerar activar PostGIS si:
1. **Volumen**: >5,000 fallas con búsquedas frecuentes
2. **Funcionalidad**: Necesitamos polígonos, rutas, intersecciones
3. **Performance**: Búsquedas geoespaciales >100ms
4. **Análisis espacial**: Clustering, heatmaps, proximidad compleja

**Estado actual**: Ninguno de estos criterios aplica.

---

## Migraciones Futuras

### Activar PostGIS (1 hora)

```sql
-- 1. Instalar extensión
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Añadir columna geography
ALTER TABLE fallas ADD COLUMN ubicacion_postgis GEOGRAPHY(POINT, 4326);

-- 3. Migrar datos existentes
UPDATE fallas
SET ubicacion_postgis = ST_MakePoint(ubicacion_lon, ubicacion_lat)::geography
WHERE ubicacion_lat IS NOT NULL;

-- 4. Crear índice GiST
CREATE INDEX idx_fallas_ubicacion_postgis ON fallas USING GIST(ubicacion_postgis);

-- 5. Opcional: eliminar columnas antiguas tras validación
-- ALTER TABLE fallas DROP COLUMN ubicacion_lat, DROP COLUMN ubicacion_lon;
```

---

## Referencias

- [PostGIS Documentation](https://postgis.net/documentation/)
- [PostgreSQL Geometric Types](https://www.postgresql.org/docs/13/datatype-geometric.html)
- [Haversine Formula](https://en.wikipedia.org/wiki/Haversine_formula)
- [01.schema.sql](../../07.datos/scripts/01.schema.sql) - Implementación actual

---

## Experiencia en Producción

**Después de implementación** (para completar tras 3 meses):
- Performance medida: _Pendiente_
- Queries geoespaciales más lentas: _Pendiente_
- Decisión de migrar a PostGIS: _Pendiente_

---

**Última revisión**: 2026-02-01  
**Próxima revisión**: Tras 3 meses en producción o cuando volumen >5,000 fallas
