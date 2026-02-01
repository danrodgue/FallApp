# application.properties - Configuración para PostgreSQL

A continuación se muestra la configuración necesaria para conectar Spring Boot a PostgreSQL.

## Ubicación del archivo

```
01.backend/src/main/resources/application.properties
```

## Configuración para Desarrollo (Docker Compose)

```properties
# =============================================================================
# BASE DE DATOS - PostgreSQL
# =============================================================================

spring.datasource.url=jdbc:postgresql://postgres:5432/fallapp
spring.datasource.username=${DB_USER:fallapp_user}
spring.datasource.password=${DB_PASSWORD:fallapp_password}
spring.datasource.driver-class-name=org.postgresql.Driver

# =============================================================================
# HIBERNATE / JPA
# =============================================================================

# Dialect para PostgreSQL 13
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL13Dialect

# DDL (Development: validate, Production: validate)
# - update: Actualiza esquema automáticamente (DESARROLLO SOLAMENTE)
# - validate: Valida esquema sin cambios (RECOMENDADO para PROD)
# - create: Crea nuevo esquema (PELIGROSO - pierde datos)
# - create-drop: Crea y destruye con cada inicio (TESTING)
spring.jpa.hibernate.ddl-auto=validate

# Mostrar SQL en logs (Development solamente)
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Propiedades adicionales de Hibernate
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# =============================================================================
# POOL DE CONEXIONES - HikariCP (por defecto en Spring Boot)
# =============================================================================

# Tamaño del pool de conexiones
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# =============================================================================
# LOGGING
# =============================================================================

# Nivel de log para JPA/Hibernate
logging.level.org.hibernate=WARN
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Nivel general
logging.level.root=INFO
logging.level.es.fallapp=DEBUG

# =============================================================================
# APLICACIÓN
# =============================================================================

# Nombre de la aplicación
spring.application.name=FallApp-API

# Puerto
server.port=8080

# Context path (ruta base de la API)
server.servlet.context-path=/api

# Timezone
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# =============================================================================
# SEGURIDAD (OPCIONAL)
# =============================================================================

# JWT Secret (usar variable de entorno en producción)
jwt.secret=${JWT_SECRET:dev-secret-key-change-in-production}
jwt.expiration=86400000  # 24 horas en ms

# CORS (Desarrollo)
server.servlet.context-parameters.cors.allowedOrigins=http://localhost:3000,http://localhost:4200
server.servlet.context-parameters.cors.allowedMethods=GET,POST,PUT,DELETE,OPTIONS
server.servlet.context-parameters.cors.allowCredentials=true

# =============================================================================
# ARCHIVO MULTIPART
# =============================================================================

spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# =============================================================================
# ACTUATOR (Health checks)
# =============================================================================

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.health.db.enabled=true
```

## Configuración para Producción

```properties
# =============================================================================
# PRODUCCIÓN - BASE DE DATOS
# =============================================================================

# Usar variables de entorno para credenciales
spring.datasource.url=jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${DB_NAME:fallapp}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20

# =============================================================================
# PRODUCCIÓN - HIBERNATE
# =============================================================================

# NUNCA usar 'update' o 'create' en producción
spring.jpa.hibernate.ddl-auto=validate

# NO mostrar SQL
spring.jpa.show-sql=false

# =============================================================================
# PRODUCCIÓN - LOGGING
# =============================================================================

logging.level.root=WARN
logging.level.es.fallapp=INFO
logging.file.name=/var/log/fallapp/app.log
logging.file.max-size=10MB
logging.file.max-history=10

# =============================================================================
# PRODUCCIÓN - SEGURIDAD
# =============================================================================

# Usar secretos gestionados (K8s, AWS Secrets Manager, etc.)
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# CORS restrictivo
server.servlet.context-parameters.cors.allowedOrigins=${ALLOWED_ORIGINS}
server.servlet.context-parameters.cors.allowCredentials=false
```

## Variables de Entorno (.env)

Crear archivo `01.backend/.env` (local):

```bash
# Base de datos
DB_HOST=postgres
DB_PORT=5432
DB_NAME=fallapp
DB_USER=fallapp_user
DB_PASSWORD=fallapp_password

# JWT
JWT_SECRET=your-super-secret-jwt-key-min-32-chars
JWT_EXPIRATION=86400000

# CORS (Desarrollo)
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# Aplicación
SPRING_PROFILES_ACTIVE=dev
```

En Docker, estas variables se pasan mediante:
- `docker-compose.yml` (variables de entorno del servicio)
- `.env` (archivo en raíz del proyecto)
- Secretos de Kubernetes (producción)

## Dependencias Maven POM.xml

Asegurar que están presentes:

```xml
<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
    <scope>runtime</scope>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Hibernate -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.4.Final</version>
</dependency>

<!-- HikariCP (ya incluido por defecto) -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

## Clase DataSource Configuration (Opcional)

```java
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        
        return new HikariDataSource(config);
    }
}
```

## Validación de Conexión

```bash
# Verificar conexión desde la aplicación
curl http://localhost:8080/api/actuator/health

# Respuesta esperada:
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

## Troubleshooting

### Error: "Connection refused"
- Verificar que PostgreSQL está corriendo: `docker-compose ps`
- Verificar host/puerto en URL de conexión
- Esperar a que el contenedor se inicie completamente

### Error: "FATAL: role 'fallapp_user' does not exist"
- Verificar que el usuario fue creado en el script `01.schema.sql`
- Recrear contenedor: `docker-compose down && docker-compose up`

### Error: "Hibernate ValidationException"
- `ddl-auto=validate` requiere esquema existente
- Ejecutar `01.schema.sql` primero
- O cambiar a `ddl-auto=update` temporalmente en desarrollo

### Conexión lenta
- Aumentar `maximum-pool-size` en HikariCP
- Verificar recursos del contenedor
- Revisar logs de PostgreSQL: `docker-compose logs postgres`

## Enlaces Útiles

- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [HikariCP Documentation](https://github.com/brettwooldridge/HikariCP)
- [Hibernate ORM](https://hibernate.org/orm/)
