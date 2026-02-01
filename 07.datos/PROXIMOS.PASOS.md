# Próximos Pasos - Integración PostgreSQL en FallApp

Documento de continuación después de la creación de scripts SQL y documentación.

## ✅ Completado en esta sesión

- [x] Creación de 07.datos/ con estructura de datos
- [x] Importación de falles-fallas.json
- [x] Especificación de base de datos (03.BASE.DATOS.md)
- [x] Docker Compose con PostgreSQL
- [x] Nomenclatura de ficheros (NOMENCLATURA.FICHEROS.md)
- [x] Scripts SQL:
  - [x] 01.schema.sql - Tablas, tipos, triggers
  - [x] 10.seed.usuarios.sql - Datos iniciales
  - [x] 20.import.fallas.json.sql - Importación de JSON
  - [x] 30.vistas.consultas.sql - Vistas y funciones

## ⏳ Próximas Tareas Recomendadas

### FASE 1: Validación (1-2 horas)

#### 1.1 - Levantar Docker Compose con PostgreSQL
```bash
cd /srv/FallApp
docker-compose up -d postgres pgAdmin
```

**Validar:**
- PostgreSQL inicia correctamente
- Puerto 5432 está accesible
- pgAdmin está en http://localhost:5050
- Volumen postgres_data/ se crea

**Troubleshooting:**
```bash
docker-compose logs postgres       # Ver logs
docker-compose exec postgres psql -U postgres -l  # Listar DBs
```

#### 1.2 - Validar scripts SQL
```bash
# Copiar scripts al contenedor
docker cp 07.datos/scripts/01.schema.sql fallapp-postgres:/tmp/

# Ejecutar script manualmente
docker-compose exec postgres psql -U fallapp_user -d fallapp < 07.datos/scripts/01.schema.sql

# Verificar tablas creadas
docker-compose exec postgres psql -U fallapp_user -d fallapp << EOF
\dt
SELECT * FROM usuarios;
SELECT COUNT(*) FROM fallas;
EOF
```

#### 1.3 - Verificar importación de datos JSON
```bash
# Ver estadísticas
docker-compose exec postgres psql -U fallapp_user -d fallapp << EOF
SELECT COUNT(*) as total_fallas FROM fallas;
SELECT seccion, COUNT(*) FROM fallas GROUP BY seccion;
SELECT * FROM v_fallas_mas_votadas LIMIT 5;
EOF
```

**Resultado esperado:**
- ~400 fallas importadas
- Tablas con estructura correcta
- Vistas accesibles

---

### FASE 2: Integración Backend (2-3 horas)

#### 2.1 - Actualizar application.properties
**Archivo:** `01.backend/src/main/resources/application.properties`

```properties
# Ver referencia: 07.datos/APPLICATION.PROPERTIES.REFERENCIA.md

spring.datasource.url=jdbc:postgresql://postgres:5432/fallapp
spring.datasource.username=fallapp_user
spring.datasource.password=fallapp_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL13Dialect
spring.jpa.hibernate.ddl-auto=validate
```

**Cambios:**
- Reemplazar conexión MongoDB con PostgreSQL
- Cambiar `ddl-auto` a `validate` (no update)
- Configurar HikariCP pool size

#### 2.2 - Actualizar pom.xml
**Agregaciones necesarias:**

```xml
<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>

<!-- Spring Data JPA (si no existe) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**Remover:**
- Dependencias de MongoDB (si existen)
- MongoTemplate, MongoRepository

#### 2.3 - Convertir Entidades JPA
**Cambios en código Java:**

```java
// DE: Spring Data MongoDB
@Document(collection = "fallas")
public class Falla { ... }

// A: Spring Data JPA
@Entity
@Table(name = "fallas")
public class Falla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_falla")
    private Integer id;
    
    @Column(name = "nombre", unique = true, nullable = false)
    private String nombre;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaFalla categoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_falla")
    private Falla falla;
    
    // ... getters/setters
}
```

**Tareas específicas:**
1. Crear clase `Falla` con anotaciones JPA
2. Crear clase `Usuario` con relaciones
3. Crear clase `Evento`, `Ninot`, `Voto`, `Comentario`
4. Convertir Repositories: `MongoRepository` → `JpaRepository`

#### 2.4 - Convertir Repositories
```java
// DE: MongoRepository
public interface FallaRepository extends MongoRepository<Falla, ObjectId> { }

// A: JpaRepository
public interface FallaRepository extends JpaRepository<Falla, Integer> {
    Optional<Falla> findByNombre(String nombre);
    List<Falla> findBySeccion(String seccion);
    List<Falla> findByCategoria(CategoriaFalla categoria);
}
```

---

### FASE 3: Servicios y Lógica (3-4 horas)

#### 3.1 - Crear Service Layer
**Patrón:** Service → Repository → Entity

```java
@Service
public class FallaService {
    @Autowired
    private FallaRepository fallaRepository;
    
    public List<FallaDTO> obtenerTodasFallas() {
        return fallaRepository.findAll()
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    public Page<FallaDTO> buscarFallasConPaginacion(
            String termino, 
            Pageable pageable) {
        return fallaRepository.findByNombreIgnoreCaseContaining(termino, pageable)
            .map(this::convertirADTO);
    }
}
```

#### 3.2 - Crear DTOs (Data Transfer Objects)
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FallaDTO {
    private Integer id;
    private String nombre;
    private String seccion;
    private String artista;
    private Integer anyoFundacion;
    private CategoriaFalla categoria;
    private Double ubicacionLat;
    private Double ubicacionLon;
    private Long totalVotos;
    private Double ratingPromedio;
}
```

#### 3.3 - Implementar búsqueda full-text
```java
@Query("SELECT f FROM Falla f WHERE " +
       "to_tsvector('spanish', COALESCE(f.nombre, '') || ' ' || " +
       "COALESCE(f.lema, '') || ' ' || " +
       "COALESCE(f.artista, '')) @@ " +
       "to_tsquery('spanish', :termino)")
List<Falla> buscarPorTexto(@Param("termino") String termino);
```

---

### FASE 4: APIs REST (2-3 horas)

#### 4.1 - Crear Controllers
```java
@RestController
@RequestMapping("/api/fallas")
public class FallaController {
    
    @Autowired
    private FallaService fallaService;
    
    @GetMapping
    public ResponseEntity<Page<FallaDTO>> listarFallas(
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(fallaService.buscarFallasConPaginacion(busqueda, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FallaDTO> obtenerFalla(@PathVariable Integer id) {
        return ResponseEntity.ok(fallaService.obtenerFallaPorId(id));
    }
    
    @PostMapping("/{id}/voto")
    public ResponseEntity<VotoDTO> crearVoto(
            @PathVariable Integer id,
            @RequestBody VotoRequest request) {
        return ResponseEntity.ok(fallaService.crearVoto(id, request));
    }
}
```

#### 4.2 - Swagger/OpenAPI
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FallApp API")
                .version("1.0.0")
                .description("API de Fallas Falleras de Valencia"));
    }
}
```

---

### FASE 5: Testing (2-3 horas)

#### 5.1 - Tests Unitarios
```java
@SpringBootTest
@DataJpaTest
public class FallaRepositoryTest {
    
    @Autowired
    private FallaRepository repository;
    
    @Test
    public void testFindByNombre() {
        // Arrange
        Falla falla = new Falla();
        falla.setNombre("Falla Test");
        repository.save(falla);
        
        // Act
        Optional<Falla> result = repository.findByNombre("Falla Test");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Falla Test", result.get().getNombre());
    }
}
```

#### 5.2 - Tests de Integración
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FallaControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testListarFallas() {
        ResponseEntity<Page> response = restTemplate.getForEntity(
            "/api/fallas", Page.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

---

### FASE 6: Documentación Actualizada (1 hora)

#### 6.1 - README del Backend
```markdown
# Backend - FallApp API

## Configuración PostgreSQL
- URL: jdbc:postgresql://postgres:5432/fallapp
- Usuario: fallapp_user
- BD: fallapp
- Pool: 10 conexiones máximo

## Ejecución
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints
- GET /api/fallas
- GET /api/fallas/{id}
- POST /api/fallas/{id}/voto
- GET /api/eventos
- GET /api/usuarios/me
```

#### 6.2 - Guía de Migrations
```markdown
# Migraciones PostgreSQL

Scripts ejecutados al iniciar:
1. 01.schema.sql - Tablas
2. 10.seed.usuarios.sql - Datos iniciales
3. 20.import.fallas.json.sql - Fallas
4. 30.vistas.consultas.sql - Vistas
```

---

## 📋 Checklist de Implementación

### Semana 1 (Días 1-3)
- [ ] Levantar PostgreSQL con Docker
- [ ] Validar scripts SQL
- [ ] Verificar importación de datos
- [ ] Actualizar application.properties
- [ ] Cambiar MongoDB → PostgreSQL en pom.xml

### Semana 1 (Días 4-5)
- [ ] Crear entidades JPA (Falla, Usuario, etc.)
- [ ] Convertir Repositories
- [ ] Crear DTOs
- [ ] Implementar Services

### Semana 2 (Días 6-8)
- [ ] Crear Controllers REST
- [ ] Implementar búsqueda full-text
- [ ] Configurar Swagger/OpenAPI
- [ ] Tests unitarios

### Semana 2 (Días 9-10)
- [ ] Tests de integración
- [ ] Documentación API
- [ ] Performance tuning
- [ ] Commit y push a main

---

## 📊 Recursos Estimados

| Tarea | Estimación | Prioridad |
|-------|-----------|----------|
| Validación DB | 1-2h | 🔴 Alta |
| Integration Backend | 2-3h | 🔴 Alta |
| Services/DTOs | 3-4h | 🔴 Alta |
| APIs REST | 2-3h | 🔴 Alta |
| Testing | 2-3h | 🟡 Media |
| Documentación | 1h | 🟡 Media |
| **TOTAL** | **12-18h** | |

**Para equipo de 3 personas:** 4-6 horas cada uno

---

## 🎯 Hitos Clave

1. **Hito 1**: PostgreSQL funcionando (Día 1)
2. **Hito 2**: Entidades JPA creadas (Día 3)
3. **Hito 3**: APIs REST básicas (Día 6)
4. **Hito 4**: Testing completo (Día 9)
5. **Hito 5**: Documentación y deploy (Día 10)

---

## 📞 Contacto y Soporte

Para dudas sobre:
- **Base de Datos**: Ver [03.BASE.DATOS.md](04.docs/especificaciones/03.BASE.DATOS.md)
- **Docker**: Ver [05.docker/README.md](05.docker/README.md)
- **SQL Scripts**: Ver [07.datos/scripts/README.md](07.datos/scripts/README.md)
- **Application.properties**: Ver [APPLICATION.PROPERTIES.REFERENCIA.md](07.datos/APPLICATION.PROPERTIES.REFERENCIA.md)

**Última actualización**: 2024-02-01
**Estado**: Listo para Desarrollo
