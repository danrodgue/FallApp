# ADR-001: Elección de PostgreSQL sobre MongoDB

**Estado**: Aceptado  
**Fecha**: 2026-02-01  
**Decisores**: Equipo de desarrollo FallApp  
**Contexto relacionado**: Migración de base de datos, [03.BASE-DATOS.md](../especificaciones/03.BASE-DATOS.md)

---

## Contexto y Problema

FallApp necesita una base de datos para gestionar:
- Usuarios con roles y permisos (ADMIN, CASAL, USUARIO)
- Fallas con datos estructurados y geolocalización
- Eventos, Ninots, Votos y Comentarios con relaciones complejas

**Problema**: ¿Qué motor de base de datos utilizar para garantizar:
1. Integridad de datos (transacciones ACID)
2. Relaciones complejas entre entidades
3. Consultas eficientes con múltiples JOINs
4. Búsqueda full-text en español
5. Escalabilidad futura

**Alternativas consideradas**:
- PostgreSQL (relacional)
- MongoDB (documental)
- Firebase (BaaS)

---

## Factores de Decisión

| Factor | Peso | PostgreSQL | MongoDB | Firebase |
|--------|------|------------|---------|----------|
| **Relaciones complejas** | Alta | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| **Integridad referencial** | Alta | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| **Transacciones ACID** | Alta | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **Full-text search** | Media | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **Geolocalización** | Media | ⭐⭐⭐⭐⭐ (PostGIS) | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Madurez/Estabilidad** | Media | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Curva de aprendizaje** | Baja | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Costo** | Media | ⭐⭐⭐⭐⭐ (Open) | ⭐⭐⭐⭐⭐ (Open) | ⭐⭐ (Escala) |
| **Lock-in de proveedor** | Media | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ |

---

## Decisión

**Elegimos PostgreSQL 13** como motor principal de base de datos.

### Justificación

1. **Modelo de datos relacional natural**
   ```
   Usuario 1:N Falla 1:N Evento
   Usuario 1:N Voto N:1 Falla
   Usuario 1:N Comentario N:1 Falla
   ```
   Las relaciones son fundamentales en el dominio de FallApp.

2. **Integridad garantizada**
   - Foreign Keys con CASCADE DELETE
   - CHECK constraints para validación
   - UNIQUE constraints para unicidad
   - NOT NULL para campos obligatorios

3. **Consultas complejas eficientes**
   - JOINs optimizados con índices B-tree
   - Agregaciones (COUNT, AVG, SUM) nativas
   - Subconsultas correlacionadas
   - Common Table Expressions (CTEs)

4. **Búsqueda full-text en español**
   ```sql
   CREATE INDEX idx_fallas_fts ON fallas 
   USING GIN(to_tsvector('spanish', nombre || ' ' || lema));
   ```

5. **Sin vendor lock-in**
   - Estándar SQL ampliamente conocido
   - Migración a AWS RDS, Google Cloud SQL o Azure sencilla
   - Herramientas de backup/restore maduras

6. **Extensibilidad**
   - PostGIS para geolocalización avanzada (opcional)
   - Extensiones maduras y bien documentadas
   - Funciones y procedimientos almacenados

### Desventajas aceptadas de PostgreSQL

1. **Más verboso que MongoDB** para documentos anidados
   - **Mitigación**: Usamos `JSONB` para datos semi-estructurados
   
2. **Esquema rígido** (cambios requieren migraciones)
   - **Mitigación**: Flyway para migraciones versionadas
   
3. **Escalado horizontal** más complejo que MongoDB
   - **Mitigación**: No es un problema para el volumen esperado (~500 fallas)

---

## Por qué NO MongoDB

### Ventajas de MongoDB (que no necesitamos)
- ✗ Esquema flexible: Nuestro dominio ES estructurado
- ✗ Escalado horizontal fácil: No tenemos millones de registros
- ✗ JSON nativo: SQL soporta JSONB suficientemente

### Desventajas de MongoDB (que SÍ nos afectan)
- ✗ Relaciones complejas requieren múltiples queries o $lookup pesados
- ✗ Integridad referencial manual (propenso a errores)
- ✗ Transacciones multi-documento solo en replica sets
- ✗ JOINs limitados y poco eficientes

### Ejemplo concreto

**Consulta**: "Listar fallas con votos promedio y número de comentarios"

**PostgreSQL** (1 query, eficiente):
```sql
SELECT f.nombre, 
       AVG(v.valor) as votos_promedio,
       COUNT(DISTINCT c.id_comentario) as num_comentarios
FROM fallas f
LEFT JOIN votos v ON f.id_falla = v.id_falla
LEFT JOIN comentarios c ON f.id_falla = c.id_falla
GROUP BY f.id_falla;
```

**MongoDB** (3 queries o $lookup complejos):
```javascript
// Query 1: Obtener fallas
const fallas = await Falla.find();

// Query 2: Agregar votos por falla
const votos = await Voto.aggregate([...]);

// Query 3: Agregar comentarios por falla
const comentarios = await Comentario.aggregate([...]);

// Merge manual en aplicación
```

---

## Por qué NO Firebase

1. **Vendor lock-in total**: Imposible migrar sin reescribir
2. **Costo escalable**: Facturación por lecturas/escritas puede crecer
3. **Limitaciones de queries**: Sin JOINs, sin full-text search robusto
4. **Dependencia de internet**: Backend acoplado a servicio externo

---

## Consecuencias

### Positivas
- ✅ Integridad de datos garantizada por el motor
- ✅ Consultas complejas simples y eficientes
- ✅ Full-text search en español sin dependencias externas
- ✅ Migración a producción (AWS RDS) trivial
- ✅ Herramientas maduras (pgAdmin, pg_dump, Flyway)
- ✅ Sin costos de licencia ni vendor lock-in

### Negativas
- ⚠️ Migraciones de esquema requieren planificación
- ⚠️ Curva de aprendizaje SQL para equipo (aceptable)
- ⚠️ Backup/restore más manual que Firebase (mitigable)

### Neutrales
- 🔄 Código backend cambia de MongoRepository a JpaRepository
- 🔄 Entidades con anotaciones JPA en lugar de @Document

---

## Experiencia en Producción

**Después de implementación** (para completar tras despliegue):
- Estado de performance: _Pendiente_
- Problemas encontrados: _Pendiente_
- Mejoras aplicadas: _Pendiente_

---

## Referencias

- [03.BASE-DATOS.md](../especificaciones/03.BASE-DATOS.md) - Especificación técnica PostgreSQL
- [07.datos/scripts/](../../07.datos/scripts/) - Scripts SQL implementados
- [PostgreSQL Documentation](https://www.postgresql.org/docs/13/)
- [PostGIS](https://postgis.net/) - Extensión geoespacial
- [Comparativa PostgreSQL vs MongoDB](https://www.postgresql.org/about/)

---

**Última revisión**: 2026-02-01  
**Próxima revisión**: Tras 3 meses en producción
