# Development Guide - FallApp

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. Compilation Errors: "release version 17 not supported"

**Problema**: Maven no encuentra Java 17
```
[ERROR] Fatal error compiling: error: release version 17 not supported
```

**Solución**:
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean compile
```

**Permanente** (añadir a ~/.bashrc):
```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc
```

---

#### 2. PostgreSQL Column Mapping Errors

**Problema**: Hibernate busca columnas con nombres incorrectos
```
ERROR: column "creado_en" does not exist
Hint: Perhaps you meant to reference the column "fecha_creacion"
```

**Causa**: Desalineación entre entidades Java (@Column) y schema PostgreSQL

**Solución**: Verificar nombres de columnas en entidades
```java
// INCORRECTO
@Column(name = "creado_en")
private LocalDateTime creadoEn;

// CORRECTO (alineado con PostgreSQL)
@Column(name = "fecha_creacion")
private LocalDateTime creadoEn;
```

**Verificación rápida**:
```bash
# Ver estructura de tabla
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "\d fallas"

# Comparar con entity Java
grep "@Column" src/main/java/com/fallapp/model/Falla.java
```

---

#### 3. ENUM Type Mismatch (ADR-008)

**Problema**: PostgreSQL ENUM incompatible con JPA @Enumerated(STRING)
```
ERROR: column "rol" is of type rol_usuario but expression is of type character varying
```

**Solución Temporal**: Usar VARCHAR con CHECK constraint
```sql
-- Migración aplicada en 99.migracion.enum.to.varchar.v2.sql
ALTER TABLE usuarios ALTER COLUMN rol TYPE VARCHAR(20) USING rol::text;
ALTER TABLE usuarios ADD CONSTRAINT check_rol_values 
  CHECK (rol IN ('admin', 'casal', 'usuario'));
```

**Referencia**: Ver [ADR-008](04.docs/arquitectura/ADR-008-postgresql-enum-varchar.md) para detalles completos

---

#### 4. Memory Issues: Swap Exhausted

**Problema**: Maven compilation fails, swap 255/256MB usado
```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
```

**Solución**: Aumentar swap RAM

```bash
# Opción 1: Aumentar zram (en RAM, más rápido)
sudo sed -i 's/^PERCENT=50/PERCENT=75/' /etc/default/zramswap
sudo systemctl restart zramswap

# Opción 2: Añadir swap file (en disco, más lento pero más grande)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon -p 10 /swapfile

# Hacer persistente (añadir a /etc/fstab)
echo '/swapfile none swap sw,pri=10 0 0' | sudo tee -a /etc/fstab

# Verificar
free -h
```

**Resultado esperado**: Swap total > 3GB

---

#### 5. Application Won't Start: Port 8080 Already in Use

**Problema**:
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Web server failed to start. Port 8080 was already in use.
```

**Solución**:
```bash
# Encontrar proceso usando puerto 8080
sudo lsof -i :8080
# O
sudo netstat -tulpn | grep :8080

# Matar proceso Spring Boot anterior
pkill -f "spring-boot:run"

# Verificar que no haya procesos
pgrep -f spring-boot

# Reiniciar aplicación
cd /srv/FallApp/01.backend
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn spring-boot:run
```

---

#### 6. JWT Token Issues: "JWT signature does not match"

**Problema**: Token rechazado con error de firma
```json
{
  "exito": false,
  "mensaje": "Token JWT inválido o malformado"
}
```

**Causas posibles**:
1. JWT secret cambiado en application.properties
2. Token generado con versión anterior de secret
3. Token expirado (24h por defecto)

**Solución**:
```bash
# 1. Generar nuevo token con login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fallapp.es","contrasena":"Admin2026!"}'

# 2. Verificar secret en application.properties (mínimo 64 caracteres para HS512)
grep "jwt.secret" src/main/resources/application.properties

# 3. Verificar expiración del token
# Token incluye claim "exp" con timestamp Unix
```

**Prevención**: No cambiar `jwt.secret` en producción sin plan de migración

---

#### 7. Database Connection Refused

**Problema**:
```
Caused by: org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Solución**:
```bash
# Verificar que PostgreSQL container está corriendo
docker ps | grep postgres

# Si no está corriendo, iniciar servicios
cd /srv/FallApp/05.docker
docker-compose up -d

# Verificar logs
docker logs fallapp-postgres

# Verificar conectividad
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "SELECT version();"
```

---

#### 8. Build Failures: Missing Dependencies

**Problema**:
```
[ERROR] Failed to execute goal on project Fallapp: Could not resolve dependencies
```

**Solución**:
```bash
# Limpiar caché de Maven
mvn dependency:purge-local-repository

# Re-descargar dependencias
mvn clean install -U

# Verificar settings.xml de Maven
cat ~/.m2/settings.xml
```

---

#### 9. Tests Failing: H2 Database Issues

**Problema**: Tests unitarios fallan por incompatibilidad H2/PostgreSQL

**Solución**: Usar profile de test con H2 compatible
```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

**Ejecutar tests**:
```bash
mvn test -Dspring.profiles.active=test
```

---

#### 10. API Returns HTML Instead of JSON

**Problema**: Endpoint retorna página de error HTML en vez de JSON

**Causa**: Excepción no capturada por @RestControllerAdvice

**Solución**: Verificar GlobalExceptionHandler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error interno del servidor: " + ex.getMessage()));
    }
}
```

---

## Development Workflow

### 1. Starting Development Session
```bash
# Iniciar servicios Docker
cd /srv/FallApp/05.docker
docker-compose up -d

# Iniciar backend en modo desarrollo
cd /srv/FallApp/01.backend
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn spring-boot:run

# En otra terminal: Ver logs en tiempo real
tail -f /tmp/spring-boot-crud.log
```

### 2. Making Code Changes
```bash
# Compilar sin reiniciar (verificar errores)
mvn compile

# Compilar y ejecutar tests
mvn clean test

# Reiniciar aplicación
pkill -f "spring-boot:run"
mvn spring-boot:run
```

### 3. Testing Endpoints
```bash
# Login y obtener token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fallapp.es","contrasena":"Admin2026!"}' \
  | jq -r '.datos.token')

# Usar token en requests autenticados
curl -X GET http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### 4. Database Operations
```bash
# Conectar a PostgreSQL
docker exec -it fallapp-postgres psql -U fallapp_user -d fallapp

# Ejecutar script SQL
docker exec -i fallapp-postgres psql -U fallapp_user -d fallapp < script.sql

# Verificar datos
docker exec fallapp-postgres psql -U fallapp_user -d fallapp \
  -c "SELECT COUNT(*) FROM fallas;"
```

---

## Performance Tips

### Maven Build Optimization
```bash
# Build sin tests (más rápido)
mvn clean install -DskipTests

# Build offline (usa caché local)
mvn clean install -o

# Compilación paralela
mvn -T 4 clean install
```

### Spring Boot DevTools
Añadir a pom.xml para hot reload:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## Quick Reference

### Important Ports
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **PostgreSQL**: localhost:5432
- **pgAdmin**: http://localhost:5050

### Default Credentials
```
PostgreSQL:
  Database: fallapp
  User: fallapp_user
  Password: fallapp2024!

Admin User:
  Email: admin@fallapp.es
  Password: Admin2026!

pgAdmin:
  Email: admin@fallapp.local
  Password: admin2024
```

### Useful Commands
```bash
# Ver endpoints disponibles
curl http://localhost:8080/v3/api-docs | jq '.paths | keys'

# Verificar health
curl http://localhost:8080/actuator/health

# Ver estadísticas
curl http://localhost:8080/api/estadisticas/resumen | jq .

# Count de tablas
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "
SELECT 
  tablename, 
  (xpath('/row/count/text()', xml_count))[1]::text::int AS row_count
FROM (
  SELECT tablename, table_schema,
    query_to_xml(format('SELECT COUNT(*) FROM %I.%I', table_schema, tablename), false, true, '') AS xml_count
  FROM pg_tables
  WHERE schemaname = 'public'
) t
ORDER BY row_count DESC;"
```

---

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PostgreSQL 13 Manual](https://www.postgresql.org/docs/13/index.html)
- [JWT.io Debugger](https://jwt.io/)
- [Swagger Editor](https://editor.swagger.io/)

---

**Última actualización**: 2026-02-01  
**Mantenido por**: Equipo de desarrollo FallApp
