# API REST FallApp (nivel principiante)

Este documento explica cómo funciona la **API REST** del backend de FallApp, pensada para principiantes que están empezando con **Spring Boot** y **MongoDB Atlas**.

---

## 1. Arrancar la aplicación

1. Asegúrate de tener **Java 17** y **Maven** instalados.
2. Configura la conexión a MongoDB Atlas en `src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb+srv://<usuario>:<password>@<cluster>.mongodb.net/fallapp?appName=<NOMBRE_CLUSTER>
spring.data.mongodb.database=fallapp
```

3. Desde la carpeta `Datos/Fallapp`, ejecuta:

```bash
mvn spring-boot:run
```

La aplicación se levantará por defecto en `http://localhost:8080`.

---

## 2. Estructura general de la API

Hay **dos recursos principales** (dos tipos de datos):

- **Artistas** → `/api/artistas`
- **Fallas** → `/api/fallas`

Cada recurso tiene un **controlador REST** que expone endpoints con los verbos HTTP típicos:

- `GET` → leer datos
- `POST` → crear nuevos registros
- `PUT` → actualizar registros existentes
- `DELETE` → borrar registros

---

## 3. Entidad `Artista` y endpoints

### 3.1. Qué es un Artista

La entidad `Artista` representa al artista fallero que diseña/construye una falla.

Campos principales (simplificado):

- `id` (String): identificador en MongoDB (se genera automáticamente).
- `nombre` (String)
- `especialidad` (String) – por ejemplo: `"Falla grande"`, `"Falla infantil"`.
- `nacionalidad` (String)
- `descripcion` (String)

### 3.2. Endpoints de `ArtistaController`

Base: `http://localhost:8080/api/artistas`

#### 1) Listar todos los artistas

- **Método**: `GET`
- **URL**: `/api/artistas`
- **Descripción**: Devuelve una lista con todos los artistas.

Ejemplo con `curl`:

```bash
curl http://localhost:8080/api/artistas
```

#### 2) Obtener un artista por id

- **Método**: `GET`
- **URL**: `/api/artistas/{id}`

Ejemplo:

```bash
curl http://localhost:8080/api/artistas/64f123abc...
```

#### 3) Crear un nuevo artista

- **Método**: `POST`
- **URL**: `/api/artistas`
- **Body (JSON)**, por ejemplo:

```json
{
  "nombre": "Juan Pérez",
  "especialidad": "Falla grande",
  "nacionalidad": "Española",
  "descripcion": "Artista con amplia experiencia en sección especial"
}
```

Ejemplo con `curl`:

```bash
curl -X POST http://localhost:8080/api/artistas \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"Juan Pérez\",\"especialidad\":\"Falla grande\",\"nacionalidad\":\"Española\",\"descripcion\":\"Artista con experiencia\"}"
```

#### 4) Actualizar un artista existente

- **Método**: `PUT`
- **URL**: `/api/artistas/{id}`
- **Body (JSON)**: igual estructura que en el `POST`, pero se sobreescribe el artista indicado por `id`.

#### 5) Borrar un artista

- **Método**: `DELETE`
- **URL**: `/api/artistas/{id}`

#### 6) Filtrar artistas por especialidad

- **Método**: `GET`
- **URL**: `/api/artistas/especialidad/{especialidad}`

Ejemplo:

```bash
curl http://localhost:8080/api/artistas/especialidad/Falla%20grande
```

#### 7) Obtener todas las fallas de un artista

- **Método**: `GET`
- **URL**: `/api/artistas/{id}/fallas`
- **Descripción**: Devuelve todas las fallas cuyo campo `artistaId` coincide con el `id` del artista.

Ejemplo:

```bash
curl http://localhost:8080/api/artistas/64f123abc.../fallas
```

---

## 4. Entidad `Falla` y endpoints

### 4.1. Qué es una Falla

La entidad `Falla` representa una falla concreta de Valencia.

Campos principales (resumen):

- `id` (String): id de MongoDB.
- `objectid` (Long)
- `idFalla` (Long): identificador de la falla (del dataset original).
- `nombre` (String)
- `seccion` (String)
- `fallera` (String)
- `presidente` (String)
- `artistaNombre` (String): nombre del artista según el JSON original.
- `lema` (String)
- `anyoFundacion` (Integer)
- `distintivo` (String)
- `boceto` (String): URL de un boceto.
- `experim` (Integer): campo de experimento/prueba.
- `lon` (Double): longitud geográfica.
- `lat` (Double): latitud geográfica.
- `artistaId` (String): **relación** con el `id` de `Artista` (ManyToOne).

### 4.2. Endpoints de `FallaController`

Base: `http://localhost:8080/api/fallas`

#### 1) Listar todas las fallas

- **Método**: `GET`
- **URL**: `/api/fallas`

#### 2) Obtener una falla por id (MongoDB)

- **Método**: `GET`
- **URL**: `/api/fallas/{id}`

#### 3) Crear una nueva falla

- **Método**: `POST`
- **URL**: `/api/fallas`
- **Body (JSON)** de ejemplo (simplificado):

```json
{
  "objectid": 1,
  "idFalla": 1001,
  "nombre": "Falla Plaza del Ayuntamiento",
  "seccion": "Especial",
  "fallera": "María García",
  "presidente": "Carlos López",
  "artistaNombre": "Juan Pérez",
  "lema": "València, llum i foc",
  "anyoFundacion": 1950,
  "distintivo": "Ninot indultat",
  "boceto": "https://ejemplo.com/boceto.png",
  "experim": 1,
  "lon": -0.37639,
  "lat": 39.46975,
  "artistaId": "ID_DEL_ARTISTA_EN_MONGO"
}
```

#### 4) Actualizar una falla

- **Método**: `PUT`
- **URL**: `/api/fallas/{id}`
- **Body (JSON)**: igual estructura que en el `POST`. Se actualiza la falla con ese `id`.

#### 5) Borrar una falla

- **Método**: `DELETE`
- **URL**: `/api/fallas/{id}`

#### 6) Filtrar fallas por sección

- **Método**: `GET`
- **URL**: `/api/fallas/seccion/{seccion}`

Ejemplo:

```bash
curl http://localhost:8080/api/fallas/seccion/Especial
```

#### 7) Obtener las fallas de un artista (por `artistaId`)

- **Método**: `GET`
- **URL**: `/api/fallas/artista/{artistaId}`

Aquí `artistaId` es el `id` de MongoDB de la colección `artistas`.

#### 8) Buscar fallas por lema (texto)

- **Método**: `GET`
- **URL**: `/api/fallas/buscar?lema={texto}`

Ejemplo:

```bash
curl "http://localhost:8080/api/fallas/buscar?lema=València"
```

#### 9) Actualizar solo el distintivo de una falla (operación custom)

- **Método**: `PUT`
- **URL**: `/api/fallas/{idFalla}/distintivo?nuevoDistintivo={valor}`
- **Descripción**: Usa un repositorio custom con `MongoTemplate` para actualizar solo el campo `distintivo` buscando por `idFalla` (no por el `id` de MongoDB).

Ejemplo:

```bash
curl -X PUT "http://localhost:8080/api/fallas/1001/distintivo?nuevoDistintivo=Primer%20Premio"
```

---

## 5. Buenas prácticas básicas

- Los **POST** se hacen sobre la **colección** (`/api/fallas`, `/api/artistas`), sin `/{id}` en la URL. El `id` se genera automáticamente.
- Los **GET/PUT/DELETE** con `/{id}` se usan para trabajar con un **recurso concreto**.
- Para hacer pruebas rápidas puedes usar:
  - `curl` en la terminal.
  - Postman / Insomnia / Thunder Client en el IDE.

Con esto tienes una API REST básica pero completa para gestionar **artistas falleros** y sus **fallas** en MongoDB Atlas.
*** End Patch***} -->
