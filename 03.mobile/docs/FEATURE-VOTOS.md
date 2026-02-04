# 🎭 Sistema de Votos - FallApp Mobile

**Versión:** 1.0.0  
**Fecha:** 2026-02-04  
**Estado:** ✅ Completado y Funcional

---

## 📋 Tabla de Contenidos

1. [Resumen](#resumen)
2. [Arquitectura](#arquitectura)
3. [Endpoints API](#endpoints-api)
4. [Modelos de Dominio](#modelos-de-dominio)
5. [Pantalla de Votos](#pantalla-de-votos)
6. [Use Cases](#use-cases)
7. [Integración con Koin](#integración-con-koin)
8. [Uso en la Aplicación](#uso-en-la-aplicación)

---

## 📌 Resumen

El sistema de votos permite a los usuarios votar por fallas con tres tipos de votos diferentes:
- **😄 Ingenioso**: Para mensajes ingeniosos y creativos
- **💭 Crítico**: Para crítica social relevante
- **🎨 Artístico**: Para gran valor artístico

### Características Principales

✅ **Votar por fallas** con confirmación antes de registrar el voto  
✅ **Ver mis votos** con opción de eliminar cada uno  
✅ **Ranking de fallas** más votadas con filtros por tipo de voto  
✅ **Restricción**: 1 voto por tipo por falla por usuario  
✅ **Navegación** directa a detalles de falla desde cualquier pantalla de votos  
✅ **Feedback visual** con snackbars para éxito y errores

---

## 🏗️ Arquitectura

El sistema de votos sigue **Clean Architecture** con tres capas:

```
features/
├── fallas/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Voto.kt                    // Modelos de dominio
│   │   │   ├── TipoVoto.kt                // Enum de tipos de voto
│   │   │   └── VotoRequest.kt             // Request para crear voto
│   │   ├── repository/
│   │   │   └── VotosRepository.kt         // Interface del repositorio
│   │   └── usecase/
│   │       ├── VotarFallaUseCase.kt       // Crear voto
│   │       ├── GetVotosUsuarioUseCase.kt  // Obtener votos del usuario
│   │       ├── EliminarVotoUseCase.kt     // Eliminar voto
│   │       └── GetVotosFallaUseCase.kt    // Obtener votos de una falla
│   ├── data/
│   │   ├── api/
│   │   │   └── VotosApiService.kt         // Cliente HTTP (Ktor)
│   │   ├── dto/
│   │   │   ├── VotoDto.kt                 // DTOs para API
│   │   │   └── Mappers.kt                 // Conversión DTO ↔ Domain
│   │   └── repository/
│   │       └── VotosRepositoryImpl.kt     // Implementación del repositorio
│   └── di/
│       └── FallasModule.kt                // Dependencias Koin
└── votos/
    └── presentation/
        ├── VotosScreen.kt                 // UI con 3 tabs
        └── VotosViewModel.kt              // Lógica de presentación
```

---

## 🌐 Endpoints API

### Base URL
```
http://35.180.21.42:8080
```

### 1. POST /api/votos - Crear Voto
**Autenticación:** Requerida (JWT Bearer Token)

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

### 2. GET /api/votos/usuario/{idUsuario}
**Autenticación:** Requerida (solo propio usuario o ADMIN)

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Votos obtenidos",
  "datos": [
    {
      "idVoto": 789,
      "idUsuario": 5,
      "nombreUsuario": "María García",
      "idFalla": 23,
      "nombreFalla": "Falla Convento Jerusalén",
      "tipoVoto": "ARTISTICO",
      "fechaCreacion": "2026-02-01T19:05:00"
    }
  ]
}
```

### 3. GET /api/votos/falla/{idFalla}
**Autenticación:** Requerida

Obtiene todos los votos de una falla específica.

### 4. DELETE /api/votos/{idVoto}
**Autenticación:** Requerida (solo autor del voto)

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Voto eliminado",
  "datos": null
}
```

---

## 📦 Modelos de Dominio

### TipoVoto (Enum)
```kotlin
enum class TipoVoto {
    INGENIOSO,  // 😄 Mensaje ingenioso y creativo
    CRITICO,    // 💭 Crítica social relevante
    ARTISTICO;  // 🎨 Gran valor artístico

    fun getDisplayName(): String = when(this) {
        INGENIOSO -> "😄 Ingenioso"
        CRITICO -> "💭 Crítico"
        ARTISTICO -> "🎨 Artístico"
    }
    
    fun getDescription(): String = when(this) {
        INGENIOSO -> "Mensaje ingenioso y creativo"
        CRITICO -> "Crítica social relevante"
        ARTISTICO -> "Gran valor artístico"
    }
}
```

### Voto (Domain Model)
```kotlin
data class Voto(
    val idVoto: Long,
    val idUsuario: Long,
    val nombreUsuario: String,
    val idFalla: Long,
    val nombreFalla: String,
    val tipoVoto: TipoVoto,
    val fechaCreacion: LocalDateTime? = null
)
```

### VotoRequest
```kotlin
data class VotoRequest(
    val idNinot: Long,
    val tipoVoto: TipoVoto
)
```

### EstadisticasVotos
```kotlin
data class EstadisticasVotos(
    val totalVotos: Int,
    val votosIngenioso: Int,
    val votosCritico: Int,
    val votosArtistico: Int
)
```

---

## 🎨 Pantalla de Votos

La pantalla de votos (`VotosScreen.kt`) contiene 3 tabs:

### 📍 Tab 1: Votar

**Funcionalidad:**
- Lista de todas las fallas disponibles para votar
- Card elevada por cada falla con:
  - Nombre de la falla
  - Sección
  - 3 botones de voto (Ingenioso, Crítico, Artístico)
- Confirmación mediante diálogo antes de votar
- Click en la falla para ver detalles completos

**Componentes:**
- `VotarTab`: Composable principal del tab
- `FallaVotarCard`: Card individual con botones de voto
- `AlertDialog`: Confirmación antes de votar

**Ejemplo de uso:**
```kotlin
VotarTab(
    fallas = listOf(...),
    isLoading = false,
    onVoteClick = { falla, tipoVoto ->
        viewModel.votar(falla, tipoVoto)
    },
    onFallaClick = { idFalla ->
        navController.navigate(Screen.FallaDetail.createRoute(idFalla))
    }
)
```

### 💝 Tab 2: Mis Votos

**Funcionalidad:**
- Lista de todos los votos del usuario actual
- Card por cada voto mostrando:
  - Emoji y tipo de voto
  - Nombre de la falla
  - Fecha del voto
  - Botón para eliminar el voto
- Estado vacío con icono y mensaje
- Confirmación antes de eliminar

**Componentes:**
- `MisVotosTab`: Composable principal del tab
- `MiVotoCard`: Card individual de voto con botón eliminar

**Ejemplo de uso:**
```kotlin
MisVotosTab(
    votos = listOf(...),
    isLoading = false,
    onDeleteVote = { idVoto ->
        viewModel.eliminarVoto(idVoto)
    },
    onFallaClick = { idFalla ->
        navController.navigate(Screen.FallaDetail.createRoute(idFalla))
    }
)
```

### 🏆 Tab 3: Ranking

**Funcionalidad:**
- Top 20 fallas más votadas
- Filtro por tipo de voto (Todos, Ingenioso, Crítico, Artístico)
- Cards con:
  - Posición (1-20)
  - Colores especiales para top 3 (oro, plata, bronce)
  - Nombre y sección de la falla
  - Contador de votos
- Estado vacío cuando no hay votos
- Click en falla para ver detalles

**Componentes:**
- `RankingTab`: Composable principal con filtros
- `RankingCard`: Card de ranking con posición y contador

**Ejemplo de uso:**
```kotlin
RankingTab(
    ranking = listOf(Pair(falla1, 25), Pair(falla2, 18), ...),
    isLoading = false,
    selectedTipoVoto = TipoVoto.INGENIOSO,
    onFilterChange = { tipoVoto ->
        viewModel.setRankingFilter(tipoVoto)
    },
    onFallaClick = { idFalla ->
        navController.navigate(Screen.FallaDetail.createRoute(idFalla))
    }
)
```

---

## ⚙️ Use Cases

### VotarFallaUseCase
```kotlin
class VotarFallaUseCase(
    private val repository: VotosRepository
) {
    suspend operator fun invoke(request: VotoRequest): Result<Voto> {
        return repository.crearVoto(request)
    }
}
```

**Uso:**
```kotlin
val request = VotoRequest(
    idNinot = 15,
    tipoVoto = TipoVoto.ARTISTICO
)

when (val result = votarFallaUseCase(request)) {
    is Result.Success -> {
        // Voto creado: result.data
    }
    is Result.Error -> {
        // Error: result.message
    }
    is Result.Loading -> {
        // Cargando...
    }
}
```

### GetVotosUsuarioUseCase
```kotlin
class GetVotosUsuarioUseCase(
    private val repository: VotosRepository
) {
    suspend operator fun invoke(idUsuario: Long): Result<List<Voto>> {
        return repository.getVotosUsuario(idUsuario)
    }
}
```

### EliminarVotoUseCase
```kotlin
class EliminarVotoUseCase(
    private val repository: VotosRepository
) {
    suspend operator fun invoke(idVoto: Long): Result<Unit> {
        return repository.eliminarVoto(idVoto)
    }
}
```

### GetVotosFallaUseCase
```kotlin
class GetVotosFallaUseCase(
    private val repository: VotosRepository
) {
    suspend operator fun invoke(idFalla: Long): Result<List<Voto>> {
        return repository.getVotosFalla(idFalla)
    }
}
```

---

## 🔌 Integración con Koin

### FallasModule.kt

```kotlin
val fallasModule = module {
    
    // API Service
    single { VotosApiService(client = get()) }
    
    // Repository
    single<VotosRepository> {
        VotosRepositoryImpl(apiService = get())
    }
    
    // Use Cases
    factory { VotarFallaUseCase(repository = get()) }
    factory { GetVotosUsuarioUseCase(repository = get()) }
    factory { EliminarVotoUseCase(repository = get()) }
    factory { GetVotosFallaUseCase(repository = get()) }
    
    // ViewModel
    viewModel {
        VotosViewModel(
            getFallasUseCase = get(),
            votarFallaUseCase = get(),
            getVotosUsuarioUseCase = get(),
            eliminarVotoUseCase = get(),
            getVotosFallaUseCase = get()
        )
    }
}
```

---

## 💻 Uso en la Aplicación

### Navegación Principal

La pantalla de votos está integrada en el `MainScreen` con Bottom Navigation:

```kotlin
val items = listOf(
    BottomNavItem("Mapa", Icons.Default.LocationOn),      // Tab 0
    BottomNavItem("Fallas", Icons.Default.List),          // Tab 1
    BottomNavItem("Votos", Icons.Default.Star),           // Tab 2 ⭐
    BottomNavItem("Perfil", Icons.Default.Person)         // Tab 3
)
```

### Acceso desde Código

```kotlin
// En MainScreen.kt
when (selectedItem) {
    2 -> VotosScreen(
        onFallaClick = { fallaId ->
            navController.navigate(Screen.FallaDetail.createRoute(fallaId))
        }
    )
}
```

### Flujo de Usuario

1. **Usuario abre tab Votos** → Se cargan fallas, mis votos y ranking
2. **Usuario selecciona una falla** → Aparecen 3 botones de voto
3. **Usuario hace click en tipo de voto** → Aparece diálogo de confirmación
4. **Usuario confirma** → Se envía request a API
5. **API responde exitosamente** → Snackbar de éxito, se recargan datos
6. **Error (ej: voto duplicado)** → Snackbar de error con mensaje

---

## 🎯 Estado UI

### VotosUiState
```kotlin
data class VotosUiState(
    val fallas: List<Falla> = emptyList(),
    val misVotos: List<Voto> = emptyList(),
    val ranking: List<Pair<Falla, Int>> = emptyList(),
    val rankingFilter: TipoVoto? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
```

---

## 🔄 Operaciones Asíncronas

Todas las operaciones del ViewModel son asíncronas usando coroutines:

```kotlin
fun votar(falla: Falla, tipoVoto: TipoVoto) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        
        val request = VotoRequest(
            idNinot = falla.idFalla,
            tipoVoto = tipoVoto
        )
        
        when (val result = votarFallaUseCase(request)) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        successMessage = "¡Voto registrado!",
                        isLoading = false
                    )
                }
                loadMisVotos()
                loadRanking()
            }
            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.message ?: "Error al votar",
                        isLoading = false
                    )
                }
            }
            is Result.Loading -> {}
        }
    }
}
```

---

## ⚠️ Limitaciones y TODOs

### Limitaciones Actuales

1. **ID de Usuario Hardcoded**: Actualmente se usa `idUsuario = 1L` en `loadMisVotos()`
   ```kotlin
   // TODO: Obtener idUsuario desde TokenManager
   val idUsuario = 1L // Temporal
   ```

2. **ID de Ninot**: Se usa `idFalla` como `idNinot` temporalmente
   ```kotlin
   // TODO: Obtener el ID del ninot real cuando esté disponible
   val idNinot = falla.idFalla
   ```

3. **Ranking Performance**: El ranking carga votos de todas las fallas secuencialmente
   - Puede ser lento con muchas fallas
   - Considerar endpoint backend para ranking agregado

### Mejoras Futuras

- [ ] Integrar TokenManager para obtener ID de usuario autenticado
- [ ] Agregar modelo Ninot y obtener ID real
- [ ] Implementar caché local con Room para votos
- [ ] Añadir pull-to-refresh en cada tab
- [ ] Implementar paginación en tab Votar
- [ ] Agregar búsqueda/filtro en tab Votar
- [ ] Mostrar estadísticas personales del usuario
- [ ] Notificaciones push cuando alguien vota tu falla
- [ ] Modo offline con sincronización

---

## 🐛 Troubleshooting

### Error: "Ya has votado por esta falla"

**Causa:** Intentando votar el mismo tipo de voto por segunda vez en la misma falla.

**Solución:** El sistema permite 1 voto de cada tipo por falla. Elimina el voto existente primero desde "Mis Votos" si quieres cambiar de opinión.

### Error: "Token JWT inválido o expirado"

**Causa:** El token de autenticación ha expirado (duración: 24 horas).

**Solución:** Cierra sesión y vuelve a iniciar sesión.

### Ranking no se actualiza

**Causa:** El ranking se carga al iniciar el tab y cuando cambias el filtro.

**Solución:** Sal del tab y vuelve a entrar, o cambia el filtro para forzar recarga.

### Mis votos no aparecen

**Causa:** El ID de usuario actualmente es hardcoded a `1L`.

**Solución:** Espera a la integración con TokenManager, o modifica temporalmente el ID en `VotosViewModel.loadMisVotos()`.

---

## 📚 Referencias

- **Guía API Frontend:** `GUIA.API.FRONTEND.md` (raíz del proyecto)
- **Clean Architecture:** https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
- **Material 3 Design:** https://m3.material.io/
- **Jetpack Compose:** https://developer.android.com/jetpack/compose
- **Koin DI:** https://insert-koin.io/

---

**Última actualización:** 2026-02-04  
**Autor:** Equipo FallApp  
**Versión:** 1.0.0
