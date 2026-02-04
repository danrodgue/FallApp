# 📝 Log de Configuración del Proyecto - FallApp User

> **Inicio**: 2026-02-02  
> **Desarrollador**: Gautier  
> **Metodología**: Spec-Driven Development

---

## 🎯 Objetivo

Configurar el proyecto Android desde cero siguiendo Clean Architecture para la aplicación **FallApp User**.

---

## ✅ PASO 1: Configuración de Gradle y Dependencias

**Fecha**: 2026-02-02  
**Estado**: ✅ COMPLETADO

### Archivos Modificados

1. **`gradle/libs.versions.toml`** - Catálogo de versiones centralizado
2. **`app/build.gradle.kts`** - Configuración de la aplicación

### Cambios Realizados

#### 1.1 libs.versions.toml

**Agregadas las siguientes categorías de dependencias:**

| Categoría | Versiones | Librerías |
|-----------|-----------|-----------|
| **Android Core** | API 34, Kotlin 2.0.21 | core-ktx, lifecycle |
| **Compose UI** | BOM 2024.09.00 | Material3, Navigation, Icons |
| **Networking** | Ktor 2.3.12 | client-core, android, auth, logging |
| **Database** | Room 2.6.1 | runtime, ktx, compiler |
| **DI** | Koin 3.5.6 | android, compose |
| **Async** | Coroutines 1.8.1 | core, android |
| **Maps** | Maps 5.0.1, Compose 4.4.1 | play-services-maps, maps-compose |
| **Images** | Coil 2.7.0 | coil-compose |
| **Storage** | DataStore 1.1.1 | datastore-preferences |
| **Testing** | JUnit, MockK, Turbine | mockk, coroutines-test, turbine |

**Plugins configurados:**
- ✅ `android-application` - Plugin de Android
- ✅ `kotlin-android` - Soporte Kotlin
- ✅ `kotlin-compose` - Compiler de Compose
- ✅ `kotlin-serialization` - Serialización JSON
- ✅ `ksp` - Kotlin Symbol Processing (para Room)

#### 1.2 app/build.gradle.kts

**Configuración actualizada:**

```kotlin
android {
    namespace = "com.fallapp.user"  // ← Package principal
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.fallapp.user"
        minSdk = 24  // Android 7.0+ (95% dispositivos)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true          // Optimización código
            isShrinkResources = true        // Optimización recursos
        }
        debug {
            applicationIdSuffix = ".debug"  // Para instalar ambas versiones
        }
    }
    
    kotlinOptions {
        jvmTarget = "17"  // Java 17 para mejor performance
    }
}
```

**Dependencias totales agregadas:** 30+ librerías

### Verificación

**Para verificar que todo está bien:**

```bash
# En la carpeta 03.mobile/
./gradlew clean build
```

**Resultado esperado:**
```
BUILD SUCCESSFUL in Xs
```

### Notas Técnicas

#### ¿Por qué Ktor en lugar de Retrofit?
- ✅ Nativo de Kotlin (no es wrapper de Java)
- ✅ Soporte Coroutines desde el diseño
- ✅ Multiplatform-ready (si escalamos a iOS)
- ✅ Más ligero y moderno

#### ¿Por qué Room en lugar de SQLite directo?
- ✅ Type-safe (compilador verifica queries)
- ✅ Soporte Flow para observar cambios
- ✅ Migraciones automáticas
- ✅ Integración con Coroutines

#### ¿Por qué Koin en lugar de Dagger/Hilt?
- ✅ Más simple de configurar
- ✅ Pure Kotlin (no code generation)
- ✅ Curva de aprendizaje menor
- ✅ Suficiente para este proyecto

### Tests de Esta Fase

**No hay tests unitarios en esta fase** porque solo estamos configurando Gradle.

**Verificación manual:**
1. ✅ Sync Gradle exitoso
2. ✅ Sin errores de compilación
3. ✅ Todas las librerías se resuelven

---

## ✅ PASO 2: Configuración de AndroidManifest y Permisos

**Fecha**: 2026-02-02  
**Estado**: ✅ COMPLETADO

### Archivos Modificados/Creados

1. **`app/src/main/AndroidManifest.xml`** - Configuración de permisos
2. **`app/src/main/java/com/fallapp/user/FallAppApplication.kt`** - Clase Application
3. **`app/src/main/java/com/fallapp/user/MainActivity.kt`** - Actividad principal

### Cambios Realizados

#### 2.1 AndroidManifest.xml

**Permisos agregados:**

| Permiso | Propósito | Requerido |
|---------|-----------|-----------|
| `INTERNET` | Llamadas a la API REST | ✅ Sí |
| `ACCESS_NETWORK_STATE` | Detectar si hay conexión | ✅ Sí |
| `ACCESS_FINE_LOCATION` | GPS preciso para mapas | ⚠️ Runtime |
| `ACCESS_COARSE_LOCATION` | Ubicación aproximada | ⚠️ Runtime |

**Features declaradas:**
- `hardware.location.gps` (opcional) - Para dispositivos con GPS
- `hardware.location.network` (opcional) - Para ubicación por red

**Configuración de Application:**
```xml
<application
    android:name=".FallAppApplication"  <!-- ← Clase custom Application -->
    android:usesCleartextTraffic="true" <!-- ← Para HTTP (no HTTPS) -->
    ...>
```

**Notas importantes:**
- ✅ `usesCleartextTraffic="true"` permite HTTP (necesario porque API es http://35.180.21.42:8080)
- ✅ `windowSoftInputMode="adjustResize"` para que el teclado no tape campos
- ⚠️ Los permisos de ubicación requieren solicitud runtime (se implementará en feature de mapas)

#### 2.2 FallAppApplication.kt

**Clase Application creada:**
```kotlin
class FallAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Koin (DI)
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@FallAppApplication)
            modules(/* se añadirán módulos */)
        }
    }
}
```

**Responsabilidades:**
1. ✅ Inicialización de Koin (Dependency Injection)
2. ✅ Logging según build type (DEBUG vs RELEASE)
3. ⏳ Módulos DI (se añadirán en paso 5)

#### 2.3 MainActivity.kt

**Actividad principal temporal:**
- ✅ Esqueleto básico con Jetpack Compose
- ✅ Muestra mensaje placeholder
- ⏳ Navegación se implementará en paso 12

### Verificación

**Para verificar que funciona:**

```bash
# Compilar proyecto
./gradlew assembleDebug

# Instalar en dispositivo/emulador
./gradlew installDebug
```

**Resultado esperado:**
- ✅ APK se genera sin errores
- ✅ App instala correctamente
- ✅ Se ve mensaje "Hello FallApp User!"

### Tests de Esta Fase

**No hay tests unitarios** - Solo configuración.

**Verificación manual:**
1. ✅ AndroidManifest válido (sin errores de sintaxis)
2. ✅ FallAppApplication se inicializa
3. ✅ MainActivity se muestra correctamente

### Errores Comunes y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| `Application class not found` | Package incorrecto en manifest | Verificar que `android:name=".FallAppApplication"` coincide con package |
| `Cleartext HTTP traffic not permitted` | Sin `usesCleartextTraffic` | Ya configurado en manifest |
| `Permission denial` | Permisos no declarados | Ya agregados en manifest |

---

## ✅ PASO 3: Core - Network (Ktor Client)

**Fecha**: 2026-02-02  
**Estado**: ✅ COMPLETADO

### Archivos Creados

1. **`core/network/KtorClient.kt`** - Cliente HTTP configurado
2. **`core/network/NetworkMonitor.kt`** - Monitor de conectividad
3. **`test/core/network/KtorClientTest.kt`** - Tests unitarios

### Implementación

#### 3.1 KtorClient.kt

**Cliente HTTP con dos modos:**

| Método | Uso | Características |
|--------|-----|----------------|
| `create()` | Requests públicas | Sin autenticación |
| `createAuthenticated(token)` | Requests privadas | Con header JWT |

**Configuración incluida:**
```kotlin
✅ ContentNegotiation (JSON automático)
✅ Logging (DEBUG mode)
✅ HttpTimeout (30s connect, 60s request)
✅ DefaultRequest (headers comunes)
✅ Android Engine (optimizado móvil)
```

**Headers por defecto:**
- `Content-Type: application/json`
- `Accept: application/json`
- `Accept-Language: es-ES`
- `User-Agent: FallApp-Android/1.0`
- `Authorization: Bearer {token}` (solo en modo autenticado)

**Ejemplo de uso:**
```kotlin
// Cliente público
val client = KtorClient.create()
val response = client.get("${ApiConfig.API_URL}/fallas")

// Cliente autenticado
val authClient = KtorClient.createAuthenticated(userToken)
val profile = authClient.get("${ApiConfig.API_URL}/usuarios/perfil")
```

#### 3.2 NetworkMonitor.kt

**Monitor de conectividad reactivo:**

```kotlin
class NetworkMonitor(context: Context) {
    val isConnected: Flow<Boolean>  // Observable de estado de red
    fun isCurrentlyConnected(): Boolean  // Consulta síncrona
    fun getConnectionType(): ConnectionType  // WIFI/CELLULAR/ETHERNET/NONE
}
```

**Características:**
- ✅ Usa Flow de Kotlin (reactivo)
- ✅ Detecta cambios automáticamente
- ✅ Diferencia tipos de conexión
- ✅ Se limpia automáticamente (callbackFlow)

**Ejemplo de uso:**
```kotlin
// En un ViewModel o Repository
networkMonitor.isConnected.collect { isConnected ->
    if (isConnected) {
        // Sincronizar con servidor
        syncDataFromServer()
    } else {
        // Mostrar datos locales
        showCachedData()
    }
}
```

#### 3.3 KtorClientTest.kt

**Tests unitarios creados:**

| Test | Verifica |
|------|----------|
| `create client has correct base URL` | Cliente se crea correctamente |
| `createAuthenticated includes Authorization header` | Token se configura |
| `client has correct timeout configuration` | Timeouts están configurados |
| `client accepts JSON content type` | Content-Type JSON |
| `token is properly formatted with Bearer prefix` | Formato "Bearer {token}" |

**Ejecutar tests:**
```bash
./gradlew test --tests "com.fallapp.core.network.KtorClientTest"
```

### Verificación

**Tests unitarios:**
```bash
cd 03.mobile/
./gradlew test
```

**Resultado esperado:**
```
KtorClientTest > create client has correct base URL PASSED
KtorClientTest > createAuthenticated includes Authorization header PASSED
KtorClientTest > client has correct timeout configuration PASSED
KtorClientTest > client accepts JSON content type PASSED
KtorClientTest > token is properly formatted with Bearer prefix PASSED

BUILD SUCCESSFUL
5 tests passed
```

### Notas Técnicas

#### ¿Por qué Ktor en lugar de Retrofit?

| Aspecto | Ktor | Retrofit |
|---------|------|----------|
| Lenguaje | Nativo Kotlin | Wrapper de OkHttp (Java) |
| Coroutines | Nativo | Adaptador externo |
| Multiplatform | ✅ Sí | ❌ No |
| Verbosidad | Menos código | Más anotaciones |

#### ¿Por qué NetworkMonitor como Flow?

- ✅ Reactivo: UI se actualiza automáticamente
- ✅ Composable-friendly: Se integra con Compose State
- ✅ Lifecycle-aware: Se limpia solo cuando se destruye
- ✅ Testable: Fácil de mockear en tests

### Pendientes para Siguiente Fase

- ⏳ Crear `NetworkModule.kt` (Koin DI) en paso 5
- ⏳ Tests de integración con API real (cuando esté feature Auth)
- ⏳ Interceptor para refresh de token (cuando esté Auth)

---

## ✅ PASO 4: Core - Database (Room)

**Fecha**: 2026-02-02  
**Estado**: ✅ COMPLETADO

### Archivos Creados

**Configuración:**
1. **`core/database/Converters.kt`** - TypeConverters para Room
2. **`core/database/FallAppDatabase.kt`** - Base de datos principal

**Entidades (basadas en API):**
3. **`core/database/entity/FallaEntity.kt`** - Tabla fallas
4. **`core/database/entity/EventoEntity.kt`** - Tabla eventos
5. **`core/database/entity/NinotEntity.kt`** - Tabla ninots
6. **`core/database/entity/UsuarioEntity.kt`** - Tabla usuarios

**DAOs (Data Access Objects):**
7. **`core/database/dao/FallaDao.kt`** - Operaciones fallas
8. **`core/database/dao/EventoDao.kt`** - Operaciones eventos
9. **`core/database/dao/NinotDao.kt`** - Operaciones ninots
10. **`core/database/dao/UsuarioDao.kt`** - Operaciones usuarios

**Tests:**
11. **`androidTest/core/database/dao/FallaDaoTest.kt`** - Tests instrumentados

### Implementación

#### 4.1 Estructura de Datos

**Tablas creadas:**

| Tabla | Registros | Foreign Keys | Índices |
|-------|-----------|--------------|---------|
| `fallas` | Fallas falleras | - | nombre, categoria |
| `eventos` | Eventos de fallas | → fallas | idFalla, fechaEvento |
| `ninots` | Ninots indultados | → fallas | idFalla, premiado |
| `usuarios` | Usuario autenticado | → fallas | email (unique), idFalla |

**Relaciones:**
```
fallas (1) ←── (N) eventos
fallas (1) ←── (N) ninots
fallas (1) ←── (N) usuarios
```

#### 4.2 Converters.kt

**TypeConverters implementados:**

| Tipo Origen | Tipo Destino | Uso |
|-------------|--------------|-----|
| `LocalDateTime` | `String` (ISO 8601) | Timestamps |
| `List<String>` | `String` (JSON) | Imágenes de ninots |
| `Categoria` | `String` | Enum categorías |
| `TipoEvento` | `String` | Enum tipos evento |
| `TipoVoto` | `String` | Enum tipos voto |
| `Rol` | `String` | Enum roles usuario |

**Enums definidos:**
```kotlin
enum class Categoria {
    ESPECIAL, PRIMERA_A, PRIMERA_B, SEGUNDA_A, SEGUNDA_B,
    TERCERA_A, TERCERA_B, CUARTA, QUINTA,
    INFANTIL_ESPECIAL, INFANTIL_PRIMERA
}

enum class TipoEvento {
    PLANTA, CREMA, OFRENDA, DESFILE, CENA, FIESTA,
    MASCLETA, CASTILLO, PROCLAMACION, EXALTACION, PAELLA, OTRO
}

enum class TipoVoto {
    INGENIOSO, CRITICO, ARTISTICO
}

enum class Rol {
    FALLERO, ADMIN, CASAL
}
```

#### 4.3 Entidades - Basadas en GUIA.API.FRONTEND.md

**FallaEntity** (refleja `GET /api/fallas`):
- ✅ Todos los campos de la API
- ✅ `lastSyncTime` para control de caché
- ✅ Ubicación (latitud/longitud)
- ✅ Estadísticas (totalEventos, totalNinots)

**EventoEntity** (refleja `GET /api/eventos`):
- ✅ ForeignKey a fallas
- ✅ `fechaEvento` como LocalDateTime
- ✅ Ubicación opcional
- ✅ Índice en fecha para queries rápidas

**NinotEntity** (refleja `GET /api/ninots`):
- ✅ ForeignKey a fallas
- ✅ Array de imágenes (List<String>)
- ✅ Estadísticas de votos
- ✅ Flag premiado

**UsuarioEntity** (refleja `POST /api/auth/login`):
- ✅ Email único
- ✅ Rol del usuario
- ✅ ForeignKey a falla (opcional)

#### 4.4 DAOs - Operaciones Reactivas

**Patrón Flow para reactividad:**
```kotlin
// UI se actualiza automáticamente cuando cambia la BD
fallaDao.getAllFallas().collect { fallas ->
    // UI se refresca con nuevos datos
}
```

**Operaciones principales:**

**FallaDao:**
- `getAllFallas()` → Flow<List<FallaEntity>>
- `getFallaById(id)` → Flow<FallaEntity?>
- `searchFallas(query)` → Flow<List<FallaEntity>>
- `getFallasPaginated(limit, offset)` → List<FallaEntity>
- `insertFalla()`, `updateFalla()`, `deleteFalla()`

**EventoDao:**
- `getEventosFuturos(now)` → Flow<List<EventoEntity>>
- `getEventosProximos(now, limit)` → Flow<List<EventoEntity>>
- `getEventosByFalla(fallaId)` → Flow<List<EventoEntity>>

**NinotDao:**
- `getNinotsPremiados()` → Flow<List<NinotEntity>>
- `getTopNinots(limit)` → Flow<List<NinotEntity>>
- `getNinotsByFalla(fallaId)` → Flow<List<NinotEntity>>

**UsuarioDao:**
- `getCurrentUser()` → Flow<UsuarioEntity?>
- `insertUser()`, `deleteCurrentUser()` (logout)

#### 4.5 FallAppDatabase

**Configuración:**
```kotlin
@Database(
    entities = [FallaEntity, EventoEntity, NinotEntity, UsuarioEntity],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
```

**Singleton pattern:**
- ✅ `getInstance(context)` - Producción
- ✅ `getInMemoryDatabase(context)` - Tests

**Estrategia de migraciones:**
- Versión 1: Tablas iniciales
- `fallbackToDestructiveMigration()` en desarrollo
- TODO: Migraciones reales para producción

#### 4.6 Tests - FallaDaoTest

**Tests implementados (10 tests):**

| Test | Verifica |
|------|----------|
| `insertFalla_andRetrieveById` | Inserción y lectura básica |
| `insertMultipleFallas_andGetAll` | Inserción múltiple |
| `searchFallas_byName` | Búsqueda por texto |
| `getFallasByCategoria` | Filtro por categoría |
| `getPaginatedFallas` | Paginación correcta |
| `updateFalla` | Actualización de registros |
| `deleteFalla` | Eliminación |
| `getTotalFallas` | Conteo total |

**Ejecutar tests:**
```bash
./gradlew connectedAndroidTest --tests "com.fallapp.core.database.dao.FallaDaoTest"
```

**Resultado esperado:**
```
FallaDaoTest > insertFalla_andRetrieveById PASSED
FallaDaoTest > insertMultipleFallas_andGetAll PASSED
FallaDaoTest > searchFallas_byName PASSED
FallaDaoTest > getFallasByCategoria PASSED
FallaDaoTest > getPaginatedFallas PASSED
FallaDaoTest > updateFalla PASSED
FallaDaoTest > deleteFalla PASSED
FallaDaoTest > getTotalFallas PASSED

8 tests PASSED
```

### Verificación

**Compilar proyecto:**
```bash
./gradlew assembleDebug
```

**Ejecutar tests instrumentados:**
```bash
./gradlew connectedAndroidTest
```

### Notas Técnicas

#### ¿Por qué Flow en lugar de LiveData?

| Aspecto | Flow | LiveData |
|---------|------|----------|
| Lifecycle | Manual | Automático |
| Operadores | Muchos (map, filter, etc) | Pocos |
| Coroutines | Nativo | Adaptador |
| Compose | Integración nativa | Conversión necesaria |

**Conclusión**: Flow es más moderno y flexible.

#### ¿Por qué TypeConverters?

Room no entiende tipos complejos como:
- ❌ `LocalDateTime` → ✅ Convertir a `String`
- ❌ `List<String>` → ✅ Convertir a `String` JSON
- ❌ `Enum` → ✅ Convertir a `String`

#### Estrategia de Caché

Cada entidad tiene `lastSyncTime`:
```kotlin
val lastSyncTime: LocalDateTime = LocalDateTime.now()
```

**Uso:**
```kotlin
// Limpiar datos de hace más de 7 días
val threshold = LocalDateTime.now().minusDays(7).toString()
fallaDao.deleteOldFallas(threshold)
```

### Pendientes para Siguiente Fase

- ⏳ Crear `DatabaseModule.kt` (Koin DI) en paso 5
- ⏳ Implementar migración 1→2 cuando se añadan tablas
- ⏳ Tabla de favoritos (versión 2)
- ⏳ Tabla de votos locales offline (versión 2)

---

## 📊 Próximo Paso

**PASO 2: Configurar AndroidManifest y Permisos**

Archivos a modificar:
- `app/src/main/AndroidManifest.xml`

Permisos necesarios:
- ✅ `INTERNET` - Para llamadas API
- ✅ `ACCESS_FINE_LOCATION` - Para mapas
- ✅ `ACCESS_COARSE_LOCATION` - Para ubicación aproximada
- ✅ `ACCESS_NETWORK_STATE` - Para detectar conectividad

---

## 📚 Referencias

- [libs.versions.toml](../gradle/libs.versions.toml)
- [build.gradle.kts](../app/build.gradle.kts)
- [Ktor Documentation](https://ktor.io/docs/client.html)
- [Room Documentation](https://developer.android.com/training/data-storage/room)
- [Koin Documentation](https://insert-koin.io/)
