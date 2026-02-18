# 👤 Feature Profile - Información de Usuario

Componente para mostrar la información completa del perfil del usuario autenticado obtenida desde el endpoint `/api/usuarios/{id}`.

## 📋 Descripción

La feature Profile permite que los usuarios vean su información de perfil completa en la aplicación móvil.

### Datos mostrados

| Campo | Tipo | Editable | Visible |
|-------|------|----------|---------|
| Email | String | ❌ | ✅ |
| Nombre Completo | String | ❌ | ✅ |
| Rol | String | ❌ | ✅ |
| Teléfono | String? | ❌ | ✅ (si existe) |
| Dirección | String? | ❌ | ✅ (si existe) |
| Ciudad | String? | ❌ | ✅ (si existe) |
| Código Postal | String? | ❌ | ✅ (si existe) |
| Fecha de Creación | String | ❌ | ✅ (no editable) |

### Campos EXCLUIDOS (según especificación)

Estos campos del endpoint NO se muestran en la interfaz:
- `idUsuario` - ID del usuario
- `idFalla` - ID de la falla asociada
- `nombreFalla` - Nombre de la falla asociada
- `activo` - Estado del usuario
- `fechaActualizacion` - Fecha de última actualización

## 🏗️ Arquitectura

La feature sigue **Clean Architecture** con capas bien definidas:

```
presentation/
  ├── ProfileScreen.kt          # UI en Jetpack Compose
  └── ProfileViewModel.kt       # Gestión de estado

domain/
  ├── model/UsuarioPerfil.kt   # Modelo de dominio
  ├── repository/               # Interface del repositorio
  └── usecase/                  # Use cases

data/
  ├── remote/
  │   ├── ProfileApiService.kt  # Llamadas HTTP
  │   └── dto/UsuarioPerfilDto.kt # DTO de respuesta API
  ├── mapper/Mappers.kt         # Conversiones DTO → Domain
  └── repository/               # Implementación del repositorio

di/ProfileModule.kt             # Inyección de dependencias
```

## 🔌 Inyección de Dependencias

Todos los componentes se inyectan automáticamente usando **Koin**:

```kotlin
val profileModule = module {
    single { ProfileApiService(httpClient = get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(profileApiService = get()) }
    factory { GetUserProfileUseCase(profileRepository = get()) }
    viewModel { ProfileViewModel(getUserProfileUseCase = get(), tokenManager = get()) }
}
```

El módulo se registra en `FallAppApplication` en la inicialización de Koin.

## 🔄 Flujo de datos

```
ProfileScreen (UI)
    ↓
ProfileViewModel (Estado)
    ↓ (llama)
GetUserProfileUseCase
    ↓ (llama)
ProfileRepository (interface)
    ↓ (implementa)
ProfileRepositoryImpl
    ↓ (llama)
ProfileApiService
    ↓ (petición HTTP)
GET /api/usuarios/{userId}
```

## 🎨 Interfaz de Usuario

### Estados de la pantalla

1. **Loading**: Muestra un indicador de carga mientras se obtienen los datos
2. **Success**: Muestra toda la información del perfil en una tarjeta
3. **Error**: Muestra mensaje de error con botón para reintentar

### Componentes principales

- **Avatar circular**: Icono de usuario como placeholder
- **Tarjeta de información**: Muestra todos los campos del perfil
- **Botón Actualizar**: Recarga el perfil desde la API
- **Botón Cerrar Sesión**: Cierra la sesión y vuelve al login

## 🔑 Clave: TokenManager actualizado

Para que funcione la feature, **TokenManager** se actualizó para almacenar el ID del usuario:

```kotlin
// Guardar durante login
tokenManager.saveToken(token, email, userId)

// Recuperar en ProfileViewModel
val userId = tokenManager.getUserId()
```

## 📡 Endpoint de API

```
GET /api/usuarios/{id}
Authorization: Bearer {token}
```

### Respuesta esperada

```json
{
  "exito": true,
  "mensaje": "Usuario recuperado",
  "datos": {
    "idUsuario": 5,
    "email": "demo@fallapp.es",
    "nombreCompleto": "Usuario Demostración",
    "rol": "usuario",
    "idFalla": null,
    "nombreFalla": null,
    "activo": true,
    "telefono": null,
    "direccion": null,
    "ciudad": null,
    "codigoPostal": null,
    "fechaCreacion": "2026-02-11T08:33:43.917722",
    "fechaActualizacion": "2026-02-11T08:35:37.412343"
  },
  "timestamp": "2026-02-01T18:30:00"
}
```

## 🐛 Errores conocidos

Ninguno por el momento.

## 📝 Notas de implementación

- Los campos de fecha se formatean a "dd/MM/yyyy HH:mm" para mejor legibilidad
- La pantalla es responsive y se adapta a diferentes tamaños de pantalla
- Los campos opcionales (teléfono, dirección, etc.) solo se muestran si tienen valor
- El emoji antes de cada etiqueta ayuda a identificar visualmente cada campo

## 🚀 Próximas mejoras potenciales

- [ ] Permitir editar información del perfil
- [ ] Agregar avatar personalizado (upload de foto)
- [ ] Mostrar historial de actividad
- [ ] Opciones de privacidad/notificaciones

