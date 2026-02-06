# 🎭 Guía de API para Equipos Desktop y Mobile - FallApp

**Versión:** 0.5.5  
**Fecha:** 2026-02-04  
**IP Pública AWS:** http://35.180.21.42:8080  
**Entorno:** Desarrollo

---

## 📋 Índice

1. [Introducción](#introducción)
2. [URL Base](#url-base)
3. [Autenticación JWT](#autenticación-jwt)
4. [Formato de Respuestas](#formato-de-respuestas)
5. [Endpoints Públicos](#endpoints-públicos)
6. [Endpoints Autenticados](#endpoints-autenticados)
7. [Endpoints Solo ADMIN](#endpoints-solo-admin)
8. [Códigos de Error](#códigos-de-error)
9. [Ejemplos de Integración](#ejemplos-de-integración)

---

## 📌 Introducción

Esta guía describe todos los endpoints disponibles en la API REST de FallApp para integración con aplicaciones **Desktop (Electron)** y **Mobile (Android/iOS)**.

### Niveles de Seguridad

| Nivel | Descripción | Endpoints |
|-------|-------------|-----------|
| **🌐 PÚBLICO** | Sin autenticación | Todos los GET (browse), login, registro |
| **🔐 AUTENTICADO** | Requiere JWT token | POST/PUT fallas, eventos, ninots, comentarios, votos |
| **👑 ADMIN** | Solo administradores | DELETE fallas, eventos, ninots, comentarios |

---

## 🌍 URL Base

### Desarrollo (AWS)
```
http://35.180.21.42:8080
```

### Localhost (pruebas locales en servidor)
```
http://localhost:8080
```

**Importante:** 
- Asegúrate de que el puerto **8080** esté abierto en AWS Security Group
- En Android, agrega `android:usesCleartextTraffic="true"` en AndroidManifest.xml
- En desarrollo, CORS está configurado con `*` (cualquier origen)

---

## 🔑 Autenticación JWT

> ✅ **ACTUALIZADO 2026-02-03**: Sistema de autenticación JWT completamente funcional con encriptación BCrypt validada.
> 
> **Estado**: ✅ OPERATIVO  
> **Encriptación**: BCrypt (hashing unidireccional seguro)  
> **Algoritmo JWT**: HS512  
> **Duración Token**: 24 horas (86400 segundos)

### 1. Registro de Usuario

**Endpoint:** `POST /api/auth/registro`  
**Autenticación:** No requerida  
**Descripción:** Crear una nueva cuenta de usuario

#### Request
```json
{
  "email": "usuario@example.com",
  "contrasena": "miPassword123",
  "nombreCompleto": "Juan Pérez García",
  "idFalla": 1
}
```

**Validaciones:**
- `email`: Formato válido, único en el sistema
- `contrasena`: Mínimo 6 caracteres (encriptada con BCrypt automáticamente)
- `nombreCompleto`: Entre 3 y 200 caracteres
- `idFalla`: Opcional, para asociar usuario a una falla

**Seguridad:**
- Las contraseñas se encriptan con BCrypt antes de almacenarse
- No se almacenan contraseñas en texto plano
- El sistema utiliza hashing unidireccional (no se pueden "desencriptar")

#### Response (201 Created)
```json
{
  "exito": true,
  "mensaje": "Usuario registrado exitosamente",
  "datos": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tipo": "Bearer",
    "expiraEn": 86400,
    "usuario": {
      "idUsuario": 42,
      "email": "usuario@example.com",
      "nombreCompleto": "Juan Pérez García",
      "rol": "FALLERO",
      "idFalla": 1,
      "nombreFalla": "Falla Convento Jerusalén"
    }
  },
  "timestamp": "2026-02-01T18:30:00"
}
```

---

### 2. Login

**Endpoint:** `POST /api/auth/login`  
**Autenticación:** No requerida  
**Descripción:** Iniciar sesión y obtener token JWT

#### Request
```json
{
  "email": "usuario@example.com",
  "contrasena": "miPassword123"
}
```

**Proceso de Autenticación:**
1. El sistema busca el usuario por email
2. Compara el hash BCrypt de la contraseña proporcionada con el almacenado
3. Si coinciden, genera un token JWT válido por 24 horas
4. Devuelve el token y los datos del usuario

#### Response (200 OK)
```json
{
  "exito": true,
  "mensaje": "Login exitoso",
  "datos": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNjc1MjY0MDAwLCJleHAiOjE2NzUzNTA0MDB9.signature",
    "tipo": "Bearer",
    "expiraEn": 86400,
    "usuario": {
      "idUsuario": 42,
      "email": "usuario@example.com",
      "nombreCompleto": "Juan Pérez García",
      "rol": "FALLERO",
      "idFalla": 1,
      "nombreFalla": "Falla Convento Jerusalén",
      "ultimoAcceso": "2026-02-01T18:30:00"
    }
  },
  "timestamp": "2026-02-01T18:30:00"
}
```

#### Error (401 Unauthorized)
```json
{
  "exito": false,
  "mensaje": "Credenciales inválidas",
  "datos": null,
  "timestamp": "2026-02-01T18:30:00"
}
```

---

### 3. Usar Token JWT

Para endpoints autenticados, incluye el token en el header `Authorization`:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Ejemplo cURL:**
```bash
curl -X POST http://35.180.21.42:8080/api/fallas \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Nueva Falla","seccion":"8A","presidente":"Juan García",...}'
```

**Características del Token:**
- **Duración:** 24 horas (86400 segundos)
- **Algoritmo:** HS512
- **Tipo:** Bearer
- **Renovación:** Solicitar nuevo login antes de expiración
- **Validación:** El backend verifica firma y expiración en cada petición

---

## 📦 Formato de Respuestas

Todas las respuestas siguen el formato estándar `ApiResponse<T>`:

```json
{
  "exito": true,
  "mensaje": "Operación exitosa",
  "datos": { ... },
  "timestamp": "2026-02-01T18:30:00"
}
```

### Respuesta Exitosa (2xx)
```json
{
  "exito": true,
  "mensaje": "Falla creada exitosamente",
  "datos": {
    "idFalla": 348,
    "nombre": "Falla Ejemplo",
    ...
  },
  "timestamp": "2026-02-01T18:30:00"
}
```

### Respuesta con Error (4xx, 5xx)
```json
{
  "exito": false,
  "mensaje": "Falla no encontrada con ID: 999",
  "datos": null,
  "timestamp": "2026-02-01T18:30:00"
}
```

---

## 🌐 Endpoints Públicos

### FALLAS

#### GET /api/fallas - Listar fallas con paginación
**Autenticación:** No requerida  
**Query Params:**
- `pagina` (int, default: 0): Número de página (0-indexed)
- `tamano` (int, default: 20): Elementos por página

**Response:**
```json
{
  "exito": true,
  "mensaje": null,
  "datos": {
    "contenido": [
      {
        "idFalla": 95,
        "nombre": "Bailén-Xàtiva",
        "seccion": "3B",
        "fallera": "María López Crespo",
        "presidente": "Leovigildo Patón Sellés",
        "artista": "ArtdeFoc Creaciones Artísticas S.L",
        "lema": "Libérate",
        "anyoFundacion": 1972,
        "distintivo": "Brillants (2012)",
        "urlBoceto": "http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_088_bm.jpg",
        "experim": false,
        "latitud": 39.46758519,
        "longitud": -0.37761259,
        "descripcion": null,
        "webOficial": null,
        "telefonoContacto": null,
        "emailContacto": null,
        "categoria": "sin_categoria",
        "totalEventos": 0,
        "totalNinots": 0,
        "totalMiembros": 0,
        "fechaCreacion": "2026-02-04T19:24:52.288945",
        "fechaActualizacion": "2026-02-04T19:24:52.288945"
      }
    ],
    "paginaActual": 0,
    "totalElementos": 351,
    "totalPaginas": 18,
    "tamano": 20,
    "esUltima": false,
    "esPrimera": true
  },
  "timestamp": "2026-02-04T19:30:00"
}
```

---

#### GET /api/fallas/{id} - Obtener falla por ID
**Autenticación:** No requerida  
**Path Param:** `id` (Long)

**Response:**
```json
{
  "exito": true,
  "datos": {
    "idFalla": 95,
    "nombre": "Bailén-Xàtiva",
    "seccion": "3B",
    "fallera": "María López Crespo",
    "presidente": "Leovigildo Patón Sellés",
    "artista": "ArtdeFoc Creaciones Artísticas S.L",
    "lema": "Libérate",
    "anyoFundacion": 1972,
    "distintivo": "Brillants (2012)",
    "urlBoceto": "http://mapas.valencia.es/WebsMunicipales/layar/img/fallasvalencia/2025_088_bm.jpg",
    "experim": false,
    "latitud": 39.46758519,
    "longitud": -0.37761259,
    "descripcion": null,
    "webOficial": null,
    "telefonoContacto": null,
    "emailContacto": null,
    "categoria": "sin_categoria",
    "totalEventos": 0,
    "totalNinots": 0,
    "totalMiembros": 0,
    "fechaCreacion": "2026-02-04T19:24:52.288945",
    "fechaActualizacion": "2026-02-04T19:24:52.288945"
  }
}
```

---

#### GET /api/fallas/{id}/ubicacion - Obtener ubicación GPS de una falla
**Autenticación:** No requerida  
**Path Param:** `id` (Long)  
**Descripción:** Retorna únicamente las coordenadas GPS de una falla específica. Útil para mapas y geolocalización sin cargar todos los datos de la falla.

**Ejemplo:** `GET /api/fallas/95/ubicacion`

**Response:**
```json
{
  "exito": true,
  "mensaje": null,
  "datos": {
    "idFalla": 95,
    "nombre": "Plaza Sant Miquel-Vicent Iborra",
    "latitud": 39.47682454,
    "longitud": -0.38087859,
    "tieneUbicacion": true
  }
}
```

**Campos:**
- `idFalla`: ID de la falla
- `nombre`: Nombre de la falla
- `latitud`: Coordenada GPS latitud (WGS84)
- `longitud`: Coordenada GPS longitud (WGS84)
- `tieneUbicacion`: Booleano indicando si tiene coordenadas disponibles

**Ejemplo de uso en JavaScript:**
```javascript
async function obtenerUbicacionFalla(idFalla) {
  const response = await fetch(`${API_BASE_URL}/api/fallas/${idFalla}/ubicacion`);
  const data = await response.json();
  
  if (data.exito && data.datos.tieneUbicacion) {
    const { latitud, longitud, nombre } = data.datos;
    // Usar en mapa (ej: Leaflet, Google Maps)
    mostrarEnMapa(latitud, longitud, nombre);
  }
}
```

---

#### GET /api/fallas/buscar - Buscar fallas por texto
**Autenticación:** No requerida  
**Query Param:** `texto` (String)

**Ejemplo:** `GET /api/fallas/buscar?texto=convento`

**Response:**
```json
{
  "exito": true,
  "datos": [
    {
      "idFalla": 1,
      "nombre": "Falla Convento Jerusalén",
      "seccion": "1A"
    },
    {
      "idFalla": 45,
      "nombre": "Falla Convento San Francisco",
      "seccion": "3B"
    }
  ]
}
```

---

#### GET /api/fallas/cercanas - Buscar fallas cercanas
**Autenticación:** No requerida  
**Query Params:**
- `latitud` (double, requerido)
- `longitud` (double, requerido)
- `radio` (double, default: 5.0): Radio en kilómetros

**Ejemplo:** `GET /api/fallas/cercanas?latitud=39.4699&longitud=-0.3763&radio=2.0`

**Response:**
```json
{
  "exito": true,
  "datos": [
    {
      "idFalla": 1,
      "nombre": "Falla Convento Jerusalén",
      "latitud": 39.4699,
      "longitud": -0.3763,
      "distancia": 0.3
    },
    {
      "idFalla": 5,
      "nombre": "Falla Mercat Central",
      "latitud": 39.4740,
      "longitud": -0.3785,
      "distancia": 1.2
    }
  ]
}
```

---

#### GET /api/fallas/seccion/{seccion} - Fallas por sección
**Autenticación:** No requerida  
**Path Param:** `seccion` (String) - Ejemplo: "1A", "2B"

---

#### GET /api/fallas/categoria/{categoria} - Fallas por categoría
**Autenticación:** No requerida  
**Path Param:** `categoria` (String) - Ejemplo: "ESPECIAL", "PRIMERA"

---

### EVENTOS

#### GET /api/eventos/futuros - Eventos futuros
**Autenticación:** No requerida

**Response:**
```json
{
  "exito": true,
  "datos": [
    {
      "idEvento": 1,
      "idFalla": 1,
      "nombreFalla": "Falla Convento Jerusalén",
      "tipo": "PLANTÀ",
      "nombre": "Plantà 2026",
      "descripcion": "Plantà de la falla gran",
      "fechaEvento": "2026-03-15T08:00:00",
      "ubicacion": "Plaza del Convento",
      "participantesEstimado": 500
    }
  ]
}
```

---

#### GET /api/eventos/proximos - Próximos N eventos
**Autenticación:** No requerida  
**Query Param:** `limite` (int, default: 10, max: 50)

---

#### GET /api/eventos/{id} - Evento por ID
**Autenticación:** No requerida

---

#### GET /api/eventos/falla/{idFalla} - Eventos de una falla
**Autenticación:** No requerida  
**Query Params:**
- `page` (int, default: 0)
- `size` (int, default: 20, max: 100)

**Response:**
```json
{
  "exito": true,
  "datos": {
    "content": [
      {
        "idEvento": 5,
        "tipo": "OFRENDA",
        "nombre": "Ofrenda de Flores",
        "fechaEvento": "2026-03-17T17:00:00"
      }
    ],
    "pageable": { ... },
    "totalElements": 12
  }
}
```

---

### NINOTS

#### GET /api/ninots - Listar ninots con paginación
**Autenticación:** No requerida  
**Query Params:**
- `page` (int, default: 0)
- `size` (int, default: 20)

**Response:**
```json
{
  "exito": true,
  "datos": {
    "content": [
      {
        "idNinot": 1,
        "idFalla": 1,
        "nombreFalla": "Falla Convento Jerusalén",
        "nombreNinot": "El Político Corrupto",
        "tituloObra": "La Trampa del Poder",
        "altura": 3.5,
        "ancho": 2.0,
        "imagenes": [
          "https://fallapp.es/ninots/1_1.jpg",
          "https://fallapp.es/ninots/1_2.jpg"
        ],
        "premiado": true,
        "totalVotos": 245,
        "votosIngenioso": 80,
        "votosCritico": 95,
        "votosArtistico": 70,
        "fechaCreacion": "2026-01-10T12:00:00"
      }
    ],
    "totalElements": 128
  }
}
```

---

#### GET /api/ninots/{id} - Ninot por ID
**Autenticación:** No requerida

---

#### GET /api/ninots/falla/{idFalla} - Ninots de una falla
**Autenticación:** No requerida

---

#### GET /api/ninots/premiados - Ninots premiados
**Autenticación:** No requerida  
**Query Params:**
- `page` (int, default: 0)
- `size` (int, default: 20)

---

### COMENTARIOS

#### GET /api/comentarios - Comentarios filtrados
**Autenticación:** No requerida  
**Query Params:**
- `idFalla` (Long, opcional)
- `idNinot` (Long, opcional)

**Ejemplo:** `GET /api/comentarios?idFalla=1`

**Response:**
```json
{
  "exito": true,
  "datos": [
    {
      "idComentario": 1,
      "idUsuario": 5,
      "nombreUsuario": "María García",
      "idFalla": 1,
      "nombreFalla": "Falla Convento Jerusalén",
      "idNinot": null,
      "nombreNinot": null,
      "contenido": "¡Espectacular la plantà de este año! Enhorabuena al casal",
      "fechaCreacion": "2026-03-16T10:30:00",
      "fechaActualizacion": "2026-03-16T10:30:00"
    }
  ]
}
```

---

#### GET /api/comentarios/{id} - Comentario por ID
**Autenticación:** No requerida

---

### ESTADÍSTICAS

#### GET /api/estadisticas/resumen - Resumen general
**Autenticación:** No requerida

**Response:**
```json
{
  "exito": true,
  "datos": {
    "totalFallas": 347,
    "totalEventos": 1245,
    "totalNinots": 982,
    "totalUsuarios": 4567,
    "totalVotos": 12890,
    "totalComentarios": 3456
  }
}
```

---

#### GET /api/estadisticas/fallas - Estadísticas de fallas
**Autenticación:** No requerida

**Response:**
```json
{
  "exito": true,
  "datos": {
    "totalFallas": 347,
    "fallasPorCategoria": {
      "ESPECIAL": 15,
      "PRIMERA": 45,
      "SEGUNDA": 80,
      "TERCERA": 120,
      "INFANTIL": 87
    },
    "fallasPorSeccion": {
      "1A": 8,
      "2A": 12,
      "3B": 15
    }
  }
}
```

---

#### GET /api/estadisticas/votos - Estadísticas de votos
**Autenticación:** No requerida

---

#### GET /api/estadisticas/usuarios - Estadísticas de usuarios
**Autenticación:** No requerida

---

#### GET /api/estadisticas/actividad - Actividad reciente
**Autenticación:** No requerida

---

#### GET /api/estadisticas/eventos - Estadísticas de eventos
**Autenticación:** No requerida

---

### USUARIOS

#### GET /api/usuarios/{id} - Usuario por ID (perfil público)
**Autenticación:** No requerida

**Response:**
```json
{
  "exito": true,
  "datos": {
    "idUsuario": 5,
    "nombreCompleto": "María García López",
    "email": "maria@example.com",
    "rol": "FALLERO",
    "idFalla": 1,
    "nombreFalla": "Falla Convento Jerusalén"
  }
}
```

---

## 🔐 Endpoints Autenticados

**Requieren header:** `Authorization: Bearer {token}`

### FALLAS

#### POST /api/fallas - Crear nueva falla
**Autenticación:** Requerida (usuario autenticado)

**Request:**
```json
{
  "nombre": "Nueva Falla Ruzafa Norte",
  "seccion": "5B",
  "fallera": "Ana Martínez Pérez",
  "presidente": "Carlos Gómez Ruiz",
  "artista": "Vicente López",
  "lema": "Tradición y Futuro",
  "anyoFundacion": 2010,
  "distintivo": "Primera",
  "urlBoceto": "https://example.com/boceto.jpg",
  "experim": false,
  "latitud": 39.4650,
  "longitud": -0.3700,
  "descripcion": "Falla joven del barrio de Ruzafa",
  "webOficial": "https://fallaruzafanorte.com",
  "telefonoContacto": "+34961987654",
  "emailContacto": "info@fallaruzafanorte.com",
  "categoria": "PRIMERA"
}
```

**Validaciones:**
- `nombre`: Obligatorio, máximo 255 caracteres
- `seccion`: Obligatorio, máximo 5 caracteres (ej: "1A", "5B")
- `presidente`: Obligatorio
- `anyoFundacion`: Obligatorio, >= 1900
- `latitud`: Entre -90 y 90
- `longitud`: Entre -180 y 180
- `emailContacto`: Formato de email válido

**Response (201 Created):**
```json
{
  "exito": true,
  "mensaje": "Falla creada exitosamente",
  "datos": {
    "idFalla": 348,
    "nombre": "Nueva Falla Ruzafa Norte",
    "seccion": "5B",
    "fechaCreacion": "2026-02-01T18:45:00"
  }
}
```

---

#### PUT /api/fallas/{id} - Actualizar falla
**Autenticación:** Requerida (usuario autenticado)

**Request:** Mismo formato que POST, campos opcionales se pueden omitir

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Falla actualizada exitosamente",
  "datos": { ... }
}
```

---

### EVENTOS

#### POST /api/eventos - Crear evento
**Autenticación:** Requerida

**Request:**
```json
{
  "idFalla": 1,
  "tipo": "MASCLETÀ",
  "nombre": "Mascletà Día Grande",
  "descripcion": "Mascletà especial para el día grande de la falla",
  "fechaEvento": "2026-03-18T14:00:00",
  "ubicacion": "Plaza del Ayuntamiento",
  "participantesEstimado": 10000
}
```

**Validaciones:**
- `idFalla`: Obligatorio, debe existir
- `tipo`: Obligatorio (ej: "PLANTÀ", "OFRENDA", "CREMÀ", "MASCLETÀ", "PAELLA")
- `nombre`: Obligatorio, máximo 255 caracteres
- `fechaEvento`: Obligatorio, formato ISO 8601
- `participantesEstimado`: >= 0

**Response (201 Created):**
```json
{
  "exito": true,
  "mensaje": "Evento creado exitosamente",
  "datos": {
    "idEvento": 125,
    "tipo": "MASCLETÀ",
    "nombre": "Mascletà Día Grande",
    "fechaEvento": "2026-03-18T14:00:00"
  }
}
```

---

#### PUT /api/eventos/{id} - Actualizar evento
**Autenticación:** Requerida

---

### NINOTS

#### POST /api/ninots - Crear ninot
**Autenticación:** Requerida

**Request:**
```json
{
  "idFalla": 1,
  "nombreNinot": "El Influencer",
  "tituloObra": "La Era Digital",
  "altura": 4.2,
  "ancho": 2.5,
  "imagenes": [
    "https://fallapp.es/ninots/nuevo_1.jpg",
    "https://fallapp.es/ninots/nuevo_2.jpg"
  ],
  "premiado": false
}
```

**Validaciones:**
- `idFalla`: Obligatorio
- `nombreNinot`: Obligatorio, máximo 255 caracteres
- `altura`: >= 0.1
- `ancho`: >= 0.1
- `imagenes`: Array de URLs

**Response (201 Created):**
```json
{
  "exito": true,
  "mensaje": "Ninot creado exitosamente",
  "datos": {
    "idNinot": 983,
    "nombreNinot": "El Influencer",
    "totalVotos": 0
  }
}
```

---

#### PUT /api/ninots/{id} - Actualizar ninot
**Autenticación:** Requerida

---

### COMENTARIOS

#### POST /api/comentarios - Crear comentario
**Autenticación:** Requerida

**Request (comentario en falla):**
```json
{
  "idUsuario": 5,
  "idFalla": 1,
  "contenido": "¡Espectacular la plantà de este año! Enhorabuena al casal"
}
```

**Request (comentario en falla a través de ninot):**
```json
{
  "idUsuario": 5,
  "idNinot": 15,
  "contenido": "Este ninot merece el premio, muy crítico y artístico"
}
```

**Nota importante v0.5.0:** Los comentarios en ninots se almacenan en la **falla** asociada, no en el ninot directamente.

**Validaciones:**
- `idUsuario`: Obligatorio (debe coincidir con usuario autenticado)
- `idFalla` O `idNinot`: Uno de los dos obligatorio (no ambos)
- `contenido`: Entre 3 y 500 caracteres

**Response (201 Created):**
```json
{
  "exito": true,
  "mensaje": "Comentario creado exitosamente",
  "datos": {
    "idComentario": 456,
    "contenido": "¡Espectacular la plantà...",
    "fechaCreacion": "2026-02-01T19:00:00"
  }
}
```

---

#### PUT /api/comentarios/{id} - Actualizar comentario
**Autenticación:** Requerida (solo autor o ADMIN)

**Request:**
```json
{
  "contenido": "Contenido actualizado del comentario"
}
```

---

### VOTOS

#### POST /api/votos - Votar por una falla (a través de ninot)
**Autenticación:** Requerida

**Nota importante v0.5.0:** Los votos se registran en la **falla** asociada al ninot, no en el ninot directamente. Esto es por diseño del esquema de base de datos.

**Request:**
```json
{
  "idNinot": 15,
  "tipoVoto": "ARTISTICO"
}
```

**Tipos de voto válidos:**
- `"INGENIOSO"`
- `"CRITICO"`
- `"ARTISTICO"`

**Validaciones:**
- Usuario solo puede votar 1 vez por falla por tipo
- `idNinot` debe existir (internamente se vota su falla)
- `tipoVoto` debe ser uno de los 3 valores permitidos

**Response (201 Created):**
```json
{
  "exito": true,
  "mensaje": "Voto registrado",
  "datos": {
    "idVoto": 789,
    "idUsuario": 5,
    "nombreUsuario": "María García",
    "idFalla": 23,
    "nombreFalla": "Falla Convento Jerusalén",
    "tipoVoto": "ARTISTICO",
    "fechaCreacion": "2026-02-01T19:05:00"
  }
}
```

**Error (400 Bad Request) - Voto duplicado:**
```json
{
  "exito": false,
  "mensaje": "Ya has votado por esta falla con tipo ARTISTICO",
  "datos": null
}
```

---

#### GET /api/votos/usuario/{idUsuario} - Votos de un usuario
**Autenticación:** Requerida (solo el propio usuario o ADMIN)

---

#### GET /api/votos/falla/{idFalla} - Votos de una falla
**Autenticación:** Requerida

---

#### DELETE /api/votos/{idVoto} - Eliminar voto
**Autenticación:** Requerida (solo autor del voto)

---

### USUARIOS

#### GET /api/usuarios - Listar usuarios
**Autenticación:** Requerida

---

#### PUT /api/usuarios/{id} - Actualizar perfil
**Autenticación:** Requerida (solo el propio usuario)

---

## 👑 Endpoints Solo ADMIN

**Requieren:** `Authorization: Bearer {token}` + usuario con rol `ADMIN`

### DELETE /api/fallas/{id} - Eliminar falla
**Autenticación:** ADMIN

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Falla eliminada exitosamente",
  "datos": null
}
```

**⚠️ Atención:** Esto eliminará también eventos, ninots y votos asociados (cascada)

---

### DELETE /api/eventos/{id} - Eliminar evento
**Autenticación:** ADMIN

---

### DELETE /api/ninots/{id} - Eliminar ninot
**Autenticación:** ADMIN

**⚠️ Atención:** Esto eliminará también los votos asociados

---

### DELETE /api/comentarios/{id} - Eliminar comentario
**Autenticación:** ADMIN

---

## ⚠️ Códigos de Error

| Código | Descripción | Ejemplo |
|--------|-------------|---------|
| **200** | OK | GET exitoso |
| **201** | Created | POST exitoso (recurso creado) |
| **400** | Bad Request | Datos inválidos, validación fallida |
| **401** | Unauthorized | Token JWT inválido o expirado |
| **403** | Forbidden | Token válido pero sin permisos (ej: no ADMIN) |
| **404** | Not Found | Recurso no encontrado (ID inválido) |
| **409** | Conflict | Conflicto (ej: email duplicado, voto duplicado) |
| **500** | Internal Server Error | Error del servidor |

### Ejemplos de Respuestas de Error

#### 400 Bad Request - Validación
```json
{
  "exito": false,
  "mensaje": "El nombre es obligatorio",
  "datos": null,
  "timestamp": "2026-02-01T19:10:00"
}
```

#### 401 Unauthorized - Token inválido
```json
{
  "exito": false,
  "mensaje": "Token JWT inválido o expirado",
  "datos": null,
  "timestamp": "2026-02-01T19:10:00"
}
```

#### 403 Forbidden - Sin permisos
```json
{
  "exito": false,
  "mensaje": "No tienes permisos para realizar esta acción",
  "datos": null,
  "timestamp": "2026-02-01T19:10:00"
}
```

#### 404 Not Found
```json
{
  "exito": false,
  "mensaje": "Falla no encontrada con ID: 999",
  "datos": null,
  "timestamp": "2026-02-01T19:10:00"
}
```

#### 409 Conflict - Voto duplicado
```json
{
  "exito": false,
  "mensaje": "Ya has votado por este ninot con tipo ARTISTICO",
  "datos": null,
  "timestamp": "2026-02-01T19:10:00"
}
```

---

## 💻 Ejemplos de Integración

### JavaScript (Desktop - Electron/Browser)

```javascript
// =================================
// 1. CONFIGURACIÓN BASE
// =================================
const API_BASE_URL = 'http://35.180.21.42:8080';

// Guardar token en localStorage
function saveToken(token) {
  localStorage.setItem('jwt_token', token);
}

// Obtener token guardado
function getToken() {
  return localStorage.getItem('jwt_token');
}

// =================================
// 2. REGISTRO
// =================================
async function registrarUsuario(email, password, nombreCompleto, idFalla) {
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/registro`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email,
        contrasena: password,
        nombreCompleto,
        idFalla
      })
    });

    const data = await response.json();
    
    if (data.exito) {
      // Guardar token automáticamente
      saveToken(data.datos.token);
      console.log('Usuario registrado:', data.datos.usuario);
      return data.datos;
    } else {
      console.error('Error registro:', data.mensaje);
      throw new Error(data.mensaje);
    }
  } catch (error) {
    console.error('Error de red:', error);
    throw error;
  }
}

// =================================
// 3. LOGIN
// =================================
async function login(email, password) {
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email,
        contrasena: password
      })
    });

    const data = await response.json();
    
    if (data.exito) {
      saveToken(data.datos.token);
      console.log('Login exitoso:', data.datos.usuario);
      return data.datos;
    } else {
      throw new Error(data.mensaje);
    }
  } catch (error) {
    console.error('Error login:', error);
    throw error;
  }
}

// =================================
// 4. OBTENER FALLAS (público)
// =================================
async function obtenerFallas(pagina = 0, tamano = 20) {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/fallas?pagina=${pagina}&tamano=${tamano}`
    );
    
    const data = await response.json();
    
    if (data.exito) {
      console.log('Total fallas:', data.datos.totalElementos);
      return data.datos.contenido;
    }
  } catch (error) {
    console.error('Error obteniendo fallas:', error);
    throw error;
  }
}

// =================================
// 5. CREAR FALLA (autenticado)
// =================================
async function crearFalla(fallaData) {
  const token = getToken();
  
  if (!token) {
    throw new Error('Debes iniciar sesión primero');
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/fallas`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(fallaData)
    });

    const data = await response.json();
    
    if (data.exito) {
      console.log('Falla creada:', data.datos);
      return data.datos;
    } else {
      throw new Error(data.mensaje);
    }
  } catch (error) {
    console.error('Error creando falla:', error);
    throw error;
  }
}

// =================================
// 6. VOTAR POR NINOT (autenticado)
// =================================
async function votarNinot(idNinot, tipoVoto) {
  const token = getToken();
  
  if (!token) {
    throw new Error('Debes iniciar sesión primero');
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/votos`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        idNinot,
        tipoVoto  // "INGENIOSO", "CRITICO", "ARTISTICO"
      })
    });

    const data = await response.json();
    
    if (data.exito) {
      console.log('Voto registrado:', data.datos);
      return data.datos;
    } else {
      throw new Error(data.mensaje);
    }
  } catch (error) {
    console.error('Error votando:', error);
    throw error;
  }
}

// =================================
// 7. OBTENER ESTADÍSTICAS (público)
// =================================
async function obtenerEstadisticas() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/estadisticas/resumen`);
    const data = await response.json();
    
    if (data.exito) {
      console.log('Estadísticas:', data.datos);
      return data.datos;
    }
  } catch (error) {
    console.error('Error obteniendo estadísticas:', error);
    throw error;
  }
}

// =================================
// EJEMPLO DE USO
// =================================
async function ejemploCompleto() {
  try {
    // 1. Registro
    await registrarUsuario(
      'test@example.com',
      'password123',
      'Usuario Test',
      1
    );

    // 2. Login (si ya estás registrado)
    // await login('test@example.com', 'password123');

    // 3. Obtener fallas
    const fallas = await obtenerFallas(0, 10);
    console.log('Primeras 10 fallas:', fallas);

    // 4. Crear nueva falla
    const nuevaFalla = await crearFalla({
      nombre: 'Falla Ejemplo JS',
      seccion: '9Z',
      presidente: 'Test User',
      anyoFundacion: 2020,
      latitud: 39.47,
      longitud: -0.38
    });

    // 5. Votar por ninot
    await votarNinot(1, 'ARTISTICO');

    // 6. Ver estadísticas
    const stats = await obtenerEstadisticas();
    console.log('Total fallas:', stats.totalFallas);

  } catch (error) {
    console.error('Error en flujo completo:', error.message);
  }
}
```

---

### Kotlin (Android)

```kotlin
// =================================
// 1. MODELO DE DATOS
// =================================
data class ApiResponse<T>(
    val exito: Boolean,
    val mensaje: String?,
    val datos: T?,
    val timestamp: String
)

data class LoginRequest(
    val email: String,
    val contrasena: String
)

data class LoginResponse(
    val token: String,
    val tipo: String,
    val expiraEn: Long,
    val usuario: Usuario
)

data class Usuario(
    val idUsuario: Long,
    val email: String,
    val nombreCompleto: String,
    val rol: String,
    val idFalla: Long?,
    val nombreFalla: String?
)

data class FallaDTO(
    val idFalla: Long?,
    val nombre: String,
    val seccion: String,
    val presidente: String,
    val anyoFundacion: Int,
    val latitud: Double?,
    val longitud: Double?,
    val categoria: String?
)

data class VotoRequest(
    val idNinot: Long,
    val tipoVoto: String  // "INGENIOSO", "CRITICO", "ARTISTICO"
)

// =================================
// 2. RETROFIT INTERFACE
// =================================
interface FallAppApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("/api/auth/registro")
    suspend fun registro(@Body request: RegistroRequest): ApiResponse<LoginResponse>

    @GET("/api/fallas")
    suspend fun obtenerFallas(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): ApiResponse<PaginatedResponse<FallaDTO>>

    @GET("/api/fallas/{id}")
    suspend fun obtenerFalla(@Path("id") id: Long): ApiResponse<FallaDTO>

    @POST("/api/fallas")
    suspend fun crearFalla(
        @Header("Authorization") token: String,
        @Body falla: FallaDTO
    ): ApiResponse<FallaDTO>

    @POST("/api/votos")
    suspend fun votar(
        @Header("Authorization") token: String,
        @Body voto: VotoRequest
    ): ApiResponse<VotoDTO>

    @GET("/api/estadisticas/resumen")
    suspend fun obtenerEstadisticas(): ApiResponse<Estadisticas>
}

// =================================
// 3. CONFIGURACIÓN RETROFIT
// =================================
object RetrofitClient {
    private const val BASE_URL = "http://35.180.21.42:8080"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: FallAppApi = retrofit.create(FallAppApi::class.java)
}

// =================================
// 4. REPOSITORY
// =================================
class FallAppRepository(private val api: FallAppApi) {
    
    private var jwtToken: String? = null

    // Login
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.exito && response.datos != null) {
                jwtToken = response.datos.token
                Result.success(response.datos)
            } else {
                Result.failure(Exception(response.mensaje ?: "Error de login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener fallas (público)
    suspend fun obtenerFallas(pagina: Int = 0): Result<List<FallaDTO>> {
        return try {
            val response = api.obtenerFallas(pagina, 20)
            if (response.exito && response.datos != null) {
                Result.success(response.datos.contenido)
            } else {
                Result.failure(Exception(response.mensaje ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Crear falla (autenticado)
    suspend fun crearFalla(falla: FallaDTO): Result<FallaDTO> {
        val token = jwtToken ?: return Result.failure(Exception("No autenticado"))
        
        return try {
            val response = api.crearFalla("Bearer $token", falla)
            if (response.exito && response.datos != null) {
                Result.success(response.datos)
            } else {
                Result.failure(Exception(response.mensaje ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Votar (autenticado)
    suspend fun votar(idNinot: Long, tipoVoto: String): Result<VotoDTO> {
        val token = jwtToken ?: return Result.failure(Exception("No autenticado"))
        
        return try {
            val response = api.votar("Bearer $token", VotoRequest(idNinot, tipoVoto))
            if (response.exito && response.datos != null) {
                Result.success(response.datos)
            } else {
                Result.failure(Exception(response.mensaje ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// =================================
// 5. VIEWMODEL (ejemplo)
// =================================
class MainViewModel : ViewModel() {
    private val repository = FallAppRepository(RetrofitClient.api)
    
    private val _fallas = MutableLiveData<List<FallaDTO>>()
    val fallas: LiveData<List<FallaDTO>> = _fallas

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            val result = repository.login(email, password)
            result.onSuccess { loginResponse ->
                _loginState.value = LoginState.Success(loginResponse.usuario)
            }.onFailure { error ->
                _loginState.value = LoginState.Error(error.message ?: "Error")
            }
        }
    }

    fun cargarFallas() {
        viewModelScope.launch {
            val result = repository.obtenerFallas()
            result.onSuccess { listaFallas ->
                _fallas.value = listaFallas
            }
        }
    }

    fun crearFalla(falla: FallaDTO) {
        viewModelScope.launch {
            repository.crearFalla(falla)
        }
    }

    fun votarNinot(idNinot: Long, tipo: String) {
        viewModelScope.launch {
            repository.votar(idNinot, tipo)
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val usuario: Usuario) : LoginState()
    data class Error(val mensaje: String) : LoginState()
}

// =================================
// 6. ACTIVITY (ejemplo)
// =================================
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Observar login
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    // Mostrar progress
                }
                is LoginState.Success -> {
                    Toast.makeText(this, "Bienvenido ${state.usuario.nombreCompleto}", Toast.LENGTH_SHORT).show()
                }
                is LoginState.Error -> {
                    Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observar fallas
        viewModel.fallas.observe(this) { fallas ->
            // Actualizar RecyclerView
        }

        // Login
        btnLogin.setOnClickListener {
            viewModel.login("user@example.com", "password123")
        }

        // Cargar fallas
        viewModel.cargarFallas()

        // Votar
        btnVotar.setOnClickListener {
            viewModel.votarNinot(1, "ARTISTICO")
        }
    }
}
```

**AndroidManifest.xml - Permitir HTTP (desarrollo):**
```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

---

### CURL (Testing rápido)

```bash
# =================================
# 1. REGISTRO
# =================================
curl -X POST http://35.180.21.42:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "contrasena": "password123",
    "nombreCompleto": "Usuario Test",
    "idFalla": 1
  }'

# =================================
# 2. LOGIN
# =================================
curl -X POST http://35.180.21.42:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
    "contrasena": "password123"
  }'

# Guardar token en variable (Linux/Mac)
TOKEN=$(curl -s -X POST http://35.180.21.42:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","contrasena":"password123"}' \
  | jq -r '.datos.token')

echo $TOKEN

# =================================
# 3. OBTENER FALLAS (público)
# =================================
curl http://35.180.21.42:8080/api/fallas?pagina=0&tamano=10

# =================================
# 4. BUSCAR FALLAS (público)
# =================================
curl "http://35.180.21.42:8080/api/fallas/buscar?texto=convento"

# =================================
# 5. ESTADÍSTICAS (público)
# =================================
curl http://35.180.21.42:8080/api/estadisticas/resumen | jq

# =================================
# 6. CREAR FALLA (autenticado)
# =================================
curl -X POST http://35.180.21.42:8080/api/fallas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Falla Test CURL",
    "seccion": "9Z",
    "presidente": "Test User",
    "anyoFundacion": 2020,
    "latitud": 39.47,
    "longitud": -0.38
  }'

# =================================
# 7. VOTAR (autenticado)
# =================================
curl -X POST http://35.180.21.42:8080/api/votos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "idNinot": 1,
    "tipoVoto": "ARTISTICO"
  }'

# =================================
# 8. CREAR COMENTARIO (autenticado)
# =================================
curl -X POST http://35.180.21.42:8080/api/comentarios \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "idUsuario": 5,
    "idFalla": 1,
    "contenido": "¡Excelente falla!"
  }'
```

---

## 📚 Recursos Adicionales

### Swagger UI (Documentación Interactiva)
```
http://35.180.21.42:8080/swagger-ui.html
```

Permite:
- Ver todos los endpoints disponibles
- Probar requests directamente desde el navegador
- Ver esquemas de datos detallados

### OpenAPI Docs (JSON)
```
http://35.180.21.42:8080/v3/api-docs
```

---

## 🔧 Troubleshooting

### Error: "Failed to connect to 35.180.21.42:8080"
**Causa:** Puerto 8080 no abierto en AWS Security Group  
**Solución:** 
1. AWS Console → EC2 → Security Groups
2. Agregar regla TCP 8080, source 0.0.0.0/0
3. Guardar (no reiniciar instancia)

### Error: "Cleartext HTTP traffic not permitted" (Android)
**Solución:** Agregar en AndroidManifest.xml:
```xml
<application android:usesCleartextTraffic="true">
```

### Error 401: "Token JWT inválido o expirado"
**Causa:** Token expirado (24h) o formato incorrecto  
**Solución:**
- Hacer login nuevamente
- Verificar header: `Authorization: Bearer TOKEN` (con espacio)

### Error 403: "No tienes permisos para realizar esta acción"
**Causa:** Endpoint requiere rol ADMIN  
**Solución:**
- Verificar que el endpoint no requiera ADMIN
- Contactar admin para elevar permisos

### Error 400: Validación fallida
**Causa:** Datos enviados no cumplen validaciones  
**Solución:** Revisar mensaje de error y validaciones del endpoint

---

## 📞 Contacto

**Equipo Backend:** fallapp-backend@example.com  
**Slack:** #fallapp-api  
**Documentación:** /srv/FallApp/04.docs/

---

**Última actualización:** 2026-02-04  
**Versión API:** 0.5.5  
**Estado:** Desarrollo activo  
**Cambios recientes:**
- ✅ API devuelve TODOS los campos de fallas (fallera, artista, lema, distintivo, urlBoceto, experim, descripcion, webOficial, telefonoContacto, emailContacto)
- ✅ UsuarioDTO incluye campos de dirección (direccion, ciudad, codigoPostal)
- ✅ Base de datos: 351 fallas con 100% cobertura GPS
