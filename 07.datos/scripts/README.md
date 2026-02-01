# 07.datos/scripts - Scripts SQL de Base de Datos

Colección de scripts SQL para inicialización, migración e importación de datos en PostgreSQL.

## 📋 Índice de Scripts

### ✅ 01.schema.sql (Obligatorio - Orden 1)
**Creación del esquema base de la base de datos**

- **Tamaño**: 14 KB
- **Tiempo**: ~1-2 segundos
- **Dependencias**: Ninguna

**Contiene:**
- Extensiones PostgreSQL: uuid-ossp, unaccent
- Tipos ENUM: `rol_usuario`, `tipo_evento`, `tipo_voto`, `categoria_falla`
- 8 tablas principales: usuarios, fallas, eventos, ninots, votos, comentarios
- Índices para performance (B-tree, GIN full-text, UNIQUE)
- Triggers para actualización automática de timestamps
- Función `actualizar_timestamp()` para auditoría

**Tablas creadas:**
1. `usuarios` - Usuarios de la plataforma (admin, casal, usuario)
2. `fallas` - Monumentos falleros
3. `eventos` - Actos falleros (plantà, cremà, etc.)
4. `ninots` - Figuras y ninots de las fallas
5. `votos` - Votaciones y ratings
6. `comentarios` - Comentarios en fallas

**Características de seguridad:**
- Constraint de rol enum
- Foreign Keys con CASCADE delete
- CHECK constraints para validación de datos
- UNIQUE constraints para integridad

---

### ✅ 10.seed.usuarios.sql (Recomendado - Orden 2)
**Carga de datos iniciales: Usuarios del sistema**

- **Tamaño**: 3.9 KB
- **Tiempo**: ~0.5 segundos
- **Dependencias**: Requiere `01.schema.sql`

**Inserta:**
- **admin@fallapp.es** → Administrador (contraseña: Admin@2024)
- **demo@fallapp.es** → Usuario de demostración (contraseña: Demo@2024)
- **casal@fallapp.es** → Responsable de casal (contraseña: Casal@2024)

**Notas:**
- Contraseñas son hashes bcrypt (ejemplo)
- Admin y demo están verificados por defecto
- Casal requiere verificación de email
- Cambiar contraseñas inmediatamente en producción
- Usar `.env` o secretos para producción

---

### ✅ 20.import.fallas.json.sql (Opcional - Orden 3)
**Importación de datos municipales de Fallas desde JSON**

- **Tamaño**: 11 KB
- **Tiempo**: Depende del volumen (~5-10 segundos para 400+ fallas)
- **Dependencias**: Requiere `01.schema.sql`

**Origen de datos:**
- Archivo: `/tmp/falles-fallas.json` (copiar a contenedor)
- Estructura: Array de objetos JSON con campos de falla
- Registros: ~400 fallas de Valencia

**Métodos disponibles:**

#### Opción 1: Importación Manual con COPY (Recomendada)
```bash
# Dentro del contenedor
psql -U fallapp_user -d fallapp << EOF
COPY fallas(datos_json) FROM '/docker-entrypoint-initdb.d/falles-fallas.json';
EOF
```

#### Opción 2: Importación con plpython3u (Automática)
- Descomentar función `importar_fallas_desde_json()`
- Requiere extensión plpython3u
- Se ejecuta automáticamente al iniciar contenedor

#### Opción 3: Importación Manual Fila por Fila
- Usar función auxiliar incluida
- Soporta validación y control de errores

**Mapeo de campos:**
| JSON | SQL | Tipo | Descripción |
|------|-----|------|-------------|
| `nombre` | `nombre` | VARCHAR(255) | Nombre de la falla |
| `seccion` | `seccion` | VARCHAR(5) | Sección (1A, 7C, etc.) |
| `fallera` | `fallera` | VARCHAR(255) | Reina o Infantil |
| `presidente` | `presidente` | VARCHAR(255) | Presidente del casal |
| `artista` | `artista` | VARCHAR(255) | Arquitecto/constructor |
| `lema` | `lema` | TEXT | Tema de la falla |
| `anyo_fundacion` | `anyo_fundacion` | INTEGER | Año fundación |
| `categoria` | `categoria` | categoria_falla | Brillants, Fulles, etc. |
| `ubicacion_lat` | `ubicacion_lat` | DECIMAL(10,8) | Latitud geográfica |
| `ubicacion_lon` | `ubicacion_lon` | DECIMAL(11,8) | Longitud geográfica |

---

### ✅ 30.vistas.consultas.sql (Recomendado - Orden 4)
**Vistas y funciones para reportes y análisis**

- **Tamaño**: 11 KB
- **Tiempo**: ~1-2 segundos
- **Dependencias**: Requiere `01.schema.sql` + datos importados

**Vistas creadas:**

| Vista | Descripción | Uso |
|-------|-------------|-----|
| `v_estadisticas_fallas` | Metrics completas por falla | Dashboard general |
| `v_fallas_mas_votadas` | Ranking de fallas por votos | Leaderboard |
| `v_fallas_comentarios` | Análisis de comentarios | Moderación |
| `v_ninots_mas_comentados` | Top ninots más votados | Rankings de figuras |
| `v_actividad_usuarios` | Análisis de usuarios activos | Community management |
| `v_fallas_por_seccion` | Métricas por sección | Análisis por zona |
| `v_eventos_proximos` | Calendario de eventos | Mobile app |
| `v_usuarios_contenido` | Creadores top | Analytics |
| `v_busqueda_fallas_fts` | Helper para búsqueda | Backend |

**Funciones creadas:**

```sql
-- Búsqueda full-text de fallas
SELECT * FROM buscar_fallas('monumento');

-- Ranking de fallas (top 10)
SELECT * FROM obtener_ranking_fallas(10, 'rating');

-- Ranking de mejores ninots
SELECT * FROM obtener_ranking_fallas(10, 'mejor_ninot');
```

---

## 🚀 Orden de Ejecución

Los scripts se ejecutan **automáticamente** en orden alfabético al iniciar PostgreSQL en Docker:

```
1. 01.schema.sql           ← Crear tablas y tipos
   ↓
2. 10.seed.usuarios.sql    ← Insertar usuarios
   ↓
3. 20.import.fallas.json   ← Importar fallas
   ↓
4. 30.vistas.consultas.sql ← Crear vistas/funciones
```

## 📦 Uso en Docker Compose

Los scripts se copian automáticamente a:
```
/docker-entrypoint-initdb.d/
```

PostgreSQL ejecuta todos los archivos `.sql` en orden alfabético durante `docker-compose up`.

## 🔧 Ejecución Manual

Si necesitas ejecutar scripts manualmente:

```bash
# Dentro del contenedor
docker-compose exec postgres psql -U fallapp_user -d fallapp < 01.schema.sql
docker-compose exec postgres psql -U fallapp_user -d fallapp < 10.seed.usuarios.sql
docker-compose exec postgres psql -U fallapp_user -d fallapp < 20.import.fallas.json
docker-compose exec postgres psql -U fallapp_user -d fallapp < 30.vistas.consultas.sql
```

O desde tu máquina local:

```bash
# Conexión remota (requiere que PostgreSQL esté expuesto)
psql -h localhost -U fallapp_user -d fallapp < 07.datos/scripts/01.schema.sql
```

## 📊 Validación Post-Importación

```sql
-- Verificar tablas creadas
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public';

-- Contar registros
SELECT 
    (SELECT COUNT(*) FROM usuarios) as usuarios,
    (SELECT COUNT(*) FROM fallas) as fallas,
    (SELECT COUNT(*) FROM votos) as votos,
    (SELECT COUNT(*) FROM comentarios) as comentarios;

-- Ver estadísticas
SELECT * FROM v_estadisticas_fallas LIMIT 5;
```

## 🔐 Configuración de Usuarios

Usuarios de base de datos creados:
- **fallapp_user** → Usuario de aplicación (SELECT, INSERT, UPDATE, DELETE)
- **postgres** → Usuario administrativo (SUPERUSER)

Permisos granulares:
- Selección de vistas: ✅ Permitido
- Inserción en tablas: ✅ Permitido (para app)
- Borrado directo: ⚠️ Requiere cascada
- Modificación de esquema: ❌ No permitido

## 📝 Convención de Nombres

Todos los scripts siguen la nomenclatura `NN.tipo.sql`:
- `NN` = Número de orden (01-99, zero-padded)
- `tipo` = Categoría del script
- Rango de números:
  - **01-09**: Estructura (schema, enums, índices)
  - **10-19**: Seeds (datos iniciales)
  - **20-29**: Importación/migración
  - **30-39**: Vistas y consultas
  - **40+**: Procedimientos especiales

## 🐛 Troubleshooting

### Script falla por permisos
```bash
chmod 644 07.datos/scripts/*.sql
```

### Error: "Relation already exists"
Los scripts usan `IF NOT EXISTS` y `ON CONFLICT DO NOTHING` para idempotencia.

### Error: "File not found"
Verificar que el JSON está en `/docker-entrypoint-initdb.d/`:
```bash
docker-compose exec postgres ls /docker-entrypoint-initdb.d/
```

### Error: "Extension not found"
Algunas extensiones pueden requerir instalación:
```bash
docker-compose exec postgres psql -U postgres << EOF
CREATE EXTENSION uuid-ossp;
CREATE EXTENSION unaccent;
EOF
```

## 📚 Referencias

- [Documentación de Base de Datos](../especificaciones/03.BASE.DATOS.md)
- [Nomenclatura de Ficheros](../NOMENCLATURA.FICHEROS.md)
- [Docker Compose Config](../../05.docker/README.md)
- [PostgreSQL Docs](https://www.postgresql.org/docs/13/)

## 👤 Autoría

Scripts creados para FallApp - Plataforma de Fallas Falleras de Valencia
