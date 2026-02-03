# 📱 FallApp Mobile - Aplicaciones Android

> **Versión**: 1.0  
> **Fecha**: 2026-02-01  
> **Estado**: 🔄 En desarrollo

---

## 📋 Descripción

Este módulo contiene las aplicaciones Android del proyecto FallApp:

1. **FallApp Admin** (`com.fallapp.admin`) - Aplicación de monitoreo y administración
2. **FallApp User** (`com.fallapp.user`) - Aplicación principal para usuarios

---

## 🚀 Quick Start

### Prerrequisitos

- Android Studio Ladybug (2024.x) o superior
- JDK 17+
- Kotlin 1.9+
- Dispositivo/Emulador Android API 24+ (Android 7.0+)

### Configuración

1. **Clonar y abrir el proyecto**
   ```bash
   cd 03.mobile
   # Abrir con Android Studio
   ```

2. **Sincronizar Gradle**
   - Android Studio sincronizará automáticamente las dependencias

3. **Configurar API URL** (si cambió)
   - Editar `app/src/main/java/com/fallapp/core/config/ApiConfig.kt`
   - Cambiar `BASE_URL` a la nueva IP

4. **Ejecutar**
   - Seleccionar configuración (admin o user)
   - Run en dispositivo/emulador

---

## 📂 Estructura del Proyecto

```
03.mobile/
├── docs/                              # 📚 Documentación
│   ├── 00.INDICE.md                  # Índice de documentación
│   ├── 00.ARQUITECTURA-MOBILE.md     # Arquitectura Clean Architecture
│   ├── 01.APP-ADMIN-SPEC.md          # Especificación App Admin
│   ├── 02.APP-USER-SPEC.md           # Especificación App User
│   ├── 03.PROMPT-GENERACION-IA.md    # Prompts para IA
│   └── 04.PLANTILLA-ERRORES.md       # Registro de errores
│
├── app/
│   ├── src/main/
│   │   ├── java/com/fallapp/
│   │   │   ├── core/                 # 🔧 Núcleo compartido
│   │   │   │   ├── config/           # Configuración (ApiConfig)
│   │   │   │   ├── di/               # Inyección de dependencias
│   │   │   │   ├── network/          # Cliente HTTP (Ktor)
│   │   │   │   ├── database/         # Room Database
│   │   │   │   └── util/             # Utilidades (Result, etc.)
│   │   │   │
│   │   │   ├── admin/                # 🛠️ App Admin
│   │   │   │   └── features/
│   │   │   │       ├── auth/
│   │   │   │       ├── health/
│   │   │   │       ├── apitests/
│   │   │   │       └── ...
│   │   │   │
│   │   │   └── user/                 # 📱 App User
│   │   │       └── features/
│   │   │           ├── auth/
│   │   │           ├── fallas/
│   │   │           ├── eventos/
│   │   │           ├── ninots/
│   │   │           └── ...
│   │   │
│   │   └── res/                      # Recursos Android
│   │
│   └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml            # Catálogo de versiones
│
└── build.gradle.kts
```

---

## 🏗️ Arquitectura

### Clean Architecture + MVI

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  • Jetpack Compose (UI)                                     │
│  • ViewModels (Estado)                                      │
│  • UiState / UiEvent / Effect                               │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│  • Use Cases (Lógica de negocio)                            │
│  • Domain Models (Entidades puras)                          │
│  • Repository Interfaces                                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
│  • Repository Implementations                                │
│  • Remote Data Source (Ktor)                                │
│  • Local Data Source (Room)                                 │
│  • Mappers (DTO ↔ Domain ↔ Entity)                          │
└─────────────────────────────────────────────────────────────┘
```

### Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Kotlin 1.9+ |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | Clean Architecture + MVI |
| Networking | Ktor Client |
| Base de datos local | Room |
| DI | Koin |
| Async | Coroutines + Flow |
| Mapas | Google Maps Compose |
| Imágenes | Coil |

---

## ⚙️ Configuración de la API

La URL de la API está centralizada en un único archivo:

```kotlin
// app/src/main/java/com/fallapp/core/config/ApiConfig.kt

object ApiConfig {
    /**
     * URL base de la API REST.
     * 
     * DESARROLLO: http://35.180.21.42:8080
     * PRODUCCIÓN: https://api.fallapp.es (futuro)
     */
    const val BASE_URL = "http://35.180.21.42:8080"
    const val API_PATH = "/api"
    const val API_URL = "$BASE_URL$API_PATH"
}
```

### Cambiar Servidor

1. Abre `ApiConfig.kt`
2. Modifica `BASE_URL`
3. Recompila la app

**Importante**: La app usa `android:usesCleartextTraffic="true"` para permitir HTTP en desarrollo.

---

## 🔐 Autenticación JWT

La app implementa autenticación JWT según la especificación del backend.

### Formato de Respuesta API

Todas las respuestas siguen el formato `ApiResponse<T>`:

```json
{
  "exito": true,
  "mensaje": "Operación exitosa",
  "datos": { ... },
  "timestamp": "2026-02-01T18:30:00"
}
```

### Endpoints de Auth

**Login**: `POST /api/auth/login`
```json
{
  "email": "usuario@example.com",
  "contrasena": "miPassword123"
}
```

**Registro**: `POST /api/auth/registro`
```json
{
  "email": "nuevo@example.com",
  "contrasena": "password123",
  "nombreCompleto": "María López García",
  "idFalla": 5
}
```

### Uso del Token

Para endpoints autenticados, el token se incluye automáticamente:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

El `TokenManager` gestiona la persistencia y expiración del token (24h).

---

## ¿La IP cambió?

1. Abre `ApiConfig.kt`
2. Cambia `BASE_URL` a la nueva IP
3. Rebuild el proyecto

---

## 🤖 Desarrollo con IA (Spec-Driven)

Este proyecto sigue la metodología **Spec-Driven Development** con asistencia de IA.

### Prompt Maestro para IA

Copia y pega este prompt al iniciar cada sesión de desarrollo:

```
# CONTEXTO DEL PROYECTO - FallApp Mobile

## Proyecto
FallApp es un sistema de gestión de fallas valencianas. 
Estoy desarrollando las aplicaciones móviles Android.

## Documentación clave (en orden de prioridad):
1. `03.mobile/docs/00.ARQUITECTURA-MOBILE.md` - Arquitectura Clean Architecture
2. `03.mobile/docs/01.APP-ADMIN-SPEC.md` - Spec de app admin
3. `03.mobile/docs/02.APP-USER-SPEC.md` - Spec de app usuario
4. `GUIA.API.FRONTEND.md` - Endpoints de la API REST

## Stack Tecnológico
- Lenguaje: Kotlin
- UI: Jetpack Compose + Material 3
- Arquitectura: Clean Architecture + MVI
- Networking: Ktor Client
- Base de datos local: Room
- DI: Koin
- Async: Coroutines + Flow

## Convenciones de Código
- Organización por features (cada feature tiene data/domain/presentation)
- ViewModels con StateFlow para estado
- Use Cases como clases invocables (operator fun invoke)
- Repository pattern con implementaciones offline-first
- Result wrapper para manejo de errores

## API Base URL
```kotlin
const val BASE_URL = "http://35.180.21.42:8080"
```

## Estructura de Respuesta API
```json
{
  "exito": true,
  "mensaje": "...",
  "datos": { ... },
  "timestamp": "2026-02-01T18:30:00"
}
```

Confirma que has entendido el contexto.
```

### Flujo de desarrollo

1. **Leer spec** → `docs/0X.APP-XXX-SPEC.md`
2. **Usar prompt apropiado** → `docs/03.PROMPT-GENERACION-IA.md`
3. **Generar código** → Domain → Data → Presentation
4. **Documentar errores** → `docs/04.PLANTILLA-ERRORES.md`

---

## 📱 Aplicaciones

### FallApp Admin

**Package**: `com.fallapp.admin`  
**Usuarios**: Solo ADMIN  

**Funcionalidades**:
- 🟢 Monitor de estado del servidor
- 🧪 Suite de tests API
- 📊 Métricas y estadísticas
- 👥 Gestión de usuarios
- ⚠️ Alertas del sistema

### FallApp User

**Package**: `com.fallapp.user`  
**Usuarios**: Todos  

**Funcionalidades**:
- 🗺️ Mapa interactivo de fallas
- 🔍 Búsqueda y filtros
- 🏆 Votación de ninots
- 📅 Calendario de eventos
- ⭐ Fallas favoritas
- 👤 Perfil de usuario

---

## 📚 Documentación

| Documento | Descripción |
|-----------|-------------|
| [Índice](docs/00.INDICE.md) | Índice de toda la documentación |
| [Arquitectura](docs/00.ARQUITECTURA-MOBILE.md) | Clean Architecture y estructura |
| [Spec Admin](docs/01.APP-ADMIN-SPEC.md) | Especificación app administrador |
| [Spec User](docs/02.APP-USER-SPEC.md) | Especificación app usuario |
| [Prompts IA](docs/03.PROMPT-GENERACION-IA.md) | Prompts para desarrollo con IA |
| [Errores](docs/04.PLANTILLA-ERRORES.md) | Registro de errores y soluciones |

---

## 🔗 Enlaces

- **API REST**: `http://35.180.21.42:8080`
- **Documentación API**: [GUIA.API.FRONTEND.md](../GUIA.API.FRONTEND.md)
- **Especificaciones**: [04.docs/especificaciones/](../04.docs/especificaciones/)

---

## ✅ Checklist de Desarrollo

### Nueva feature:
- [ ] Leer especificación en `docs/`
- [ ] Identificar endpoints en API
- [ ] Crear modelos de dominio
- [ ] Implementar repository (offline-first)
- [ ] Crear use cases
- [ ] Implementar UI (Screen + ViewModel)
- [ ] Documentar errores encontrados

### Antes de commit:
- [ ] Código compila sin errores
- [ ] Funcionalidad básica probada
- [ ] Errores documentados en `04.PLANTILLA-ERRORES.md`

---

> **Siguiente paso**: Lee la documentación en `docs/` y usa los prompts de `03.PROMPT-GENERACION-IA.md` para desarrollar.
