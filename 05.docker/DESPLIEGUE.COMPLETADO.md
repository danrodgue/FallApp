# ✅ Despliegue de Base de Datos - COMPLETADO

**Fecha**: 2026-02-01  
**Estado**: ✅ OPERATIVO

## 🎯 Resumen

La infraestructura de base de datos PostgreSQL para FallApp ha sido desplegada exitosamente con todos los datos iniciales cargados.

## 📊 Estado de los Servicios

### Contenedores Docker Activos

| Servicio | Estado | Puerto |
|----------|--------|--------|
| **PostgreSQL 13** | ✅ Running | 5432 |
| **pgAdmin 4** | ✅ Running | 5050 |

### Estadísticas de Datos

| Tabla | Registros |
|-------|-----------|
| **usuarios** | 3 |
| **fallas** | 346 |
| **eventos** | 0 |
| **ninots** | 0 |
| **votos** | 0 |
| **comentarios** | 0 |

## 🔑 Credenciales de Acceso

### PostgreSQL

```
Host: localhost
Puerto: 5432
Base de datos: fallapp
Usuario: fallapp_user
Contraseña: fallapp_secure_password_2026
```

**Cadena de conexión JDBC:**
```
jdbc:postgresql://localhost:5432/fallapp
```

### pgAdmin (Interfaz Web)

```
URL: http://localhost:5050
Email: admin@fallapp.local
Contraseña: admin1234
```

### Usuarios de la Aplicación

| Email | Contraseña | Rol |
|-------|------------|-----|
| admin@fallapp.es | admin123 | admin |
| demo@fallapp.es | demo123 | usuario |
| casal@fallapp.es | casal123 | casal |

> ⚠️ **Importante**: Las contraseñas están hasheadas con bcrypt en la base de datos. Las mostradas aquí son las originales antes de hashear.

## 🗄️ Estructura de la Base de Datos

### Tablas Principales

1. **usuarios** - Gestión de usuarios y autenticación
2. **fallas** - Información de las 346 fallas valencianas
3. **eventos** - Eventos y actividades
4. **ninots** - Registro de ninots
5. **votos** - Sistema de votación
6. **comentarios** - Comentarios de usuarios

### Características Técnicas

- ✅ Extensiones habilitadas: `uuid-ossp`, `unaccent`
- ✅ Índices de performance configurados
- ✅ Triggers de auditoría automática
- ✅ Full-text search en español
- ✅ 9 vistas especializadas
- ✅ 2 funciones SQL reutilizables

## 🚀 Comandos Útiles

### Gestión de Contenedores

```bash
# Ver estado de servicios
sudo docker-compose ps

# Ver logs de PostgreSQL
sudo docker logs fallapp-postgres

# Ver logs de pgAdmin
sudo docker logs fallapp-pgadmin

# Detener servicios
sudo docker-compose down

# Iniciar servicios
sudo docker-compose up -d

# Reiniciar servicios
sudo docker-compose restart
```

### Acceso a PostgreSQL

```bash
# Conectar con psql
sudo docker exec -it fallapp-postgres psql -U fallapp_user -d fallapp

# Ejecutar consulta desde línea de comandos
sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "SELECT COUNT(*) FROM fallas;"

# Backup de la base de datos
sudo docker exec fallapp-postgres pg_dump -U fallapp_user fallapp > backup_fallapp_$(date +%Y%m%d).sql

# Restaurar backup
cat backup_fallapp_20260201.sql | sudo docker exec -i fallapp-postgres psql -U fallapp_user -d fallapp
```

## 📁 Archivos de Configuración

```
05.docker/
├── docker-compose.yml      # Configuración de servicios
├── .env                     # Variables de entorno
├── .env.example            # Plantilla de configuración
└── postgres_data/          # Datos persistentes de PostgreSQL
```

## 🔍 Consultas de Verificación

```sql
-- Listar todas las tablas
\dt

-- Ver estructura de una tabla
\d fallas

-- Estadísticas generales
SELECT 
  (SELECT COUNT(*) FROM usuarios) as usuarios,
  (SELECT COUNT(*) FROM fallas) as fallas;

-- Buscar fallas por nombre
SELECT id_falla, nombre, seccion, categoria 
FROM fallas 
WHERE nombre ILIKE '%valencia%' 
LIMIT 10;

-- Ver usuarios activos
SELECT id_usuario, nombre_completo, email, rol 
FROM usuarios 
WHERE activo = true;
```

## 🛠️ Solución de Problemas

### Error: Permission denied (Docker)

Si obtienes errores de permisos al ejecutar docker-compose:

```bash
# Usar sudo temporalmente
sudo docker-compose up -d

# O agregar usuario al grupo docker (requiere logout/login)
sudo usermod -aG docker $USER
newgrp docker
```

### Error: Puerto en uso

Si el puerto 5432 o 5050 ya está en uso:

```bash
# Ver qué está usando el puerto
sudo lsof -i :5432
sudo lsof -i :5050

# Cambiar puerto en .env
nano .env
# Modificar POSTGRES_PORT o PGADMIN_PORT
```

### Reiniciar base de datos desde cero

```bash
# ⚠️ Esto elimina todos los datos
cd /srv/FallApp/05.docker
sudo docker-compose down -v
sudo rm -rf postgres_data/*
sudo docker-compose up -d postgres
```

## 📋 Próximos Pasos

1. ✅ Base de datos PostgreSQL desplegada
2. ✅ Datos iniciales cargados (346 fallas)
3. ✅ Usuarios de prueba creados
4. ⏳ Compilar y desplegar backend Spring Boot
5. ⏳ Conectar aplicación desktop Electron
6. ⏳ Conectar aplicación móvil Android

## 📚 Documentación Relacionada

- [README Docker](./README.md)
- [Scripts SQL](../07.datos/scripts/README.md)
- [Especificación Base de Datos](../04.docs/especificaciones/03.BASE-DATOS.md)
- [Configuración Spring Boot](../07.datos/APPLICATION.PROPERTIES.REFERENCIA.md)

---

**✅ Despliegue verificado y operativo**  
*Todos los servicios funcionando correctamente*
