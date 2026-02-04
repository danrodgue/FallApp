# Feature Ninots - Documentación

## Descripción General

La feature **Ninots** gestiona la visualización, consulta y votación de figuras artísticas (ninots) de las fallas valencianas. Los usuarios pueden explorar ninots, ver detalles artísticos, y votar en diferentes categorías.

## Cambios Importantes del Sistema

### Sistema de Votación (Actualizado 2026-02-03)

**ANTES (Diseño Original)**:
- Votos a **fallas completas**
- Tipos: `me_gusta`, `mejor_ninot`, `mejor_tema`
- Restricción: 1 voto por tipo por falla

**AHORA (Implementación Actual)**:
- Votos a **ninots individuales**
- Tipos: `favorito`, `ingenioso`, `critico`, `artistico`, `rating`
- Restricción: 1 voto por tipo por ninot
- Permite votar múltiples ninots de la misma falla

## Arquitectura (Clean Architecture)

```
features/ninots/
├── data/
│   ├── api/
│   │   └── NinotsApiService.kt       # Llamadas HTTP a /api/ninots
│   ├── dto/
│   │   ├── NinotDto.kt               # Response del backend
│   │   └── Mappers.kt                # DTO ↔ Domain ↔ Entity
│   └── repository/
│       └── NinotsRepositoryImpl.kt   # Implementación con Room + API
├── domain/
│   ├── model/
│   │   ├── Ninot.kt                  # Modelo de dominio
│   │   └── TipoVoto.kt               # Enum: FAVORITO, INGENIOSO, CRITICO, ARTISTICO
│   ├── repository/
│   │   └── NinotsRepository.kt       # Interface del repositorio
│   └── usecase/
│       ├── GetNinotsUseCase.kt       # Obtener lista de ninots
│       ├── GetNinotByIdUseCase.kt    # Detalle de ninot
│       ├── GetNinotsByFallaUseCase.kt# Ninots de una falla específica
│       ├── GetNinotsPremiados.kt     # Ranking de premiados/más votados
│       └── VotarNinotUseCase.kt      # Registrar voto (POST /api/votos)
└── presentation/
    ├── list/
    │   ├── NinotsListScreen.kt       # Pantalla lista
    │   ├── NinotsListViewModel.kt
    │   └── NinotsListUiState.kt
    ├── detail/
    │   ├── NinotDetailScreen.kt      # Pantalla detalle
    │   ├── NinotDetailViewModel.kt
    │   └── NinotDetailUiState.kt
    └── vote/
        ├── VoteDialog.kt             # Diálogo para votar
        └── VoteUiState.kt
```

## Modelos de Datos

### Domain Model: Ninot
```kotlin
data class Ninot(
    val idNinot: Long,
    val idFalla: Long,
    val nombreFalla: String,
    
    // Información básica
    val nombreNinot: String,
    val tituloObra: String,
    val descripcion: String?,
    
    // Dimensiones
    val dimensiones: Dimensiones?,
    
    // Técnica artística
    val materialPrincipal: String?,
    val artistaConstructor: String?,
    val anyoConstruccion: Int?,
    
    // Multimedia
    val urlImagenPrincipal: String?,
    val imagenes: List<String>,
    
    // Premios
    val premiado: Boolean,
    val categoriaPremio: String?,
    val anyoPremio: Int?,
    
    // Estadísticas de votos
    val estadisticasVotos: EstadisticasVotos,
    
    // Auditoría
    val notasTecnicas: String?,
    val fechaCreacion: LocalDateTime,
    val actualizadoEn: LocalDateTime?
)

data class Dimensiones(
    val alturaMetros: Double,
    val anchoMetros: Double,
    val profundidadMetros: Double?,
    val pesoToneladas: Double?
)

data class EstadisticasVotos(
    val totalVotos: Int,
    val votosFavorito: Int,
    val votosIngenioso: Int,
    val votosCritico: Int,
    val votosArtistico: Int
)

enum class TipoVoto {
    FAVORITO,     // Voto general a ninot preferido
    INGENIOSO,    // Voto a ninot más ingenioso
    CRITICO,      // Voto crítico/satírico
    ARTISTICO,    // Voto por calidad artística
    RATING        // Valoración general (1-5 estrellas) - NO IMPLEMENTADO AÚN
}
```

### Room Entity: NinotEntity
```kotlin
@Entity(tableName = "ninots")
data class NinotEntity(
    @PrimaryKey val idNinot: Long,
    val idFalla: Long,
    val nombreFalla: String,
    val nombreNinot: String,
    val tituloObra: String,
    val descripcion: String?,
    
    // Dimensiones
    val alturaMetros: Double?,
    val anchoMetros: Double?,
    val profundidadMetros: Double?,
    val pesoToneladas: Double?,
    
    // Técnica
    val materialPrincipal: String?,
    val artistaConstructor: String?,
    val anyoConstruccion: Int?,
    
    // Multimedia
    val urlImagenPrincipal: String?,
    val imagenes: List<String>,
    
    // Premios
    val premiado: Boolean,
    val categoriaPremio: String?,
    val anyoPremio: Int?,
    
    // Estadísticas
    val totalVotos: Int,
    val votosFavorito: Int,
    val votosIngenioso: Int,
    val votosCritico: Int,
    val votosArtistico: Int,
    
    // Auditoría
    val notasTecnicas: String?,
    val fechaCreacion: LocalDateTime,
    val actualizadoEn: LocalDateTime?,
    val lastSyncTime: LocalDateTime
)
```

## API Endpoints

### GET /api/ninots
Lista paginada de ninots.

**Query Parameters**:
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 20)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "idNinot": 1,
      "idFalla": 5,
      "nombreFalla": "Falla Plaza del Ayuntamiento",
      "nombreNinot": "El Político",
      "tituloObra": "Promesas Vacías",
      "descripcion": "Ninot satírico sobre...",
      "alturaMetros": 15.5,
      "anchoMetros": 8.2,
      "materialPrincipal": "Cartón piedra",
      "artistaConstructor": "Juan Pérez",
      "anyoConstruccion": 2026,
      "urlImagenPrincipal": "https://...",
      "premiado": false,
      "totalVotos": 342,
      "votosFavorito": 120,
      "votosIngenioso": 95,
      "votosCritico": 80,
      "votosArtistico": 47
    }
  ]
}
```

### GET /api/ninots/{id}
Detalle de un ninot específico.

### GET /api/ninots/falla/{idFalla}
Ninots de una falla específica.

### GET /api/ninots/premiados
Ranking de ninots más votados/premiados.

**Query Parameters**:
- `tipo`: Tipo de voto para ordenar (`favorito`, `ingenioso`, `critico`, `artistico`)
- `limite`: Número máximo de resultados (default: 10)

### POST /api/votos
Registrar un voto a un ninot.

**Request Body**:
```json
{
  "idNinot": 1,
  "tipoVoto": "favorito"
}
```

**Validaciones**:
- Usuario debe estar autenticado (JWT)
- No puede votar dos veces el mismo tipo al mismo ninot
- Tipos válidos: `favorito`, `ingenioso`, `critico`, `artistico`

**Response 201 Created**:
```json
{
  "success": true,
  "message": "Voto registrado",
  "data": {
    "idVoto": 123,
    "idNinot": 1,
    "tipoVoto": "favorito",
    "fechaCreacion": "2026-02-03T10:30:00"
  }
}
```

**Response 409 Conflict** (voto duplicado):
```json
{
  "success": false,
  "error": "VOTO_DUPLICADO",
  "message": "Ya has votado este ninot con tipo 'favorito'"
}
```

### GET /api/votos/ninot/{idNinot}
Lista de votos de un ninot específico.

### GET /api/votos/usuario/{idUsuario}
Lista de votos del usuario autenticado.

### DELETE /api/votos/{idVoto}
Eliminar un voto propio.

## Casos de Uso

### 1. Listar Ninots
```kotlin
class GetNinotsUseCase(
    private val repository: NinotsRepository
) {
    suspend operator fun invoke(
        forceRefresh: Boolean = false
    ): Flow<Result<List<Ninot>>> {
        return repository.getAllNinots(forceRefresh)
    }
}
```

**Lógica**:
1. Intenta obtener desde API
2. Guarda en caché local (Room)
3. Si falla, devuelve datos cacheados
4. Permite forzar refresh ignorando caché

### 2. Votar Ninot
```kotlin
class VotarNinotUseCase(
    private val repository: NinotsRepository
) {
    suspend operator fun invoke(
        idNinot: Long,
        tipoVoto: TipoVoto
    ): Result<Voto> {
        // Validaciones
        if (idNinot <= 0) return Result.Error("ID inválido")
        
        // Registrar voto vía API
        return repository.votarNinot(idNinot, tipoVoto)
    }
}
```

**Validaciones**:
- ID de ninot válido
- Usuario autenticado (token JWT)
- Tipo de voto válido
- No voto duplicado (backend valida)

### 3. Ver Ranking
```kotlin
class GetNinotsPremiados(
    private val repository: NinotsRepository
) {
    suspend operator fun invoke(
        tipoVoto: TipoVoto? = null,
        limite: Int = 10
    ): Flow<Result<List<Ninot>>> {
        return repository.getNinotsPremiados(tipoVoto, limite)
    }
}
```

## Pantallas UI

### NinotsListScreen
- Grid de ninots con imágenes
- Filtros: por falla, por premiados, por tipo de voto
- Búsqueda por nombre/título
- Pull-to-refresh
- Navegación a detalle

### NinotDetailScreen
- Imagen principal (full-width)
- Galería de imágenes adicionales
- Información básica (nombre, título, descripción)
- Dimensiones y técnica artística
- Artista constructor
- Estadísticas de votos (gráfico de barras)
- Botones de votación (4 tipos)
- Indicador si ya votó

### VoteDialog
- Selección de tipo de voto
- Descripción de cada tipo
- Confirmación
- Feedback de éxito/error

## Estrategia Offline-First

1. **Primera carga**: Descarga desde API → Guarda en Room
2. **Cargas subsecuentes**: Lee desde Room (instantáneo)
3. **Refresh**: Pull-to-refresh actualiza desde API
4. **Votación**: Requiere conexión (POST a API)
5. **Sincronización**: `lastSyncTime` para determinar si refrescar

## Testing

### Unit Tests
- `NinotsRepositoryTest`: Verificar lógica de caché
- `VotarNinotUseCaseTest`: Validaciones de voto
- `MappersTest`: Conversiones DTO ↔ Domain ↔ Entity

### Integration Tests
- `NinotsApiServiceTest`: Endpoints funcionando
- `NinotDaoTest`: Queries de Room correctas
- End-to-end: Listar → Detalle → Votar → Verificar

## Dependencias Koin

```kotlin
val ninotsModule = module {
    // Data Layer
    single { NinotsApiService(httpClient = get()) }
    single<NinotsRepository> {
        NinotsRepositoryImpl(
            apiService = get(),
            ninotDao = get(),
            votoDao = get(),
            networkMonitor = get()
        )
    }
    
    // Domain Layer
    factory { GetNinotsUseCase(repository = get()) }
    factory { GetNinotByIdUseCase(repository = get()) }
    factory { GetNinotsByFallaUseCase(repository = get()) }
    factory { GetNinotsPremiados(repository = get()) }
    factory { VotarNinotUseCase(repository = get()) }
    
    // Presentation Layer
    viewModel { NinotsListViewModel(/* UseCases */) }
    viewModel { NinotDetailViewModel(/* UseCases */) }
}
```

## Próximos Pasos

1. ✅ Documentar sistema de votos actualizado
2. ⏳ Implementar Domain Layer (modelos, repository interface, use cases)
3. ⏳ Implementar Data Layer (API service, DTOs, repository impl)
4. ⏳ Implementar Presentation Layer (screens, ViewModels)
5. ⏳ Integrar con navegación principal
6. ⏳ Tests unitarios e integración

## Referencias

- Backend: `/01.backend/src/main/java/com/fallapp/model/Ninot.java`
- API Docs: `/01.backend/README_API.md` (endpoints de ninots)
- Base de datos: `/07.datos/scripts/01.schema.sql` (tabla ninots)
- Documentación general: `/04.docs/especificaciones/03.BASE-DATOS.md`
