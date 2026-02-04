# 📍 Tests de Ubicaciones GPS - FallApp v0.5.2

Suite completa de tests para validar la funcionalidad de ubicaciones GPS en fallas.

## 📋 Resumen

| Tipo | Archivo | Tests | Descripción |
|------|---------|-------|-------------|
| **SQL** | test_05_ubicaciones_gps.sql | 9 | Validación de datos GPS en PostgreSQL |
| **E2E** | test_api_ubicaciones.sh | 20 | Tests del endpoint `/api/fallas/{id}/ubicacion` |
| **Performance** | test_ubicaciones_performance.sh | 6 | Benchmarks de rendimiento del endpoint |
| **TOTAL** | - | **35** | **Cobertura completa** |

---

## 🧪 Test 1: Integridad de Datos GPS (SQL)

**Archivo:** `06.tests/integration/test_05_ubicaciones_gps.sql`

### Ejecución

```bash
cd /srv/FallApp
docker exec -i fallapp-postgres psql -U fallapp_user -d fallapp \
  < 06.tests/integration/test_05_ubicaciones_gps.sql
```

### Tests Incluidos

1. ✅ Verificar columnas `ubicacion_lat` y `ubicacion_lon` existen
2. ✅ Cobertura de ubicaciones >= 99%
3. ✅ Validar rango de latitudes (38° a 40°)
4. ✅ Validar rango de longitudes (-1° a 0°)
5. ✅ Verificar precisión decimal >= 6 decimales
6. ✅ Verificar consistencia (ambas coordenadas o ninguna)
7. ℹ️  Verificar índices en columnas de ubicación
8. ℹ️  Estadísticas generales (promedio, min, max)
9. ✅ Listar fallas sin ubicación GPS

### Resultado Esperado

```
Test 1: PASS | Columnas ubicacion_lat y ubicacion_lon existen
Test 2: PASS | Cobertura: 99.71% (>= 99%)
Test 3: PASS | Todas las latitudes en rango válido (38-40)
Test 4: PASS | Todas las longitudes en rango válido (-1 a 0)
Test 5: PASS | Latitud con precisión >= 6 decimales
        PASS | Longitud con precisión >= 6 decimales
Test 6: PASS | Todas las fallas tienen ambas coordenadas o ninguna
Test 7: INFO | Índices encontrados
Test 8: INFO | Estadísticas GPS
Test 9: PASS | Solo 1 falla(s) sin ubicación
```

---

## 🌐 Test 2: API Endpoint (E2E)

**Archivo:** `06.tests/e2e/test_api_ubicaciones.sh`

### Ejecución

```bash
cd /srv/FallApp/06.tests/e2e
bash test_api_ubicaciones.sh
```

### Tests Incluidos

#### Conectividad (2 tests)
1. ✅ Backend API activo
2. ✅ GET /api/fallas/95/ubicacion (HTTP 200)

#### Estructura JSON (8 tests)
3. ✅ Respuesta JSON válida
4. ✅ Campo 'exito' es true
5. ✅ Campo 'datos' existe
6. ✅ Campo 'datos.idFalla' es número
7. ✅ Campo 'datos.nombre' es string
8. ✅ Campo 'datos.latitud' es número
9. ✅ Campo 'datos.longitud' es número
10. ✅ Campo 'datos.tieneUbicacion' es boolean

#### Validación de Datos (3 tests)
11. ✅ Latitud en rango válido (38-40)
12. ✅ Longitud en rango válido (-1 a 0)
13. ✅ tieneUbicacion=true para falla con GPS

#### Casos Especiales (3 tests)
14. ✅ Falla sin GPS: tieneUbicacion=false
15. ✅ ID inexistente devuelve HTTP 404
16. ✅ Acceso sin token JWT (público)

#### Múltiples Fallas (5 tests)
17-21. ✅ Fallas ID 100, 150, 200, 250, 300 responden correctamente

#### Metadatos (3 tests)
22. ✅ Campo 'timestamp' existe
23. ✅ Content-Type es application/json
24. ✅ Tiempo de respuesta < 1 segundo

### Resultado Esperado

```
=========================================
RESUMEN DE TESTS
=========================================
Total:  20 tests
Passed: 20 tests
Failed: 0 tests

✓ TODOS LOS TESTS PASARON (100%)
```

---

## ⚡ Test 3: Performance (Benchmarks)

**Archivo:** `06.tests/performance/test_ubicaciones_performance.sh`

### Ejecución

```bash
cd /srv/FallApp/06.tests/performance
bash test_ubicaciones_performance.sh
```

### Tests Incluidos

1. **Tiempo de respuesta individual**
   - ✅ PASS: < 0.5 segundos
   - ⚠️ WARN: < 1.0 segundos
   - ❌ FAIL: >= 1.0 segundos

2. **10 requests secuenciales**
   - ✅ PASS: < 0.3s promedio
   - ⚠️ WARN: < 0.5s promedio
   - ❌ FAIL: >= 0.5s promedio

3. **5 requests concurrentes**
   - ✅ PASS: < 1.0s total
   - ⚠️ WARN: < 2.0s total
   - ❌ FAIL: >= 2.0s total

4. **Tamaño de respuesta**
   - ✅ PASS: < 500 bytes
   - ⚠️ WARN: < 1 KB
   - ❌ FAIL: >= 1 KB

5. **Carga pesada (100 requests)**
   - ✅ PASS: < 30s total
   - ⚠️ WARN: < 60s total
   - ❌ FAIL: >= 60s total

6. **Uso de recursos del backend**
   - ✅ PASS: < 512 MB memoria
   - ⚠️ WARN: < 768 MB memoria
   - ❌ FAIL: >= 768 MB memoria

### Resultado Esperado

```
Test 1: PASS | Respuesta rápida (< 0.5s)
Test 2: PASS | Rendimiento excelente (< 0.3s promedio)
Test 3: PASS | Maneja concurrencia eficientemente (< 1.0s)
Test 4: PASS | Respuesta compacta (< 500 bytes)
Test 5: PASS | Soporta carga pesada eficientemente (< 30s)
Test 6: PASS | Uso de memoria bajo (< 512 MB)
```

---

## 🚀 Ejecución Completa

Ejecutar todos los tests de ubicaciones:

```bash
cd /srv/FallApp

# Test 1: Integridad SQL
echo "=== Test 1: Integridad GPS (SQL) ===" && \
docker exec -i fallapp-postgres psql -U fallapp_user -d fallapp \
  < 06.tests/integration/test_05_ubicaciones_gps.sql

# Test 2: API E2E
echo "" && echo "=== Test 2: API Endpoint (E2E) ===" && \
bash 06.tests/e2e/test_api_ubicaciones.sh

# Test 3: Performance
echo "" && echo "=== Test 3: Performance ===" && \
bash 06.tests/performance/test_ubicaciones_performance.sh
```

---

## 📊 Métricas Actuales (2026-02-03)

### Base de Datos
- **Total fallas:** 347
- **Con ubicación GPS:** 346 (99.71%)
- **Sin ubicación:** 1 (falla de testing)
- **Precisión:** 6-8 decimales
- **Rango latitud:** 39.43° - 39.50° (Valencia)
- **Rango longitud:** -0.42° - -0.33° (Valencia)

### API Performance
- **Tiempo respuesta:** ~0.2-0.3s (promedio)
- **Tamaño respuesta:** ~200-300 bytes
- **Concurrencia:** Soporta 5+ requests paralelos
- **Carga pesada:** 100 requests en ~15-20s
- **Disponibilidad:** 100% (sin autenticación)

### Cobertura de Tests
- **Tests SQL:** 9/9 ✅ (100%)
- **Tests E2E:** 20/20 ✅ (100%)
- **Tests Performance:** 6/6 ✅ (100%)
- **TOTAL:** 35/35 ✅ (100%)

---

## 🐛 Troubleshooting

### Error: "Connection refused"
```bash
# Verificar que el backend está activo
sudo systemctl status fallapp

# Reiniciar si es necesario
sudo systemctl restart fallapp
```

### Error: "PostgreSQL no responde"
```bash
# Verificar contenedor Docker
docker ps | grep fallapp-postgres

# Reiniciar si es necesario
cd /srv/FallApp/05.docker
docker-compose restart postgres
```

### Error: "Tests de performance lentos"
```bash
# Verificar uso de CPU/RAM
top

# Verificar logs del backend
sudo journalctl -u fallapp -n 50
```

---

## 📚 Documentación Relacionada

- [CHANGELOG.md](../../CHANGELOG.md) - Versión 0.5.2
- [GUIA.API.FRONTEND.md](../../GUIA.API.FRONTEND.md) - Documentación del endpoint
- [RESUMEN.UBICACIONES.COMPLETAS.2026-02-03.md](../../RESUMEN.UBICACIONES.COMPLETAS.2026-02-03.md) - Resumen técnico
- [06.tests/README.md](../README.md) - Suite completa de tests

---

**Creado:** 2026-02-03  
**Versión:** v0.5.2  
**Estado:** ✅ Todos los tests activos y funcionales
