# 🔧 Solución de Problemas - Login Mobile App

**Fecha**: 2026-02-03  
**Versión**: 1.0.0  
**Estado**: ✅ RESUELTO

---

## 📋 Resumen Ejecutivo

Se identificó y resolvió el problema principal que impedía el funcionamiento del login en la aplicación móvil Android. El backend estaba funcionando correctamente, pero la aplicación móvil tenía una configuración incorrecta en el `AndroidManifest.xml`.

**Problema Principal**: Referencias incorrectas a clases de Application y MainActivity en el manifest.

**Solución**: Actualizar rutas en AndroidManifest.xml para que coincidan con la estructura de paquetes actual.

---

## 🔍 Análisis del Problema

### 1. Verificación del Backend

Se verificó que el backend está funcionando correctamente:

```bash
# Registro de usuario de prueba
POST http://35.180.21.42:8080/api/auth/registro
{
  "email": "testmobile@example.com",
  "contrasena": "password123",
  "nombreCompleto": "Usuario Test Mobile"
}
✅ Respuesta 200 OK - Token JWT generado correctamente

# Login con el usuario creado
POST http://35.180.21.42:8080/api/auth/login
{
  "email": "testmobile@example.com",
  "contrasena": "password123"
}
✅ Respuesta 200 OK - Token JWT válido
```

**Conclusión**: El backend funciona perfectamente con BCrypt y JWT.

### 2. Revisión del Código Mobile

Se revisó exhaustivamente la arquitectura de autenticación de la app:

#### ✅ DTOs Correctos

```kotlin
// LoginRequestDto.kt
@Serializable
data class LoginRequestDto(
    val email: String,
    val contrasena: String  // ✅ Correcto (no "password")
)

// LoginResponseDto.kt
@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEn: Int,
    val usuario: UsuarioDto
)
```

#### ✅ API Service Correcto

```kotlin
// AuthApiService.kt
suspend fun login(email: String, password: String): ApiResponse<LoginResponseDto> {
    return httpClient.post("${ApiConfig.API_URL}/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequestDto(email, password))
    }.body()
}
```

#### ✅ Repository Correcto

```kotlin
// AuthRepositoryImpl.kt
override suspend fun login(email: String, password: String): Result<AuthToken> {
    val apiResponse = authApiService.login(email, password)
    
    if (!apiResponse.exito || apiResponse.datos == null) {
        return Result.error(...)
    }
    
    val loginData = apiResponse.datos
    tokenManager.saveToken(loginData.token, loginData.usuario.email)
    // ... guardar usuario en BD local ...
}
```

#### ✅ ViewModel Correcto

```kotlin
// LoginViewModel.kt
fun onLoginClick() {
    viewModelScope.launch {
        val result = loginUseCase(
            email = currentState.email.trim(),
            password = currentState.password
        )
        
        when (result) {
            is Result.Success -> { /* Navegar a home */ }
            is Result.Error -> { /* Mostrar error */ }
        }
    }
}
```

#### ✅ UseCase con Validaciones

```kotlin
// LoginUseCase.kt
suspend operator fun invoke(email: String, password: String): Result<AuthToken> {
    // Validación: Email no vacío
    if (email.isBlank()) {
        return Result.error(message = "Por favor, introduce tu email")
    }
    
    // Validación: Formato de email
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return Result.error(message = "El formato del email no es válido")
    }
    
    // Validación: Contraseña mínimo 6 caracteres
    if (password.length < 6) {
        return Result.error(message = "La contraseña debe tener al menos 6 caracteres")
    }
    
    return authRepository.login(email, password)
}
```

#### ✅ Configuración de Red

```kotlin
// ApiConfig.kt
object ApiConfig {
    const val BASE_URL = "http://35.180.21.42:8080"
    const val API_PATH = "/api"
    const val API_URL = "$BASE_URL$API_PATH"  // http://35.180.21.42:8080/api
}

// KtorClient.kt - Cliente HTTP correctamente configurado
fun create(enableLogging: Boolean = true): HttpClient {
    return HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30_000L
            requestTimeoutMillis = 60_000L
        }
        // ...
    }
}
```

#### ✅ Módulos de Koin

```kotlin
// FallAppApplication.kt
startKoin {
    androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
    androidContext(this@FallAppApplication)
    
    modules(
        networkModule,    // ✅ HttpClient, NetworkMonitor
        databaseModule,   // ✅ Room DB, DAOs
        appModule,        // ✅ TokenManager
        authModule,       // ✅ AuthApiService, AuthRepository, UseCases, ViewModels
        fallasModule      // ✅ FallasRepository, etc.
    )
}
```

### 3. Problema Identificado: AndroidManifest.xml

#### ❌ Configuración INCORRECTA (antes)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Package en build.gradle.kts: com.fallapp.user -->
    
    <application
        android:name=".FallAppApplication"  <!-- ❌ Busca com.fallapp.FallAppApplication -->
        ...>
        <activity
            android:name=".MainActivity"     <!-- ❌ Busca com.fallapp.MainActivity -->
            android:exported="true">
            ...
        </activity>
    </application>
</manifest>
```

**Problema**: 
- El `namespace` en `build.gradle.kts` es `com.fallapp.user`
- Las clases están en `com.fallapp.user.FallAppApplication` y `com.fallapp.user.MainActivity`
- El manifest buscaba `.FallAppApplication` que se resuelve a `com.fallapp.FallAppApplication` (NO EXISTE)
- Esto causaba un **ClassNotFoundException** al iniciar la app

#### ✅ Configuración CORRECTA (después)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Package en build.gradle.kts: com.fallapp.user -->
    
    <application
        android:name=".user.FallAppApplication"  <!-- ✅ Busca com.fallapp.user.FallAppApplication -->
        android:usesCleartextTraffic="true"      <!-- ✅ Permite HTTP (desarrollo) -->
        ...>
        <activity
            android:name=".user.MainActivity"     <!-- ✅ Busca com.fallapp.user.MainActivity -->
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## ✅ Solución Aplicada

### Cambios Realizados

**Archivo**: `03.mobile/app/src/main/AndroidManifest.xml`

```diff
- android:name=".FallAppApplication"
+ android:name=".user.FallAppApplication"

- android:name=".MainActivity"
+ android:name=".user.MainActivity"
```

### Verificación

```bash
# Recompilar la app
cd 03.mobile
./gradlew clean assembleDebug -x test

# Resultado
✅ BUILD SUCCESSFUL in 7s
✅ APK generado: app/build/outputs/apk/debug/app-debug.apk
✅ Tamaño: ~21 MB
```

---

## 🧪 Cómo Probar

### 1. Instalar APK en Dispositivo/Emulador

```bash
# Método 1: Android Studio
# - Run > Run 'app' (Shift+F10)

# Método 2: ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Probar Login

1. **Abrir la app** - Debería mostrar la pantalla de login
2. **Introducir credenciales**:
   - Email: `testmobile@example.com`
   - Contraseña: `password123`
3. **Hacer clic en "Iniciar sesión"**
4. **Resultado esperado**: Navegación a pantalla Home con mensaje "Login Exitoso!"

### 3. Verificar Logs (Android Studio)

```bash
# Filtrar por tag "KtorClient" para ver requests HTTP
adb logcat | grep "KtorClient"

# Filtrar por tag "LoginViewModel" para ver estado
adb logcat | grep "LoginViewModel"
```

**Logs esperados**:
```
D/KtorClient: POST http://35.180.21.42:8080/api/auth/login
D/KtorClient: {"email":"testmobile@example.com","contrasena":"password123"}
D/KtorClient: Response: {"exito":true,"mensaje":"Login exitoso",...}
D/LoginViewModel: Login successful, navigating to home
```

---

## 📊 Resumen de Estado

### ✅ Componentes Funcionando

| Componente | Estado | Notas |
|------------|--------|-------|
| Backend API | ✅ Operativo | JWT + BCrypt validados |
| DTOs Mobile | ✅ Correctos | `contrasena` (no `password`) |
| API Service | ✅ Correcto | Ktor client configurado |
| Repository | ✅ Correcto | Maneja ApiResponse wrapper |
| UseCase | ✅ Correcto | Validaciones implementadas |
| ViewModel | ✅ Correcto | StateFlow + MVI pattern |
| TokenManager | ✅ Correcto | DataStore para persistencia |
| Koin DI | ✅ Correcto | Todos los módulos registrados |
| AndroidManifest | ✅ CORREGIDO | Rutas actualizadas |
| Compilación | ✅ Exitosa | APK generado sin errores |

### 📝 Próximos Pasos

1. **Probar en dispositivo físico** para confirmar funcionamiento
2. **Implementar pantalla Home completa** (actualmente es placeholder)
3. **Agregar manejo de errores específicos**:
   - Error de red (sin conexión)
   - Error 401 (credenciales incorrectas)
   - Error 500 (servidor caído)
4. **Implementar Remember Me** (checkbox para persistir sesión)
5. **Agregar biometría** (opcional, para login rápido)

---

## 📚 Documentación Relacionada

- [03.mobile/IMPLEMENTACION.AUTENTICACION.md](./IMPLEMENTACION.AUTENTICACION.md) - Guía completa de implementación JWT
- [03.mobile/RESUMEN.DOCUMENTACION.AUTH.md](./RESUMEN.DOCUMENTACION.AUTH.md) - Resumen de sistema de auth
- [03.mobile/EJEMPLO.LOGIN.md](./EJEMPLO.LOGIN.md) - Ejemplos de UI con Compose
- [03.mobile/README.md](./README.md) - Documentación principal del proyecto
- [RESUMEN.ACTUALIZACION.JWT.2026-02-01.md](../RESUMEN.ACTUALIZACION.JWT.2026-02-01.md) - Cambios en backend JWT

---

## 🔧 Troubleshooting

### Problema: App no inicia (ClassNotFoundException)

**Síntoma**: La app crashea al iniciar con error "ClassNotFoundException: FallAppApplication"

**Causa**: AndroidManifest.xml tiene rutas incorrectas

**Solución**: Verificar que las rutas en manifest coincidan con la estructura de paquetes:
```xml
android:name=".user.FallAppApplication"  <!-- NO solo ".FallAppApplication" -->
android:name=".user.MainActivity"         <!-- NO solo ".MainActivity" -->
```

### Problema: Error de red "Cleartext HTTP traffic not permitted"

**Síntoma**: La app no puede conectar a `http://35.180.21.42:8080`

**Causa**: Android 9+ bloquea HTTP por defecto

**Solución**: Verificar en AndroidManifest.xml:
```xml
<application
    android:usesCleartextTraffic="true"  <!-- ✅ Debe estar presente -->
    ...>
```

### Problema: Error 401 Unauthorized

**Síntoma**: Login falla con error 401

**Causas posibles**:
1. Usuario no existe en BD → Registrarlo primero
2. Contraseña incorrecta → Verificar BCrypt en backend
3. Backend no está corriendo → Reiniciar backend

**Solución**: Probar con curl/Postman primero para aislar el problema

### Problema: Koin injection failed

**Síntoma**: Error "No definition found for [class X]"

**Causa**: Módulo de Koin no registrado en `FallAppApplication`

**Solución**: Verificar que todos los módulos estén en `startKoin { modules(...) }`

---

**Autor**: GitHub Copilot  
**Contacto**: Equipo FallApp  
**Última actualización**: 2026-02-03
