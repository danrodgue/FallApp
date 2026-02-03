# 📱 FallApp Mobile - Aplicación Android

**Versión:** 0.5.3  
**Fecha Creación:** 2026-02-03  
**Última Actualización:** 2026-02-03 (Validación backend)  
**Framework:** Jetpack Compose + Kotlin  
**Autenticación:** JWT (Spring Security) - ✅ OPERATIVO

> ✅ **ACTUALIZADO 2026-02-03**: Backend JWT validado y operativo. Sistema BCrypt funcional.

---

## 📋 Índice

1. [Visión General](#visión-general)
2. [Arquitectura de Autenticación](#arquitectura-de-autenticación)
3. [Implementación JWT](#implementación-jwt)
4. [Guía de Desarrollo](#guía-de-desarrollo)
5. [Gestión de Tokens](#gestión-de-tokens)
6. [Ejemplos de Código](#ejemplos-de-código)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Visión General

Esta aplicación móvil Android se conecta a la API REST de FallApp utilizando autenticación JWT (JSON Web Token) mediante Spring Security.

### Estado del Backend (2026-02-03)
- ✅ API REST operativa en http://35.180.21.42:8080
- ✅ Autenticación JWT funcional (algoritmo HS512, 24h)
- ✅ Encriptación BCrypt validada en producción
- ✅ Endpoints de registro y login operativos

### Características Principales

- ✅ **Login con usuario/contraseña** (email + password)
- ✅ **Registro de nuevos usuarios**
- ✅ **Almacenamiento seguro del token** (SharedPreferences encriptadas)
- ✅ **Renovación automática de sesión**
- ✅ **Manejo de expiración de token** (24 horas)
- ✅ **Interceptor para agregar token automáticamente**
- ✅ **UI con Jetpack Compose**

### Flujo de Autenticación

```
Usuario → Login Screen → API (/auth/login) → JWT Token (HS512) → 
→ Guardar Token (Encrypted) → Navegar a Home → Todas las requests incluyen token
```

---

## 🔐 Arquitectura de Autenticación

### Componentes Principales

```
┌─────────────────────────────────────────────┐
│         LoginActivity / LoginScreen          │
│  (UI - Jetpack Compose)                     │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│         AuthViewModel                        │
│  (Lógica de negocio - ViewModel)           │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│         AuthRepository                       │
│  (Capa de datos - Repository Pattern)      │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│         ApiService (Retrofit)                │
│  POST /api/auth/login                       │
│  POST /api/auth/registro                    │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│         Spring Security Backend              │
│  JWT Authentication                          │
└─────────────────────────────────────────────┘
```

### Almacenamiento del Token

```kotlin
// SharedPreferences encriptadas (EncryptedSharedPreferences)
┌────────────────────────────────┐
│ Key: "jwt_token"              │
│ Value: "eyJhbGci..."          │
├────────────────────────────────┤
│ Key: "token_expiry"           │
│ Value: 1707235072000 (timestamp)│
├────────────────────────────────┤
│ Key: "user_email"             │
│ Value: "usuario@example.com"  │
└────────────────────────────────┘
```

---

## 🚀 Implementación JWT

### Paso 1: Dependencias en `build.gradle.kts`

```kotlin
dependencies {
    // ... dependencias existentes ...
    
    // Networking - Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines para async/await
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // ViewModel y LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Navigation para Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // DataStore / SharedPreferences encriptadas
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Gson para JSON
    implementation("com.google.code.gson:gson:2.10.1")
}
```

### Paso 2: Permisos en `AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.fallapp">

    <!-- Permiso de Internet -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".FallApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:usesCleartextTraffic="true"
        android:theme="@style/Theme.FallApp">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.FallApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**⚠️ IMPORTANTE:** `android:usesCleartextTraffic="true"` solo para desarrollo. En producción usar HTTPS.

---

## 📦 Estructura de Archivos

```
app/src/main/java/com/example/fallapp/
├── FallApp.kt                    # Application class
├── MainActivity.kt               # Activity principal
├── navigation/
│   └── NavGraph.kt              # Navegación entre pantallas
├── data/
│   ├── model/
│   │   ├── LoginRequest.kt      # Data class para login
│   │   ├── LoginResponse.kt     # Respuesta del API
│   │   ├── RegisterRequest.kt   # Data class para registro
│   │   └── User.kt              # Modelo de usuario
│   ├── remote/
│   │   ├── ApiService.kt        # Interface Retrofit
│   │   ├── AuthInterceptor.kt   # Interceptor para JWT
│   │   └── RetrofitClient.kt    # Cliente Retrofit singleton
│   ├── repository/
│   │   └── AuthRepository.kt    # Repository de autenticación
│   └── local/
│       └── TokenManager.kt      # Gestión de tokens
├── ui/
│   ├── theme/                   # Temas Material 3
│   ├── screens/
│   │   ├── LoginScreen.kt       # Pantalla de login
│   │   ├── RegisterScreen.kt    # Pantalla de registro
│   │   ├── HomeScreen.kt        # Pantalla principal
│   │   └── FallasScreen.kt      # Lista de fallas
│   └── viewmodel/
│       ├── AuthViewModel.kt     # ViewModel de auth
│       └── FallasViewModel.kt   # ViewModel de fallas
└── util/
    ├── Constants.kt             # Constantes (API_URL, etc.)
    ├── Resource.kt              # Sealed class para estados
    └── Extensions.kt            # Extension functions
```

---

## 🔧 Guía de Desarrollo

Ver archivos detallados:
- [IMPLEMENTACION.AUTENTICACION.md](./IMPLEMENTACION.AUTENTICACION.md) - Código completo paso a paso
- [EJEMPLO.LOGIN.md](./EJEMPLO.LOGIN.md) - Ejemplo de pantalla de login
- [EJEMPLO.INTERCEPTOR.md](./EJEMPLO.INTERCEPTOR.md) - Interceptor HTTP con JWT

---

## 💾 Gestión de Tokens

### TokenManager - Almacenamiento Seguro

```kotlin
class TokenManager(context: Context) {
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "fallapp_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String, expiresIn: Long) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000)
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRY, expiryTime)
            .apply()
    }

    fun getToken(): String? = sharedPreferences.getString(KEY_TOKEN, null)

    fun isTokenValid(): Boolean {
        val token = getToken() ?: return false
        val expiry = sharedPreferences.getLong(KEY_EXPIRY, 0)
        return System.currentTimeMillis() < expiry
    }

    fun clearToken() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_EXPIRY = "token_expiry"
    }
}
```

### Flujo de Token

```
1. LOGIN EXITOSO
   ↓
2. Guardar token en EncryptedSharedPreferences
   ↓
3. Guardar timestamp de expiración (ahora + 24h)
   ↓
4. CADA REQUEST HTTP
   ↓
5. AuthInterceptor lee token de SharedPreferences
   ↓
6. Verifica si token está expirado
   ↓
7. Si válido: agrega header "Authorization: Bearer TOKEN"
   ↓
8. Si expirado: redirige a Login
```

---

## 🔄 Interceptor HTTP

### AuthInterceptor - Agregar Token Automáticamente

```kotlin
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // No agregar token a endpoints de auth
        if (request.url.encodedPath.contains("/auth/")) {
            return chain.proceed(request)
        }
        
        // Verificar si hay token válido
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty() || !tokenManager.isTokenValid()) {
            // Token expirado o no existe
            return chain.proceed(request)
        }
        
        // Agregar token al header
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}
```

### RetrofitClient - Configuración

```kotlin
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/" // Emulador Android
    // private const val BASE_URL = "http://35.180.21.42:8080/api/" // Producción
    
    fun create(tokenManager: TokenManager): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

---

## 📱 Pantalla de Login (Jetpack Compose)

### Ejemplo Básico

```kotlin
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("FallApp", style = MaterialTheme.typography.headlineLarge)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is Resource.Loading
        ) {
            Text("Iniciar Sesión")
        }
        
        when (val state = loginState) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Success -> {
                LaunchedEffect(Unit) {
                    onLoginSuccess()
                }
            }
            is Resource.Error -> {
                Text(
                    text = state.message ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}
```

---

## 🧪 Testing de Autenticación

### Prueba Manual con Logcat

```kotlin
// En AuthViewModel.kt
fun login(email: String, password: String) {
    viewModelScope.launch {
        _loginState.value = Resource.Loading()
        
        Log.d("AuthViewModel", "Iniciando login para: $email")
        
        val result = repository.login(email, password)
        
        when (result) {
            is Resource.Success -> {
                Log.d("AuthViewModel", "Login exitoso: ${result.data?.token?.substring(0, 20)}...")
                _loginState.value = result
            }
            is Resource.Error -> {
                Log.e("AuthViewModel", "Login error: ${result.message}")
                _loginState.value = result
            }
        }
    }
}
```

### Verificar Token en Logcat

```
D/AuthViewModel: Iniciando login para: test@example.com
D/OkHttp: --> POST http://10.0.2.2:8080/api/auth/login
D/OkHttp: {"email":"test@example.com","contrasena":"password123"}
D/OkHttp: <-- 200 OK
D/AuthViewModel: Login exitoso: eyJhbGciOiJIUzUxMiJ9...
D/TokenManager: Token guardado, expira en: 86400 segundos
```

---

## 🔍 Troubleshooting

### Error: "Unable to resolve host"

**Causa:** El emulador no puede conectar a `localhost`

**Solución:**
```kotlin
// ❌ NO FUNCIONA en emulador
const val BASE_URL = "http://localhost:8080/api/"

// ✅ USAR ESTO para emulador Android
const val BASE_URL = "http://10.0.2.2:8080/api/"

// ✅ USAR ESTO para dispositivo físico (misma red WiFi)
const val BASE_URL = "http://192.168.1.X:8080/api/"

// ✅ USAR ESTO para producción
const val BASE_URL = "http://35.180.21.42:8080/api/"
```

### Error: "Cleartext HTTP traffic not permitted"

**Causa:** Android 9+ bloquea HTTP sin cifrar

**Solución:**
```xml
<!-- AndroidManifest.xml -->
<application
    android:usesCleartextTraffic="true"
    ... >
```

O crear `network_security_config.xml`:
```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">35.180.21.42</domain>
    </domain-config>
</network-security-config>
```

### Error: 401 Unauthorized en requests

**Causa:** Token no se está agregando o está expirado

**Solución:**
```kotlin
// Verificar que el interceptor está agregado
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor(tokenManager)) // ← Verificar esto
    .build()

// Verificar que el token existe
val token = tokenManager.getToken()
Log.d("DEBUG", "Token: ${token?.substring(0, 20)}")
Log.d("DEBUG", "Token válido: ${tokenManager.isTokenValid()}")
```

### Token expirado después de 24h

**Solución:** Implementar refresh automático o forzar re-login

```kotlin
class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val onTokenExpired: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(request)
        
        // Si recibimos 401, token expiró
        if (response.code == 401) {
            tokenManager.clearToken()
            onTokenExpired() // Navegar a login
        }
        
        return response
    }
}
```

---

## 📚 Recursos Adicionales

### Documentación del Backend

- [GUIA.PRUEBAS.API.md](../../GUIA.PRUEBAS.API.md) - Guía completa de la API
- [GUIA.API.FRONTEND.md](../../GUIA.API.FRONTEND.md) - Endpoints disponibles
- [RESUMEN.ACTUALIZACION.JWT.2026-02-01.md](../../RESUMEN.ACTUALIZACION.JWT.2026-02-01.md) - Detalles JWT

### Tests Automatizados

```bash
# Ejecutar tests de autenticación del backend
bash /srv/FallApp/06.tests/e2e/test_api_auth.sh

# Ver endpoints disponibles
curl http://localhost:8080/swagger-ui.html
```

### Endpoints de Autenticación

| Endpoint | Método | Descripción | Requiere Token |
|----------|--------|-------------|----------------|
| `/api/auth/registro` | POST | Registrar nuevo usuario | ❌ |
| `/api/auth/login` | POST | Iniciar sesión | ❌ |
| `/api/fallas` | GET | Listar fallas | ❌ |
| `/api/fallas` | POST | Crear falla | ✅ |
| `/api/eventos` | POST | Crear evento | ✅ |
| `/api/votos` | POST | Votar por ninot | ✅ |

---

## 🎯 Próximos Pasos

1. **Implementar código base** - Ver [IMPLEMENTACION.AUTENTICACION.md](./IMPLEMENTACION.AUTENTICACION.md)
2. **Crear pantallas de UI** - Ver [EJEMPLO.LOGIN.md](./EJEMPLO.LOGIN.md)
3. **Probar en emulador** - Conectar a `http://10.0.2.2:8080`
4. **Implementar refresh token** - Para sesiones más largas
5. **Agregar biometría** - Fingerprint/Face ID para login rápido
6. **Implementar logout** - Limpiar token y navegar a login

---

**Última actualización:** 2026-02-03  
**Versión:** 0.5.2  
**Mantenido por:** Equipo FallApp
