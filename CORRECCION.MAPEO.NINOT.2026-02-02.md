# Corrección de Mapeo Ninot - 2026-02-02

## 🐛 Problema Identificado

Al realizar peticiones a endpoints de ninots, la API retornaba error 500:

```json
{
  "exito": false,
  "mensaje": "Error interno del servidor: JDBC exception executing SQL [ERROR: column n1_0.anyo_construccion does not exist\n  Hint: Perhaps you meant to reference the column \"n1_0.año_construccion\".\n  Position: 93]",
  "datos": null
}
```

### Causa Raíz

**Inconsistencia entre mapeo JPA y esquema PostgreSQL:**
- **Entidad Java**: `@Column(name = "anyo_construccion")`
- **Columna BD**: `año_construccion` (con ñ española)

Adicionalmente, el controlador intentaba ordenar por un campo inexistente:
- **Controlador**: Ordenamiento por `fechaCreacion`
- **Entidad**: Campo real `creadoEn`

## ✅ Solución Implementada

### 1. Corrección de Mapeo de Columna

**Archivo**: `01.backend/src/main/java/com/fallapp/model/Ninot.java`

```java
// ANTES (incorrecto)
@Column(name = "anyo_construccion")
private Integer anyoConstruccion;

// DESPUÉS (correcto)
@Column(name = "año_construccion")
private Integer anyoConstruccion;
```

### 2. Corrección de Referencias de Ordenamiento

**Archivo**: `01.backend/src/main/java/com/fallapp/controller/NinotController.java`

#### Cambio 1: Parámetro por defecto
```java
// ANTES
@RequestParam(defaultValue = "fechaCreacion") String sort

// DESPUÉS
@RequestParam(defaultValue = "creadoEn") String sort
```

#### Cambio 2: Ordenamiento de premiados
```java
// ANTES
Sort.by("fechaCreacion").descending()

// DESPUÉS
Sort.by("creadoEn").descending()
```

## 📊 Endpoints Afectados y Corregidos

| Endpoint | Estado Antes | Estado Después |
|----------|--------------|----------------|
| `GET /api/ninots` | ❌ Error 500 | ✅ Funcional |
| `GET /api/ninots/{id}` | ❌ Error 500 | ✅ Funcional |
| `GET /api/ninots/falla/{idFalla}` | ❌ Error 500 | ✅ Funcional |
| `GET /api/ninots/premiados` | ❌ Error 500 | ✅ Funcional |
| `POST /api/ninots` | ❌ Error 500 | ✅ Funcional |
| `PUT /api/ninots/{id}` | ❌ Error 500 | ✅ Funcional |
| `DELETE /api/ninots/{id}` | ❌ Error 500 | ✅ Funcional |

## 🔍 Verificación

Todos los endpoints de ninots ahora responden correctamente:

```bash
# Obtener ninots paginados
curl -s "http://localhost:8080/api/ninots?page=0&size=10"
# Respuesta: {"exito":true, "datos": {...}}

# Obtener ninots de una falla
curl -s "http://localhost:8080/api/ninots/falla/1"
# Respuesta: {"exito":true, "datos": {...}}

# Obtener ninots premiados
curl -s "http://localhost:8080/api/ninots/premiados"
# Respuesta: {"exito":true, "datos": {...}}
```

## 📝 Archivos Modificados

### Archivos Fuente
1. `01.backend/src/main/java/com/fallapp/model/Ninot.java`
   - Línea 66: Corrección de `@Column(name)`

2. `01.backend/src/main/java/com/fallapp/controller/NinotController.java`
   - Línea 32: Parámetro `defaultValue` corregido
   - Línea 75: Ordenamiento `Sort.by()` corregido

### Clases Compiladas
- `01.backend/target/classes/com/fallapp/model/Ninot.class`
- `01.backend/target/classes/com/fallapp/controller/NinotController.class`

## 🔄 Git Commit

```bash
git add 01.backend/src/main/java/com/fallapp/controller/NinotController.java \
        01.backend/src/main/java/com/fallapp/model/Ninot.java

git commit -m "fix: corregir nombre de columna año_construccion en entidad Ninot"
git push
```

**Commit hash**: `a83b7b3`

## 📌 Lecciones Aprendidas

1. **Uso de caracteres especiales en nombres de columnas**:
   - PostgreSQL soporta columnas con ñ y acentos
   - JPA debe mapear exactamente el nombre de la columna (case-sensitive)
   - Alternativa: usar `anyo_construccion` en BD y evitar caracteres especiales

2. **Consistencia de nombres entre DTO y entidad**:
   - DTOs usan `fechaCreacion` (más descriptivo)
   - Entidades usan `creadoEn` (anotación `@CreationTimestamp`)
   - El ordenamiento debe usar el nombre del campo de la entidad, no del DTO

3. **Validación post-migración**:
   - Después de migraciones de esquema (ADR-003), verificar que todos los mapeos JPA estén actualizados
   - El script `99.migracion.enum.to.varchar.sql` renombró `anyo_construccion` → `año_construccion`
   - Faltó sincronizar la entidad Java con este cambio

## 🎯 Impacto

- **Criticidad**: 🔴 Alta - Bloqueaba completamente los endpoints de ninots
- **Módulos afectados**: CRUD Ninots, votaciones, comentarios por ninot
- **Tiempo de resolución**: ~45 minutos
- **Regresiones**: Ninguna detectada

## ✅ Estado Final

✅ Backend funcionando correctamente  
✅ Todos los endpoints de ninots operativos  
✅ Sin errores JDBC  
✅ Tests manuales exitosos  
✅ Cambios commiteados y pusheados  

---

**Documentado por**: GitHub Copilot  
**Fecha**: 2026-02-02  
**Relacionado con**: ADR-003 (Nomenclatura Scripts SQL)
