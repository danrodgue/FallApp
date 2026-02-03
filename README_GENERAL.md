## 🎭 FallApp – Guía General del Proyecto

Este documento resume, de forma sencilla y en un único sitio, todo lo importante que aparece repartido por los distintos `.md` del repositorio.  
Está pensado para que **cualquier persona (incluso principiante)** pueda entender **qué es FallApp, cómo está montado y cómo se usa** sin tener que leerlo todo.

---

## 1. ¿Qué es FallApp?

- **Objetivo**: plataforma digital para las Fallas de Valencia.
- **Qué permite**:
  - **Ver fallas en un mapa** y consultar su información.
  - **Ver y gestionar eventos falleros** (plantà, cremà, conciertos, etc.).
  - **Ver y votar ninots**.
  - **Comentar** sobre fallas y ninots.
  - **Gestionar usuarios y roles** (admin, casales, usuarios normales).
  - **Obtener estadísticas** (número de fallas, votos, usuarios…).
- **Contexto**: proyecto intermodular del ciclo DAM, con una duración de 4 semanas, donde se trabaja:
  - Backend y base de datos (ADA).
  - App Android (PMDP).
  - App escritorio con Electron (DI).
  - Parte de negocio y memoria (IPE2).

---

## 2. Arquitectura general (cómo se organiza el sistema)

Piensa en tres bloques principales:

- **Backend (`01.backend`)**
  - API REST hecha con **Spring Boot 3** y **Java 17**.
  - Expone endpoints como `/api/fallas`, `/api/eventos`, `/api/ninots`, `/api/votos`, `/api/usuarios`, `/api/auth/...`.
  - Usa **Spring Security + JWT** para autenticación.
  - Habla con la base de datos **PostgreSQL**.

- **Base de datos (`07.datos` + `04.docs/especificaciones/03.BASE-DATOS.md`)**
  - Motor: **PostgreSQL 13+** dentro de Docker.
  - Tablas principales:
    - `usuarios`: credenciales, rol (`ADMIN`, `CASAL`, `USUARIO`), datos básicos.
    - `fallas`: datos de cada falla/casal (nombre, sección, presidente, coordenadas, descripción…).
    - `eventos`: actos de cada falla (tipo, fecha, descripción…).
    - `ninots`: figuras asociadas a fallas.
    - `votos`: votos que los usuarios hacen sobre ninots/fallas.
    - `comentarios`: comentarios de usuarios.
  - Además:
    - **ENUMs** (tipos de evento, tipos de voto, categorías de falla, roles…).
    - **9 vistas** SQL para estadísticas y rankings.
    - **2 funciones SQL** reutilizables (búsqueda, rankings).
    - **Triggers** para actualizar timestamps automáticamente.

- **Aplicaciones cliente**
  - **Escritorio (`02.desktop`) – Electron + JS/HTML/CSS:**
    - Muestra vistas de fallas, eventos, ninots, etc.
    - Se comunica con la API por HTTP (`fetch`).
    - Guarda el token JWT (por ejemplo en `localStorage`) para peticiones autenticadas.
  - **Móvil (`03.mobile`) – Android + Kotlin:**
    - Usa Retrofit para consumir la API.
    - Muestra mapa con fallas, detalle de ninots, votación, etc.
    - Puede cachear datos en **Room** para trabajar offline.

Todo esto suele desplegarse (en desarrollo) con **Docker Compose** desde `05.docker`.

---

## 3. Roles de usuario y permisos

Hay 3 roles principales (definidos en la visión general y en las especificaciones):

- **ADMIN**
  - Control total: crear/editar/borrar fallas, eventos, ninots, usuarios, etc.
  - Ver estadísticas avanzadas.
- **CASAL**
  - Responsable de **su propia falla**.
  - Puede crear/editar eventos y ninots de su falla.
  - Puede actualizar los datos de su falla (con ciertas limitaciones).
- **USUARIO** (público general)
  - Puede ver fallas, ninots, eventos, estadísticas.
  - Puede **registrarse, iniciar sesión, votar ninots y comentar**.

La lógica de permisos está tanto en las **specs** (`00.VISION-GENERAL.md`, `01.SISTEMA-USUARIOS.md`, `02.FALLAS.md`, etc.) como en la API (`04.API-REST.md`) y en la guía de frontend.

---

## 4. API REST: cómo se usa (a nivel muy básico)

### 4.1 URL base

- **Local**: `http://localhost:8080`
- **Servidor AWS (desarrollo)**: `http://35.180.21.42:8080`

Todas las rutas empiezan por `/api/...`, por ejemplo:
- `GET /api/fallas`
- `POST /api/auth/login`
- `POST /api/votos`

### 4.2 Autenticación con JWT

1. **Registro** (`POST /api/auth/registro`) → crea usuario y devuelve token.
2. **Login** (`POST /api/auth/login`) → comprueba email/contraseña y devuelve token.
3. **Uso del token**:
   - En cada petición autenticada añades un header:
     - `Authorization: Bearer TU_TOKEN_AQUI`
4. **Duración**: el token dura 24 horas; después hay que volver a hacer login.

### 4.3 Tipos de endpoints

- **Públicos (sin token)**:
  - Ver fallas, eventos, ninots, estadísticas, comentarios.
  - Hacer login y registro.
- **Autenticados (token obligatorio)**:
  - Crear/editar fallas (según rol).
  - Crear/editar eventos, ninots, comentarios.
  - Votar ninots.
  - Ver/editar tu perfil, ver tus votos, etc.
- **Solo ADMIN**:
  - Eliminar fallas, eventos, ninots, comentarios, usuarios.

### 4.4 Formato genérico de respuesta

La API siempre envía algo parecido a:

```json
{
  "exito": true,
  "mensaje": "Operación exitosa",
  "datos": { ... },
  "timestamp": "2026-02-01T18:30:00"
}
```

- `exito: true/false` indica si todo ha ido bien.
- `mensaje` explica qué ha pasado.
- `datos` contiene la información que te interesa (un objeto, lista, etc.).

Si hay error (400, 401, 404, 409, etc.), `exito` será `false` y `mensaje` te dice el motivo.

---

## 5. Dominio funcional (qué entidades hay y qué hacen)

### 5.1 Usuarios

- Datos: email, contraseña (hasheada), nombre, apellidos, rol, activo, fecha de registro, última sesión…
- Relaciones:
  - Puede estar asociado a una falla (casal).
  - Tiene muchos votos y comentarios.
- Endpoints clave:
  - `/api/auth/registro`, `/api/auth/login`
  - `/api/usuarios/perfil` (ver y editar perfil propio).
  - `/api/usuarios` (solo ADMIN, gestión global).

### 5.2 Fallas (casales)

- Datos: nombre, sección, presidente, artista, lema, año de fundación, categoría, coordenadas, descripción, contactos…
- Relaciones:
  - Tiene muchos eventos, ninots, votos, comentarios.
  - Tiene uno o varios usuarios responsables (según diseño).
- Ejemplos de operaciones:
  - `GET /api/fallas` → listar fallas (con paginación y filtros).
  - `GET /api/fallas/{id}` → detalle de una falla.
  - `GET /api/fallas/cercanas` → fallas cerca de unas coordenadas.
  - `POST /api/fallas` → crear falla (ADMIN / CASAL).

### 5.3 Eventos

- Representan actos como plantà, cremà, ofrenda, conciertos, etc.
- Datos: tipo, nombre, descripción, fecha/hora, ubicación, estimación de participantes, falla asociada…
- Operaciones típicas:
  - `GET /api/eventos/futuros`, `GET /api/eventos/proximos`.
  - `GET /api/eventos/falla/{idFalla}` → eventos de una falla.
  - `POST /api/eventos`, `PUT /api/eventos/{id}`, `DELETE /api/eventos/{id}` (según rol).

### 5.4 Ninots

- Figuras artísticas asociadas a una falla.
- Datos: nombre, título de la obra, dimensiones, imágenes, artista, premiado o no, etc.
- Operaciones:
  - `GET /api/ninots`, `GET /api/ninots/{id}`, `GET /api/ninots/falla/{idFalla}`.
  - `POST /api/ninots`, `PUT /api/ninots/{id}`, `DELETE /api/ninots/{id}` (según rol).

### 5.5 Votos

- Un usuario puede votar un ninot (una vez por tipo de voto).
- Tipos: por ejemplo `INGENIOSO`, `CRITICO`, `ARTISTICO` (según guía de API frontend).
- Operaciones:
  - `POST /api/votos` → crear voto (requiere token).
  - `GET /api/votos/usuario/{idUsuario}` → votos de un usuario.
  - `GET /api/votos/ninot/{idNinot}` → votos de un ninot.
  - `DELETE /api/votos/{idVoto}` → eliminar voto propio.

### 5.6 Comentarios

- Comentarios de usuarios sobre fallas o ninots.
- Solo se puede comentar **una cosa a la vez** (o falla o ninot).
- Operaciones:
  - `GET /api/comentarios` (filtrado por `idFalla` o `idNinot`).
  - `POST /api/comentarios` → crear comentario (con token).
  - `PUT /api/comentarios/{id}` → editar tu comentario.
  - `DELETE /api/comentarios/{id}` → borrar (tú o ADMIN).

### 5.7 Estadísticas

- Resúmenes globales: número de fallas, eventos, ninots, usuarios, votos, etc.
- Rutas típicas:
  - `GET /api/estadisticas/resumen`
  - `GET /api/estadisticas/fallas`
  - `GET /api/estadisticas/votos`
  - `GET /api/estadisticas/actividad-reciente`

---

## 6. Cómo arrancar el proyecto en local (resumen)

Para desarrollo rápido lo normal es usar **Docker**.

1. **Requisitos previos**:
   - Docker y Docker Compose instalados.
   - Git.
   - (Opcional) Java 17 y Maven si vas a arrancar backend fuera de Docker.

2. **Clonar repositorio**:
   ```bash
   git clone https://github.com/danrodgue/FallApp.git
   cd FallApp
   ```

3. **Configurar `.env` para Docker**:
   ```bash
   cd 05.docker
   cp .env.example .env
   # Edita credenciales si quieres (usuario BD, contraseñas…)
   ```

4. **Levantar servicios básicos (BD + backend + pgAdmin)**:
   ```bash
   docker-compose up -d
   ```

5. **Comprobar que todo responde**:
   - API: `http://localhost:8080/api/estadisticas/resumen`
   - Swagger: `http://localhost:8080/swagger-ui.html`
   - pgAdmin: `http://localhost:5050`

6. **Credenciales típicas de desarrollo** (pueden variar según README principal):
   - Admin API: algo como `admin@fallapp.es` / `Admin2026!` (ver documentos de credenciales).

---

## 7. Buenas prácticas de desarrollo en FallApp (resumen)

Tomado de las guías de programación y de IA:

- **Escribir código simple y claro** (principio KISS).
- **Evitar duplicar lógica** (principio DRY).
- **Fail-fast**:
  - Mejor lanzar un error claro que poner un valor por defecto “mágico”.
  - Ejemplo: si falta una URL en configuración, lanzar excepción.
- **Seguir las convenciones**:
  - Java/Kotlin/JS con `camelCase` para métodos/variables y `PascalCase` para clases.
  - Constantes en `MAYUSCULA_CON_GUIONES_BAJO`.
  - Estructura de paquetes y carpetas tal y como está descrito en `LEEME.DESARROLLADORES.md`.
- **Antes de tocar código**:
  - Leer la **especificación** correspondiente en `04.docs/especificaciones`.
  - Revisar `01.GUIA-PROGRAMACION.md` para respetar el estilo.

---

## 8. Cómo usan la API las apps Desktop y Mobile

### 8.1 Desktop (Electron, JS)

Patrón básico:

1. Configuras la URL base:
   ```javascript
   const API_BASE_URL = 'http://35.180.21.42:8080';
   ```
2. Al registrarse o hacer login:
   - Llamas a `/api/auth/registro` o `/api/auth/login`.
   - Guardas `datos.token` en `localStorage`.
3. Para peticiones autenticadas:
   ```javascript
   fetch(`${API_BASE_URL}/api/votos`, {
     method: 'POST',
     headers: {
       'Content-Type': 'application/json',
       'Authorization': `Bearer ${token}`
     },
     body: JSON.stringify({ idNinot, tipoVoto: 'ARTISTICO' })
   })
   ```
4. Para datos públicos (fallas, estadísticas, etc.) simplemente haces `GET` sin token.

### 8.2 Android (Kotlin)

Patrón básico con Retrofit:

1. Definir `data class` que coincidan con las respuestas de la API.
2. Definir una interfaz Retrofit (`FallAppApi`) con métodos como:
   - `@POST("/api/auth/login") suspend fun login(...)`
   - `@GET("/api/fallas") suspend fun obtenerFallas(...)`
3. Crear un `RetrofitClient` con:
   ```kotlin
   private const val BASE_URL = "http://35.180.21.42:8080"
   ```
4. En un `Repository`, guardar el token cuando haces login y añadir:
   ```kotlin
   @Header("Authorization") token: String
   ```
   en los métodos que lo necesitan.
5. El `ViewModel` llama al repository y expone `LiveData` para que la UI se actualice.

---

## 9. Errores típicos y cómo interpretarlos

Algunos códigos de error comunes:

- **400 Bad Request**: has enviado datos inválidos (faltan campos, formato incorrecto…).
- **401 Unauthorized**: no has puesto token o es inválido/ha caducado.
- **403 Forbidden**: tienes token, pero tu rol no tiene permiso (por ejemplo, no eres ADMIN).
- **404 Not Found**: el recurso no existe (ID incorrecto).
- **409 Conflict**: conflicto lógico (email duplicado, voto ya realizado, etc.).
- **500 Internal Server Error**: algo ha fallado en el servidor (revisar logs).

Siempre revisa el campo **`mensaje`** de la respuesta JSON; ahí la API explica qué ha pasado.

---

## 10. Dónde leer más (si necesitas detalle)

Si quieres profundizar en algún tema concreto:

- **Visión global del sistema**: `04.docs/especificaciones/00.VISION-GENERAL.md`
- **Usuarios y autenticación**: `04.docs/especificaciones/01.SISTEMA-USUARIOS.md`
- **Fallas/casales**: `04.docs/especificaciones/02.FALLAS.md`
- **Base de datos completa**: `04.docs/especificaciones/03.BASE-DATOS.md`
- **API REST detallada (todas las rutas)**: `04.docs/especificaciones/04.API-REST.md` y `GUIA.API.FRONTEND.md`
- **Guía de programación y convenciones**: `04.docs/01.GUIA-PROGRAMACION.md` y `04.docs/03.CONVENCIONES-IDIOMA.md`
- **Docker y despliegue BD**: `05.docker/README.md`, `CHECKLIST.DESPLIEGUE.BD.md`, `AUDITORIA.DESPLIEGUE.BD.md`
- **Acceso externo a la API (AWS)**: `ACCESO.EXTERNO.md`

Con este `README_GENERAL.md` deberías poder hacerte una idea clara de **qué hace FallApp**, **cómo está montado** y **cómo hablar con su API**, sin necesidad de leer toda la documentación desde el principio.

