# Backend API REST - FallApp

> API REST para la gestión de Fallas de Valencia construida con Spring Boot 4.0.1 y PostgreSQL

## 📋 Tabla de Contenidos

- [Tecnologías](#tecnologías)
- [Configuración](#configuración)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Endpoints Implementados](#endpoints-implementados)
- [Modelos de Datos](#modelos-de-datos)
- [Queries Personalizados](#queries-personalizados)
- [Autenticación](#autenticación)
- [Desarrollo](#desarrollo)
- [Limitaciones Actuales](#limitaciones-actuales)

## 🛠 Tecnologías

### Core
- **Spring Boot**: 4.0.1
- **Java**: 17
- **Maven**: 3.8.7
- **PostgreSQL**: 13 (driver 42.7.8)

### Dependencias Principales
```xml
<!-- Base de datos -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Seguridad -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- Documentación API -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- Utilidades -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

## ⚙️ Configuración

### Base de Datos Local

**application.properties**:
```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/fallapp
spring.datasource.username=fallapp_user
spring.datasource.password=fallapp_secure_password_2026

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

### Base de Datos Docker

**application-docker.properties**:
```properties
spring.datasource.url=jdbc:postgresql://fallapp-postgres:5432/fallapp
```

## 📂 Estructura del Proyecto

```
src/main/java/com/fallapp/
├── FallappApplication.java          # Clase principal
├── config/
│   ├── OpenAPIConfig.java           # Configuración Swagger/OpenAPI 3.0
│   └── SecurityConfig.java          # Configuración Spring Security
├── controller/                      # 6 controladores REST
│   ├── AuthController.java          # POST /api/auth/registro, /login
│   ├── UsuarioController.java       # CRUD usuarios
│   ├── FallaController.java         # CRUD + búsqueda fallas
│   ├── EventoController.java        # Consulta eventos
│   ├── NinotController.java         # Consulta ninots
│   └── VotoController.java          # Gestión de votos
├── dto/                             # 13 DTOs
│   ├── ApiResponse.java             # Respuesta genérica <T>
│   ├── UsuarioDTO.java              
│   ├── FallaDTO.java
│   ├── EventoDTO.java
│   ├── NinotDTO.java
│   ├── VotoDTO.java
│   ├── ComentarioDTO.java
│   ├── CreateUsuarioRequest.java
│   ├── CreateFallaRequest.java
│   ├── CreateEventoRequest.java
│   ├── CreateNinotRequest.java
│   ├── CreateVotoRequest.java
│   └── LoginRequest.java
├── exception/                       # Manejo global de errores
│   ├── GlobalExceptionHandler.java  # @ControllerAdvice
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
├── model/                           # 6 Entidades JPA
│   ├── Usuario.java                 # @Entity @Table("usuarios")
│   ├── Falla.java                   # @Entity @Table("fallas")
│   ├── Evento.java                  # @Entity @Table("eventos")
│   ├── Ninot.java                   # @Entity @Table("ninots")
│   ├── Voto.java                    # @Entity @Table("votos")
│   └── Comentario.java              # @Entity @Table("comentarios")
├── repository/                      # 6 Interfaces JPA
│   ├── UsuarioRepository.java
│   ├── FallaRepository.java         # + @Query búsqueda full-text
│   ├── EventoRepository.java
│   ├── NinotRepository.java
│   ├── VotoRepository.java
│   └── ComentarioRepository.java
├── service/                         # 5 Services con lógica de negocio
│   ├── UsuarioService.java
│   ├── FallaService.java
│   ├── EventoService.java
│   ├── NinotService.java
│   └── VotoService.java
└── security/                        # ⚠️ VACÍO - JWT pendiente
```

**Total: 42 archivos Java**

## 🌐 Endpoints Implementados

### Base URL
```
http://localhost:8080/api
```

### Documentación Interactiva
```
http://localhost:8080/swagger-ui.html
http://localhost:8080/api-docs
```

### 1. Autenticación (2/3 endpoints)

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| POST | `/api/auth/registro` | Registrar nuevo usuario | ✅ |
| POST | `/api/auth/login` | Iniciar sesión | ⚠️ Sin JWT |

**Ejemplo - Registro**:
```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "nombreUsuario": "juan_fallero",
    "email": "juan@example.com",
    "contrasena": "Password123!",
    "nombre": "Juan",
    "apellidos": "García López"
  }'
```

**Respuesta**:
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "id": 4,
    "nombreUsuario": "juan_fallero",
    "email": "juan@example.com",
    "nombre": "Juan",
    "apellidos": "García López",
    "rol": "USER",
    "activo": true,
    "fechaRegistro": "2026-02-01T14:30:00"
  },
  "timestamp": "2026-02-01T14:30:00"
}
```

### 2. Usuarios (4/7 endpoints)

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| GET | `/api/usuarios` | Listar usuarios activos (paginado) | ✅ |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID | ✅ |
| PUT | `/api/usuarios/{id}` | Actualizar usuario | ✅ |
| DELETE | `/api/usuarios/{id}` | Desactivar usuario (soft delete) | ✅ |

**Ejemplo - Listar Usuarios**:
```bash
curl http://localhost:8080/api/usuarios?page=0&size=10
```

### 3. Fallas (6/10 endpoints)

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| GET | `/api/fallas` | Listar fallas (paginado) | ✅ |
| GET | `/api/fallas/{id}` | Obtener falla por ID | ✅ |
| GET | `/api/fallas/buscar?q={texto}` | Búsqueda full-text | ✅ |
| GET | `/api/fallas/cercanas?lat={}&lon={}&radio={}` | Búsqueda geográfica | ✅ |
| GET | `/api/fallas/seccion/{seccion}` | Filtrar por sección | ✅ |
| GET | `/api/fallas/categoria/{categoria}` | Filtrar por categoría | ✅ |

**Ejemplo - Búsqueda Geográfica**:
```bash
# Fallas a menos de 2km del centro de Valencia
curl "http://localhost:8080/api/fallas/cercanas?lat=39.4699&lon=-0.3763&radio=2000"
```

**Respuesta**:
```json
{
  "success": true,
  "message": "Fallas cercanas encontradas",
  "data": [
    {
      "id": 1,
      "nombre": "Falla Plaza del Ayuntamiento",
      "lema": "Valencia en Fallas",
      "seccion": "ESPECIAL",
      "categoria": "PRIMERA",
      "direccion": "Plaza del Ayuntamiento, s/n",
      "latitud": 39.4699,
      "longitud": -0.3763,
      "distanciaMetros": 50.5,
      "totalNinots": 15,
      "totalEventos": 8,
      "totalVotos": 342
    }
  ]
}
```

**Ejemplo - Búsqueda Full-Text**:
```bash
# Buscar fallas que contengan "ayuntamiento" en nombre, lema o descripción
curl "http://localhost:8080/api/fallas/buscar?q=ayuntamiento"
```

### 4. Eventos (4/6 endpoints)

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| GET | `/api/eventos/futuros` | Eventos desde hoy | ✅ |
| GET | `/api/eventos/proximos?limite={n}` | Próximos N eventos | ✅ |
| GET | `/api/eventos/{id}` | Obtener evento por ID | ✅ |
| GET | `/api/eventos/falla/{idFalla}` | Eventos de una falla | ✅ |

### 5. Ninots (4/5 endpoints)

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| GET | `/api/ninots` | Listar ninots (paginado) | ✅ |
| GET | `/api/ninots/{id}` | Obtener ninot por ID | ✅ |
| GET | `/api/ninots/falla/{idFalla}` | Ninots de una falla | ✅ |
| GET | `/api/ninots/premiados` | Ranking por votos | ✅ |

### 6. Votos (4/4 endpoints)

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| POST | `/api/votos` | Crear voto | ✅ |
| GET | `/api/votos/usuario/{idUsuario}` | Votos de un usuario | ✅ |
| GET | `/api/votos/ninot/{idNinot}` | Votos de un ninot | ✅ |
| DELETE | `/api/votos/{idVoto}` | Eliminar voto | ✅ |

### 7. Comentarios (0/4 endpoints)

❌ **Módulo NO implementado**

### 8. Estadísticas (0/5 endpoints)

❌ **Módulo NO implementado**

**Cobertura total: 24/44 endpoints (52%)**

## 📊 Modelos de Datos

### Usuario
```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String nombreUsuario;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String contrasena;  // ⚠️ Sin encriptar aún
    
    private String nombre;
    private String apellidos;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;  // ADMIN, USER
    
    private Boolean activo = true;
    
    @ManyToOne
    @JoinColumn(name = "id_falla")
    private Falla falla;  // Falla asociada
    
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;
    
    // Relaciones
    @OneToMany(mappedBy = "usuario")
    private List<Voto> votos;
    
    @OneToMany(mappedBy = "usuario")
    private List<Comentario> comentarios;
}
```

### Falla
```java
@Entity
@Table(name = "fallas")
public class Falla {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    private String lema;
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    private Seccion seccion;  // ESPECIAL, PRIMERA, SEGUNDA, etc.
    
    @Enumerated(EnumType.STRING)
    private Categoria categoria;
    
    private String direccion;
    
    // Coordenadas GPS (almacenadas como NUMERIC en BD)
    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;
    
    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;
    
    private String urlImagen;
    private String urlSitioWeb;
    
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    
    // Relaciones
    @OneToMany(mappedBy = "falla")
    private List<Ninot> ninots;
    
    @OneToMany(mappedBy = "falla")
    private List<Evento> eventos;
    
    @OneToMany(mappedBy = "falla")
    private List<Usuario> usuarios;
}
```

### Evento
```java
@Entity
@Table(name = "eventos")
public class Evento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    private String descripcion;
    
    @Column(nullable = false)
    private String tipoEvento;  // "mascletà", "ofrenda", "cabalgata", etc.
    
    @ManyToOne
    @JoinColumn(name = "id_falla")
    private Falla falla;
    
    @Column(nullable = false)
    private LocalDateTime fechaEvento;
    
    private String ubicacion;
}
```

### Ninot
```java
@Entity
@Table(name = "ninots")
public class Ninot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    private String descripcion;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal alturaMetros;
    
    @ManyToOne
    @JoinColumn(name = "id_falla", nullable = false)
    private Falla falla;
    
    private String urlImagenPrincipal;
    
    @Column(columnDefinition = "text[]")
    private String[] urlImagenesAdicionales;
    
    private LocalDateTime creadoEn;
    
    @OneToMany(mappedBy = "ninot")
    private List<Voto> votos;
}
```

### Voto
```java
@Entity
@Table(name = "votos")
public class Voto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "id_ninot", nullable = false)
    private Ninot ninot;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVoto tipoVoto;  // POPULAR, ARTISTICO
    
    private LocalDateTime creadoEn;
}
```

## 🔍 Queries Personalizados

### 1. Búsqueda Full-Text en Fallas

**FallaRepository.java**:
```java
@Query(value = """
    SELECT f.* FROM fallas f
    WHERE to_tsvector('spanish', 
        COALESCE(f.nombre, '') || ' ' || 
        COALESCE(f.lema, '') || ' ' || 
        COALESCE(f.descripcion, '')
    ) @@ plainto_tsquery('spanish', :texto)
    ORDER BY f.nombre
    """, nativeQuery = true)
List<Falla> buscarPorTexto(@Param("texto") String texto);
```

**Características**:
- Usa índice GIN de PostgreSQL (`idx_fallas_busqueda_texto`)
- Búsqueda en español con stemming
- Busca en nombre + lema + descripción

### 2. Búsqueda Geográfica (Haversine)

**FallaRepository.java**:
```java
@Query(value = """
    SELECT f.*, 
        (6371000 * acos(
            cos(radians(:latitud)) * 
            cos(radians(f.latitud)) * 
            cos(radians(f.longitud) - radians(:longitud)) + 
            sin(radians(:latitud)) * 
            sin(radians(f.latitud))
        )) AS distancia
    FROM fallas f
    WHERE (6371000 * acos(
            cos(radians(:latitud)) * 
            cos(radians(f.latitud)) * 
            cos(radians(f.longitud) - radians(:longitud)) + 
            sin(radians(:latitud)) * 
            sin(radians(f.latitud))
        )) <= :radioMetros
    ORDER BY distancia
    """, nativeQuery = true)
List<Object[]> buscarFallasCercanas(
    @Param("latitud") Double latitud,
    @Param("longitud") Double longitud,
    @Param("radioMetros") Double radioMetros
);
```

**Características**:
- Fórmula Haversine para cálculo de distancia en esfera
- Radio en metros
- Ordenado por proximidad

### 3. Eventos Futuros

**EventoRepository.java**:
```java
@Query("SELECT e FROM Evento e WHERE e.fechaEvento >= :fechaDesde ORDER BY e.fechaEvento")
List<Evento> findEventosFuturos(@Param("fechaDesde") LocalDateTime fechaDesde);
```

### 4. Ranking de Ninots por Votos

**NinotRepository.java**:
```java
@Query("""
    SELECT n, COUNT(v) as totalVotos 
    FROM Ninot n 
    LEFT JOIN n.votos v 
    WHERE v.tipoVoto = :tipoVoto 
    GROUP BY n 
    ORDER BY totalVotos DESC
    """)
List<Object[]> findClasificacionPorVotos(@Param("tipoVoto") TipoVoto tipoVoto, Pageable pageable);
```

### 5. Validación de Voto Duplicado

**VotoRepository.java**:
```java
boolean existsByUsuarioAndNinotAndTipoVoto(Usuario usuario, Ninot ninot, TipoVoto tipoVoto);
```

## 🔐 Autenticación

### Estado Actual

⚠️ **JWT NO IMPLEMENTADO** - Configuración básica de Spring Security sin autenticación funcional.

**TODOs en código**:
```java
// AuthController.java línea 36
@PostMapping("/login")
public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
    // TODO: Implementar lógica de autenticación JWT
    return ResponseEntity.ok(ApiResponse.success("Login pendiente de implementar", null));
}

// VotoController.java líneas 31 y 57
// TODO: Obtener idUsuario del token JWT en lugar de parámetro
```

### Pendiente de Implementar

1. **JwtTokenProvider** (security/JwtTokenProvider.java)
   - Generación de tokens con expiración 24h
   - Validación de firma
   - Extracción de claims

2. **JwtAuthenticationFilter** (security/JwtAuthenticationFilter.java)
   - Interceptar requests
   - Validar header Authorization
   - Establecer SecurityContext

3. **PasswordEncoder Bean** (SecurityConfig.java)
   - BCryptPasswordEncoder para contraseñas

4. **Actualizar AuthController**
   - Login retorna token JWT
   - Registro retorna token JWT
   - Validar credenciales contra BD

## 🚀 Desarrollo

### Requisitos Previos
```bash
# Java 17
java -version

# Maven 3.8+
mvn -version

# PostgreSQL 13 corriendo
docker ps | grep fallapp-postgres
```

### Arrancar Base de Datos
```bash
cd /srv/FallApp/05.docker
docker compose up -d
```

### Compilar
```bash
cd /srv/FallApp/01.backend
mvn clean compile -DskipTests
```

### Ejecutar
```bash
# Desarrollo local
mvn spring-boot:run

# Con profile Docker
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Logs de Arranque
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

HikariPool-1 - Starting...
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@7c3df479
HikariPool-1 - Start completed.
Tomcat started on port 8080 (http)
Started FallappApplication in 3.245 seconds
```

### Verificar API
```bash
# Health check
curl http://localhost:8080/api/fallas | jq

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

## ⚠️ Limitaciones Actuales

### Funcionalidades Críticas Pendientes

1. **Autenticación JWT** (Bloqueante para producción)
   - Sin tokens, todos los endpoints son públicos
   - Contraseñas almacenadas en texto plano
   - No hay autorización por roles

2. **Endpoints CRUD Incompletos** (21 endpoints)
   - POST/PUT en Fallas, Eventos, Ninots
   - DELETE en Eventos, Ninots
   - Todo el módulo Comentarios (0/4)
   - Todo el módulo Estadísticas (0/5)

3. **Tests** (0% cobertura)
   - No hay tests de Services
   - No hay tests de Repositories
   - No hay tests de Controllers
   - Solo test vacío contextLoads()

4. **Validaciones**
   - Validaciones básicas con @Valid
   - Sin validaciones de negocio complejas
   - Sin validación de permisos por rol

### Diferencias con Especificación

5. **Formato de Respuesta**
   - Implementado: `ApiResponse<T>` con campos en inglés
   - Especificado: Campos en español (exito, mensaje, datos)

6. **Paginación**
   - Implementado: Spring `Page<T>` directamente
   - Especificado: Objeto `PaginatedResponse` custom

7. **Gestión de Imágenes**
   - Sin implementar upload de imágenes
   - Solo URLs almacenadas

### Deuda Técnica

8. **Documentación**
   - Sin Javadoc en Services
   - Sin comentarios en queries complejos
   - ADRs faltantes (JWT, paginación, formato API)

9. **Logging**
   - Solo logs por defecto de Spring
   - Sin logging estructurado
   - Sin métricas de performance

10. **Configuración**
    - Contraseñas en application.properties (sin externalizar)
    - Sin profiles para diferentes entornos
    - Sin health checks customizados

## 📝 Próximos Pasos

### Prioridad Alta
1. Implementar JWT authentication completo
2. Agregar tests de integración (objetivo: 80% cobertura)
3. Completar endpoints CRUD faltantes

### Prioridad Media
4. Implementar módulo Comentarios
5. Implementar módulo Estadísticas
6. Agregar validaciones de negocio

### Prioridad Baja
7. Externalizar configuración sensible
8. Agregar logging estructurado
9. Documentar código con Javadoc

## 📚 Recursos

- **Especificación API**: [04.docs/especificaciones/04.API-REST.md](../04.docs/especificaciones/04.API-REST.md)
- **ADRs**: [04.docs/arquitectura/](../04.docs/arquitectura/)
- **Scripts BD**: [07.datos/scripts/](../07.datos/scripts/)
- **Tests E2E**: [06.tests/](../06.tests/)

---

**Estado**: ⚠️ Funcional pero incompleto (52% de endpoints)  
**Última actualización**: 2026-02-01
