# ✅ Sistema de Login Completado

**Fecha:** 2026-02-03  
**Estado:** ✅ OPERATIVO  
**Versión:** 1.0.0

---

## 📋 Resumen

El sistema de autenticación JWT está completamente funcional tanto para **registro** como para **login**. Los usuarios pueden crear cuentas y autenticarse correctamente con la app móvil.

---

## 🔧 Problemas Resueltos

### 1. Error de Serialización - Timestamp Opcional (REGISTRO)

**Problema:**
```
Error al registrar: Illegal input: Field 'timestamp' is required for type ApiResponse, but it was missing
```

**Causa:**  
El campo `timestamp` en `ApiResponse<T>` era obligatorio (`String`), pero el backend no siempre lo incluye en las respuestas de error (especialmente en validaciones 400 Bad Request).

**Solución:**  
Cambiar el campo a opcional en `ApiResponse.kt`:

```kotlin
@Serializable
data class ApiResponse<T>(
    val exito: Boolean,
    val mensaje: String? = null,
    val datos: T? = null,
    val timestamp: String? = null  // ← Ahora opcional
)
```

**Archivo:** [ApiResponse.kt](../app/src/main/java/com/fallapp/core/network/ApiResponse.kt)

---

### 2. Error de Enum - Rol No Encontrado (LOGIN)

**Problema:**
```
Error al iniciar sesión: No enum constant com.fallapp.core.database.Rol.USUARIO
```

**Causa:**  
El backend devuelve `"rol": "usuario"` (minúsculas) pero:
1. El enum `Rol` no tenía el valor `USUARIO` (solo tenía `FALLERO`, `ADMIN`, `CASAL`)
2. El mapeo no era case-insensitive

**Solución:**

**A) Agregar USUARIO al enum Rol:**
```kotlin
enum class Rol {
    FALLERO,
    ADMIN,
    CASAL,
    USUARIO  // ← Añadido para usuarios regulares
}
```

**Archivo:** [Converters.kt](../app/src/main/java/com/fallapp/core/database/Converters.kt#L170)

**B) Hacer mapeo case-insensitive:**
```kotlin
rol = Rol.valueOf(user.rol.name.uppercase())  // ← Añadido .uppercase()
```

**Archivo:** [AuthRepositoryImpl.kt](../app/src/main/java/com/fallapp/features/auth/data/repository/AuthRepositoryImpl.kt#L62)

---

## 🏗️ Arquitectura de Autenticación

### Flujo de Registro

```
1. Usuario ingresa datos en RegisterScreen
2. RegisterViewModel valida datos localmente
3. RegisterUseCase ejecuta reglas de negocio
4. AuthRepository llama a AuthApiService
5. API devuelve token JWT + datos de usuario
6. TokenManager guarda token en SharedPreferences
7. Usuario se guarda en Room (base de datos local)
8. Navegación a HomeScreen
```

### Flujo de Login

```
1. Usuario ingresa email + contraseña en LoginScreen
2. LoginViewModel valida formato de email
3. LoginUseCase verifica contraseña (mínimo 6 caracteres)
4. AuthRepository llama a AuthApiService.login()
5. Backend valida con BCrypt (hashing unidireccional)
6. API devuelve token JWT (HS512, 24h duración)
7. TokenManager persiste token
8. Usuario se actualiza en Room con ultimoAcceso
9. Navegación a HomeScreen
```

### Componentes Clave

| Componente | Responsabilidad | Archivo |
|------------|----------------|---------|
| **LoginScreen** | UI Compose con Material 3 | LoginScreen.kt |
| **LoginViewModel** | Maneja estado de UI (StateFlow) | LoginViewModel.kt |
| **LoginUseCase** | Validación + lógica de negocio | LoginUseCase.kt |
| **AuthRepository** | Coordina API, TokenManager, BD | AuthRepositoryImpl.kt |
| **AuthApiService** | Llamadas HTTP con Ktor | AuthApiService.kt |
| **TokenManager** | Persistencia de JWT | TokenManager.kt |
| **ApiResponse<T>** | Wrapper genérico de respuestas | ApiResponse.kt |

---

## 🔐 Seguridad Implementada

### Encriptación de Contraseñas
- **Algoritmo:** BCrypt (hashing unidireccional)
- **Backend:** Encripta contraseñas automáticamente antes de guardar
- **No reversible:** No se pueden "desencriptar" (es hashing, no encriptación)

### JWT (JSON Web Tokens)
- **Algoritmo:** HS512 (HMAC con SHA-512)
- **Duración:** 24 horas (86400 segundos)
- **Formato:** Bearer Token
- **Almacenamiento:** SharedPreferences en Android
- **Transmisión:** Header `Authorization: Bearer {token}`

### Validaciones Cliente (App)
```kotlin
// Email válido
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Contraseña mínimo 6 caracteres
contrasena.length >= 6
```

---

## 📊 DTOs y Mapeos

### LoginRequestDto
```kotlin
@Serializable
data class LoginRequestDto(
    val email: String,
    val contrasena: String  // ← Backend usa "contrasena", no "password"
)
```

### LoginResponseDto
```kotlin
@Serializable
data class LoginResponseDto(
    val token: String,           // JWT token
    val tipo: String,            // "Bearer"
    val expiraEn: Long,          // 86400 (24 horas en segundos)
    val usuario: UsuarioDto
)
```

### UsuarioDto
```kotlin
@Serializable
data class UsuarioDto(
    val idUsuario: Long,
    val email: String,
    val nombreCompleto: String,
    val rol: String,              // "usuario", "admin", "casal"
    val idFalla: Long? = null,
    val nombreFalla: String? = null
)
```

### Mapeo a Dominio
```kotlin
fun UsuarioDto.toDomain(): User {
    val parts = nombreCompleto.split(" ", limit = 2)
    return User(
        idUsuario = idUsuario,
        email = email,
        nombre = parts.getOrNull(0) ?: "",
        apellidos = parts.getOrNull(1) ?: "",
        rol = UserRole.fromString(rol),  // Case-insensitive
        idFalla = idFalla
    )
}
```

---

## 🧪 Pruebas Realizadas

### Registro
- ✅ Usuario nuevo creado exitosamente
- ✅ Token JWT recibido y almacenado
- ✅ Usuario guardado en Room
- ✅ Navegación a Home después de registro

### Login
- ✅ Credenciales válidas → Login exitoso
- ✅ Token JWT persistido correctamente
- ✅ Campo `ultimoAcceso` actualizado en Room
- ✅ Rol "usuario" mapeado correctamente a `Rol.USUARIO`
- ✅ Navegación a Home después de login

### Errores Manejados
- ✅ Email inválido → Mensaje de error
- ✅ Contraseña muy corta → Mensaje de error
- ✅ Credenciales incorrectas → Mensaje del backend
- ✅ Sin conexión → Manejo de error de red

---

## 🌐 Endpoints Utilizados

### POST /api/auth/registro
**Base URL:** http://35.180.21.42:8080

**Request:**
```json
{
  "email": "usuario@example.com",
  "contrasena": "miPassword123",
  "nombreCompleto": "Juan Pérez García",
  "idFalla": 1
}
```

**Response (201 Created):**
```json
{
  "exito": true,
  "mensaje": "Usuario registrado exitosamente",
  "datos": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tipo": "Bearer",
    "expiraEn": 86400,
    "usuario": {
      "idUsuario": 42,
      "email": "usuario@example.com",
      "nombreCompleto": "Juan Pérez García",
      "rol": "usuario",
      "idFalla": 1,
      "nombreFalla": "Falla Convento Jerusalén"
    }
  }
}
```

### POST /api/auth/login
**Request:**
```json
{
  "email": "usuario@example.com",
  "contrasena": "miPassword123"
}
```

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Login exitoso",
  "datos": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tipo": "Bearer",
    "expiraEn": 86400,
    "usuario": {
      "idUsuario": 42,
      "email": "usuario@example.com",
      "nombreCompleto": "Juan Pérez García",
      "rol": "usuario",
      "idFalla": 1,
      "nombreFalla": "Falla Convento Jerusalén"
    }
  },
  "timestamp": "2026-02-03T15:30:45"
}
```

---

## 📦 Dependencias

```kotlin
// Ktor Client (HTTP)
implementation("io.ktor:ktor-client-android:2.3.7")
implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

// Kotlinx Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

// Koin DI
implementation("io.insert-koin:koin-android:3.5.3")
implementation("io.insert-koin:koin-androidx-compose:3.5.3")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

---

## 🎯 Próximos Pasos

- [ ] Implementar logout (limpiar token + usuario de Room)
- [ ] Agregar refresh token automático
- [ ] Implementar "Recordar sesión" (biometría)
- [ ] Manejo de sesión expirada (401 Unauthorized)
- [ ] Pantalla de perfil de usuario
- [ ] Cambio de contraseña

---

## 📚 Referencias

- [GUIA.API.FRONTEND.md](../../GUIA.API.FRONTEND.md) - Documentación completa de API
- [IMPLEMENTACION.AUTENTICACION.md](../IMPLEMENTACION.AUTENTICACION.md) - Guía de implementación
- [JWT.io](https://jwt.io/) - Estándar JWT
- [BCrypt](https://en.wikipedia.org/wiki/Bcrypt) - Algoritmo de hashing

---

**Autor:** Equipo FallApp  
**Última actualización:** 2026-02-03
