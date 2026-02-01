# 🎭 FallApp - Plataforma de Fallas Falleras de Valencia

> Plataforma digital para gestión, votación y promoción de las Fallas de Valencia

![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow)
![Java](https://img.shields.io/badge/Java-17%2B-red)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

## 📖 Descripción

FallApp es una plataforma integral para la gestión digital de las Fallas de Valencia, permitiendo:

- 🏛️ Catálogo de fallas y monumentos falleros
- 🎨 Galería de ninots (figuras) con detalle técnico
- 🗳️ Sistema de votación y ranking
- 💬 Comunidad de comentarios y opiniones
- 📅 Calendario de eventos falleros
- 🔐 Gestión de usuarios con roles
- 📊 Estadísticas y análisis

## 🚀 Stack Tecnológico

### Backend
- **Framework**: Spring Boot 3.x
- **Lenguaje**: Java 17+
- **ORM**: Hibernate/JPA
- **Base de Datos**: PostgreSQL 13
- **API**: REST + OpenAPI/Swagger
- **Autenticación**: JWT

### Infraestructura
- **Contenedorización**: Docker + Docker Compose
- **Base de Datos**: PostgreSQL con volúmenes persistentes
- **Gestión DB**: pgAdmin para desarrollo
- **Control de Versiones**: Git + GitHub

### Frontend (Próximamente)
- **Framework**: React 18 / Angular 16+
- **UI**: TailwindCSS / Material Design
- **Despliegue**: Vercel / Netlify

## 📁 Estructura del Proyecto

```
FallApp/
├── 01.backend/                 # Spring Boot API
│   ├── src/main/java/         # Código fuente
│   ├── src/main/resources/    # Configuración
│   ├── pom.xml                 # Dependencias Maven
│   └── Dockerfile              # Imagen Docker del backend
│
├── 02.frontend/                # Aplicación React/Angular (próximamente)
│   ├── src/                    # Código fuente
│   ├── package.json            # Dependencias npm
│   └── Dockerfile              # Imagen Docker del frontend
│
├── 03.mobile/                  # Aplicación móvil (próximamente)
│   ├── ios/                    # Código iOS
│   └── android/                # Código Android
│
├── 04.docs/                    # Documentación del proyecto
│   ├── especificaciones/       # Documentación técnica
│   │   └── 03.BASE.DATOS.md   # Especificación de BD
│   ├── NOMENCLATURA.FICHEROS.md # Convenciones de nombres
│   └── README.md               # Índice de docs
│
├── 05.docker/                  # Configuración Docker
│   ├── docker-compose.yml      # Orquestación de servicios
│   ├── .env.example            # Plantilla de variables
│   ├── Dockerfile.backend      # Imagen custom backend
│   └── README.md               # Guía Docker
│
├── 06.tests/                   # Pruebas automatizadas
│   ├── unit/                   # Tests unitarios
│   ├── integration/            # Tests de integración
│   ├── e2e/                    # Tests end-to-end
│   └── performance/            # Tests de carga
│
├── 07.datos/                   # Gestión de datos
│   ├── raw/                    # Datos brutos (JSON, CSV)
│   ├── transformado/           # Datos procesados
│   ├── scripts/                # Scripts SQL (NN.tipo.sql)
│   │   ├── 01.schema.sql      # Creación de tablas
│   │   ├── 10.seed.usuarios.sql # Datos iniciales
│   │   ├── 20.import.fallas.json # Importación de fallas
│   │   └── 30.vistas.consultas.sql # Vistas y funciones
│   ├── migracion/              # Scripts de migración
│   ├── PROXIMOS.PASOS.md       # Hoja de ruta
│   ├── APPLICATION.PROPERTIES.REFERENCIA.md
│   └── README.md               # Guía de datos
│
├── 99.obsoleto/                # Código/docs deprecated
│   └── [archivos viejos]
│
├── docker-compose.yml          # (Ver 05.docker/)
├── .env.example                # (Ver 05.docker/)
├── .gitignore                  # Archivos ignorados
├── README.md                   # Este archivo
└── CONTRIBUTING.md             # Guía de contribución

```

## ⚡ Quick Start

### Requisitos Previos
- Docker 20.10+
- Docker Compose 1.29+
- Git
- Java 17+ (para desarrollo local)
- Maven 3.8+ (para desarrollo local)

### 1️⃣ Clonar Repositorio
```bash
git clone https://github.com/danrodgue/FallApp.git
cd FallApp
```

### 2️⃣ Configurar Variables de Entorno
```bash
cp 05.docker/.env.example .env
# Editar .env con credenciales propias
```

### 3️⃣ Levantar Infraestructura
```bash
docker-compose up -d postgres pgAdmin backend
```

### 4️⃣ Verificar Servicios
```bash
# Logs en vivo
docker-compose logs -f backend

# Health check
curl http://localhost:8080/api/actuator/health
```

### 5️⃣ Acceder a las Aplicaciones
- **API**: http://localhost:8080/api
- **Swagger API Docs**: http://localhost:8080/api/swagger-ui.html
- **pgAdmin (DB)**: http://localhost:5050

## 🔐 Credenciales por Defecto

⚠️ **SOLO PARA DESARROLLO** - Cambiar inmediatamente en producción

| Servicio | Usuario | Contraseña | URL |
|----------|---------|-----------|-----|
| API Admin | admin@fallapp.es | Admin@2024 | localhost:8080/api |
| pgAdmin | admin@pgadmin.com | pgadmin | localhost:5050 |
| PostgreSQL | fallapp_user | fallapp_password | localhost:5432 |
| API Demo | demo@fallapp.es | Demo@2024 | localhost:8080/api |

## 📚 Documentación

### Para Desarrolladores
- [Guía de Configuración Backend](04.docs/README.md)
- [Especificación de Base de Datos](04.docs/especificaciones/03.BASE.DATOS.md)
- [Scripts SQL](07.datos/scripts/README.md)
- [Docker & Compose](05.docker/README.md)
- [Próximos Pasos](07.datos/PROXIMOS.PASOS.md)

### Convenciones
- [Nomenclatura de Ficheros](04.docs/NOMENCLATURA.FICHEROS.md) - Convenciones de nombres
- [Guía de Commits](CONTRIBUTING.md) - Formato de commits git

### API
- [OpenAPI/Swagger](http://localhost:8080/api/swagger-ui.html) - Documentación interactiva

## 🗂️ Módulos Principales

### Backend (01.backend/)
Aplicación Spring Boot con:
- **Controllers**: REST APIs (`/api/fallas`, `/api/usuarios`, etc.)
- **Services**: Lógica de negocio
- **Repositories**: Acceso a datos (JPA)
- **Entities**: Modelos de dominio
- **DTOs**: Objetos de transferencia

### Base de Datos (PostgreSQL)
- **6 tablas principales**: usuarios, fallas, eventos, ninots, votos, comentarios
- **Tipos ENUM**: rol_usuario, tipo_evento, tipo_voto, categoria_falla
- **Vistas especializadas**: rankings, búsqueda full-text, estadísticas
- **Índices optimizados**: FTS, UNIQUE, Foreign Keys

### Docker
- **Servicios**: PostgreSQL, Backend, pgAdmin
- **Redes**: Bridge personalizado
- **Volúmenes**: Persistencia de datos
- **Health checks**: Monitoreo de servicios

## 🔄 Flujo de Desarrollo (SCRUM)

**Sprint de 15 días con equipo de 3 personas**

### Semana 1: Infraestructura
- [x] PostgreSQL + Docker Compose
- [x] Scripts SQL (schema, seeds, import)
- [ ] Integración con backend
- [ ] Tests de BD

### Semana 2: Backend
- [ ] Entidades JPA
- [ ] Controllers REST
- [ ] Services y DTOs
- [ ] Tests unitarios

### Semana 3: Frontend (Próximo Sprint)
- [ ] Proyecto React/Angular
- [ ] Componentes UI
- [ ] Integración con API
- [ ] Tests E2E

## 🧪 Testing

```bash
# Tests unitarios
mvn test

# Tests de integración
mvn verify

# Coverage
mvn clean test jacoco:report

# Performance
docker-compose up -d & \
  ab -n 1000 -c 10 http://localhost:8080/api/fallas
```

## 📊 Estadísticas del Proyecto

| Métrica | Valor |
|---------|-------|
| Tablas BD | 6 |
| Vistas SQL | 9 |
| Funciones SQL | 2 |
| Líneas SQL | ~850 |
| Endpoints API | 20+ (desarrollo) |
| Cobertura Testing | (por configurar) |

## 🚀 Próximas Fases

### ✅ Completado
- [x] Planificación y arquitectura
- [x] Especificación de BD
- [x] Docker Compose + PostgreSQL
- [x] Scripts SQL

### 🔄 En Progreso
- [ ] Integración backend (Semana 1)
- [ ] APIs REST (Semana 2)
- [ ] Testing automatizado (Semana 2-3)

### ⏳ Por Iniciar
- [ ] Interfaz web (Frontend)
- [ ] Aplicación móvil
- [ ] CI/CD (GitHub Actions)
- [ ] Despliegue en producción

Ver [Próximos Pasos Detallados](07.datos/PROXIMOS.PASOS.md)

## 🤝 Contribuir

Este proyecto está en fase de desarrollo inicial.

### Equipo Actual
- **Daniel Rodríguez** (Lead)
- [Team Members](CONTRIBUTING.md)

### Cómo Contribuir
1. Crear rama desde `main`: `git checkout -b feature/mi-caracteristica`
2. Hacer cambios y commits siguiendo [convenciones](CONTRIBUTING.md)
3. Push a rama: `git push origin feature/mi-caracteristica`
4. Crear Pull Request con descripción detallada

### Reportar Issues
Ver [Issues del Proyecto](https://github.com/danrodgue/FallApp/issues)

## 📝 Licencia

Este proyecto es propietario (Privado).

Para uso comercial o distribución, contactar con el propietario.

## 📞 Contacto

- **GitHub**: [@danrodgue](https://github.com/danrodgue)
- **Email**: [Tu Email]
- **Issues**: [GitHub Issues](https://github.com/danrodgue/FallApp/issues)

## 🎯 Visión del Proyecto

> *Ser la plataforma digital líder en la comunidad fallera valenciana, conectando entusiastas, artistas y público general alrededor del patrimonio cultural de las Fallas.*

### Objetivos
1. Centralizar información de fallas 📍
2. Facilitar participación e interacción 👥
3. Preservar historia y tradición 📚
4. Modernizar experiencia digital 💻

## 📅 Hitos Planificados

| Fecha | Hito | Estado |
|-------|------|--------|
| 2024-02-15 | API Backend Completa | 🔄 En progreso |
| 2024-03-01 | Frontend React | ⏳ Por iniciar |
| 2024-03-15 | Mobile App | ⏳ Por iniciar |
| 2024-04-01 | Beta Release | ⏳ Por iniciar |
| 2024-05-15 | Production Launch | ⏳ Por iniciar |

## 📖 Recursos Útiles

### Documentación Externa
- [Spring Boot](https://spring.io/projects/spring-boot)
- [PostgreSQL](https://www.postgresql.org/docs/)
- [Docker](https://docs.docker.com/)
- [Git](https://git-scm.com/doc)

### Comunidad
- [Spring Community](https://spring.io/community)
- [PostgreSQL Discuss](https://www.postgresql.org/community/)

---

**Última actualización**: 2024-02-01  
**Versión**: 0.1.0-SNAPSHOT  
**Rama**: main

---

<div align="center">

### ⭐ Si te gusta el proyecto, déjanos una estrella en GitHub ⭐

</div>
