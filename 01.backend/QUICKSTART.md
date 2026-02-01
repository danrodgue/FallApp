# Guía Rápida del Backend - FallApp API REST

## 🎯 Estado Actual (2026-02-01)

### ✅ Implementado (52% de especificación)
- **42 archivos Java** en 9 paquetes
- **24 endpoints REST** funcionando
- **Conexión PostgreSQL** validada (347 fallas)
- **Compilación exitosa** sin errores
- **Aplicación corriendo** en puerto 8080

### ⚠️ Pendiente (Crítico)
- **JWT no implementado** (3 TODOs en código)
- **21 endpoints faltantes** (POST/PUT/DELETE)
- **0% tests** (solo contextLoads)
- **Módulos completos**: Comentarios, Estadísticas

## 🚀 Inicio Rápido

### 1. Arrancar Base de Datos
```bash
cd /srv/FallApp/05.docker
docker compose up -d
```

### 2. Compilar Backend
```bash
cd /srv/FallApp/01.backend
mvn clean compile -DskipTests
```

### 3. Ejecutar Aplicación
```bash
mvn spring-boot:run
```

### 4. Verificar
```bash
# Listar fallas
curl http://localhost:8080/api/fallas | jq

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

## 📊 Endpoints por Módulo

| Módulo | Implementados | Total | % |
|--------|--------------|-------|---|
| Auth | 2 | 3 | 67% |
| Usuarios | 4 | 7 | 57% |
| Fallas | 6 | 10 | 60% |
| Eventos | 4 | 6 | 67% |
| Ninots | 4 | 5 | 80% |
| Votos | 4 | 4 | 100% |
| Comentarios | 0 | 4 | 0% |
| Estadísticas | 0 | 5 | 0% |
| **TOTAL** | **24** | **44** | **52%** |

## 🔍 Queries Destacados

### Búsqueda Full-Text
```bash
curl "http://localhost:8080/api/fallas/buscar?q=ayuntamiento"
```

### Búsqueda Geográfica (Haversine)
```bash
curl "http://localhost:8080/api/fallas/cercanas?lat=39.4699&lon=-0.3763&radio=2000"
```

### Clasificación de Ninots
```bash
curl "http://localhost:8080/api/ninots/premiados?page=0&size=10"
```

## 📚 Documentación Completa

- **README_API.md**: Documentación exhaustiva del backend
- **ADR-006**: Decisión sobre JWT (pendiente implementar)
- **ADR-007**: Formato de respuestas (ApiResponse)
- **04.API-REST.md**: Especificación completa de 44 endpoints

## ⚡ Próximos Pasos Recomendados

1. **Implementar JWT** (4-6 horas) - Ver ADR-006
2. **Agregar tests** (objetivo 80% cobertura)
3. **Completar endpoints CRUD faltantes**
4. **Implementar módulos Comentarios y Estadísticas**

---

**Versión**: 0.2.0  
**Última actualización**: 2026-02-01
