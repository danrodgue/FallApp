# ADR-009: Simplificación de Tabla Ninots

## Estado
✅ **IMPLEMENTADO** - Ejecutado el 2026-02-02

## Fecha
2026-02-02

## Contexto

La tabla `ninots` fue diseñada inicialmente con 20+ campos para almacenar información detallada de figuras falleras:
- Dimensiones físicas (altura, ancho, profundidad, peso)
- Información técnica (material, artista, año construcción)
- Sistema de premios (premiado, categoría, año)
- Múltiples imágenes (principal + array adicionales)
- Notas técnicas y descripciones extensas

**Problema identificado**:
1. Los datos fuente (`07.datos/raw/falles-fallas.jsonl`) **NO contienen información de ninots individuales**
2. Solo disponemos de **imágenes de bocetos** de cada falla (campo `boceto`)
3. El 90% de los campos de la tabla permanecen **NULL** o con valores por defecto
4. Mantener esta complejidad sin datos reales genera:
   - Código innecesario en servicios y controladores
   - Validaciones complejas que no se utilizan
   - DTOs con muchos campos opcionales
   - Confusión sobre qué campos usar

## Decisión

**Simplificar la tabla `ninots` a campos esenciales** con datos realmente disponibles:

```sql
CREATE TABLE ninots (
    id_ninot SERIAL PRIMARY KEY,
    id_falla INTEGER NOT NULL,
    nombre VARCHAR(255) NULL,
    url_imagen VARCHAR(500) NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ninots_id_falla FOREIGN KEY (id_falla) 
        REFERENCES fallas(id_falla) ON DELETE CASCADE
);
```

**Reducción**: De 20+ campos a **5 campos esenciales**

## Campos Eliminados

| Campo | Justificación |
|-------|---------------|
| `titulo_obra` | Sin datos disponibles |
| `descripcion` | Sin descripciones de ninots individuales |
| `altura_metros`, `ancho_metros`, `profundidad_metros`, `peso_toneladas` | Información técnica no disponible |
| `material_principal` | Sin datos de materiales |
| `artista_constructor` | Sin datos de artistas de ninots |
| `año_construccion` | No disponible |
| `url_imagenes_adicionales` | Solo 1 imagen disponible por falla |
| `premiado`, `categoria_premio`, `año_premio` | Sistema de premios no implementado |
| `notas_tecnicas` | Sin información técnica |
| `actualizado_en` | Innecesario para entidad read-only |

## Consecuencias

### Positivas ✅

1. **Simplicidad**: Reducción drástica de complejidad
2. **Datos reales**: 100% de campos con información disponible
3. **Menos validaciones**: Solo validar URL de imagen
4. **Código más limpio**: Menos mapeos en servicios
5. **Mejor rendimiento**: Menos datos en memoria y transferencia
6. **Mantenibilidad**: Más fácil de entender y modificar

### Negativas ❌

1. **Pérdida de flexibilidad**: No se pueden almacenar datos técnicos detallados
2. **Sistema de premios deshabilitado**: Requeriría rediseño
3. **Una sola imagen**: No múltiples vistas del ninot
4. **Menos capacidades de búsqueda**: Sin filtros por dimensiones/materiales

### Mitigaciones ✅

1. **Extensibilidad futura**: Agregar campos cuando tengamos datos reales
2. **Alternativa premios**: Usar tabla separada `premios_ninots` si es necesario
3. **Múltiples imágenes**: Crear múltiples registros de ninot para una falla
4. **Búsquedas**: Implementar cuando los datos estén disponibles

## Alternativas Consideradas

### Alternativa 1: Mantener Estructura Actual
**Rechazada** - Genera complejidad innecesaria sin datos reales

### Alternativa 2: Agregar Todos los Campos como JSONB
**Rechazada** - Dificulta validaciones y queries. JSONB apropiado para datos dinámicos, no para estructura vacía

### Alternativa 3: Tabla Intermedia con Información Extendida
**Rechazada** - Sobre-ingeniería para problema inexistente

## Implementación

Ver especificación completa en: `04.docs/especificaciones/SPEC-NINOT-SIMPLIFICADO.md`

**Script de migración**: `07.datos/scripts/10.migracion.ninots.simplificados.sql`

**Estimación**: 5 días laborables
- Base de datos: 1 día
- Backend: 2 días
- Frontend: 1 día
- Testing: 1 día

## Referencias

- **Especificación**: `SPEC-NINOT-001` (SPEC-NINOT-SIMPLIFICADO.md)
- **Issue**: TBD
- **Datos originales**: `07.datos/raw/falles-fallas.jsonl`
- **ADRs relacionados**: 
  - ADR-001: PostgreSQL vs MongoDB
  - ADR-005: Vistas vs Queries Backend

## Notas

Esta decisión sigue el principio **YAGNI** (You Aren't Gonna Need It):
> "No implementes funcionalidad hasta que sea necesaria"

Cuando tengamos acceso a datos reales de ninots (dimensiones, artistas, materiales), será sencillo agregar campos a la tabla simplificada.

---

**Estado de aprobación**:
- [ ] Tech Lead / Arquitecto
- [ ] Product Owner  
- [ ] Equipo Frontend

**Última actualización**: 2026-02-02
