# Nomenclatura de Ficheros - FallApp

## 🎯 Principios Generales

- **Minúsculas**: Todos los nombres en minúsculas (excepto constantes)
- **Sin guiones**: Usar puntos (`.`) en lugar de guiones (`-`)
- **Números con ceros**: Secuencias numéricas con ceros a la izquierda (01, 02, 03...)
- **Descriptivos**: Nombres claros y específicos

## 📁 Directorios

### Estructura principal

```
FallApp/
├── 01.backend/          # Backend Spring Boot
├── 02.desktop/          # Aplicación Electron
├── 03.mobile/           # Aplicación Android
├── 04.docs/             # Documentación
├── 05.docker/           # Configuración Docker
├── 06.tests/            # Tests e2e
├── 07.datos/            # Datos y migraciones
└── 99.obsoleto/         # Código deprecado
```

Formato: `NN.nombre` donde NN es número de orden

## 📄 Ficheros SQL

**Ubicación**: `07.datos/scripts/`

### Nomenclatura SQL

```
NN.tipo.sql

Ejemplos:
├── 01.schema.sql            # Creación de tablas
├── 02.enums.sql             # Creación de tipos ENUM
├── 03.indices.sql           # Índices y optimizaciones
├── 04.security.sql          # Row-level security
├── 05.functions.sql         # Funciones stored procedures
├── 06.triggers.sql          # Triggers
├── 10.seed.usuarios.sql     # Datos iniciales - Usuarios
├── 11.seed.fallas.sql       # Datos iniciales - Fallas
├── 12.seed.eventos.sql      # Datos iniciales - Eventos
├── 20.import.fallas.json.sql # Importación desde JSON
└── 30.views.sql             # Vistas y materialized views
```

**Rango de números**:
- `01-09`: Estructura base (schema, enums, índices, seguridad)
- `10-19`: Datos iniciales (seeds)
- `20-29`: Importación/migración
- `30-39`: Vistas y queries
- `40-49`: Procedimientos especiales
- `50+`: Historiales y auditoría

### Orden de ejecución

Docker Compose ejecuta scripts en orden alfabético en `/docker-entrypoint-initdb.d/`:

```bash
01.schema.sql           ← Primero: estructura
02.enums.sql            ← Tipos
03.indices.sql          ← Optimizaciones
04.security.sql         ← Permisos
...
10.seed.usuarios.sql    ← Datos (después de estructura)
11.seed.fallas.sql
20.import.fallas.json.sql
```

## 📋 Archivos de Configuración

### Docker

```
05.docker/
├── docker-compose.yml   # Composición de servicios
├── .env.example         # Variables de entorno (template)
├── README.md            # Documentación Docker
├── Dockerfile           # (en subdirectorios de servicios)
└── postgres.conf        # Configuración PostgreSQL (opcional)
```

Formato: `nombre.extension` (sin números)

### Backend (Spring Boot)

```
01.backend/src/main/resources/
├── application.properties           # Configuración principal
├── application.dev.properties       # Desarrollo
├── application.prod.properties      # Producción
└── application.test.properties      # Tests
```

Formato: `application.PERFIL.properties`

## 📚 Documentación

### En `04.docs/`

```
04.docs/
├── 00.INDICE.md                     # Índice maestro
├── 01.GUIA.PROGRAMACION.md          # Convenciones de código
├── 02.GUIA.PROMPTS.IA.md            # Patrones IA
├── 03.CONVENCIONES.IDIOMA.md        # Uso del español
├── NOMENCLATURA.FICHEROS.md         # ← Este archivo
├── LEEME.DESARROLLADORES.md         # Quick start
├── LEEME.IA.md                      # Para asistentes IA
└── especificaciones/
    ├── 00.VISION.GENERAL.md
    ├── 01.SISTEMA.USUARIOS.md
    ├── 02.FALLAS.md
    └── 03.BASE.DATOS.md
```

**Formato documentación**:
- `NN.NOMBRE.COMPLETO.md` para documentos estructurados
- `LEEME.TIPO.md` para archivos de lectura inmediata
- Números con ceros: `00`, `01`, `02`...

## 🔤 Convenciones por Tipo

### Python (si aplica)

```
nombre_archivo.py           # Minúsculas, guiones bajos
mi_modulo.py
utilidades.py
```

### JavaScript/TypeScript

```
miArchivo.js                # camelCase para archivos funcionales
MyComponent.jsx             # PascalCase para componentes
my_util.js                  # snake_case para utilidades genéricas
```

### Java (Backend)

```
MiClase.java                # PascalCase (convención Java)
miMetodo()                  # camelCase para métodos
MI_CONSTANTE               # UPPER_SNAKE_CASE para constantes
```

### Kotlin (Mobile)

```
MiClase.kt                  # PascalCase
miMetodo()                  # camelCase
```

## 📝 Archivos de Datos

```
07.datos/
├── raw/
│   └── falles.fallas.json
├── transformado/
│   ├── usuarios.json
│   ├── fallas.csv
│   └── eventos.json
├── scripts/
│   ├── 01.schema.sql
│   ├── 02.enums.sql
│   └── ...
└── migracion/
    ├── 01.migracion.mongodb.a.postgresql.sql
    ├── 02.limpieza.datos.sql
    └── README.migracion.md
```

**Formato datos**: `nombre.extension`
**Migración**: `NN.descripcion.sql` o `NN.TIPO.DESCRIPCION.md`

## 🎯 Resumen de Patrones

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Directorios | `NN.nombre` | `01.backend`, `07.datos` |
| Scripts SQL | `NN.tipo.sql` | `01.schema.sql`, `10.seed.usuarios.sql` |
| Documentación | `NN.NOMBRE.COMPLETO.md` | `01.GUIA.PROGRAMACION.md` |
| Config general | `nombre.extension` | `docker-compose.yml`, `.env.example` |
| Config por perfil | `application.PERFIL.ext` | `application.dev.properties` |
| Código fuente | Convenciones de lenguaje | `MiClase.java`, `mi_archivo.py` |
| Datos | `nombre.json/csv` | `falles.fallas.json` |

## ✅ Checklist al crear ficheros

- [ ] Usar minúsculas (excepto PascalCase en clases)
- [ ] Usar puntos (`.`) en lugar de guiones (`-`)
- [ ] Números con ceros a la izquierda (`01`, `02`)
- [ ] Nombres descriptivos y claros
- [ ] Extensión correcta (`.sql`, `.md`, `.json`, etc.)
- [ ] Ubicación correcta según categoría
- [ ] Documentar si es fichero de referencia importante

## 🔄 Cambios a partir de ahora

**Anterior** → **Nuevo**
```
docker-compose.yml          → docker-compose.yml        (sin cambios)
01-backend                  → 01.backend                (punto en lugar de guión)
guía-programación.md        → GUIA.PROGRAMACION.md      (punto, mayúsculas)
init-db.sql                 → 01.schema.sql             (número, punto, tipo)
seed-usuarios.sql           → 10.seed.usuarios.sql      (número secuencial)
```

---

Última actualización: 2026-02-01
Aplicable a partir de: Sprint 2 (Feature: Base de Datos)
