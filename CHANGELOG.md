# Changelog

Todos los cambios notables de FallApp serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

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
