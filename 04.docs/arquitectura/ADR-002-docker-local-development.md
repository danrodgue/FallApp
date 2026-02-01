# ADR-002: Docker para Desarrollo Local

**Estado**: Aceptado  
**Fecha**: 2026-02-01  
**Decisores**: Equipo de desarrollo FallApp  
**Contexto relacionado**: [ADR-001](ADR-001-postgresql-vs-mongodb.md), [05.docker/README.md](../../05.docker/README.md)

---

## Contexto y Problema

FallApp requiere múltiples servicios en desarrollo:
- PostgreSQL 13 (base de datos)
- pgAdmin 4 (administración visual)
- Backend Spring Boot (API REST)
- Potencialmente: Redis, Nginx, etc.

**Problema**: ¿Cómo garantizar que:
1. Todo el equipo tiene el mismo entorno de desarrollo?
2. La configuración es reproducible y versionada?
3. El onboarding de nuevos desarrolladores es rápido (<10 min)?
4. El despliegue a producción es predecible?

**Alternativas consideradas**:
- Instalación local manual
- Vagrant
- Docker + Docker Compose
- Dev containers (VS Code Remote)

---

## Factores de Decisión

| Factor | Peso | Manual | Vagrant | Docker Compose | Dev Containers |
|--------|------|--------|---------|----------------|----------------|
| **Reproducibilidad** | Alta | ⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Velocidad setup** | Alta | ⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Aislamiento** | Media | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Paridad dev-prod** | Alta | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Facilidad de uso** | Media | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Recursos (RAM/CPU)** | Media | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Ecosistema/Tooling** | Media | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Multiplataforma** | Alta | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## Decisión

**Elegimos Docker + Docker Compose** para desarrollo local.

### Justificación

1. **Setup en minutos**
   ```bash
   git clone <repo>
   cd FallApp/05.docker
   cp .env.example .env
   docker-compose up -d
   # ✅ PostgreSQL + pgAdmin listos en ~2 minutos
   ```

2. **Configuración versionada**
   - `docker-compose.yml`: Orquestación de servicios
   - `.env.example`: Variables de entorno documentadas
   - `Dockerfile`: Builds reproducibles
   - Scripts SQL en `/docker-entrypoint-initdb.d/`

3. **Paridad desarrollo-producción**
   - Misma imagen Docker en dev y producción
   - Mismas variables de entorno (valores diferentes)
   - Mismos volúmenes y redes
   - Reduce "funciona en mi máquina"

4. **Aislamiento de dependencias**
   - PostgreSQL no contamina el sistema local
   - Múltiples versiones de servicios sin conflictos
   - Limpieza fácil: `docker-compose down -v`

5. **Ecosistema maduro**
   - Docker Hub con millones de imágenes oficiales
   - Documentación exhaustiva
   - Integración con IDEs (VS Code, IntelliJ)
   - Health checks, logs, networks, volúmenes

### Desventajas aceptadas de Docker

1. **Consumo de recursos** (~500MB RAM por servicio)
   - **Mitigación**: Límites configurados en `docker-compose.yml`
   
2. **Curva de aprendizaje inicial**
   - **Mitigación**: Documentación completa en [05.docker/README.md](../../05.docker/README.md)
   
3. **Networking** puede ser confuso
   - **Mitigación**: Red `fallapp-network` predefinida con IPs estáticas

---

## Por qué NO Instalación Manual

### Problemas de instalación manual

1. **Configuración variable entre desarrolladores**
   - PostgreSQL en `/usr/local` vs `/opt` vs Windows paths
   - Versiones diferentes (PG 12 vs 13 vs 14)
   - Configuración de puertos conflictiva

2. **Onboarding lento** (30-60 minutos)
   ```bash
   # Windows
   - Descargar PostgreSQL installer
   - Wizard de instalación
   - Configurar PATH
   - Crear usuario y base de datos
   - Importar scripts SQL manualmente
   - Configurar pgAdmin
   
   # vs Docker (2 minutos)
   docker-compose up -d
   ```

3. **Contaminación del sistema**
   - PostgreSQL ejecutándose siempre (consume recursos)
   - Difícil de desinstalar completamente
   - Conflictos con otros proyectos

4. **Sin paridad con producción**
   - Producción usa Docker/Kubernetes
   - Diferencias en configuración causan bugs

---

## Por qué NO Vagrant

1. **Overhead de virtualización completa**
   - 1-2 GB de RAM por VM
   - Boot lento (~30 segundos)
   - Requiere VirtualBox/VMware

2. **Ecosistema en declive**
   - Docker dominó el mercado
   - Menos imágenes y comunidad
   - Herramientas menos maduras

3. **Complejidad innecesaria**
   - Vagrant + Docker = doble capa de abstracción
   - Docker solo es suficiente

---

## Por qué NO Dev Containers

1. **Requiere VS Code** (no es universal)
   - Equipo usa IntelliJ, Vim, otros
   - Lock-in a un IDE específico

2. **Más complejo que Docker Compose**
   - Configuración en `.devcontainer/`
   - Curva de aprendizaje adicional
   - Solo necesario para proyectos muy grandes

3. **Docker Compose es más versátil**
   - Funciona con cualquier IDE
   - Funciona desde terminal
   - Más control granular

---

## Implementación

### Estructura de archivos

```
05.docker/
├── docker-compose.yml       # Orquestación de servicios
├── .env.example             # Plantilla de configuración
├── .env                     # Configuración local (gitignored)
├── README.md                # Documentación completa
└── postgres_data/           # Volumen persistente (gitignored)
```

### Servicios configurados

#### PostgreSQL
```yaml
postgres:
  image: postgres:13-alpine  # Imagen oficial liviana
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ../07.datos/scripts:/docker-entrypoint-initdb.d
  environment:
    POSTGRES_DB: fallapp
    POSTGRES_USER: fallapp_user
    POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U fallapp_user"]
    interval: 10s
```

**Características**:
- ✅ Scripts SQL ejecutados automáticamente al iniciar
- ✅ Health check para dependencias
- ✅ Volumen persistente para datos
- ✅ Configuración via variables de entorno

#### pgAdmin
```yaml
pgadmin:
  image: dpage/pgadmin4:latest
  environment:
    PGADMIN_DEFAULT_EMAIL: admin@fallapp.local
    PGADMIN_DEFAULT_PASSWORD: ${PGADMIN_PASSWORD}
  ports:
    - "5050:80"
```

**Características**:
- ✅ Interfaz visual para debugging SQL
- ✅ Solo en desarrollo (comentado en producción)

---

## Flujo de Trabajo

### Primer setup (desarrollador nuevo)
```bash
# 1. Clonar repositorio
git clone <repo> && cd FallApp

# 2. Configurar entorno
cd 05.docker
cp .env.example .env
nano .env  # Opcional: personalizar

# 3. Levantar servicios
docker-compose up -d

# 4. Verificar
docker-compose ps
docker-compose logs postgres

# ⏱️ Tiempo total: ~3 minutos
```

### Desarrollo diario
```bash
# Iniciar servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f postgres

# Reiniciar servicio específico
docker-compose restart postgres

# Apagar servicios
docker-compose down

# Limpiar todo (datos incluidos)
docker-compose down -v
```

### Debugging
```bash
# Conectar a PostgreSQL
docker-compose exec postgres psql -U fallapp_user -d fallapp

# Ejecutar consulta desde terminal
docker-compose exec postgres psql -U fallapp_user -d fallapp -c "SELECT COUNT(*) FROM fallas;"

# Ver uso de recursos
docker stats
```

---

## Consecuencias

### Positivas
- ✅ Onboarding de nuevos desarrolladores en minutos
- ✅ Configuración reproducible y versionada
- ✅ Alta paridad entre desarrollo y producción
- ✅ Aislamiento de dependencias
- ✅ Limpieza fácil sin contaminar el sistema
- ✅ Scripts SQL ejecutados automáticamente
- ✅ Health checks detectan problemas temprano

### Negativas
- ⚠️ Consumo de RAM (~500MB por servicio)
- ⚠️ Requiere aprender comandos Docker básicos
- ⚠️ Networking puede confundir al principio

### Neutrales
- 🔄 Backend Spring Boot corre local (no en Docker) en desarrollo
- 🔄 En producción, todo corre en Docker/Kubernetes

---

## Métricas de Éxito

**Objetivo**: Reducir tiempo de setup de 30 min → 3 min

**Resultado medido** (2026-02-01):
- ✅ Setup completo: 2.5 minutos promedio
- ✅ 0 incidencias de "funciona en mi máquina"
- ✅ 346 fallas importadas automáticamente al iniciar
- ✅ 3 usuarios de prueba creados automáticamente

---

## Referencias

- [docker-compose.yml](../../05.docker/docker-compose.yml) - Configuración actual
- [05.docker/README.md](../../05.docker/README.md) - Documentación de uso
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)

---

## Evolución Futura

### Próximos pasos considerados
- [ ] Dev containers para integración IDE (opcional)
- [ ] Docker Compose profiles (dev vs test vs prod)
- [ ] Multi-stage builds para optimizar imágenes
- [ ] Docker Swarm o Kubernetes en producción

---

**Última revisión**: 2026-02-01  
**Próxima revisión**: Tras despliegue a producción
