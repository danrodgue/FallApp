# 🔐 Implementación Completa de Autenticación JWT

**Guía paso a paso para implementar autenticación JWT en FallApp Mobile**

---

## 📦 Paso 1: Dependencias

### `build.gradle.kts` (Módulo app)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Para procesamiento de anotaciones
}

android {
    namespace = "com.example.fallapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fallapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Base URL para desarrollo
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/api/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"http://35.180.21.42:8080/api/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Compose BOM (Bill of Materials)
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    
    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel y LiveData para Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Activity Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Core KTX
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Retrofit para networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OkHttp para interceptores y logging
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Gson para JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // EncryptedSharedPreferences para almacenamiento seguro
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // DataStore (alternativa a SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## 📁 Paso 2: Estructura de Paquetes

Crear la siguiente estructura en `app/src/main/java/com/example/fallapp/`:

```
com.example.fallapp/
├── FallApp.kt
├── MainActivity.kt
├── data/
│   ├── model/
│   │   ├── ApiResponse.kt
│   │   ├── LoginRequest.kt
│   │   ├── LoginResponse.kt
│   │   ├── RegisterRequest.kt
│   │   └── User.kt
│   ├── remote/
│   │   ├── ApiService.kt
│   │   ├── AuthInterceptor.kt
│   │   └── RetrofitClient.kt
│   ├── repository/
│   │   └── AuthRepository.kt
│   └── local/
│       └── TokenManager.kt
├── ui/
│   ├── screens/
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   └── HomeScreen.kt
│   ├── viewmodel/
│   │   └── AuthViewModel.kt
│   └── navigation/
│       └── NavGraph.kt
└── util/
    ├── Constants.kt
    └── Resource.kt
```

---

## 💾 Paso 3: Modelos de Datos

### `data/model/ApiResponse.kt`

```kotlin
package com.example.fallapp.data.model

data class ApiResponse<T>(
    val exito: Boolean,
    val mensaje: String?,
    val datos: T?,
    val errores: List<String>?
)
```

### `data/model/LoginRequest.kt`

```kotlin
package com.example.fallapp.data.model

data class LoginRequest(
    val email: String,
    val contrasena: String
)
```

### `data/model/LoginResponse.kt`

```kotlin
package com.example.fallapp.data.model

data class LoginResponse(
    val token: String,
    val tipo: String,
    val expiraEn: Long,
    val usuario: User
)
```

### `data/model/RegisterRequest.kt`

```kotlin
package com.example.fallapp.data.model

data class RegisterRequest(
    val email: String,
    val contrasena: String,
    val nombreCompleto: String,
    val idFalla: Long? = null
)
```

### `data/model/User.kt`

```kotlin
package com.example.fallapp.data.model

data class User(
    val idUsuario: Long,
    val email: String,
    val nombreCompleto: String?,
    val rol: String,
    val idFalla: Long?,
    val activo: Boolean
)
```

---

## 🔧 Paso 4: Utilidades

### `util/Resource.kt`

```kotlin
package com.example.fallapp.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
```

### `util/Constants.kt`

```kotlin
package com.example.fallapp.util

object Constants {
    // Base URL - Cambiar según entorno
    const val BASE_URL = "http://10.0.2.2:8080/api/" // Emulador
    // const val BASE_URL = "http://35.180.21.42:8080/api/" // Producción
    
    // SharedPreferences
    const val PREFS_NAME = "fallapp_prefs"
    const val KEY_TOKEN = "jwt_token"
    const val KEY_EXPIRY = "token_expiry"
    const val KEY_USER_EMAIL = "user_email"
    
    // Timeouts
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
```

---

## 💾 Paso 5: TokenManager (Almacenamiento Seguro)

### `data/local/TokenManager.kt`

```kotlin
package com.example.fallapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.fallapp.util.Constants

class TokenManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        Constants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Guarda el token JWT y su tiempo de expiración
     */
    fun saveToken(token: String, expiresIn: Long) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000)
        sharedPreferences.edit()
            .putString(Constants.KEY_TOKEN, token)
            .putLong(Constants.KEY_EXPIRY, expiryTime)
            .apply()
    }
    
    /**
     * Obtiene el token JWT almacenado
     */
    fun getToken(): String? {
        return sharedPreferences.getString(Constants.KEY_TOKEN, null)
    }
    
    /**
     * Verifica si el token es válido (no expiró)
     */
    fun isTokenValid(): Boolean {
        val token = getToken() ?: return false
        val expiry = sharedPreferences.getLong(Constants.KEY_EXPIRY, 0)
        return System.currentTimeMillis() < expiry
    }
    
    /**
     * Guarda el email del usuario
     */
    fun saveUserEmail(email: String) {
        sharedPreferences.edit()
            .putString(Constants.KEY_USER_EMAIL, email)
            .apply()
    }
    
    /**
     * Obtiene el email del usuario
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, null)
    }
    
    /**
     * Limpia todos los datos (logout)
     */
    fun clearToken() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Verifica si el usuario está logueado
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null && isTokenValid()
    }
}
```

---

## 🌐 Paso 6: Interceptor HTTP

### `data/remote/AuthInterceptor.kt`

```kotlin
package com.example.fallapp.data.remote

import com.example.fallapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // No agregar token a endpoints de autenticación
        val path = request.url.encodedPath
        if (path.contains("/auth/login") || path.contains("/auth/registro")) {
            return chain.proceed(request)
        }
        
        // Verificar si hay token válido
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty() || !tokenManager.isTokenValid()) {
            return chain.proceed(request)
        }
        
        // Agregar token al header Authorization
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}
```

---

## 🔌 Paso 7: Configuración de Retrofit

### `data/remote/ApiService.kt`

```kotlin
package com.example.fallapp.data.remote

import com.example.fallapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== AUTENTICACIÓN ====================
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>
    
    @POST("auth/registro")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<LoginResponse>>
    
    // ==================== FALLAS (Ejemplos) ====================
    
    @GET("fallas")
    suspend fun getFallas(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<List<Any>>> // Reemplazar Any con modelo Falla
    
    @GET("fallas/{id}")
    suspend fun getFallaById(
        @Path("id") id: Long
    ): Response<ApiResponse<Any>>
    
    @POST("fallas")
    suspend fun createFalla(
        @Body falla: Any // Reemplazar con modelo Falla
    ): Response<ApiResponse<Any>>
    
    // Agregar más endpoints según necesites...
}
```

### `data/remote/RetrofitClient.kt`

```kotlin
package com.example.fallapp.data.remote

import com.example.fallapp.data.local.TokenManager
import com.example.fallapp.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    @Volatile
    private var INSTANCE: ApiService? = null
    
    fun getInstance(tokenManager: TokenManager): ApiService {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildApiService(tokenManager).also { INSTANCE = it }
        }
    }
    
    private fun buildApiService(tokenManager: TokenManager): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
        
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

---

## 📦 Paso 8: Repository

### `data/repository/AuthRepository.kt`

```kotlin
package com.example.fallapp.data.repository

import com.example.fallapp.data.local.TokenManager
import com.example.fallapp.data.model.*
import com.example.fallapp.data.remote.ApiService
import com.example.fallapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    
    /**
     * Login de usuario
     */
    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = apiService.login(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.exito == true && body.datos != null) {
                        // Guardar token
                        tokenManager.saveToken(
                            body.datos.token,
                            body.datos.expiraEn
                        )
                        tokenManager.saveUserEmail(email)
                        
                        Resource.Success(body.datos)
                    } else {
                        Resource.Error(body?.mensaje ?: "Error en login")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error de conexión")
            }
        }
    }
    
    /**
     * Registro de nuevo usuario
     */
    suspend fun register(
        email: String,
        password: String,
        nombreCompleto: String,
        idFalla: Long? = null
    ): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(email, password, nombreCompleto, idFalla)
                val response = apiService.register(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.exito == true && body.datos != null) {
                        // Guardar token
                        tokenManager.saveToken(
                            body.datos.token,
                            body.datos.expiraEn
                        )
                        tokenManager.saveUserEmail(email)
                        
                        Resource.Success(body.datos)
                    } else {
                        Resource.Error(body?.mensaje ?: "Error en registro")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error de conexión")
            }
        }
    }
    
    /**
     * Logout
     */
    fun logout() {
        tokenManager.clearToken()
    }
    
    /**
     * Verificar si está logueado
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}
```

---

## 🎨 Paso 9: ViewModel

### `ui/viewmodel/AuthViewModel.kt`

```kotlin
package com.example.fallapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fallapp.data.model.LoginResponse
import com.example.fallapp.data.repository.AuthRepository
import com.example.fallapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<Resource<LoginResponse>?>(null)
    val loginState: StateFlow<Resource<LoginResponse>?> = _loginState
    
    private val _registerState = MutableStateFlow<Resource<LoginResponse>?>(null)
    val registerState: StateFlow<Resource<LoginResponse>?> = _registerState
    
    /**
     * Login de usuario
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = repository.login(email, password)
            _loginState.value = result
        }
    }
    
    /**
     * Registro de usuario
     */
    fun register(
        email: String,
        password: String,
        nombreCompleto: String,
        idFalla: Long? = null
    ) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = repository.register(email, password, nombreCompleto, idFalla)
            _registerState.value = result
        }
    }
    
    /**
     * Logout
     */
    fun logout() {
        repository.logout()
    }
    
    /**
     * Verificar si está logueado
     */
    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
    
    /**
     * Resetear estado de login
     */
    fun resetLoginState() {
        _loginState.value = null
    }
    
    /**
     * Resetear estado de registro
     */
    fun resetRegisterState() {
        _registerState.value = null
    }
}
```

---

## 📱 Paso 10: Application Class

### `FallApp.kt`

```kotlin
package com.example.fallapp

import android.app.Application
import com.example.fallapp.data.local.TokenManager
import com.example.fallapp.data.remote.RetrofitClient
import com.example.fallapp.data.repository.AuthRepository

class FallApp : Application() {
    
    lateinit var tokenManager: TokenManager
        private set
    
    lateinit var authRepository: AuthRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar TokenManager
        tokenManager = TokenManager(this)
        
        // Inicializar Repository
        val apiService = RetrofitClient.getInstance(tokenManager)
        authRepository = AuthRepository(apiService, tokenManager)
    }
}
```

### Actualizar `AndroidManifest.xml`

```xml
<application
    android:name=".FallApp"
    ...>
```

---

## ✅ Paso 11: Verificación

### Checklist de Implementación

- [ ] Dependencias agregadas en `build.gradle.kts`
- [ ] Todos los modelos creados (`LoginRequest`, `LoginResponse`, etc.)
- [ ] `TokenManager` implementado con EncryptedSharedPreferences
- [ ] `AuthInterceptor` agrega token a requests
- [ ] `RetrofitClient` configurado correctamente
- [ ] `AuthRepository` maneja login/registro
- [ ] `AuthViewModel` expone estados con Flow
- [ ] `FallApp` inicializa dependencias
- [ ] `AndroidManifest.xml` tiene permisos de Internet

### Probar la Implementación

Crea un test simple en `LoginScreen.kt`:

```kotlin
@Composable
fun TestLoginScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as FallApp
    val viewModel = remember {
        AuthViewModel(app.authRepository)
    }
    
    // Probar login
    LaunchedEffect(Unit) {
        viewModel.login("test@example.com", "password123")
    }
    
    val loginState by viewModel.loginState.collectAsState()
    
    when (val state = loginState) {
        is Resource.Loading -> Text("Cargando...")
        is Resource.Success -> Text("Login exitoso: ${state.data?.token?.take(20)}")
        is Resource.Error -> Text("Error: ${state.message}")
        null -> Text("Sin estado")
    }
}
```

---

## 🎉 ¡Implementación Completada!

Ahora puedes:
1. Crear pantallas de UI (ver [EJEMPLO.LOGIN.md](./EJEMPLO.LOGIN.md))
2. Implementar navegación entre pantallas
3. Agregar más endpoints a `ApiService`
4. Probar en emulador con `http://10.0.2.2:8080`

**Próximo paso:** Crear la interfaz de usuario con Jetpack Compose
