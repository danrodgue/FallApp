# FallApp — Plataforma digital para las Fallas de Valencia

FallApp es una plataforma para la gestión, consulta y promoción de las Fallas de Valencia. El repositorio incluye el **backend (API)**, un cliente **desktop**, una app **móvil Android** y recursos para **base de datos**.

La **API está desplegada en AWS** (no es necesario levantarla en local para usar los clientes).

---

## Autores

- **Daniel Rodríguez Guerola** — GitHub: [@danrodgue](https://github.com/danrodgue)
- **Gautier Bastidas Joly** — GitHub: [@gautierbastidas](https://github.com/gautierbastidas)
- **Jose Burgos Martínez** — GitHub: [@Joseburgosmtnz](https://github.com/Joseburgosmtnz)



---

## Contenido del repositorio

- **API (backend)**: `01.backend/` (Spring Boot).
- **Desktop**: `02.desktop/` (Electron).
- **Móvil (Android)**: `03.mobile/` (Kotlin + Jetpack Compose).
- **Testing / utilidades**: `04.testing-dashboard/`, `06.tests/`.
- **Docker (DB)**: `05.docker/` (PostgreSQL + pgAdmin).
- **Datos y scripts SQL**: `07.datos/`.

---

## Funcionalidades principales

- Catálogo/consulta de fallas y monumentos falleros.
- Consulta de información asociada (ubicación, detalles, etc.).
- Gestión de usuarios y autenticación mediante JWT.
- Base para votaciones, comentarios y rankings (según el modelo de datos).
- Cliente desktop para consumo de la API.
- App móvil Android para consumo de la API.

---

## Tecnologías

### Backend (API)
- Java 17
- Spring Boot (REST)
- Spring Data JPA (Hibernate)
- Spring Security + JWT
- Validación (Bean Validation)
- OpenAPI/Swagger (springdoc)
- PostgreSQL
- Email (Spring Boot Mail)

### Infraestructura
- API desplegada en AWS
- Docker / Docker Compose (base de datos local y herramientas de desarrollo)

### Desktop
- Electron
- Node.js / npm
- electron-builder (distribución)
- Jest (tests)

### Móvil
- Android (Kotlin)
- Jetpack Compose + Material 3
- Koin (inyección de dependencias)
- Estructura por features (estilo Clean Architecture)

---

## Arquitectura (visión rápida)

- Los clientes (Desktop y Android) consumen la **API REST**.
- La API persiste información en **PostgreSQL**.
- Para desarrollo de datos, se puede levantar PostgreSQL en local con Docker.

---

## API (AWS)

La API está desplegada en AWS.

- Base URL: `http://35.180.21.42:8080`
- Base path: `/api`

Recomendación: centralizar cualquier cambio de URL en el cliente móvil:
- `03.mobile/.../core/config/ApiConfig.kt`

---

## Endpoints

Documentados en Swagger:

- Swagger UI: http://35.180.21.42:8080/swagger-ui/index.html

---

## Requisitos

### Para usar / desarrollar Desktop
- Node.js (LTS recomendado)
- npm

### Para usar / desarrollar Móvil
- Android Studio
- Android SDK (ver `03.mobile/README.SETUP.md`)

### Para trabajar con BD local (opcional)
- Docker
- Docker Compose

### Para desarrollar API (si aplica)
- Java 17
- Maven

---

## Quick start

### 1) Clonar el repositorio
```bash
git clone https://github.com/danrodgue/FallApp.git
cd FallApp
```

### 2) (Opcional) Levantar PostgreSQL + pgAdmin en local
Si necesitas una base de datos local para pruebas o para ejecutar scripts SQL:

```bash
cp 05.docker/.env.example .env
docker compose -f 05.docker/docker-compose.yml up -d postgres pgadmin
```

- PostgreSQL: `localhost:5432`
- pgAdmin: `http://localhost:5050`

---

## Desktop (Electron)

### Instalar dependencias y ejecutar
```bash
cd 02.desktop
npm install
npm run start
```

### Tests y build
```bash
npm run test
npm run dist
```

---

## Móvil (Android)

### Configuración inicial
- Ver `03.mobile/README.SETUP.md` (creación de `local.properties` por máquina).

### Ejecución
- Abrir `03.mobile/` con Android Studio y ejecutar el módulo `app`.

---

## Estructura de carpetas (ejemplos)

### API (Backend) — `01.backend/`
```text
01.backend/
├── pom.xml
├── Dockerfile
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── fallapp/
    │   │           ├── controllers/
    │   │           ├── services/
    │   │           ├── repositories/
    │   │           ├── entities/
    │   │           ├── dto/
    │   │           └── security/
    │   └── resources/
    │       ├── application.properties
    │       └── ...
    └── test/
        └── java/
            └── com/
                └── fallapp/
                    └── ...
```

### Desktop (Electron) — `02.desktop/`
```text
02.desktop/
├── package.json
├── package-lock.json
├── main/
│   └── index.js
├── js/
│   └── ...
├── build/
│   ├── icon.ico
│   └── ...
├── tools/
│   └── convert_icon.js
├── wdio.conf.js
└── ...
```

### Móvil (Android) — `03.mobile/`
```text
03.mobile/
├── README.SETUP.md
├── local.properties.example
└── app/
    └── src/
        └── main/
            └── java/
                └── com/
                    └─�� fallapp/
                        ├── core/
                        │   ├── config/
                        │   ├── di/
                        │   ├── ui/
                        │   │   └── theme/
                        │   └── util/
                        └── features/
                            └── auth/
                                ├── data/
                                ├── domain/
                                └── presentation/
```

---
## Entregables

El proyecto incluye un archivo ENTREGABLES.md, donde se encuentra la carpeta de Drive destinada a contener las versiones
compiladas del software para su distribución académica o evaluación.


## Convenciones y notas

- La URL de la API se gestiona de forma centralizada en el cliente móvil para facilitar cambios de entorno.
- Para desarrollo, la base de datos puede recrearse con scripts SQL en `07.datos/`.

---

## Licencia

Este proyecto está protegido por derechos de autor.
Todos los derechos están reservados, salvo los permisos expresamente concedidos en el archivo LICENSE
.

