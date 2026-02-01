# ADR-003: Nomenclatura de Scripts SQL (NN.tipo.sql)

**Estado**: Aceptado  
**Fecha**: 2026-02-01  
**Decisores**: Equipo de desarrollo FallApp  
**Contexto relacionado**: [ADR-002](ADR-002-docker-local-development.md), [NOMENCLATURA.FICHEROS.md](../NOMENCLATURA.FICHEROS.md)

---

## Contexto y Problema

PostgreSQL en Docker ejecuta automáticamente scripts SQL del directorio `/docker-entrypoint-initdb.d/` durante la inicialización. Los scripts se ejecutan en **orden alfabético**.

**Problema**: ¿Cómo garantizar que los scripts se ejecuten en el orden correcto considerando que:
1. `01.schema.sql` debe ejecutarse antes que `10.seed.usuarios.sql`
2. `10.seed.usuarios.sql` debe ejecutarse antes que `20.import.fallas.sql` (por FK)
3. `30.vistas.consultas.sql` debe ejecutarse al final (depende de datos)
4. Los nombres deben ser autodescriptivos
5. Debe ser fácil insertar nuevos scripts en medio

**Alternativas consideradas**:
- Nombres secuenciales simples (1, 2, 3...)
- Nombres con timestamp (20260201_schema.sql)
- Nombres con prefijo decimal (01, 02, 03...)
- Nombres con prefijo decimal espaciado (01, 10, 20, 30...)
- Nombres sin prefijo numérico

---

## Factores de Decisión

| Factor | Peso | 1,2,3 | Timestamp | 01,02,03 | 01,10,20 | Sin número |
|--------|------|-------|-----------|----------|----------|------------|
| **Orden garantizado** | Alta | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ |
| **Fácil inserción** | Alta | ⭐⭐ | ⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Legibilidad** | Media | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Autodescriptivo** | Media | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Compatible Docker** | Alta | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ |

---

## Decisión

**Elegimos el formato `NN.tipo.sql`** con prefijos decimales espaciados:
- **NN**: Número de 2 dígitos (01, 10, 20, 30...)
- **tipo**: Descripción del propósito del script
- **.sql**: Extensión

### Ejemplos implementados

```
07.datos/scripts/
├── 01.schema.sql             # Creación de tablas, tipos, índices
├── 10.seed.usuarios.sql      # Datos iniciales de usuarios
├── 20.import.fallas.sql      # Importación de fallas desde JSON
└── 30.vistas.consultas.sql   # Vistas y funciones SQL
```

### Reglas de nomenclatura

1. **Prefijos espaciados de 10 en 10**
   - 01, 10, 20, 30, 40, 50...
   - Permite insertar scripts intermedios sin renombrar
   
2. **Nombres descriptivos en minúsculas**
   - `schema`, `seed`, `import`, `vistas`
   - Separados por guiones si necesario: `import-fallas`
   
3. **Un propósito por script**
   - `01.schema.sql`: SOLO estructura (tablas, tipos, índices)
   - `10.seed.usuarios.sql`: SOLO datos de usuarios
   - NO mezclar propósitos

4. **Idempotencia obligatoria**
   - Usar `CREATE TABLE IF NOT EXISTS`
   - Usar `INSERT ... ON CONFLICT DO NOTHING`
   - Scripts ejecutables múltiples veces sin error

---

## Justificación

### 1. Orden garantizado por alfabético

Docker ejecuta en orden alfabético:
```
01.schema.sql       → Primero
10.seed.usuarios.sql → Segundo
20.import.fallas.sql → Tercero
30.vistas.consultas.sql → Cuarto
```

### 2. Fácil inserción de scripts intermedios

Si necesitamos un script entre `10.seed` y `20.import`:
```
10.seed.usuarios.sql
15.seed.fallas_default.sql    ← Nuevo script
20.import.fallas.sql
```

Sin renombrar archivos existentes.

### 3. Autodescriptivo

El nombre comunica:
- **Orden**: `01` antes que `10`
- **Propósito**: `schema`, `seed`, `import`
- **Tipo**: `.sql`

### 4. Compatible con herramientas

- ✅ Docker: Ejecuta en orden
- ✅ Git: Diffs claros por archivo
- ✅ IDEs: Autocompletado alfabético
- ✅ Scripts bash: Fácil de iterar con `ls *.sql`

---

## Por qué NO otras alternativas

### Secuencia simple (1, 2, 3...)

**Problema de inserción**:
```
1.schema.sql
2.seed.sql
# ❌ ¿Cómo insertar script entre 2 y 3?
# ❌ Hay que renombrar 3 → 4, 4 → 5, etc.
3.import.sql
```

### Timestamp (20260201_schema.sql)

**Problemas**:
1. **No comunica orden lógico**
   - `20260201` no dice "esto va primero"
   - Hay que mirar la fecha completa
   
2. **Inserción confusa**
   - ¿Qué timestamp poner para un script intermedio?
   - Timestamps no son semánticos

3. **Conflictos en equipo**
   - Dos desarrolladores crean scripts el mismo día
   - Nombres colisionan

### Prefijos consecutivos (01, 02, 03...)

**Problema de inserción**:
```
01.schema.sql
02.seed.sql
# ❌ Necesito script entre 02 y 03
# ❌ No puedo usar 02.5 o 02a (ambiguo)
03.import.sql
```

### Sin número

**Problema de orden**:
```
import.sql     ← Puede ejecutarse antes que schema.sql
schema.sql
seed.sql
vistas.sql
```

Sin garantía de orden = falla la inicialización.

---

## Implementación

### Convención adoptada

```
NN.tipo.sql

Donde:
NN   = 01, 10, 20, 30, 40, 50, 60, 70, 80, 90
tipo = Descripción breve del propósito
```

### Rangos asignados

| Rango | Propósito | Ejemplos |
|-------|-----------|----------|
| 01-09 | Estructura base | 01.schema.sql, 02.extensions.sql |
| 10-19 | Datos iniciales | 10.seed.usuarios.sql, 11.seed.roles.sql |
| 20-29 | Importaciones | 20.import.fallas.sql, 21.import.eventos.sql |
| 30-39 | Vistas/Funciones | 30.vistas.consultas.sql, 31.funciones.sql |
| 40-49 | Triggers | 40.triggers.sql |
| 50-59 | Permisos/Seguridad | 50.permissions.sql |
| 60-69 | Migraciones | 60.migracion.v2.sql |
| 70-79 | Optimizaciones | 70.indices.adicionales.sql |
| 80-89 | Limpieza | 80.cleanup.sql |
| 90-99 | Auditoría/Debug | 90.audit.sql, 99.debug.sql |

### Ejemplo de evolución

**Versión 1** (inicial):
```
01.schema.sql
10.seed.usuarios.sql
20.import.fallas.sql
30.vistas.consultas.sql
```

**Versión 2** (añadimos eventos):
```
01.schema.sql
10.seed.usuarios.sql
15.seed.categorias.sql        ← Nuevo
20.import.fallas.sql
25.import.eventos.sql          ← Nuevo
30.vistas.consultas.sql
35.vistas.eventos.sql          ← Nuevo
```

Sin renombrar archivos existentes.

---

## Consecuencias

### Positivas
- ✅ Orden de ejecución predecible y garantizado
- ✅ Inserción de scripts intermedios sin renombrar
- ✅ Nombres autodescriptivos y legibles
- ✅ Compatible con todas las herramientas
- ✅ Documentado en [NOMENCLATURA.FICHEROS.md](../NOMENCLATURA.FICHEROS.md)
- ✅ Rangos permiten organización semántica

### Negativas
- ⚠️ Requiere planificación inicial de rangos
- ⚠️ Posible confusión con numeración no consecutiva (01, 10 en lugar de 01, 02)

### Neutrales
- 🔄 Convención específica de FallApp (no estándar universal)
- 🔄 Documentada para onboarding

---

## Validación en Producción

**Resultado medido** (2026-02-01):
- ✅ 4 scripts ejecutados en orden correcto
- ✅ 0 errores de dependencias
- ✅ Inserción de script `21.run_import_fallas_fix.sql` sin renombrar
- ✅ Logs de Docker muestran orden claro:

```
/docker-entrypoint-initdb.d/01.schema.sql
/docker-entrypoint-initdb.d/10.seed.usuarios.sql
/docker-entrypoint-initdb.d/20.import.fallas.sql
/docker-entrypoint-initdb.d/30.vistas.consultas.sql
```

---

## Referencias

- [NOMENCLATURA.FICHEROS.md](../NOMENCLATURA.FICHEROS.md) - Convenciones completas
- [07.datos/scripts/README.md](../../07.datos/scripts/README.md) - Guía de scripts
- [PostgreSQL Docker Docs](https://hub.docker.com/_/postgres) - Init scripts
- Scripts implementados: [07.datos/scripts/](../../07.datos/scripts/)

---

## Evolución Futura

### Consideraciones para migrar a Flyway

Si en el futuro usamos Flyway (migraciones versionadas):
- Flyway usa formato `V###__description.sql` (3 dígitos)
- Nuestra nomenclatura es compatible: `V001__schema.sql`, `V010__seed.sql`
- Migración trivial: renombrar prefijos de NN → V0NN

```bash
# Migración automática
for f in *.sql; do
  mv "$f" "V0${f}"
done

# Resultado
01.schema.sql → V001__schema.sql
10.seed.usuarios.sql → V010__seed.usuarios.sql
```

---

**Última revisión**: 2026-02-01  
**Próxima revisión**: Al implementar Flyway
