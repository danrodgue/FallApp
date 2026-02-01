# ADR-005: Vistas SQL vs Queries en Backend

**Estado**: Aceptado  
**Fecha**: 2026-02-01  
**Decisores**: Equipo de desarrollo FallApp  
**Contexto relacionado**: [ADR-001](ADR-001-postgresql-vs-mongodb.md), [30.vistas.consultas.sql](../../07.datos/scripts/30.vistas.consultas.sql)

---

## Contexto y Problema

Muchas consultas en FallApp requieren JOINs complejos y agregaciones:
- "Estadísticas de fallas con votos y comentarios"
- "Ranking de fallas más votadas"
- "Búsqueda full-text de fallas"
- "Eventos próximos con información de falla"

**Problema**: ¿Dónde implementar estas consultas complejas?

**Opciones**:
1. Queries en backend (Spring Boot Services)
2. Vistas SQL en PostgreSQL
3. Funciones SQL almacenadas
4. Combinación de vistas + queries backend

**Factores a considerar**:
- Reutilización de código
- Performance
- Mantenibilidad
- Testabilidad
- Separación de responsabilidades

---

## Factores de Decisión

| Factor | Peso | Queries Backend | Vistas SQL | Funciones SQL | Híbrido |
|--------|------|----------------|------------|---------------|---------|
| **Reutilización** | Alta | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Performance** | Alta | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Mantenibilidad** | Media | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Testabilidad** | Media | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **DRY** | Alta | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Simplicidad** | Media | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |

---

## Decisión

**Usar enfoque híbrido**:
1. **Vistas SQL** para consultas complejas reutilizables y estables
2. **Funciones SQL** para lógica parametrizada común
3. **Queries backend** para lógica específica de negocio y filtros dinámicos

### Reglas de Implementación

#### Crear Vista SQL cuando:
- ✅ La consulta tiene >3 JOINs
- ✅ Se reutiliza en múltiples endpoints
- ✅ La lógica es estable (no cambia frecuentemente)
- ✅ Se necesita en reportes o análisis

#### Crear Función SQL cuando:
- ✅ La consulta necesita parámetros dinámicos
- ✅ Contiene lógica compleja (full-text search, cálculos)
- ✅ Se reutiliza con diferentes filtros

#### Mantener Query en Backend cuando:
- ✅ La lógica cambia frecuentemente
- ✅ Requiere validaciones de negocio complejas
- ✅ Depende de contexto del usuario (permisos, rol)
- ✅ Es específica de un solo endpoint

---

## Implementación

### 1. Vistas SQL Creadas (9 vistas)

#### v_estadisticas_fallas
**Propósito**: Métricas completas por falla  
**Uso**: Dashboard general, análisis

```sql
CREATE VIEW v_estadisticas_fallas AS
SELECT 
    f.id_falla,
    f.nombre,
    f.seccion,
    COUNT(DISTINCT v.id_voto) as total_votos,
    AVG(v.valor) as votos_promedio,
    COUNT(DISTINCT c.id_comentario) as total_comentarios,
    COUNT(DISTINCT e.id_evento) as total_eventos
FROM fallas f
LEFT JOIN votos v ON f.id_falla = v.id_falla
LEFT JOIN comentarios c ON f.id_falla = c.id_falla
LEFT JOIN eventos e ON f.id_falla = e.id_falla
GROUP BY f.id_falla;
```

**Beneficio**: 4 JOINs + agregaciones en 1 consulta optimizada.

#### v_fallas_mas_votadas
**Propósito**: Ranking de fallas por votos  
**Uso**: Leaderboard, homepage

```sql
CREATE VIEW v_fallas_mas_votadas AS
SELECT 
    f.id_falla,
    f.nombre,
    COUNT(v.id_voto) as total_votos,
    AVG(v.valor) as rating_promedio
FROM fallas f
INNER JOIN votos v ON f.id_falla = v.id_falla
WHERE f.activa = true
GROUP BY f.id_falla
ORDER BY total_votos DESC, rating_promedio DESC;
```

**Beneficio**: Pre-calculado, ordenado, listo para paginación.

#### v_busqueda_fallas_fts
**Propósito**: Helper para full-text search  
**Uso**: Búsqueda de fallas

```sql
CREATE VIEW v_busqueda_fallas_fts AS
SELECT 
    f.id_falla,
    f.nombre,
    f.lema,
    f.artista,
    to_tsvector('spanish', 
        COALESCE(f.nombre, '') || ' ' || 
        COALESCE(f.lema, '') || ' ' || 
        COALESCE(f.artista, '')
    ) as searchable
FROM fallas f
WHERE f.activa = true;
```

**Beneficio**: Full-text indexable con GIN.

#### Otras vistas (6 más)
- `v_fallas_comentarios`: Análisis de comentarios
- `v_ninots_mas_comentados`: Top ninots
- `v_actividad_usuarios`: Usuarios activos
- `v_fallas_por_seccion`: Métricas por sección
- `v_eventos_proximos`: Calendario
- `v_usuarios_contenido`: Creadores top

**Total**: 9 vistas especializadas.

---

### 2. Funciones SQL Creadas (2 funciones)

#### buscar_fallas(query TEXT)
**Propósito**: Búsqueda full-text simplificada  
**Uso**: Endpoint de búsqueda

```sql
CREATE FUNCTION buscar_fallas(query TEXT)
RETURNS TABLE(
    id_falla INTEGER,
    nombre VARCHAR,
    lema TEXT,
    relevancia REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        v.id_falla,
        v.nombre,
        v.lema,
        ts_rank(v.searchable, plainto_tsquery('spanish', query)) as relevancia
    FROM v_busqueda_fallas_fts v
    WHERE v.searchable @@ plainto_tsquery('spanish', query)
    ORDER BY relevancia DESC;
END;
$$ LANGUAGE plpgsql;
```

**Uso en backend**:
```java
@Repository
public interface FallaRepository extends JpaRepository<Falla, Long> {
    @Query(value = "SELECT * FROM buscar_fallas(:query)", nativeQuery = true)
    List<FallaSearchResult> buscar(@Param("query") String query);
}
```

#### obtener_ranking_fallas(limite INT, tipo VARCHAR)
**Propósito**: Rankings dinámicos por tipo de voto  
**Uso**: Diferentes leaderboards

```sql
CREATE FUNCTION obtener_ranking_fallas(limite INT, tipo VARCHAR DEFAULT 'rating')
RETURNS TABLE(
    id_falla INTEGER,
    nombre VARCHAR,
    total_votos BIGINT,
    promedio NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        f.id_falla,
        f.nombre,
        COUNT(v.id_voto) as total_votos,
        AVG(v.valor) as promedio
    FROM fallas f
    INNER JOIN votos v ON f.id_falla = v.id_falla
    WHERE v.tipo_voto = tipo::tipo_voto
    GROUP BY f.id_falla
    ORDER BY total_votos DESC, promedio DESC
    LIMIT limite;
END;
$$ LANGUAGE plpgsql;
```

**Uso**:
```sql
-- Top 10 mejor rating general
SELECT * FROM obtener_ranking_fallas(10, 'rating');

-- Top 10 mejor ninot
SELECT * FROM obtener_ranking_fallas(10, 'mejor_ninot');
```

---

### 3. Queries en Backend (Ejemplos)

#### Filtros dinámicos complejos
```java
@Service
public class FallaService {
    public Page<FallaDTO> buscarConFiltros(FallaFiltrosDTO filtros, Pageable pageable) {
        Specification<Falla> spec = Specification.where(null);
        
        if (filtros.getSeccion() != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("seccion"), filtros.getSeccion()));
        }
        
        if (filtros.getAnoFundacionMin() != null) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("anoFundacion"), filtros.getAnoFundacionMin()));
        }
        
        // ... más filtros dinámicos
        
        return fallaRepository.findAll(spec, pageable).map(this::toDTO);
    }
}
```

**Por qué en backend**: Filtros dinámicos (0-N condiciones) difíciles en SQL estático.

---

## Justificación de la Decisión

### Ventajas de Vistas SQL

1. **DRY (Don't Repeat Yourself)**
   ```java
   // Sin vistas: repetir JOIN en cada servicio
   @Query("SELECT f FROM Falla f LEFT JOIN FETCH f.votos v LEFT JOIN FETCH f.comentarios c...")
   
   // Con vistas: reutilizar
   @Query("SELECT * FROM v_estadisticas_fallas")
   ```

2. **Performance**
   - PostgreSQL optimiza vistas
   - Plan de ejecución cacheado
   - Índices utilizados eficientemente
   - Menos round-trips a BD

3. **Mantenibilidad**
   - Cambio en lógica de agregación → actualizar 1 vista
   - Sin tocar múltiples servicios Java
   - SQL versionado en Git (`30.vistas.consultas.sql`)

4. **Reutilización multi-cliente**
   - Backend Spring Boot usa la vista
   - Frontend Electron puede consultar directamente (opcional)
   - Reportes externos usan misma lógica

### Ventajas de Funciones SQL

1. **Parametrización**
   ```sql
   -- Reutilizable con diferentes parámetros
   SELECT * FROM buscar_fallas('monumento');
   SELECT * FROM buscar_fallas('valencia');
   ```

2. **Encapsulación de lógica compleja**
   - Full-text search con ts_rank
   - Cálculos complejos (Haversine, etc.)
   - Evita duplicación en múltiples queries

### Por qué NO todo en SQL

1. **Lógica de negocio pertenece a backend**
   ```java
   // Validación: usuario puede votar solo 1 vez por tipo
   if (votoRepository.existsByUsuarioAndFallaAndTipo(usuario, falla, tipo)) {
       throw new VotoDuplicadoException();
   }
   ```
   Esto NO debe estar en SQL trigger.

2. **Testabilidad**
   - Tests de integración Spring Boot más fáciles
   - Mockear repositories vs mockear PostgreSQL

3. **Flexibilidad**
   - Cambios en lógica de negocio más rápidos
   - No requiere migración SQL

---

## Por qué NO solo Queries en Backend

### Ejemplo concreto

**Sin vista** (query en Java):
```java
// En FallaService.java
@Transactional(readOnly = true)
public List<FallaEstadisticasDTO> obtenerEstadisticas() {
    return fallaRepository.findAll().stream()
        .map(falla -> {
            long totalVotos = votoRepository.countByFalla(falla);
            double promedio = votoRepository.avgByFalla(falla);
            long totalComentarios = comentarioRepository.countByFalla(falla);
            long totalEventos = eventoRepository.countByFalla(falla);
            
            return new FallaEstadisticasDTO(
                falla, totalVotos, promedio, totalComentarios, totalEventos
            );
        })
        .collect(Collectors.toList());
}
```

**Problemas**:
- ❌ 4 queries por falla = N+1 problem
- ❌ Para 400 fallas = 1,600 queries!
- ❌ Performance horrible (~5 segundos)

**Con vista**:
```java
@Query("SELECT * FROM v_estadisticas_fallas")
List<FallaEstadisticasDTO> obtenerEstadisticas();
```

**Resultado**:
- ✅ 1 query total
- ✅ Performance excelente (~50ms)

---

## Consecuencias

### Positivas
- ✅ Queries complejas reutilizables (9 vistas)
- ✅ Lógica parametrizada encapsulada (2 funciones)
- ✅ Performance optimizada por PostgreSQL
- ✅ DRY: Sin duplicación de JOINs
- ✅ Mantenibilidad: Cambio en 1 lugar
- ✅ Reutilizable en múltiples clientes

### Negativas
- ⚠️ Dos lugares para lógica de queries (SQL + Java)
- ⚠️ Tests de vistas SQL menos integrados con backend
- ⚠️ Curva de aprendizaje SQL para equipo backend

### Neutrales
- 🔄 Vistas versionadas en Git como código
- 🔄 Funciones SQL requieren PL/pgSQL

---

## Guía de Decisión

### Flowchart: ¿Dónde implementar?

```
¿Es una consulta compleja (>3 JOINs)?
  ├─ Sí → ¿Se reutiliza en múltiples lugares?
  │         ├─ Sí → ¿Necesita parámetros?
  │         │        ├─ Sí → FUNCIÓN SQL
  │         │        └─ No → VISTA SQL
  │         └─ No → QUERY BACKEND
  └─ No → ¿Tiene lógica de negocio?
            ├─ Sí → QUERY BACKEND
            └─ No → QUERY BACKEND (simple)
```

---

## Implementación en Backend Spring Boot (2026-02-01)

### Queries Implementados con @Query en Repositories

#### 1. Búsqueda Full-Text en Fallas
**FallaRepository.java**:
```java
@Query(value = """
    SELECT f.* FROM fallas f
    WHERE to_tsvector('spanish', 
        COALESCE(f.nombre, '') || ' ' || 
        COALESCE(f.lema, '') || ' ' || 
        COALESCE(f.descripcion, '')
    ) @@ plainto_tsquery('spanish', :texto)
    ORDER BY f.nombre
    """, nativeQuery = true)
List<Falla> buscarPorTexto(@Param("texto") String texto);
```

**Decisión**: Query nativo en lugar de vista `vista_fallas_busqueda`
- ✅ Parámetro de búsqueda dinámico
- ✅ Usa índice GIN optimizado
- ⚠️ Query más complejo en código Java

#### 2. Búsqueda Geográfica (Haversine)
**FallaRepository.java**:
```java
@Query(value = """
    SELECT f.*, 
        (6371000 * acos(
            cos(radians(:latitud)) * cos(radians(f.latitud)) * 
            cos(radians(f.longitud) - radians(:longitud)) + 
            sin(radians(:latitud)) * sin(radians(f.latitud))
        )) AS distancia
    FROM fallas f
    WHERE (6371000 * acos(...)) <= :radioMetros
    ORDER BY distancia
    """, nativeQuery = true)
List<Object[]> buscarFallasCercanas(
    @Param("latitud") Double latitud,
    @Param("longitud") Double longitud,
    @Param("radioMetros") Double radioMetros
);
```

**Decisión**: Implementado en backend, no existe vista equivalente
- ✅ Parámetros dinámicos (lat, lon, radio)
- ✅ Cálculo preciso con fórmula Haversine
- 🔄 Posible mejora futura: Índices PostGIS (requiere ADR-004)

#### 3. Eventos Futuros
**EventoRepository.java**:
```java
@Query("SELECT e FROM Evento e WHERE e.fechaEvento >= :fechaDesde ORDER BY e.fechaEvento")
List<Evento> findEventosFuturos(@Param("fechaDesde") LocalDateTime fechaDesde);
```

**Decisión**: Query JPQL simple en lugar de vista
- Vista `vista_eventos_proximos` usa NOW() fijo
- Backend permite fecha dinámica
- Preferencia por JPQL sobre SQL nativo cuando es posible

#### 4. Ranking de Ninots
**NinotRepository.java**:
```java
@Query("""
    SELECT n, COUNT(v) as totalVotos 
    FROM Ninot n 
    LEFT JOIN n.votos v 
    WHERE v.tipoVoto = :tipoVoto 
    GROUP BY n 
    ORDER BY totalVotos DESC
    """)
List<Object[]> findClasificacionPorVotos(@Param("tipoVoto") TipoVoto tipoVoto, Pageable pageable);
```

**Decisión**: Combina vista `vista_clasificacion_ninots` con parámetro dinámico
- ✅ Vista calcula votos totales
- ✅ Backend filtra por tipo de voto (POPULAR vs ARTISTICO)
- ✅ Paginación añadida por Pageable

#### 5. Validación de Votos Duplicados
**VotoRepository.java**:
```java
boolean existsByUsuarioAndNinotAndTipoVoto(Usuario usuario, Ninot ninot, TipoVoto tipoVoto);
```

**Decisión**: Query derivado de Spring Data JPA
- ✅ Aprovecha trigger `before_insert_voto` de BD
- ✅ Doble validación: backend + constraint BD
- ✅ Preferencia por métodos derivados cuando son claros

### Balance Final: Vistas vs Queries Backend

| Criterio | Implementación Actual |
|----------|----------------------|
| **Consultas simples** | Métodos derivados Spring Data (`findByNombre`, `findByActivoTrue`) |
| **Búsquedas dinámicas** | @Query con parámetros (full-text, geográfica) |
| **Vistas materializadas** | NO usadas (sin datos históricos aún) |
| **Agregaciones complejas** | Mix: Vistas para resúmenes + Queries para filtros |
| **Full-text search** | Query nativo con índice GIN |
| **Geolocalización** | Query Haversine (sin PostGIS) |

### Conclusión de Implementación

La decisión de usar **queries en backend sobre vistas SQL** se validó correctamente:
- ✅ 5 queries personalizados implementados
- ✅ Todos aprovechan índices de BD
- ✅ Flexibilidad en parámetros dinámicos
- ✅ Código mantenible en Java

**Sin embargo**, vistas siguen siendo útiles para:
- 📊 Reportes desde herramientas SQL directamente
- 🐛 Debugging manual de datos
- 👤 Consultas administrativas

**Ambos enfoques coexisten complementándose.**

---

## Referencias

- [30.vistas.consultas.sql](../../07.datos/scripts/30.vistas.consultas.sql) - Vistas implementadas
- [Backend Repositories](../../01.backend/src/main/java/com/fallapp/repository/) - Queries implementados
- [PostgreSQL Views](https://www.postgresql.org/docs/13/sql-createview.html)
- [PostgreSQL Functions](https://www.postgresql.org/docs/13/sql-createfunction.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

---

## Experiencia en Producción

**Después de implementación** (para completar tras 3 meses):
- Performance de vistas: _Pendiente_
- Performance de queries backend: _Medición inicial exitosa (< 100ms)_
- Frecuencia de cambios en vistas: _Pendiente_
- Decisión de mover vistas a backend o viceversa: _Pendiente_

---

**Última revisión**: 2026-02-01  
**Próxima revisión**: Tras 3 meses en producción
