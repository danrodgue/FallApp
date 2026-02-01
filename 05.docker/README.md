# Docker - FallApp

Configuración de contenedores para FallApp usando Docker Compose.

## 📋 Servicios

- **PostgreSQL 13** - Base de datos relacional
- **Backend Spring Boot** - API REST en puerto 8080
- **pgAdmin** - Interfaz web para administrar PostgreSQL (desarrollo)

## 🚀 Inicio Rápido

### 1. Configurar variables de entorno

```bash
cd 05.docker
cp .env.example .env
```

Personalizar `.env` según necesidad (credenciales, puertos, etc.)

### 2. Crear estructura de datos (opcional)

Si aún no existe, crear directorios:

```bash
mkdir -p ../07.datos/{raw,transformado,scripts,migracion}
```

### 3. Iniciar servicios

```bash
# Construir imágenes (primera vez)
docker-compose build

# Iniciar contenedores
docker-compose up -d

# Ver logs
docker-compose logs -f
```

### 4. Verificar servicios

```bash
# Ver estado de contenedores
docker-compose ps

# Prueba de conectividad PostgreSQL
docker-compose exec postgres pg_isready -U fallapp_user -d fallapp

# Prueba de API
curl http://localhost:8080/api/health
```

## 🗄️ Base de Datos

### Conexión desde aplicación local (sin Docker)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fallapp
spring.datasource.username=fallapp_user
spring.datasource.password=fallapp_secure_password_2026
```

### Conexión desde aplicación en Docker

```properties
spring.datasource.url=jdbc:postgresql://postgres:5432/fallapp
spring.datasource.username=fallapp_user
spring.datasource.password=fallapp_secure_password_2026
```

### Cliente PostgreSQL local

```bash
psql -h localhost -U fallapp_user -d fallapp

# O usando Docker
docker-compose exec postgres psql -U fallapp_user -d fallapp
```

## 🖥️ pgAdmin (Desarrollo)

Acceder a: **http://localhost:5050**

Credenciales (desde `.env`):
- Email: `admin@fallapp.local`
- Contraseña: `admin1234`

### Conectar servidor PostgreSQL en pgAdmin

1. Abrir pgAdmin en el navegador
2. Click derecho en "Servers" → "Register" → "Server"
3. Pestaña "General":
   - Name: `FallApp PostgreSQL`
4. Pestaña "Connection":
   - Host: `postgres`
   - Port: `5432`
   - Maintenance database: `fallapp`
   - Username: `fallapp_user`
   - Password: `fallapp_secure_password_2026`
5. Guardar

## 📁 Volúmenes Persistentes

### PostgreSQL

**Ubicación local**: `./postgres_data/` (en 05.docker/)

Los datos persisten entre reinicios de contenedores.

**Para limpiar completamente** (CUIDADO - elimina datos):

```bash
docker-compose down -v
# Elimina también: rm -rf postgres_data/
```

### pgAdmin

**Ubicación**: Volumen Docker `pgadmin_data`

Almacena configuración y servidor registrado.

## 🔧 Operaciones Comunes

### Detener servicios

```bash
docker-compose down
```

### Reiniciar específico

```bash
docker-compose restart postgres
docker-compose restart backend
```

### Ver logs en tiempo real

```bash
docker-compose logs -f postgres    # PostgreSQL
docker-compose logs -f backend      # API
docker-compose logs -f pgadmin      # pgAdmin
```

### Ejecutar comandos en contenedor

```bash
# Bash en PostgreSQL
docker-compose exec postgres bash

# psql directo
docker-compose exec postgres psql -U fallapp_user -d fallapp

# Bash en Backend
docker-compose exec backend bash
```

### Importar datos SQL

```bash
# Desde archivo local
docker-compose exec -T postgres psql -U fallapp_user -d fallapp < ../07.datos/scripts/init-db.sql

# Desde dentro del contenedor
docker-compose exec postgres psql -U fallapp_user -d fallapp -f /docker-entrypoint-initdb.d/01-schema.sql
```

## 📊 Migración de Datos

### Importar JSON de fallas

Los scripts en `../07.datos/scripts/` se ejecutan automáticamente al iniciar PostgreSQL por primera vez.

Para importar manualmente:

```bash
docker-compose exec postgres psql -U fallapp_user -d fallapp \
  -f /docker-entrypoint-initdb.d/02-import-fallas.sql
```

## 🔐 Seguridad

### Desarrollo

- Credenciales simples en `.env`
- pgAdmin expuesto (SOLO desarrollo)
- Base de datos en host local

### Producción

1. **Cambiar todas las contraseñas** en `.env`
2. **Remover pgAdmin** del docker-compose
3. **Variables de entorno** seguras (AWS Secrets Manager, etc.)
4. **Backups automáticos** a S3
5. **SSL/TLS** en Nginx
6. **Restricción de puertos** - solo API al exterior

## 🐛 Troubleshooting

### PostgreSQL no inicia

```bash
# Ver logs
docker-compose logs postgres

# Verificar puerto disponible
lsof -i :5432

# Remover volumen y reiniciar (PIERDE DATOS)
docker-compose down -v
docker-compose up -d postgres
```

### Backend no conecta a BD

```bash
# Verificar conexión de red
docker-compose exec backend ping postgres

# Ver variables de entorno en backend
docker-compose exec backend env | grep SPRING_DATASOURCE
```

### pgAdmin no carga

```bash
# Remover volumen y reiniciar
docker-compose down
rm -rf pgadmin_data/ || true
docker-compose up -d pgadmin
```

## 📝 Configuración Avanzada

### Cambiar dialectos de PostgreSQL

En `docker-compose.yml`, Backend sección:

```yaml
SPRING_JPA_DATABASE_PLATFORM: org.hibernate.dialect.PostgreSQL13Dialect
```

Opciones: `PostgreSQL9Dialect`, `PostgreSQL10Dialect`, `PostgreSQL13Dialect`

### Limitar recursos

En `docker-compose.yml`, cada servicio tiene sección `deploy`:

```yaml
deploy:
  resources:
    limits:
      cpus: "1"
      memory: 512M
```

### Variables de entorno por entorno

Crear archivos separados:

```bash
.env                    # Local
.env.staging            # Staging
.env.production          # Producción

# Usar en comandos:
docker-compose --env-file .env.production up -d
```

## 🔄 Ciclo de Desarrollo

1. **Cambios en Spring Boot** → `docker-compose build backend` → `docker-compose restart backend`
2. **Cambios en BD** → Editar scripts en `../07.datos/scripts/` → `docker-compose restart postgres`
3. **Cambios en Docker** → `docker-compose down` → `docker-compose up -d`

---

**Documentación relacionada**: [03.BASE-DATOS.md](../04.docs/especificaciones/03.BASE-DATOS.md)

Última actualización: 2026-02-01
