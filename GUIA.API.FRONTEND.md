# 🎭 Guía de API para Equipos Desktop y Mobile - FallApp

> **📱 Guía para Desarrolladores Frontend**  
> **Versión API:** 2.0  
> **Última actualización:** 2026-02-11  
> **Sistema de Votación:** v4.0 (Votos directos a FALLAS por categoría)  
> **URL Base:** http://35.180.21.42:8080/api  
> **Doc Técnica:** [04.API-REST.md](04.docs/especificaciones/04.API-REST.md)

---

> ⚠️ **Nota para Desarrolladores**: Esta guía está basada en la especificación oficial [04.API-REST.md](04.docs/especificaciones/04.API-REST.md). 
> Para detalles técnicos completos, consulta ese documento.

---

## 1. Resumen

API REST para FallApp que proporciona acceso a datos de fallas valencianas, gestión de usuarios, eventos, ninots y sistema de votación.

**Características**:
- Autenticación basada en JWT
- Control de acceso por roles (ADMIN, CASAL, USUARIO)
- Rutas públicas y protegidas
- Paginación en listados
- Filtros y búsqueda avanzada
- Documentación OpenAPI 3.0

**URL Base**: `http://35.180.21.42:8080/api`

---

## 2. Arquitectura General

### 2.1 Estructura de Capas

```
┌─────────────────────────────────────┐
│   CLIENTES (Electron + Android)     │
└──────────────┬──────────────────────┘
               │ HTTP/REST (JSON)
               ▼
┌─────────────────────────────────────┐
│  CONTROLADORES (Controllers)        │
│  - Validación de entrada            │
│  - Mapeo de rutas HTTP              │
│  - Respuestas HTTP                  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  SERVICIOS (Services)               │
│  - Lógica de negocio                │
│  - Validaciones complejas           │
│  - Transformación de datos          │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  REPOSITORIOS (Repositories)        │
│  - Acceso a base de datos (JPA)    │
│  - Consultas SQL                    │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  BASE DE DATOS (PostgreSQL)         │
└─────────────────────────────────────┘
```

### 2.2 Convenciones REST

| Método HTTP | Operación | Ejemplo |
|-------------|-----------|---------|
| `GET` | Leer/Consultar | `GET /api/fallas` → Listar fallas |
| `POST` | Crear | `POST /api/fallas` → Crear nueva falla |
| `PUT` | Actualizar completo | `PUT /api/fallas/1` → Actualizar falla #1 |
| `DELETE` | Eliminar | `DELETE /api/fallas/1` → Eliminar falla #1 |

### 2.3 Formatos de Respuesta

**Respuesta exitosa**:
```json
{
  "exito": true,
  "datos": { ... },
  "mensaje": "Operación completada con éxito"
}
```

**Respuesta con error**:
```json
{
  "exito": false,
  "error": {
    "codigo": "VALIDACION_FALLIDA",
    "mensaje": "El campo 'nombre' es obligatorio",
    "detalles": ["nombre: no puede estar vacío"]
  },
  "ruta": "/api/fallas",
  "timestamp": "2026-02-01T10:30:00Z"
}
```

---

## 3. Autenticación y Seguridad

### 3.1 JWT (JSON Web Token)

**Flujo de autenticación**:
1. Cliente envía credenciales a `/api/auth/iniciar-sesion`
2. Servidor valida y devuelve JWT
3. Cliente incluye JWT en header `Authorization: Bearer <token>`
4. Servidor valida token en cada petición protegida

**Estructura del token JWT**:
```json
{
  "sub": "usuario@ejemplo.com",
  "id_usuario": 123,
  "rol": "CASAL",
  "id_falla": 45,
  "iat": 1706780400,
  "exp": 1706866800
}
```

**Duración del token**: 24 horas

### 3.2 Headers Requeridos

| Header | Valor | Descripción |
|--------|-------|-------------|
| `Authorization` | `Bearer <token>` | Token JWT (rutas protegidas) |
| `Content-Type` | `application/json` | Formato de cuerpo de petición |
| `Accept` | `application/json` | Formato de respuesta esperado |

### 3.3 Roles y Permisos

| Rol | Código | Permisos |
|-----|--------|----------|
| **Administrador** | `ADMIN` | Acceso total, gestión de usuarios |
| **Casal** | `CASAL` | Gestión de su propia falla y eventos |
| **Usuario** | `USUARIO` | Visualización y votación |

---

## 4. Recursos y Rutas

### 4.1 Autenticación (`/api/auth`)

#### POST `/api/auth/iniciar-sesion`
Autenticación de usuario con email y contraseña.

**Público**: ✅ Sí

**Petición**:
```json
{
  "email": "usuario@ejemplo.com",
  "contrasena": "MiContraseña123!"
}
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tipo": "Bearer",
    "expiracion": "2026-02-02T10:30:00Z",
    "usuario": {
      "id": 123,
      "email": "usuario@ejemplo.com",
      "nombre_completo": "Juan Pérez",
      "rol": "CASAL",
      "id_falla": 45
    }
  }
}
```

**Errores**:
- `401 UNAUTHORIZED`: Credenciales incorrectas
- `403 FORBIDDEN`: Usuario desactivado

---

#### POST `/api/auth/registrar`
Registro de nuevo usuario.

**Público**: ✅ Sí

**Petición**:
```json
{
  "email": "nuevo@ejemplo.com",
  "contrasena": "Contraseña123!",
  "nombre_completo": "María García",
  "telefono": "+34612345678"
}
```

**Respuesta exitosa** (201):
```json
{
  "exito": true,
  "datos": {
    "id": 124,
    "email": "nuevo@ejemplo.com",
    "nombre_completo": "María García",
    "rol": "USUARIO",
    "activo": true,
    "fecha_registro": "2026-02-01T10:30:00Z"
  },
  "mensaje": "Usuario registrado con éxito"
}
```

**Validaciones**:
- Email único
- Contraseña mínimo 8 caracteres
- Nombre obligatorio

---

#### POST `/api/auth/renovar-token`
Renovar token JWT antes de expiración.

**Autenticación**: 🔒 Requerida

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiracion": "2026-02-03T10:30:00Z"
  }
}
```

---

#### POST `/api/auth/cerrar-sesion`
Cerrar sesión (invalida token actual).

**Autenticación**: 🔒 Requerida

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "mensaje": "Sesión cerrada correctamente"
}
```

---

#### GET `/api/auth/perfil`
Obtener información del usuario autenticado.

**Autenticación**: 🔒 Requerida

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id": 123,
    "email": "usuario@ejemplo.com",
    "nombre_completo": "Juan Pérez",
    "rol": "CASAL",
    "id_falla": 45,
    "nombre_falla": "Falla Plaza del Ayuntamiento",
    "activo": true,
    "fecha_registro": "2025-01-15T08:00:00Z",
    "ultimo_acceso": "2026-02-01T10:30:00Z"
  }
}
```

---

### 4.2 Usuarios (`/api/usuarios`)

#### GET `/api/usuarios`
Listar todos los usuarios (paginado).

**Autenticación**: 🔒 ADMIN

**Parámetros de consulta**:
- `pagina` (int, default: 0): Número de página
- `tamano` (int, default: 20): Elementos por página
- `rol` (string, opcional): Filtrar por rol (ADMIN, CASAL, USUARIO)
- `activo` (boolean, opcional): Filtrar por estado

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "contenido": [
      {
        "id": 1,
        "email": "admin@fallapp.com",
        "nombre_completo": "Administrador FallApp",
        "rol": "ADMIN",
        "activo": true,
        "fecha_registro": "2025-01-01T00:00:00Z"
      },
      {
        "id": 2,
        "email": "casal@falla.com",
        "nombre_completo": "Responsable Falla",
        "rol": "CASAL",
        "id_falla": 10,
        "activo": true,
        "fecha_registro": "2025-01-10T12:00:00Z"
      }
    ],
    "pagina_actual": 0,
    "tamano_pagina": 20,
    "total_elementos": 150,
    "total_paginas": 8
  }
}
```

---

#### GET `/api/usuarios/{id}`
Obtener usuario por ID.

**Autenticación**: 🔒 ADMIN o propio usuario

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id": 123,
    "email": "usuario@ejemplo.com",
    "nombre_completo": "Juan Pérez",
    "telefono": "+34612345678",
    "rol": "CASAL",
    "id_falla": 45,
    "activo": true,
    "fecha_registro": "2025-06-15T10:00:00Z",
    "ultimo_acceso": "2026-02-01T09:45:00Z"
  }
}
```

**Errores**:
- `404 NOT_FOUND`: Usuario no existe
- `403 FORBIDDEN`: Sin permisos para ver este usuario

---

#### PUT `/api/usuarios/{id}`
Actualizar información de usuario.

**Autenticación**: 🔒 ADMIN o propio usuario

**Petición**:
```json
{
  "nombre_completo": "Juan Pérez García",
  "telefono": "+34612345679",
  "email": "nuevo_email@ejemplo.com"
}
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id": 123,
    "email": "nuevo_email@ejemplo.com",
    "nombre_completo": "Juan Pérez García",
    "telefono": "+34612345679",
    "rol": "CASAL",
    "activo": true,
    "fecha_actualizacion": "2026-02-01T10:35:00Z"
  },
  "mensaje": "Usuario actualizado correctamente"
}
```

**Restricciones**:
- No se puede cambiar el rol (solo ADMIN puede hacerlo con ruta específica)
- Email debe ser único

---

#### PUT `/api/usuarios/{id}/foto`
Actualizar foto de perfil del usuario.

**Autenticación**: 🔒 ADMIN o propio usuario

**Content-Type**: `multipart/form-data`

**Campos del formulario**:
- `foto` (file, requerido): Imagen de perfil (`image/jpeg`, `image/png`, etc.). Tamaño máximo recomendado: **2 MB**.

**Comportamiento**:
- La imagen se almacena en la base de datos en un campo binario (`BYTEA`).
- Se guarda también el `content_type` original para devolver la cabecera HTTP correcta.

**Ejemplo (curl)**:
```bash
curl -X PUT http://localhost:8080/api/usuarios/123/foto \
  -H "Authorization: Bearer TOKEN_USUARIO" \
  -H "Content-Type: multipart/form-data" \
  -F "foto=@/ruta/local/foto_perfil.jpg"
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "mensaje": "Foto de perfil actualizada",
  "datos": null
}
```

**Errores**:
- `400 BAD_REQUEST`: Imagen vacía o tamaño superior a 2 MB
- `404 NOT_FOUND`: Usuario no existe
- `403 FORBIDDEN`: Sin permisos para actualizar este usuario

---

#### GET `/api/usuarios/{id}/foto`
Obtener la foto de perfil de un usuario.

**Autenticación**: 🔒 ADMIN o propio usuario (recomendado, según política de privacidad)

**Respuesta exitosa** (200):
- Cuerpo: bytes de la imagen en bruto.
- Cabecera `Content-Type`: `image/jpeg`, `image/png`, etc. según el tipo almacenado.

**Ejemplo (curl)**:
```bash
curl -X GET http://localhost:8080/api/usuarios/123/foto \
  -H "Authorization: Bearer TOKEN_USUARIO" \
  -o foto_perfil_123.jpg
```

**Errores**:
- `404 NOT_FOUND`: Usuario no existe o no tiene foto almacenada
- `403 FORBIDDEN`: Sin permisos para ver esta imagen

---

#### DELETE `/api/usuarios/{id}`
Eliminar usuario (borrado lógico: `activo = false`).

**Autenticación**: 🔒 ADMIN

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "mensaje": "Usuario eliminado correctamente"
}
```

**Errores**:
- `404 NOT_FOUND`: Usuario no existe
- `409 CONFLICT`: No se puede eliminar usuario con datos relacionados

---

#### PUT `/api/usuarios/{id}/estado`
Activar o desactivar usuario.

**Autenticación**: 🔒 ADMIN

**Petición**:
```json
{
  "activo": false
}
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id": 123,
    "activo": false
  },
  "mensaje": "Estado de usuario actualizado"
}
```

---

#### GET `/api/usuarios/{id}/votos`
Obtener votos realizados por un usuario.

**Autenticación**: 🔒 ADMIN o propio usuario

**Parámetros de consulta**:
- `pagina` (int, default: 0)
- `tamano` (int, default: 20)

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "contenido": [
      {
        "id": 501,
        "id_ninot": 34,
        "nombre_ninot": "El Caloret",
        "id_falla": 12,
        "nombre_falla": "Falla Convento Jerusalén",
        "tipo_voto": "favorito",
        "fecha_voto": "2026-01-28T15:30:00Z"
      }
    ],
    "pagina_actual": 0,
    "total_elementos": 5
  }
}
```

---

### 4.3 Fallas (`/api/fallas`)

#### GET `/api/fallas`
Listar todas las fallas con filtros opcionales.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `pagina` (int, default: 0)
- `tamano` (int, default: 50)
- `seccion` (string, opcional): Filtrar por sección (ej: "1A", "7C")
- `categoria` (string, opcional): Filtrar por categoría (especial, primera, segunda, etc.)
- `activas` (boolean, default: true): Solo fallas activas
- `ordenar_por` (string, default: "nombre"): nombre, anyo_fundacion, seccion

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "contenido": [
      {
        "id_falla": 1,
        "nombre": "Falla Plaza del Ayuntamiento",
        "seccion": "1A",
        "presidente": "Juan García López",
        "anyo_fundacion": 1942,
        "categoria": "especial",
        "ubicacion": {
          "latitud": 39.4699,
          "longitud": -0.3763,
          "direccion": "Plaza del Ayuntamiento, Valencia"
        },
        "estadisticas": {
          "total_eventos": 12,
          "total_ninots": 3,
          "total_votos": 1250
        }
      }
    ],
    "pagina_actual": 0,
    "tamano_pagina": 50,
    "total_elementos": 347,
    "total_paginas": 7
  }
}
```

---

#### GET `/api/fallas/{id}`
Obtener información detallada de una falla.

**Público**: ✅ Sí

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_falla": 1,
    "nombre": "Falla Plaza del Ayuntamiento",
    "seccion": "1A",
    "presidente": "Juan García López",
    "artista": "Miguel Santaeulalia",
    "lema": "La Valencia eterna",
    "anyo_fundacion": 1942,
    "categoria": "especial",
    "distintivo": "brillants",
    "ubicacion": {
      "latitud": 39.4699,
      "longitud": -0.3763,
      "direccion": "Plaza del Ayuntamiento, Valencia"
    },
    "descripcion": "Falla histórica ubicada en el corazón de Valencia...",
    "contacto": {
      "web_oficial": "https://fallaplazaayuntamiento.com",
      "email": "info@fallaplaza.com",
      "telefono": "+34963123456"
    },
    "estadisticas": {
      "total_eventos": 12,
      "total_ninots": 3,
      "total_votos": 1250,
      "proximos_eventos": 2
    },
    "fecha_ultima_actualizacion": "2026-01-25T14:30:00Z"
  }
}
```

**Errores**:
- `404 NOT_FOUND`: Falla no existe

---

#### POST `/api/fallas`
Crear nueva falla.

**Autenticación**: 🔒 ADMIN

**Petición**:
```json
{
  "nombre": "Falla Nueva Campanar",
  "seccion": "8C",
  "presidente": "María Sánchez",
  "artista": "José Martínez",
  "lema": "Valencia al món",
  "anyo_fundacion": 2024,
  "categoria": "tercera",
  "ubicacion": {
    "latitud": 39.4850,
    "longitud": -0.3950,
    "direccion": "Calle Campanar, 45"
  },
  "descripcion": "Nueva falla en el barrio de Campanar",
  "contacto": {
    "email": "contacto@fallacampanar.com",
    "telefono": "+34963555666"
  }
}
```

**Respuesta exitosa** (201):
```json
{
  "exito": true,
  "datos": {
    "id_falla": 348,
    "nombre": "Falla Nueva Campanar",
    "seccion": "8C",
    "fecha_creacion": "2026-02-01T11:00:00Z"
  },
  "mensaje": "Falla creada correctamente"
}
```

**Validaciones**:
- Nombre único
- Sección válida
- Coordenadas válidas (lat: -90 a 90, lon: -180 a 180)
- Año fundación <= año actual

---

#### PUT `/api/fallas/{id}`
Actualizar información de falla.

**Autenticación**: 🔒 ADMIN o CASAL (propia falla)

**Petición**:
```json
{
  "nombre": "Falla Plaza del Ayuntamiento - Actualizada",
  "presidente": "Nuevo Presidente",
  "descripcion": "Descripción actualizada...",
  "contacto": {
    "web_oficial": "https://nueva-web.com",
    "telefono": "+34963999888"
  }
}
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_falla": 1,
    "nombre": "Falla Plaza del Ayuntamiento - Actualizada",
    "fecha_actualizacion": "2026-02-01T11:15:00Z"
  },
  "mensaje": "Falla actualizada correctamente"
}
```

**Restricciones CASAL**:
- Solo puede actualizar su propia falla
- No puede cambiar: nombre, sección, categoría, año fundación

---

#### DELETE `/api/fallas/{id}`
Eliminar falla (borrado lógico).

**Autenticación**: 🔒 ADMIN

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "mensaje": "Falla eliminada correctamente"
}
```

**Errores**:
- `409 CONFLICT`: Falla tiene eventos o ninots asociados

---

#### GET `/api/fallas/buscar`
Búsqueda de fallas por texto.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `texto` (string, requerido): Texto a buscar
- `campos` (string[], opcional): Campos donde buscar (nombre, lema, artista, presidente)
- `pagina` (int, default: 0)
- `tamano` (int, default: 20)

**Ejemplo**: `/api/fallas/buscar?texto=ayuntamiento&campos=nombre,lema`

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "contenido": [
      {
        "id_falla": 1,
        "nombre": "Falla Plaza del Ayuntamiento",
        "seccion": "1A",
        "coincidencias": {
          "campo": "nombre",
          "fragmento": "...Plaza del Ayuntamiento..."
        }
      }
    ],
    "total_elementos": 3
  }
}
```

---

#### GET `/api/fallas/cercanas`
Fallas cercanas a una ubicación.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `latitud` (decimal, requerido): Latitud del punto
- `longitud` (decimal, requerido): Longitud del punto
- `radio_km` (decimal, default: 1.0): Radio de búsqueda en kilómetros
- `limite` (int, default: 10): Máximo de resultados

**Ejemplo**: `/api/fallas/cercanas?latitud=39.4699&longitud=-0.3763&radio_km=2.0`

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": [
    {
      "id_falla": 1,
      "nombre": "Falla Plaza del Ayuntamiento",
      "distancia_km": 0.15,
      "ubicacion": {
        "latitud": 39.4699,
        "longitud": -0.3763
      }
    },
    {
      "id_falla": 5,
      "nombre": "Falla Convento Jerusalén",
      "distancia_km": 0.82,
      "ubicacion": {
        "latitud": 39.4756,
        "longitud": -0.3650
      }
    }
  ]
}
```

---

#### GET `/api/fallas/{id}/eventos`
Obtener eventos de una falla.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `tipo` (string, opcional): Filtrar por tipo (planta, crema, ofrenda)
- `desde_fecha` (date, opcional): Eventos desde fecha
- `hasta_fecha` (date, opcional): Eventos hasta fecha

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": [
    {
      "id_evento": 101,
      "tipo": "planta",
      "nombre": "Plantà Falla Grande",
      "descripcion": "Plantà de la falla grande 2026",
      "fecha_evento": "2026-03-15T18:00:00Z",
      "ubicacion": "Plaza del Ayuntamiento"
    },
    {
      "id_evento": 102,
      "tipo": "crema",
      "nombre": "Cremà",
      "fecha_evento": "2026-03-19T01:00:00Z"
    }
  ]
}
```

---

#### GET `/api/fallas/{id}/ninots`
Obtener ninots de una falla.

**Público**: ✅ Sí

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": [
    {
      "id_ninot": 34,
      "nombre_ninot": "El Caloret",
      "titulo_obra": "Valencia bajo el sol",
      "altura_metros": 15.5,
      "artista_constructor": "Miguel Santaeulalia",
      "url_imagen_principal": "https://cdn.fallapp.com/ninots/34-principal.jpg",
      "premiado": false,
      "estadisticas": {
        "total_votos": 450,
        "puntuacion_media": 4.5
      }
    }
  ]
}
```

---

#### GET `/api/fallas/{id}/estadisticas`
Estadísticas de una falla.

**Público**: ✅ Sí

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_falla": 1,
    "nombre": "Falla Plaza del Ayuntamiento",
    "estadisticas": {
      "eventos": {
        "total": 12,
        "por_tipo": {
          "planta": 2,
          "crema": 1,
          "ofrenda": 1,
          "concierto": 5,
          "exposicion": 3
        },
        "proximos": 2
      },
      "ninots": {
        "total": 3,
        "premiados": 1,
        "total_votos": 1250,
        "puntuacion_media": 4.2
      },
      "usuarios": {
        "responsables": 1,
        "seguidores": 320
      }
    }
  }
}
```

---

### 4.4 Eventos (`/api/eventos`)

#### GET `/api/eventos`
Listar eventos con filtros.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `pagina` (int, default: 0)
- `tamano` (int, default: 20)
- `id_falla` (int, opcional): Filtrar por falla
- `tipo` (string, opcional): Filtrar por tipo
- `desde_fecha` (date, opcional): Desde fecha
- `hasta_fecha` (date, opcional): Hasta fecha
- `ordenar_por` (string, default: "fecha_evento"): fecha_evento, nombre

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "contenido": [
      {
        "id_evento": 101,
        "tipo": "planta",
        "nombre": "Plantà Falla Grande",
        "id_falla": 1,
        "nombre_falla": "Falla Plaza del Ayuntamiento",
        "fecha_evento": "2026-03-15T18:00:00Z",
        "ubicacion": "Plaza del Ayuntamiento",
        "participantes_estimado": 5000
      }
    ],
    "pagina_actual": 0,
    "total_elementos": 450
  }
}
```

---

#### GET `/api/eventos/{id}`
Obtener evento por ID.

**Público**: ✅ Sí

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_evento": 101,
    "tipo": "planta",
    "nombre": "Plantà Falla Grande",
    "descripcion": "Plantación del monumento fallero grande",
    "id_falla": 1,
    "nombre_falla": "Falla Plaza del Ayuntamiento",
    "fecha_evento": "2026-03-15T18:00:00Z",
    "ubicacion": "Plaza del Ayuntamiento",
    "direccion": "Plaza del Ayuntamiento, s/n",
    "url_imagen": "https://cdn.fallapp.com/eventos/101.jpg",
    "participantes_estimado": 5000,
    "creado_por": {
      "id": 2,
      "nombre_completo": "Responsable Falla"
    },
    "fecha_creacion": "2026-01-15T10:00:00Z"
  }
}
```

---

#### POST `/api/eventos`
Crear nuevo evento.

**Autenticación**: 🔒 ADMIN o CASAL (propia falla)

**Petición**:
```json
{
  "id_falla": 1,
  "tipo": "concierto",
  "nombre": "Concierto de Música Festera",
  "descripcion": "Concierto de bandas de música...",
  "fecha_evento": "2026-03-10T20:00:00Z",
  "ubicacion": "Casal de la falla",
  "direccion": "Calle Mayor, 45",
  "participantes_estimado": 200,
  "url_imagen": "https://ejemplo.com/imagen.jpg"
}
```

**Respuesta exitosa** (201):
```json
{
  "exito": true,
  "datos": {
    "id_evento": 501,
    "nombre": "Concierto de Música Festera",
    "fecha_evento": "2026-03-10T20:00:00Z",
    "fecha_creacion": "2026-02-01T11:30:00Z"
  },
  "mensaje": "Evento creado correctamente"
}
```

**Validaciones**:
- fecha_evento debe ser futura
- id_falla debe existir
- CASAL solo puede crear eventos de su propia falla

---

#### PUT `/api/eventos/{id}`
Actualizar evento.

**Autenticación**: 🔒 ADMIN o CASAL (propia falla)

**Petición**:
```json
{
  "nombre": "Concierto de Música Festera - ACTUALIZADO",
  "fecha_evento": "2026-03-10T21:00:00Z",
  "participantes_estimado": 250
}
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_evento": 501,
    "nombre": "Concierto de Música Festera - ACTUALIZADO",
    "fecha_actualizacion": "2026-02-01T11:45:00Z"
  },
  "mensaje": "Evento actualizado correctamente"
}
```

---

#### PUT `/api/eventos/{id}/imagen`
Actualizar imagen principal de un evento.

**Autenticación**: 🔒 ADMIN o CASAL (propia falla)

**Content-Type**: `multipart/form-data`

**Campos del formulario**:
- `imagen` (file, requerido): Imagen principal del evento (`image/jpeg`, `image/png`, etc.). Tamaño máximo recomendado: **5 MB**.

**Comportamiento**:
- La imagen se almacena como binario en la tabla `eventos.imagen` (tipo `BYTEA`).
- Se guarda el `content_type` en `eventos.imagen_content_type`.
- El campo `url_imagen` se mantiene para compatibilidad con clientes que usen URLs externas.

**Ejemplo (curl)**:
```bash
curl -X PUT http://localhost:8080/api/eventos/501/imagen \
  -H "Authorization: Bearer TOKEN_CASAL" \
  -H "Content-Type: multipart/form-data" \
  -F "imagen=@/ruta/local/evento_501.jpg"
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "mensaje": "Imagen del evento actualizada correctamente",
  "datos": null
}
```

**Errores**:
- `400 BAD_REQUEST` / `422 UNPROCESSABLE_ENTITY`: Imagen vacía o tamaño superior a 5 MB
- `404 NOT_FOUND`: Evento no existe
- `403 FORBIDDEN`: Sin permisos para modificar este evento

---

#### GET `/api/eventos/{id}/imagen`
Obtener imagen principal de un evento.

**Público**: ✅ Sí (o restringido según política de visibilidad que se configure)

**Respuesta exitosa** (200):
- Cuerpo: bytes de la imagen almacenada.
- Cabecera `Content-Type`: `image/jpeg`, `image/png`, etc.

**Ejemplo (curl)**:
```bash
curl -X GET http://localhost:8080/api/eventos/501/imagen -o evento_501.jpg
```

**Errores**:
- `404 NOT_FOUND`: Evento no existe o no tiene imagen almacenada

---

#### DELETE `/api/eventos/{id}`
Eliminar evento.

**Autenticación**: 🔒 ADMIN o CASAL (propia falla)

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "mensaje": "Evento eliminado correctamente"
}
```

---

#### GET `/api/eventos/proximos`
Eventos próximos (próximos 30 días).

**Público**: ✅ Sí

**Parámetros de consulta**:
- `dias` (int, default: 30): Días hacia el futuro
- `limite` (int, default: 50): Máximo de resultados

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": [
    {
      "id_evento": 101,
      "nombre": "Plantà Falla Grande",
      "tipo": "planta",
      "id_falla": 1,
      "nombre_falla": "Falla Plaza del Ayuntamiento",
      "fecha_evento": "2026-03-15T18:00:00Z",
      "dias_restantes": 42
    }
  ]
}
```

---

#### GET `/api/eventos/tipo/{tipo}`
Eventos filtrados por tipo.

**Público**: ✅ Sí

**Tipos válidos**: `planta`, `crema`, `ofrenda`, `concierto`, `exposicion`, `infantil`, `cena`, `teatro`

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": [
    {
      "id_evento": 101,
      "nombre": "Plantà Falla Grande",
      "tipo": "planta",
      "fecha_evento": "2026-03-15T18:00:00Z"
    }
  ]
}
```

---

### 4.5 Ninots (`/api/ninots`)

#### GET `/api/ninots`
Listar ninots con filtros.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `pagina` (int, default: 0)
- `tamano` (int, default: 20)
- `id_falla` (int, opcional): Filtrar por falla
- `premiado` (boolean, opcional): Solo ninots premiados
- `ordenar_por` (string, default: "nombre_ninot"): nombre_ninot, altura_metros, total_votos

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "contenido": [
      {
        "id_ninot": 34,
        "nombre_ninot": "El Caloret",
        "titulo_obra": "Valencia bajo el sol",
        "id_falla": 1,
        "nombre_falla": "Falla Plaza del Ayuntamiento",
        "altura_metros": 15.5,
        "artista_constructor": "Miguel Santaeulalia",
        "url_imagen_principal": "https://cdn.fallapp.com/ninots/34.jpg",
        "premiado": false,
        "estadisticas": {
          "total_votos": 450,
          "puntuacion_media": 4.5
        }
      }
    ],
    "pagina_actual": 0,
    "total_elementos": 120
  }
}
```

---

#### GET `/api/ninots/{id}`
Obtener ninot por ID.

**Público**: ✅ Sí

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_ninot": 34,
    "nombre_ninot": "El Caloret",
    "titulo_obra": "Valencia bajo el sol",
    "descripcion": "Representación del calor valenciano...",
    "id_falla": 1,
    "nombre_falla": "Falla Plaza del Ayuntamiento",
    "dimensiones": {
      "altura_metros": 15.5,
      "ancho_metros": 8.2
    },
    "material_principal": "Madera y cartón",
    "artista_constructor": "Miguel Santaeulalia",
    "anyo_construccion": 2026,
    "imagenes": {
      "principal": "https://cdn.fallapp.com/ninots/34-principal.jpg",
      "adicionales": [
        "https://cdn.fallapp.com/ninots/34-lateral1.jpg",
        "https://cdn.fallapp.com/ninots/34-lateral2.jpg"
      ]
    },
    "premiado": false,
    "categoria_premio": null,
    "estadisticas": {
      "total_votos": 450,
      "puntuacion_media": 4.5,
      "votos_por_tipo": {
        "favorito": 320,
        "ingenioso": 90,
        "critico": 40
      }
    },
    "fecha_creacion": "2026-01-10T12:00:00Z"
  }
}
```

---

#### POST `/api/ninots`
Crear nuevo ninot.

**Autenticación**: 🔒 ADMIN o CASAL (propia falla)

**Petición**:
```json
{
  "id_falla": 1,
  "nombre_ninot": "La Fallera Mayor",
  "titulo_obra": "Tradición valenciana",
  "descripcion": "Homenaje a la fallera mayor...",
  "altura_metros": 12.5,
  "ancho_metros": 6.0,
  "material_principal": "Madera y poliestireno",
  "artista_constructor": "José Martínez",
  "anyo_construccion": 2026,
  "url_imagen_principal": "https://ejemplo.com/ninot.jpg"
}
```

**Respuesta exitosa** (201):
```json
{
  "exito": true,
  "datos": {
    "id_ninot": 121,
    "nombre_ninot": "La Fallera Mayor",
    "id_falla": 1,
    "fecha_creacion": "2026-02-01T12:00:00Z"
  },
  "mensaje": "Ninot creado correctamente"
}
```

**Validaciones**:
- id_falla debe existir
- altura_metros y ancho_metros > 0
- CASAL solo puede crear ninots de su propia falla

---

#### PUT `/api/ninots/{id}`
Actualizar ninot.

**Autenticación**: 🔒 ADMIN o CASAL (propia falla)

**Petición**:
```json
{
  "descripcion": "Descripción actualizada...",
  "url_imagen_principal": "https://nueva-imagen.jpg"
}
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_ninot": 121,
    "fecha_actualizacion": "2026-02-01T12:15:00Z"
  },
  "mensaje": "Ninot actualizado correctamente"
}
```

---

#### DELETE `/api/ninots/{id}`
Eliminar ninot.

**Autenticación**: 🔒 ADMIN o CASAL (propia falla)

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "mensaje": "Ninot eliminado correctamente"
}
```

**Errores**:
- `409 CONFLICT`: Ninot tiene votos asociados (no se puede eliminar)

---

#### GET `/api/ninots/{id}/votos`
Obtener votos de un ninot.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `pagina` (int, default: 0)
- `tamano` (int, default: 50)
- `tipo_voto` (string, opcional): Filtrar por tipo

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "contenido": [
      {
        "id_voto": 1001,
        "id_usuario": 123,
        "nombre_usuario": "Juan Pérez",
        "tipo_voto": "favorito",
        "fecha_voto": "2026-01-28T15:30:00Z"
      }
    ],
    "total_votos": 450,
    "votos_por_tipo": {
      "favorito": 320,
      "ingenioso": 90,
      "critico": 40
    }
  }
}
```

---

#### GET `/api/ninots/clasificacion`
Clasificación de ninots por votos.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `tipo_voto` (string, opcional): Filtrar por tipo de voto
- `limite` (int, default: 10): Máximo de resultados

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": [
    {
      "posicion": 1,
      "id_ninot": 34,
      "nombre_ninot": "El Caloret",
      "id_falla": 1,
      "nombre_falla": "Falla Plaza del Ayuntamiento",
      "total_votos": 1250,
      "puntuacion_media": 4.8
    },
    {
      "posicion": 2,
      "id_ninot": 56,
      "nombre_ninot": "La Paella Gigant",
      "id_falla": 5,
      "nombre_falla": "Falla Convento Jerusalén",
      "total_votos": 980,
      "puntuacion_media": 4.5
    }
  ]
}
```

---

### 4.6 Votos (`/api/votos`)

> **📖 Documentación Completa**: Ver [05.SISTEMA-VOTACION.md](05.SISTEMA-VOTACION.md) para especificación detallada del sistema de votación.

**Modelo Actual (v4.0)**: Votos directos a **fallas** por categoría de concurso fallero.

#### POST `/api/votos`
Registrar voto a una falla en una categoría específica.

**Autenticación**: 🔒 Requerida (JWT)

**Petición**:
```json
{
  "idFalla": 15,
  "tipoVoto": "EXPERIMENTAL"
}
```

**Tipos de voto válidos (v4.0)**:
- `EXPERIMENTAL`: Categoría Experimental (fallas innovadoras y vanguardistas)
- `INGENIO_Y_GRACIA`: Categoría Ingenio y Gracia (sátira e ingenio valenciano)
- `MONUMENTO`: Categoría Monumento (excelencia artística y técnica)

**Respuesta exitosa** (201):
```json
{
  "success": true,
  "data": {
    "idVoto": 342,
    "idUsuario": 5,
    "nombreUsuario": "Juan García",
    "idFalla": 15,
    "nombreFalla": "Falla Plaza del Ayuntamiento",
    "tipoVoto": "EXPERIMENTAL",
    "valor": 1,
    "fechaCreacion": "2026-02-10T10:45:30Z"
  },
  "message": "Voto registrado exitosamente",
  "timestamp": "2026-02-10T10:45:30Z"
}
```

**Validaciones**:
- ✅ JWT válido en header `Authorization: Bearer {token}`
- ✅ `idFalla` debe existir en tabla `fallas`
- ✅ `tipoVoto` debe ser uno de los 3 valores válidos
- ✅ No debe existir voto previo (usuario + falla + tipo) - Garantizado por constraint DB

**Errores**:
- `409 CONFLICT`: Ya has votado esta falla en esta categoría
- `404 NOT_FOUND`: Falla no existe
- `400 BAD_REQUEST`: Tipo de voto inválido
- `401 UNAUTHORIZED`: Token JWT inválido o ausente

---

#### GET `/api/votos/usuario/{idUsuario}`
Obtener votos de un usuario específico.

**Autenticación**: 🔒 Requerida (JWT)

**Control de Acceso**:
- Usuario solo puede ver sus propios votos
- ADMIN puede ver votos de cualquier usuario

**Respuesta exitosa** (200):
```json
{
  "success": true,
  "data": [
    {
      "idVoto": 342,
      "idFalla": 15,
      "nombreFalla": "Falla Plaza del Ayuntamiento",
      "seccion": "Especial",
      "tipoVoto": "EXPERIMENTAL",
      "valor": 1,
      "fechaCreacion": "2026-02-10T10:45:30Z"
    },
    {
      "idVoto": 343,
      "idFalla": 15,
      "nombreFalla": "Falla Plaza del Ayuntamiento",
      "seccion": "Especial",
      "tipoVoto": "INGENIO_Y_GRACIA",
      "valor": 1,
      "fechaCreacion": "2026-02-10T11:20:15Z"
    }
  ],
  "timestamp": "2026-02-10T15:00:00Z"
}
```

**Errores**:
- `403 FORBIDDEN`: No tienes permisos para ver votos de este usuario
- `404 NOT_FOUND`: Usuario no existe

---

#### GET `/api/votos/falla/{idFalla}`
Obtener todos los votos de una falla.

**Público**: ✅ Sí (no requiere autenticación)

**Query Parameters**:
- `tipo` (opcional): Filtrar por tipo (`EXPERIMENTAL`, `INGENIO_Y_GRACIA`, `MONUMENTO`)

**Ejemplos**:
- `GET /api/votos/falla/15` - Todos los votos
- `GET /api/votos/falla/15?tipo=EXPERIMENTAL` - Solo votos EXPERIMENTAL

**Respuesta exitosa** (200):
```json
{
  "success": true,
  "data": {
    "idFalla": 15,
    "nombreFalla": "Falla Plaza del Ayuntamiento",
    "estadisticas": {
      "totalVotos": 428,
      "votosExperimental": 156,
      "votosIngenioYGracia": 98,
      "votosMonumento": 174
    },
    "votos": [
      {
        "idVoto": 342,
        "tipoVoto": "EXPERIMENTAL",
        "fechaCreacion": "2026-02-10T10:45:30Z"
      }
    ]
  },
  "timestamp": "2026-02-10T15:00:00Z"
}
```

---

#### DELETE `/api/votos/{idVoto}`
Eliminar voto propio.

**Autenticación**: 🔒 Requerida (JWT)

**Control de Acceso**:
- Usuario solo puede eliminar sus propios votos
- ADMIN puede eliminar cualquier voto

**Respuesta exitosa** (200):
```json
{
  "success": true,
  "message": "Voto eliminado exitosamente",
  "timestamp": "2026-02-10T11:30:00Z"
}
```

**Errores**:
- `403 FORBIDDEN`: No tienes permisos para eliminar este voto
- `404 NOT_FOUND`: Voto no existe

---

#### GET `/api/estadisticas/votos`
Estadísticas y rankings de votación.

**Público**: ✅ Sí (no requiere autenticación)

**Respuesta exitosa** (200):
```json
{
  "success": true,
  "data": {
    "topExperimental": [
      {
        "posicion": 1,
        "idFalla": 15,
        "nombreFalla": "Falla Plaza del Ayuntamiento",
        "seccion": "Especial",
        "totalVotos": 156
      }
    ],
    "topIngenioYGracia": [
      {
        "posicion": 1,
        "idFalla": 45,
        "nombreFalla": "Falla Na Jordana",
        "seccion": "Segunda A",
        "totalVotos": 189
      }
    ],
    "topMonumento": [
      {
        "posicion": 1,
        "idFalla": 8,
        "nombreFalla": "Falla Sueca-Literato Azorín",
        "seccion": "Especial",
        "totalVotos": 234
      }
    ],
    "estadisticasGenerales": {
      "totalVotos": 12456,
      "usuariosActivos": 1834,
      "fallasVotadas": 312,
      "promedioVotosPorUsuario": 6.8
    }
  },
  "timestamp": "2026-02-10T15:00:00Z"
}
```

**Nota**: Ver [05.SISTEMA-VOTACION.md](05.SISTEMA-VOTACION.md) para:
- Lógica de negocio completa
- Flujos de usuario
- Casos de prueba
- Implementación móvil/desktop
- Métricas y KPIs

---

### 4.7 Comentarios (`/api/comentarios`)

#### GET `/api/comentarios`
Listar comentarios con filtros.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `pagina` (int, default: 0)
- `tamano` (int, default: 20)
- `id_falla` (int, opcional): Comentarios de una falla
- `id_ninot` (int, opcional): Comentarios de un ninot
- `ordenar_por` (string, default: "fecha_desc"): fecha_desc, fecha_asc

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "contenido": [
      {
        "id_comentario": 501,
        "contenido": "¡Impresionante el trabajo del artista!",
        "id_usuario": 123,
        "nombre_usuario": "Juan Pérez",
        "id_ninot": 34,
        "nombre_ninot": "El Caloret",
        "fecha_creacion": "2026-01-28T16:00:00Z",
        "fecha_actualizacion": null
      }
    ],
    "total_elementos": 250
  }
}
```

---

#### GET `/api/comentarios/{id}`
Obtener comentario por ID.

**Público**: ✅ Sí

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_comentario": 501,
    "contenido": "¡Impresionante el trabajo del artista!",
    "id_usuario": 123,
    "nombre_usuario": "Juan Pérez",
    "id_ninot": 34,
    "nombre_ninot": "El Caloret",
    "id_falla": 1,
    "nombre_falla": "Falla Plaza del Ayuntamiento",
    "fecha_creacion": "2026-01-28T16:00:00Z",
    "fecha_actualizacion": null
  }
}
```

---

#### POST `/api/comentarios`
Crear comentario.

**Autenticación**: 🔒 Requerida

**Petición**:
```json
{
  "contenido": "¡Impresionante el trabajo del artista!",
  "id_ninot": 34,
  "id_falla": null
}
```

**Nota**: Se puede comentar un ninot O una falla (no ambos).

**Respuesta exitosa** (201):
```json
{
  "exito": true,
  "datos": {
    "id_comentario": 501,
    "contenido": "¡Impresionante el trabajo del artista!",
    "fecha_creacion": "2026-02-01T13:00:00Z"
  },
  "mensaje": "Comentario creado correctamente"
}
```

**Validaciones**:
- contenido mínimo 3 caracteres, máximo 500
- Debe especificar id_ninot O id_falla

---

#### PUT `/api/comentarios/{id}`
Editar comentario propio.

**Autenticación**: 🔒 Requerida (propio comentario)

**Petición**:
```json
{
  "contenido": "¡Impresionante! Actualizado con más detalles..."
}
```

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "id_comentario": 501,
    "contenido": "¡Impresionante! Actualizado con más detalles...",
    "fecha_actualizacion": "2026-02-01T13:15:00Z"
  },
  "mensaje": "Comentario actualizado correctamente"
}
```

**Errores**:
- `403 FORBIDDEN`: Solo puedes editar tus propios comentarios

---

#### DELETE `/api/comentarios/{id}`
Eliminar comentario.

**Autenticación**: 🔒 ADMIN o propio comentario

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "mensaje": "Comentario eliminado correctamente"
}
```

---

### 4.8 Estadísticas (`/api/estadisticas`)

#### GET `/api/estadisticas/resumen`
Resumen general del sistema.

**Público**: ✅ Sí

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "fallas": {
      "total": 347,
      "activas": 340,
      "por_categoria": {
        "especial": 15,
        "primera": 45,
        "segunda": 80,
        "tercera": 120,
        "sin_categoria": 87
      }
    },
    "eventos": {
      "total": 1250,
      "proximos_30_dias": 85
    },
    "ninots": {
      "total": 890,
      "premiados": 45
    },
    "votos": {
      "total": 12500,
      "usuarios_activos": 3200
    },
    "usuarios": {
      "total": 5600,
      "activos": 5200,
      "por_rol": {
        "ADMIN": 5,
        "CASAL": 340,
        "USUARIO": 4855
      }
    }
  }
}
```

---

#### GET `/api/estadisticas/clasificacion-fallas`
Clasificación de fallas más votadas.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `limite` (int, default: 10)
- `tipo_voto` (string, opcional): Filtrar por tipo de voto

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": [
    {
      "posicion": 1,
      "id_falla": 1,
      "nombre": "Falla Plaza del Ayuntamiento",
      "seccion": "1A",
      "total_votos": 3500,
      "total_ninots": 3,
      "total_eventos": 12
    },
    {
      "posicion": 2,
      "id_falla": 5,
      "nombre": "Falla Convento Jerusalén",
      "seccion": "2B",
      "total_votos": 2800,
      "total_ninots": 2,
      "total_eventos": 8
    }
  ]
}
```

---

#### GET `/api/estadisticas/actividad-reciente`
Últimos eventos y votos del sistema.

**Público**: ✅ Sí

**Parámetros de consulta**:
- `limite` (int, default: 20)

**Respuesta exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "ultimos_eventos": [
      {
        "id_evento": 450,
        "nombre": "Concierto de Música Festera",
        "id_falla": 12,
        "nombre_falla": "Falla Convento",
        "fecha_creacion": "2026-02-01T10:00:00Z"
      }
    ],
    "ultimos_votos": [
      {
        "id_voto": 12501,
        "id_ninot": 67,
        "nombre_ninot": "El Toro de Fuego",
        "tipo_voto": "favorito",
        "fecha_voto": "2026-02-01T13:25:00Z"
      }
    ],
    "ultimos_comentarios": [
      {
        "id_comentario": 890,
        "contenido": "¡Qué artista más bueno!",
        "id_ninot": 34,
        "fecha_creacion": "2026-02-01T13:20:00Z"
      }
    ]
  }
}
```

---

## 5. Paginación

Todas las rutas que devuelven listas utilizan **paginación estándar**:

### Parámetros de consulta:
- `pagina` (int, default: 0): Número de página (comienza en 0)
- `tamano` (int, default: 20): Elementos por página
- `ordenar_por` (string, opcional): Campo de ordenación
- `direccion` (string, opcional): `ASC` o `DESC`

### Estructura de respuesta paginada:
```json
{
  "exito": true,
  "datos": {
    "contenido": [ /* array de elementos */ ],
    "pagina_actual": 0,
    "tamano_pagina": 20,
    "total_elementos": 347,
    "total_paginas": 18,
    "primera_pagina": true,
    "ultima_pagina": false
  }
}
```

---

## 6. Códigos de Estado HTTP

| Código | Significado | Uso |
|--------|-------------|-----|
| `200 OK` | Éxito | GET, PUT, DELETE exitosos |
| `201 CREATED` | Creado | POST exitoso |
| `204 NO CONTENT` | Sin contenido | DELETE exitoso (sin respuesta) |
| `400 BAD REQUEST` | Petición incorrecta | Validación fallida |
| `401 UNAUTHORIZED` | No autenticado | Token JWT ausente o inválido |
| `403 FORBIDDEN` | Sin permisos | Usuario no tiene permisos |
| `404 NOT FOUND` | No encontrado | Recurso no existe |
| `409 CONFLICT` | Conflicto | Duplicado o restricción violada |
| `422 UNPROCESSABLE ENTITY` | Entidad no procesable | Error de lógica de negocio |
| `500 INTERNAL SERVER ERROR` | Error del servidor | Error inesperado |

---

## 7. Códigos de Error Personalizados

| Código | Descripción |
|--------|-------------|
| `VALIDACION_FALLIDA` | Error de validación de campos |
| `CREDENCIALES_INVALIDAS` | Email o contraseña incorrectos |
| `TOKEN_INVALIDO` | Token JWT inválido o expirado |
| `TOKEN_EXPIRADO` | Token JWT expirado |
| `SIN_PERMISOS` | Usuario sin permisos para esta operación |
| `RECURSO_NO_ENCONTRADO` | Recurso no existe |
| `DUPLICADO` | Recurso duplicado (email, nombre, etc.) |
| `RESTRICCION_VIOLADA` | Restricción de integridad violada |
| `VOTO_DUPLICADO` | Usuario ya votó este ninot |
| `FALLA_CON_DEPENDENCIAS` | No se puede eliminar falla con eventos/ninots |

---

## 8. Filtros y Búsqueda

### 8.1 Operadores de Filtro

Los filtros se aplican mediante parámetros de consulta:

**Ejemplos**:
- `/api/fallas?seccion=1A` → Fallas de sección 1A
- `/api/eventos?tipo=planta&desde_fecha=2026-03-01` → Eventos de tipo plantà desde marzo
- `/api/usuarios?rol=CASAL&activo=true` → Usuarios CASAL activos

### 8.2 Búsqueda Full-Text

Ruta: `/api/fallas/buscar?texto=ayuntamiento`

Busca en campos: nombre, lema, artista, presidente usando índice GIN de PostgreSQL.

---

## 9. Límites y Cuotas

| Límite | Valor |
|--------|-------|
| Tamaño máximo de página | 100 elementos |
| Peticiones por minuto | 60 (sin autenticación), 300 (autenticado) |
| Tamaño máximo de cuerpo de petición | 10 MB |
| Tamaño máximo de comentario | 500 caracteres |
| Imágenes por ninot | 1 principal + 5 adicionales |

---

## 10. Seguridad

### 10.1 CORS (Cross-Origin Resource Sharing)

**Orígenes permitidos**:
- `http://localhost:3000` (desarrollo frontend)
- `http://localhost:5173` (Vite dev server)
- `https://fallapp.com` (producción)

**Métodos permitidos**: GET, POST, PUT, DELETE, OPTIONS

### 10.2 Validación de Entrada

- **Todos los campos** se validan en el servidor
- **SQL Injection**: Protegido mediante JPA/Hibernate
- **XSS**: Sanitización automática de campos de texto

### 10.3 Rate Limiting

- Usuarios no autenticados: **60 peticiones/minuto**
- Usuarios autenticados: **300 peticiones/minuto**
- ADMIN: sin límite

---

## 11. Versionado de API

**Versión actual**: `1.0`

**URL con versión** (opcional): `/api/v1/fallas`

**Header de versión**:
```
X-API-Version: 1.0
```

**Política de cambios**:
- **Cambios compatibles**: Sin incremento de versión (nuevos campos opcionales)
- **Cambios incompatibles**: Nueva versión (v2, v3, etc.)

---

## 12. Documentación OpenAPI

**URL Swagger UI**: `http://localhost:8080/swagger-ui.html`

**URL OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

**Características**:
- Prueba de endpoints interactiva
- Esquemas de petición/respuesta
- Autenticación JWT integrada

---

## 13. Ejemplos de Uso

### 13.1 Flujo completo de autenticación y votación

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8080/api/auth/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@ejemplo.com",
    "contrasena": "MiContraseña123!",
    "nombre_completo": "Juan Pérez"
  }'

# 2. Iniciar sesión
curl -X POST http://localhost:8080/api/auth/iniciar-sesion \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@ejemplo.com",
    "contrasena": "MiContraseña123!"
  }'

# Respuesta:
# { "datos": { "token": "eyJhbGci..." } }

# 3. Votar un ninot
curl -X POST http://localhost:8080/api/votos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGci..." \
  -d '{
    "id_ninot": 34,
    "tipo_voto": "favorito"
  }'

# 4. Ver mis votos
curl -X GET http://localhost:8080/api/votos/mis-votos \
  -H "Authorization: Bearer eyJhbGci..."
```

### 13.2 Búsqueda de fallas cercanas

```bash
# Buscar fallas en un radio de 2 km
curl -X GET "http://localhost:8080/api/fallas/cercanas?latitud=39.4699&longitud=-0.3763&radio_km=2.0"
```

### 13.3 Crear evento (CASAL)

```bash
curl -X POST http://localhost:8080/api/eventos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN_CASAL" \
  -d '{
    "id_falla": 1,
    "tipo": "concierto",
    "nombre": "Concierto de Bandas",
    "fecha_evento": "2026-03-10T20:00:00Z",
    "ubicacion": "Casal de la falla"
  }'
```

---

## 14. Testing de la API

### 14.1 Herramientas Recomendadas

- **Postman**: Colección de pruebas
- **Insomnia**: Cliente REST alternativo
- **curl**: Pruebas desde terminal
- **HTTPie**: Cliente HTTP moderno

### 14.2 Colección Postman

Importar colección desde: `/docs/postman/FallApp.postman_collection.json`

**Variables de entorno**:
- `BASE_URL`: `http://localhost:8080`
- `TOKEN_ADMIN`: `eyJhbGci...` (obtenido tras login)
- `TOKEN_CASAL`: `eyJhbGci...`
- `TOKEN_USUARIO`: `eyJhbGci...`

---

## 15. Próximas Características (Roadmap)

### Versión 1.1 (Marzo 2026)
- [ ] Carga de imágenes (multipart/form-data)
- [ ] Notificaciones push
- [ ] Exportación de datos (CSV, PDF)

### Versión 1.2 (Abril 2026)
- [ ] Geolocalización avanzada (PostGIS)
- [ ] Chat en tiempo real (WebSockets)
- [ ] Sistema de mensajería entre usuarios

### Versión 2.0 (Mayo 2026)
- [ ] GraphQL API
- [ ] Sistema de caché avanzado (Redis)
- [ ] Búsqueda con Elasticsearch

---

## 16. Contacto y Soporte

**Documentación adicional**:
- Guía de desarrollo: `/04.docs/01.GUIA-PROGRAMACION.md`
- ADRs: `/04.docs/arquitectura/`
- Base de datos: `/04.docs/especificaciones/03.BASE-DATOS.md`

**Issues y bugs**: GitHub Issues

**Email de soporte**: soporte@fallapp.com

---

**Última actualización**: 2026-02-01  
**Versión del documento**: 1.0  
**Estado**: ✅ Aprobada para implementación
