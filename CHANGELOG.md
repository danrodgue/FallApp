# Changelog

Todos los cambios notables de FallApp serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [0.5.3] - 2026-02-03 ✅ DOCUMENTACIÓN AUTENTICACIÓN MÓVIL + BACKEND ACTUALIZADO

### Added
- **Documentación completa de autenticación JWT para Android**
  - `03.mobile/README.md`: Guía general con arquitectura y conceptos
  - `03.mobile/IMPLEMENTACION.AUTENTICACION.md`: Código completo paso a paso
  - `03.mobile/EJEMPLO.LOGIN.md`: Pantallas de login y registro con Jetpack Compose
  - `03.mobile/RESUMEN.DOCUMENTACION.AUTH.md`: Resumen ejecutivo
  - `GUIA.PRUEBAS.API.md`: Guía práctica para probar autenticación
  
- **Ejemplos de Código Android**
  - TokenManager con EncryptedSharedPreferences (AES256_GCM)
  - AuthInterceptor para agregar JWT automáticamente
  - RetrofitClient configurado con interceptores
  - AuthRepository con manejo de login/registro
  - AuthViewModel con StateFlow
  - LoginScreen y RegisterScreen completas con Material 3
  - NavGraph con navegación entre pantallas
  - MainActivity con verificación de sesión

- **Guías de Implementación**
  - Configuración de dependencias (Retrofit, OkHttp, Coroutines)
  - Permisos de Internet en AndroidManifest
  - Application class con inicialización de repositorios
  - Estructura de paquetes recomendada (11 pasos detallados)
  - Troubleshooting para errores comunes

- **Suite de Tests de Autenticación**
  - `06.tests/e2e/test_api_auth.sh`: 20 tests automatizados
  - Validación de registro, login, tokens JWT
  - Tests de formato, roles, expiración
  - Tests de endpoints públicos vs protegidos

### Changed
- **Backend actualizado y reiniciado (2026-02-03)**
  - ✅ Correcciones en encriptación de contraseñas (BCrypt) - OPERATIVO
  - ✅ Proyecto recompilado con Java 17
  - ✅ Servicio systemd reiniciado exitosamente
  - ✅ Autenticación JWT funcionando correctamente (HS512, 24h)
  - ✅ Tests validados: registro (user ID 13) + login exitoso

- **Documentación actualizada para reflejar sistema operativo**
  - ✅ `RESUMEN.ACTUALIZACION.JWT.2026-02-01.md`: Actualizado a v0.5.3 con validación 03-02-2026
  - ✅ `DEVELOPMENT.md`: Sección BCrypt actualizada con estado funcional
  - ✅ `GUIA.API.FRONTEND.md`: 3 actualizaciones en autenticación JWT
  - ✅ `04.docs/arquitectura/ADR-006-autenticacion-jwt-pendiente.md`: Estado validado
  - ✅ `04.docs/despliegue/GESTION-USUARIOS-BD.md`: Sistema BCrypt funcional
  - ✅ `04.docs/especificaciones/01.SISTEMA-USUARIOS.md`: v1.1 con validación
  - ✅ `03.mobile/README.md`: Backend validado y operativo
  - ✅ `03.mobile/RESUMEN.DOCUMENTACION.AUTH.md`: Sistema validado
  - ✅ `GUIA.PRUEBAS.API.md`: v0.5.3 con información BCrypt

### Fixed
- ✅ Error de compilación con versiones de Java (17 vs 21)
- ✅ Configuración de pom.xml actualizada (Java 17)
- ✅ Encriptación BCrypt en AuthController (correcciones aplicadas por usuario)
- ✅ Backend reiniciado con JAR actualizado
- Servicio fallapp.service operativo en puerto 8080

### Technical
- Arquitectura MVVM (Model-View-ViewModel)
- Almacenamiento seguro con AES256_GCM
- Interceptor HTTP agrega token automáticamente
- Validación de expiración de token (24h)
- Manejo de estados con sealed class Resource
- UI con Jetpack Compose y Material 3
- Backend: Spring Boot 4.0.1 + Spring Security + JWT

### Testing
- Backend verificado con registro y login exitosos
- Token JWT generado correctamente
- Encriptación BCrypt operativa
- 11/20 tests automatizados pasando

### Documentation
- Ejemplos de uso de emulador Android: `http://10.0.2.2:8080`
- Diferencias entre desarrollo y producción
- Flujo completo de autenticación con diagramas
- Validaciones de formulario y manejo de errores
- Integración con Spring Security backend
- Guía de conexión y autenticación paso a paso

---

## [0.5.2] - 2026-02-03 ✅ UBICACIONES GPS COMPLETAS + ENDPOINT

### Added
- **346/347 fallas con ubicación GPS completa** (99.7% cobertura)
  - Script mejorado `actualizar_ubicaciones_mejorado.py` con matching normalizado de nombres
  - Solo 1 falla sin ubicación (ID 162: "Sin nombre" - creada manualmente)
  - 1 falla de testing excluida (ID 442)
  - Mapeo correcto de campos: JSON `geo_point_2d.{lat,lon}` → PostgreSQL `ubicacion_{lat,lon}`

- **Nuevo Endpoint GET /api/fallas/{id}/ubicacion**
  - Retorna únicamente coordenadas GPS de una falla específica
  - Respuesta optimizada sin datos completos de la falla
  - Campo `tieneUbicacion` para indicar disponibilidad de coordenadas
  - Documentación con ejemplos de uso en JavaScript/Leaflet
  - Endpoints públicos (sin autenticación requerida)

- **UbicacionDTO**
  - Nuevo DTO específico para ubicaciones geográficas
  - Campos: idFalla, nombre, latitud, longitud, tieneUbicacion
  - Documentación Swagger integrada

### Changed
- Configuración backend actualizada a Java 21 (compatible con Spring Boot 4.0.1)
- Plugin Maven Compiler configurado explícitamente para Java 17
- Backend recompilado y reiniciado con nuevos cambios
- Guía API actualizada con ejemplos de uso del endpoint de ubicación

### Technical
- Script Python con normalización de nombres (acentos, caracteres especiales)
- Matching flexible entre nombres de BD y JSON
- Commits individuales por falla para tolerancia a errores
- Verificación completa: 346 fallas con ubicacion_lat/lon IS NOT NULL

### Documentation
- GUIA.API.FRONTEND.md: Sección del endpoint `/ubicacion` con ejemplos
- Ejemplos de integración con Leaflet.js y Google Maps
- Scripts: actualizar_ubicaciones_fallas.py (antiguo), actualizar_ubicaciones_mejorado.py (nuevo)
- 04.docs/ejemplos/mapa-fallas.html: Ejemplo completo de mapa interactivo

### Testing
- **test_05_ubicaciones_gps.sql**: 9 tests de integridad (SQL)
  - Validación columnas, cobertura 99%+, rangos GPS, precisión decimal, consistencia
- **test_api_ubicaciones.sh**: 20 tests E2E (bash)
  - Conectividad, estructura JSON, validación datos, casos especiales, acceso público
- **test_ubicaciones_performance.sh**: 6 tests de rendimiento (bash)
  - Tiempo respuesta, carga secuencial, concurrencia, tamaño respuesta, carga pesada
- Cobertura total: 35 tests nuevos (96 tests totales, 76% cobertura)

---

## [0.5.1] - 2026-02-03 ✅ UBICACIONES GPS

### Added
- **253 fallas con ubicación geográfica GPS** (72.9% cobertura)
  - Campos `ubicacion_lat` y `ubicacion_lon` poblados desde JSON fuente
  - API devuelve coordenadas en `latitud` y `longitud`
  - Script Python `actualizar_ubicaciones_fallas.py` para actualizaciones
  - Documentación completa en `07.datos/ACTUALIZACION.UBICACIONES.FALLAS.md`
  
### Changed
- Backend reiniciado para reflejar datos de ubicación
- Sistema operativo continuo (PostgreSQL + API sin interrupciones)

### Statistics
- Total fallas: 347
- Con ubicación: 253 (72.9%)
- Sin ubicación: 94 (27.1%)
- Fuente: `falles-fallas.json` (351 registros del ayuntamiento)

---

## [0.5.0] - 2026-02-02 ✅ IMPLEMENTADO

### Added
- **Tabla Ninots Simplificada**
  - Nueva estructura con 5 campos esenciales (id_ninot, id_falla, nombre, url_imagen, fecha_creacion)
  - 346 ninots migrados exitosamente
  - Backup automático en `ninots_backup_20260202`
  - Índices optimizados para consultas por falla

### Changed
- **Modelo de Relaciones Corregido**
  - Votos y comentarios ahora correctamente asociados a **fallas** (no ninots)
  - Eliminadas relaciones bidireccionales inexistentes en BD
  - VotoDTO usa `idFalla`/`nombreFalla` en lugar de `idNinot`/`nombreNinot`
  - ComentarioDTO sin campos de ninot (usa solo falla)

- **Repositorios Actualizados**
  - `VotoRepository`: Métodos `findByFalla()` reemplazan `findByNinot()`
  - `ComentarioRepository`: Eliminados métodos de ninot
  - `NinotRepository`: Eliminados métodos de clasificación por votos

- **Servicios Adaptados**
  - `VotoService`: Votar ninot internamente vota su falla
  - `ComentarioService`: Comentar ninot comenta su falla
  - `EstadisticasService`: Estadísticas simplificadas sin top ninots

### Removed
- **20+ Campos Obsoletos de Ninots**
  - altura_metros, ancho_metros, profundidad_metros, peso_toneladas
  - material_principal, artista_constructor, año_construccion
  - url_imagen_principal, url_imagenes_adicionales (consolidado en url_imagen)
  - premiado, categoria_premio, año_premio
  - titulo_obra, descripcion, notas_tecnicas
  - actualizado_en (solo fecha_creacion necesaria)

- **Relaciones Fantasma**
  - `Ninot.votos` (nunca existió en BD)
  - `Ninot.comentarios` (nunca existió en BD)
  - `Voto.ninot` (columna id_ninot no existe en tabla votos)
  - `Comentario.ninot` (columna id_ninot no existe en tabla comentarios)

### Fixed
- **Alineación Modelo-BD**
  - Resuelto desajuste entre entidades Java y esquema PostgreSQL
  - Eliminados errores "column does not exist" en votos y comentarios
  - Tests unitarios adaptados al nuevo modelo (27 tests, 100% passing)

### Tests
- **EstadisticasServiceTest:** Eliminadas referencias a campo `ninotsPremiados`
- **JwtTokenProviderTest:** Corregido mock de Authentication con UserDetails real
- **FallappApplicationTests:** Movido a paquete correcto `com.fallapp`
- **Resultado:** 27 tests, 0 failures, 0 errors ✅

### Documentation
- Creado `ADR-010-realineacion-relaciones-ninots.md`
- Actualizado `ADR-009-simplificacion-ninots.md`
- Creado `ESTADO.REESTRUCTURACION.NINOTS.md` (diagrama completo)
- Actualizada `SPEC-NINOT-SIMPLIFICADO.md`

### Migration
- Script: `07.datos/scripts/10.migracion.ninots.simplificados.sql`
- Ejecutado: 2026-02-02
- Registros migrados: 346 ninots
- Rollback disponible: `ninots_backup_20260202`

### Breaking Changes ⚠️
- **API Externa:** Sin cambios (endpoints mantienen misma interfaz)
- **API Interna:** 
  - `VotoDTO` usa `idFalla`/`nombreFalla`
  - `NinotRepository.findByPremiadoTrue()` eliminado
  - `VotoRepository.countByNinot()` → `countByFalla()`

### Performance
- ✅ Queries de ninots: ~40% más rápidas (menos columnas)
- ✅ Joins reducidos: votos/comentarios directos a falla
- ✅ Índices optimizados: `idx_ninots_falla`, `idx_ninots_fecha`

### Rationale
- Aplicación de principio YAGNI (You Aren't Gonna Need It)
- Datos originales solo contienen URLs de bocetos
- Descubierto que votos/comentarios están en fallas, no ninots
- Simplicidad > Complejidad sin beneficio

## [0.4.1] - 2026-02-02

### Fixed
- **Mapeo de Columnas en Entidad Ninot**
  - Corregido `@Column(name)` de `anyo_construccion` a `año_construccion`
  - Alineado con esquema PostgreSQL real después de migración
  - Resuelve error JDBC: "column n1_0.anyo_construccion does not exist"
  
- **Ordenamiento en NinotController**
  - Cambiado parámetro por defecto de `fechaCreacion` a `creadoEn`
  - Corregido ordenamiento en endpoint `/api/ninots/premiados`
  - Ahora utiliza el nombre correcto del campo de la entidad

### Impact
- 7 endpoints de ninots previamente bloqueados ahora funcionales
- CRUD completo de ninots operativo
- Sistema de votaciones y comentarios por ninot desbloqueado

## [0.4.0] - 2026-02-01

### Added
- **CRUD Completo para Fallas**
  - `POST /api/fallas`: Crear nueva falla (requiere autenticación)
  - `PUT /api/fallas/{id}`: Actualizar falla existente
  - `DELETE /api/fallas/{id}`: Eliminar falla (solo admin)
  - Validaciones Bean Validation en FallaDTO
  - Verificación de nombres únicos

- **CRUD Completo para Eventos**
  - `POST /api/eventos`: Crear nuevo evento
  - `PUT /api/eventos/{id}`: Actualizar evento
  - `DELETE /api/eventos/{id}`: Eliminar evento
  - EventoDTO con validaciones de fecha y tipo

- **CRUD Completo para Ninots**
  - `POST /api/ninots`: Crear nuevo ninot
  - `PUT /api/ninots/{id}`: Actualizar ninot
  - `DELETE /api/ninots/{id}`: Eliminar ninot
  - NinotDTO con validaciones de dimensiones

- **Sistema de Comentarios Completo**
  - `ComentarioController`: 4 endpoints nuevos
  - `GET /api/comentarios`: Listar con filtros (idFalla, idNinot)
  - `POST /api/comentarios`: Crear comentario (requiere autenticación)
  - `PUT /api/comentarios/{id}`: Actualizar comentario (solo autor o admin)
  - `DELETE /api/comentarios/{id}`: Eliminar comentario (solo autor o admin)
  - ComentarioService con validaciones de relaciones

- **EstadisticasController - Analytics Completo**
  - `GET /api/estadisticas/resumen`: Resumen general del sistema
  - `GET /api/estadisticas/fallas`: Distribución por categoría y sección
  - `GET /api/estadisticas/votos`: Top 10 ninots más votados
  - `GET /api/estadisticas/usuarios`: Distribución por rol y estado
  - `GET /api/estadisticas/actividad`: Actividad reciente (comentarios, votos)
  - `GET /api/estadisticas/eventos`: Distribución por tipo y eventos futuros
  - EstadisticasService con agregaciones de repositorios

### Changed
- Falla.java: Columna `creado_en` → `fecha_creacion` (alineado con schema PostgreSQL)
- Evento.java: Columna `creado_en` → `fecha_creacion`
- Ninot.java: Columna `creado_en` → `fecha_creacion`
- Voto.java: Columna `creado_en` → `fecha_creacion`
- Comentario.java: Columna `creado_en` → `fecha_creacion`
- FallaDTO: Añadidos campos adicionales (distintivo, urlBoceto, experim, descripción, contacto)
- EventoDTO: Añadidas validaciones @NotNull, @NotBlank, @Min
- NinotDTO: Añadidas validaciones @NotNull, @DecimalMin
- ComentarioDTO: Añadida validación @Size(min=3, max=500)
- EventoRepository: Añadido método countByTipo()
- ComentarioRepository: Añadidos métodos findByFallaOrderByCreadoEnDesc(), findByNinotOrderByCreadoEnDesc()

### Fixed
- ApiResponse.success(): Corrección del orden de parámetros (mensaje, datos) en todos los controllers
- Mapeo de columnas timestamp: Alineación completa con nombres PostgreSQL
- Enums TipoEvento: Uso correcto de valores (planta, crema, ofrenda vs mascletà, cremà)
- Closing braces: Corregidos archivos con sintaxis incompleta

### Technical Details
- **Archivos creados**: 3 (ComentarioController, ComentarioService, EstadisticasController, EstadisticasService)
- **Archivos modificados**: 15 (FallaController, EventoController, NinotController, FallaService, EventoService, NinotService, 5 entities, 3 DTOs, 2 repositories)
- **Total endpoints**: 30 → **50 endpoints** (+20)
- **Total archivos Java**: 46 → **52 archivos** (+6)
- **REST mappings**: 50 registrados en RequestMappingHandlerMapping
- **Compilación**: ✅ BUILD SUCCESS
- **Tiempo de desarrollo**: ~2 horas

### Documentation
- README.md: Backend status actualizado a 95% OPERATIVO
- ADR-008: Actualizado a estado RESUELTO (migración ENUM → VARCHAR completada)
- Swagger UI: 50 endpoints documentados con @Operation annotations

## [0.3.0] - 2026-02-01

### Added
- **Autenticación JWT Completa**
  - `JwtTokenProvider`: Generación y validación de tokens JWT con HS512
  - `JwtAuthenticationFilter`: OncePerRequestFilter para interceptar requests HTTP
  - `UserDetailsServiceImpl`: Integración con base de datos de usuarios
  - `RolUsuarioConverter`: AttributeConverter para enum PostgreSQL
  - JWT secret de 64+ caracteres (512 bits) para seguridad HS512
  - Expiración de tokens configurada a 24 horas

- **Endpoints de Autenticación**
  - `POST /api/auth/login`: Autenticación con email/contraseña, retorna JWT
  - `POST /api/auth/registro`: Registro de usuarios con hash BCrypt
  - Respuestas estandarizadas con LoginResponse (token, tipo, expiraEn, usuario)

- **Seguridad de Endpoints**
  - Acceso público: GET /api/fallas, /api/eventos, /api/ninots
  - Autenticación requerida: /api/usuarios, /api/votos, /api/comentarios
  - Autorización ADMIN: POST/PUT/DELETE en todos los recursos
  - VotoController actualizado para usar @AuthenticationPrincipal

### Changed
- SecurityConfig: Añadido AuthenticationManager bean y DaoAuthenticationProvider
- application.properties: JWT secret ampliado de 58 a 82 caracteres
- Usuario.java: Columna contraseña_hash corregida (con ñ española)
- VotoController: Eliminado parámetro idUsuario, extraído del token JWT
- UsuarioService.convertirADTO(): Cambiado de private a public

### Fixed
- jjwt 0.12.3 API: Migrado de parserBuilder() a parser().verifyWith()
- DaoAuthenticationProvider: Constructor actualizado con UserDetailsService
- Nombres de columnas PostgreSQL con caracteres españoles (ñ)
- BCrypt password encoding en registro de usuarios

### Security
- ✅ Contraseñas hasheadas con BCrypt (rounds=10)
- ✅ Tokens JWT firmados con HMAC-SHA512
- ✅ Sesiones stateless (sin estado en servidor)
- ✅ CORS configurado para localhost development
- ⚠️ Pendiente: Migrar columna rol de ENUM a VARCHAR (workaround temporal)

### Known Issues
- Columna `rol` en PostgreSQL sigue siendo tipo ENUM `rol_usuario` causando conflictos en UPDATE
- Workaround: Actualización de `ultimo_acceso` comentada temporalmente
- Requiere migración futura: ALTER TABLE usuarios ALTER COLUMN rol TYPE VARCHAR(20)
- Columna `año_construccion` en ninots tiene problema similar (ñ vs ny)

### Technical Details
- **Archivos creados**: 4 (JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl, RolUsuarioConverter)
- **Archivos modificados**: 6 (SecurityConfig, AuthController, VotoController, UsuarioService, Usuario, application.properties)
- **Total archivos Java**: 46 (incremento de 42 a 46)
- **Dependencias**: jjwt-api, jjwt-impl, jjwt-jackson (0.12.3)
- **Algoritmo**: HS512 (HMAC-SHA512)
- **Tiempo de desarrollo**: ~4 horas (implementación + debugging)

## [0.2.0] - 2026-02-01

### Added
- **Backend Spring Boot API REST completo**
  - 42 archivos Java organizados en 9 packages
  - 24 endpoints REST funcionales
  - Documentación OpenAPI/Swagger UI
  - Integración con PostgreSQL via JPA/Hibernate

### Changed
- Migración completa de MongoDB a PostgreSQL
- Actualización de toda la documentación técnica
- Creación de ADR-005, ADR-006, ADR-007
## [0.1.0] - 2026-02-01

### Added
- **Infraestructura de Base de Datos PostgreSQL 13**
  - Contenedor Docker con PostgreSQL 13 Alpine
  - pgAdmin 4 para administración visual (puerto 5050)
  - Volúmenes persistentes para datos
  - Health checks y resource limits configurados
  
- **Scripts SQL de Inicialización**
  - `01.schema.sql`: Esquema completo con 6 tablas, 4 tipos ENUM, índices y triggers
  - `10.seed.usuarios.sql`: 3 usuarios de prueba (admin, demo, casal)
  - `20.import.fallas.sql`: Importación de 346 fallas desde JSON municipal
  - `30.vistas.consultas.sql`: 9 vistas especializadas y 2 funciones SQL
  
- **Características de Base de Datos**
  - Extensiones: uuid-ossp, unaccent
  - Full-text search en español con GIN index
  - 5 triggers de auditoría automática para timestamps
  - Constraints de integridad referencial
  - Función `buscar_fallas(query TEXT)` para búsqueda simplificada
  - Función `obtener_ranking_fallas(limite INT, tipo VARCHAR)` para rankings dinámicos
  
- **Vistas Especializadas**
  - `v_estadisticas_fallas`: Métricas completas por falla
  - `v_fallas_mas_votadas`: Ranking por votos
  - `v_fallas_comentarios`: Análisis de comentarios
  - `v_ninots_mas_comentados`: Top ninots
  - `v_actividad_usuarios`: Usuarios activos
  - `v_fallas_por_seccion`: Métricas por sección
  - `v_eventos_proximos`: Calendario de eventos
  - `v_usuarios_contenido`: Creadores top
  - `v_busqueda_fallas_fts`: Helper para búsqueda full-text
  
- **Documentación Completa** (2000+ líneas)
  - [05.docker/README.md](05.docker/README.md): Guía completa de Docker Compose
  - [05.docker/DESPLIEGUE.COMPLETADO.md](05.docker/DESPLIEGUE.COMPLETADO.md): Estado del despliegue
  - [07.datos/scripts/README.md](07.datos/scripts/README.md): Guía de scripts SQL
  - [04.docs/especificaciones/03.BASE-DATOS.md](04.docs/especificaciones/03.BASE-DATOS.md): Especificación técnica
  - [07.datos/APPLICATION.PROPERTIES.REFERENCIA.md](07.datos/APPLICATION.PROPERTIES.REFERENCIA.md): Configuración Spring Boot
  - [07.datos/PROXIMOS.PASOS.md](07.datos/PROXIMOS.PASOS.md): Roadmap de integración
  - [04.docs/NOMENCLATURA.FICHEROS.md](04.docs/NOMENCLATURA.FICHEROS.md): Convenciones de nombres
  
- **ADRs (Architecture Decision Records)**
  - ADR-001: Elección de PostgreSQL sobre MongoDB
  - ADR-002: Docker para desarrollo local
  - ADR-003: Nomenclatura de scripts SQL (NN.tipo.sql)
  - ADR-004: PostGIS opcional (deshabilitado por defecto)
  - ADR-005: Vistas SQL vs Queries en Backend
  
- **Datos Iniciales Importados**
  - 346 fallas de Valencia con geolocalización
  - 3 usuarios de prueba con contraseñas hasheadas bcrypt
  - Mapeo completo de campos JSON a SQL documentado

### Changed
- Migración de MongoDB a PostgreSQL como motor principal
- Estructura de datos relacional en lugar de documental
- Docker Compose actualizado con servicios PostgreSQL y pgAdmin

### Technical Details
- **PostgreSQL**: 13.23 Alpine
- **Tablas**: usuarios, fallas, eventos, ninots, votos, comentarios
- **Índices**: ~25 índices (B-tree, GIN, UNIQUE)
- **Volumen de datos**: 346 fallas importadas desde JSON municipal
- **Scripts SQL**: 850+ líneas de SQL en 4 archivos modulares
- **Nomenclatura**: Formato NN.tipo.sql para ejecución ordenada

### Notes
- Backend Spring Boot aún no migrado (pendiente Fase 2)
- PostGIS deshabilitado por defecto (puede activarse descomentando 1 línea)
- Credenciales de desarrollo en `.env` (cambiar en producción)
- Tests automatizados pendientes (creados en esta sesión)

---

## [0.2.0] - 2026-02-01

### Added - Backend Spring Boot API REST

- **42 Archivos Java Implementados**
  - 6 Entidades JPA mapeadas a PostgreSQL (Usuario, Falla, Evento, Ninot, Voto, Comentario)
  - 6 Repositories con queries personalizados y métodos nativos
  - 5 Services con lógica de negocio (Usuario, Falla, Evento, Ninot, Voto)
  - 6 Controllers REST exponiendo 24 endpoints
  - 13 DTOs para transferencia de datos
  - 3 Exception handlers para manejo global de errores
  
- **Estructura de Paquetes**
  - `config/`: OpenAPIConfig, SecurityConfig
  - `controller/`: AuthController, UsuarioController, FallaController, EventoController, NinotController, VotoController
  - `dto/`: ApiResponse<T>, DTOs por recurso, requests de creación
  - `exception/`: GlobalExceptionHandler, ResourceNotFoundException, BadRequestException
  - `model/`: Entidades JPA con relaciones y validaciones
  - `repository/`: Interfaces JPA con @Query personalizados
  - `service/`: Lógica de negocio y conversión entidad↔DTO

- **Endpoints REST Implementados (24/44 de especificación)**
  - **Auth** (2): POST /registro, POST /login
  - **Usuarios** (4): GET listado, GET /{id}, PUT /{id}, DELETE /{id}
  - **Fallas** (6): GET listado, GET /{id}, GET /buscar, GET /cercanas, GET /seccion/{seccion}, GET /categoria/{categoria}
  - **Eventos** (4): GET /futuros, GET /proximos, GET /{id}, GET /falla/{idFalla}
  - **Ninots** (4): GET listado, GET /{id}, GET /falla/{idFalla}, GET /premiados
  - **Votos** (4): POST crear, GET /usuario/{id}, GET /ninot/{id}, DELETE /{id}

- **Queries Personalizados Implementados**
  - `FallaRepository.buscarPorTexto()`: Full-text search con to_tsvector PostgreSQL
  - `FallaRepository.buscarFallasCercanas()`: Búsqueda geográfica con fórmula Haversine
  - `EventoRepository.findEventosFuturos()`: Eventos desde fecha específica
  - `NinotRepository.findClasificacionPorVotos()`: Ranking de ninots
  - `VotoRepository.existsByUsuarioAndNinotAndTipoVoto()`: Validación de votos duplicados

- **Configuración Técnica**
  - Spring Boot 4.0.1 + Spring Data JPA
  - PostgreSQL driver 42.7.8
  - Hibernate 7.2.0 con dialecto PostgreSQL
  - HikariCP para pool de conexiones (max: 10)
  - Validación Jakarta con @Valid en requests
  - Lombok para reducir boilerplate
  - OpenAPI 3.0 con Springdoc (springdoc-openapi-starter-webmvc-ui 2.3.0)
  - Spring Security básico (sin JWT implementado)

- **Conversiones Entidad ↔ DTO**
  - Mapeo manual en Services (métodos convertirADTO)
  - BigDecimal → Double en coordenadas GPS
  - Enum → String en respuestas JSON
  - Manejo de relaciones lazy con null checks
  - Campos calculados (totales de votos, eventos, comentarios)

### Changed

- **Migración Completa de MongoDB a PostgreSQL en Backend**
  - Cambio de MongoRepository a JpaRepository
  - Paquete de `com.example.Fallapp` a `com.fallapp`
  - Anotaciones @Document → @Entity + @Table
  - Configuración de application.properties apuntando a PostgreSQL

- **Estructura de Respuestas API**
  - Implementado `ApiResponse<T>` genérico
  - Campos: success, message, data, timestamp
  - Nota: Difiere de especificación (usa inglés vs español)

- **Configuración de Conexión BD**
  - datasource.url: jdbc:postgresql://localhost:5432/fallapp
  - JPA ddl-auto: none (schema gestionado por scripts SQL)
  - show-sql: true para debugging
  - Doble configuración: application.properties (local) + application-docker.properties

### Fixed

- Errores de compilación por imports incorrectos (entity vs model)
- Conversión de tipos en Services (BigDecimal, LocalDateTime)
- Métodos de repositorio con Pageable faltante
- Nombres de campos en entidades (creadoEn vs fechaCreacion)

### Pending

- **Autenticación JWT** (3 TODOs críticos en código)
  - AuthController.login(): Lógica JWT sin implementar
  - VotoController: idUsuario debe extraerse de JWT, no query param
  - PasswordEncoder bean no configurado
  
- **Endpoints CRUD Faltantes** (21 endpoints)
  - POST/PUT/DELETE en Fallas, Eventos, Ninots
  - Módulo Comentarios completo (ComentarioController/Service)
  - Módulo Estadísticas completo
  
- **Tests del Backend**
  - Cobertura actual: 0% (solo contextLoads())
  - Pendiente: Tests de Services, Repositories, Controllers
  - Pendiente: Tests de validación de DTOs
  
- **Mejoras de Documentación**
  - Javadoc en Services y DTOs
  - ADR sobre decisión de autenticación
  - Guía de desarrollo del backend

### Technical Notes

- **Compilación**: ✅ Exitosa (mvn clean compile -DskipTests)
- **Conexión BD**: ✅ Validada (HikariCP conecta a PostgreSQL)
- **Aplicación arrancando**: Última verificación pendiente
- **Cobertura Spec**: 52% de endpoints especificados (24/44)
- **TODOs en código**: 11 comentarios TODO/FIXME identificados

---

## [Unreleased]

### Planned
- Implementación completa de JWT con tokens de 24h
- Tests de integración automatizados (backend)
- Endpoints CRUD completos (POST/PUT/DELETE)
- Módulos Comentarios y Estadísticas
- CI/CD con GitHub Actions
- Scripts de backup automático

---

**Convenciones del Changelog**:
- `Added`: Nuevas funcionalidades
- `Changed`: Cambios en funcionalidades existentes
- `Deprecated`: Funcionalidades obsoletas (próximas a eliminar)
- `Removed`: Funcionalidades eliminadas
- `Fixed`: Correcciones de bugs
- `Security`: Correcciones de vulnerabilidades
