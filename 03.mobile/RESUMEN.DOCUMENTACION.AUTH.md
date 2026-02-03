# 📱 Resumen: Documentación de Autenticación Móvil Android

**Fecha Creación:** 2026-02-03  
**Última Actualización:** 2026-02-03 (Backend validado)  
**Versión:** 0.5.3  
**Objetivo:** Documentar implementación de JWT en aplicación móvil Android

> ✅ **ACTUALIZADO 2026-02-03**: Backend JWT y BCrypt completamente funcional. Sistema validado en producción.

---

## 🎯 Objetivo

Proporcionar documentación completa para que cualquier desarrollador Android pueda implementar autenticación JWT conectándose a la API Spring Boot de FallApp usando **solo email y contraseña**.

### Estado del Sistema (2026-02-03)
- ✅ Backend API operativo en http://35.180.21.42:8080
- ✅ JWT funcional (algoritmo HS512, 24h)
- ✅ BCrypt validado (backend recompilado y reiniciado)
- ✅ Endpoints /auth/registro y /auth/login operativos
- ✅ Tests exitosos: registro + login

---

## 📚 Documentos Creados

### 1. **README.md** (Guía Principal)
**Ubicación:** `03.mobile/README.md`

**Contenido:**
- Visión general de la aplicación
- Arquitectura de autenticación (diagramas)
- Componentes principales (ViewModel, Repository, etc.)
- Flujo de autenticación completo
- Gestión de tokens (TokenManager)
- Interceptor HTTP para agregar JWT automáticamente
- Troubleshooting de errores comunes
- Links a documentación detallada

**Características Clave:**
- ✅ Explicación de JWT y cómo funciona (HS512, 24h)
- ✅ Niveles de acceso (Público, Autenticado, Admin)
- ✅ Almacenamiento seguro con EncryptedSharedPreferences
- ✅ Renovación y expiración de tokens (24h)
- ✅ URLs para emulador vs producción
- ✅ Backend validado 2026-02-03

---

### 2. **IMPLEMENTACION.AUTENTICACION.md** (Código Completo)
**Ubicación:** `03.mobile/IMPLEMENTACION.AUTENTICACION.md`

**Contenido:**
- **Paso 1:** Dependencias en `build.gradle.kts` (Retrofit, OkHttp, Coroutines)
- **Paso 2:** Estructura de paquetes recomendada
- **Paso 3:** Modelos de datos (LoginRequest, LoginResponse, User, etc.)
- **Paso 4:** Utilidades (Resource sealed class, Constants)
- **Paso 5:** TokenManager con EncryptedSharedPreferences
- **Paso 6:** AuthInterceptor para agregar token a requests
- **Paso 7:** ApiService (Retrofit interface) con endpoints
- **Paso 8:** RetrofitClient configurado con interceptores
- **Paso 9:** AuthRepository con login/registro
- **Paso 10:** AuthViewModel con StateFlow
- **Paso 11:** FallApp (Application class)
- **Checklist de verificación**

**Código Completo Incluido:**
```kotlin
// TokenManager con AES256_GCM
class TokenManager(context: Context) {
    fun saveToken(token: String, expiresIn: Long)
    fun getToken(): String?
    fun isTokenValid(): Boolean
    fun clearToken()
}

// AuthInterceptor
class AuthInterceptor(tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Chain): Response {
        // Agregar "Authorization: Bearer TOKEN"
    }
}

// AuthRepository
suspend fun login(email: String, password: String): Resource<LoginResponse>
suspend fun register(...): Resource<LoginResponse>

// AuthViewModel
fun login(email: String, password: String)
val loginState: StateFlow<Resource<LoginResponse>?>
```

---

### 3. **EJEMPLO.LOGIN.md** (Pantallas UI)
**Ubicación:** `03.mobile/EJEMPLO.LOGIN.md`

**Contenido:**
- **LoginScreen completa** con Jetpack Compose
- **RegisterScreen completa** con validaciones
- **HomeScreen** básica
- **NavGraph** con navegación entre pantallas
- **MainActivity** con verificación de sesión

**Características UI:**
- ✅ Material 3 Design
- ✅ Validación de formularios en tiempo real
- ✅ Manejo de estados (Loading, Success, Error)
- ✅ Password visibility toggle
- ✅ Navegación con Navigation Compose
- ✅ Auto-login si token válido
- ✅ Logout funcional

**Código de LoginScreen:**
```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    
    // UI con OutlinedTextField, Button, CircularProgressIndicator
    // Validaciones, manejo de errores, navegación
}
```

---

## 🔐 Flujo de Autenticación Documentado

### 1. Usuario ingresa email/contraseña
```
LoginScreen → AuthViewModel.login() → AuthRepository.login()
```

### 2. Request HTTP al backend
```
POST http://10.0.2.2:8080/api/auth/login
{
  "email": "usuario@example.com",
  "contrasena": "password123"
}
```

### 3. Backend responde con JWT
```json
{
  "exito": true,
  "datos": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tipo": "Bearer",
    "expiraEn": 86400,
    "usuario": { ... }
  }
}
```

### 4. App guarda token de forma segura
```
TokenManager.saveToken(token, expiresIn)
→ EncryptedSharedPreferences (AES256_GCM)
```

### 5. Requests automáticos incluyen token
```
AuthInterceptor intercepta todas las peticiones
→ Agrega header: "Authorization: Bearer TOKEN"
→ Backend valida JWT con Spring Security
```

---

## 🏗️ Arquitectura Implementada

```
┌─────────────────────────────────────┐
│  UI Layer (Jetpack Compose)        │
│  LoginScreen, RegisterScreen, etc.  │
└─────────────┬───────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│  ViewModel Layer                    │
│  AuthViewModel (StateFlow)          │
└─────────────┬───────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│  Repository Layer                   │
│  AuthRepository (suspend functions) │
└─────────────┬───────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│  Network Layer (Retrofit)           │
│  ApiService + AuthInterceptor       │
└─────────────┬───────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│  Spring Security Backend            │
│  JWT Validation                     │
└─────────────────────────────────────┘
```

---

## 🔧 Tecnologías Documentadas

| Componente | Tecnología | Propósito |
|------------|------------|-----------|
| UI | Jetpack Compose + Material 3 | Interfaz moderna |
| Networking | Retrofit 2.9.0 | Llamadas HTTP |
| HTTP Client | OkHttp 4.12.0 | Interceptores |
| JSON | Gson 2.10.1 | Serialización |
| Async | Coroutines 1.7.3 | Operaciones asíncronas |
| Storage | EncryptedSharedPreferences | Almacenamiento seguro |
| Architecture | MVVM | Separación de responsabilidades |
| Navigation | Navigation Compose | Navegación entre pantallas |

---

## 📋 Endpoints Documentados

### Autenticación (Sin token)
- `POST /api/auth/registro` - Crear cuenta
- `POST /api/auth/login` - Iniciar sesión

### Públicos (Sin token)
- `GET /api/fallas` - Listar fallas
- `GET /api/fallas/{id}` - Ver detalle
- `GET /api/fallas/{id}/ubicacion` - GPS
- `GET /api/eventos/futuros` - Eventos

### Protegidos (Con token)
- `POST /api/fallas` - Crear falla
- `PUT /api/fallas/{id}` - Actualizar falla
- `POST /api/eventos` - Crear evento
- `POST /api/votos` - Votar
- `POST /api/comentarios` - Comentar

### Admin (Token + rol ADMIN)
- `DELETE /api/fallas/{id}` - Eliminar falla

---

## 🛠️ Configuración Especial

### URLs según Entorno

```kotlin
// Emulador Android Studio
const val BASE_URL = "http://10.0.2.2:8080/api/"

// Dispositivo físico (misma WiFi)
const val BASE_URL = "http://192.168.1.X:8080/api/"

// Producción
const val BASE_URL = "http://35.180.21.42:8080/api/"
```

### AndroidManifest.xml

```xml
<!-- Permisos -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Permitir HTTP (solo desarrollo) -->
<application
    android:name=".FallApp"
    android:usesCleartextTraffic="true"
    ...>
```

---

## ✅ Validaciones Implementadas

### En LoginScreen:
- ✅ Email no vacío
- ✅ Contraseña no vacía
- ✅ Botón deshabilitado durante carga
- ✅ Mostrar/ocultar contraseña
- ✅ Manejo de errores del backend

### En RegisterScreen:
- ✅ Nombre completo requerido
- ✅ Email válido
- ✅ Contraseña mínimo 6 caracteres
- ✅ Confirmar contraseña (debe coincidir)
- ✅ Indicadores visuales de error
- ✅ Navegación de regreso

### En TokenManager:
- ✅ Token encriptado (AES256_GCM)
- ✅ Validación de expiración
- ✅ Limpieza en logout
- ✅ Verificación de sesión activa

---

## 🐛 Troubleshooting Documentado

### Error: "Unable to resolve host"
**Solución:** Usar `10.0.2.2` en emulador, no `localhost`

### Error: "Cleartext HTTP traffic not permitted"
**Solución:** Agregar `android:usesCleartextTraffic="true"` o usar HTTPS

### Error: 401 Unauthorized
**Solución:** Verificar que AuthInterceptor está agregado y token es válido

### Token expirado después de 24h
**Solución:** Implementar refresh automático o forzar re-login

---

## 📊 Cobertura de Documentación

| Aspecto | Estado | Archivo |
|---------|--------|---------|
| Arquitectura | ✅ Completo | README.md |
| Código Backend | ✅ Completo | IMPLEMENTACION... |
| UI/UX | ✅ Completo | EJEMPLO.LOGIN.md |
| Navegación | ✅ Completo | EJEMPLO.LOGIN.md |
| Seguridad | ✅ Completo | README.md |
| Networking | ✅ Completo | IMPLEMENTACION... |
| Storage | ✅ Completo | IMPLEMENTACION... |
| Testing | 🟡 Parcial | - |
| CI/CD | ❌ Pendiente | - |

---

## 🎯 Próximos Pasos Sugeridos

1. **Implementar el código** siguiendo IMPLEMENTACION.AUTENTICACION.md
2. **Crear las pantallas UI** copiando EJEMPLO.LOGIN.md
3. **Probar en emulador** con `http://10.0.2.2:8080`
4. **Agregar más pantallas:**
   - Lista de fallas con RecyclerView/LazyColumn
   - Mapa con Google Maps o OpenStreetMap
   - Calendario de eventos
   - Galería de ninots
5. **Implementar funcionalidades:**
   - Refresh token automático
   - Biometría (Fingerprint/Face ID)
   - Caché offline con Room
   - Push notifications
6. **Testing:**
   - Unit tests para ViewModels
   - Integration tests para Repository
   - UI tests con Compose Test
7. **CI/CD:**
   - GitHub Actions para builds
   - Firma de APK para producción

---

## 📝 Ejemplo de Uso Final

```kotlin
// En MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as FallApp
        
        setContent {
            FallAppTheme {
                // Si está logueado, ir a Home
                // Si no, ir a Login
                NavGraph(
                    startDestination = if (app.tokenManager.isLoggedIn())
                        Screen.Home.route
                    else
                        Screen.Login.route
                )
            }
        }
    }
}

// El usuario solo pone email y contraseña
// La app automáticamente:
// 1. Hace login con Spring Security
// 2. Recibe JWT del backend
// 3. Guarda token encriptado
// 4. Agrega token a todas las requests
// 5. Valida expiración (24h)
// 6. Maneja logout
```

---

## 🔗 Referencias

- **Backend API:** [GUIA.PRUEBAS.API.md](../GUIA.PRUEBAS.API.md)
- **Endpoints:** [GUIA.API.FRONTEND.md](../GUIA.API.FRONTEND.md)
- **JWT Details:** [RESUMEN.ACTUALIZACION.JWT.2026-02-01.md](../RESUMEN.ACTUALIZACION.JWT.2026-02-01.md)
- **Tests Auth:** `bash 06.tests/e2e/test_api_auth.sh`

---

## 🎉 Resultado Final

Con esta documentación, cualquier desarrollador Android puede:

1. ✅ Entender cómo funciona JWT en Spring Security
2. ✅ Implementar autenticación completa en Android
3. ✅ Crear pantallas de login/registro con Material 3
4. ✅ Almacenar tokens de forma segura
5. ✅ Manejar requests autenticados automáticamente
6. ✅ Navegar entre pantallas según estado de sesión
7. ✅ Resolver errores comunes de networking

**La app móvil puede conectarse a la API usando solo email y contraseña, aprovechando toda la seguridad de Spring Security con JWT.**

---

**Creado:** 2026-02-03  
**Versión:** 0.5.3  
**Mantenido por:** Equipo FallApp  
**Estado:** ✅ Documentación completa y lista para implementar
