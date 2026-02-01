# 🗺️ Guía Rápida de Navegación - Despliegue BD FallApp

## 📄 Documentación Principal

### Para Revisión Rápida
- 📋 **[RESUMEN.EJECUTIVO.md](RESUMEN.EJECUTIVO.md)** - Resumen en 2 minutos (métricas, logros, archivos)
- ✅ **[CHECKLIST.DESPLIEGUE.BD.md](CHECKLIST.DESPLIEGUE.BD.md)** - Checklist completo de validación (detallado)

### Para Desarrollo
- 📖 **[README.md](README.md)** - Información general del proyecto
- 📝 **[CHANGELOG.md](CHANGELOG.md)** - Historial de versiones (Keep a Changelog)
- 🔍 **[AUDITORIA.DESPLIEGUE.BD.md](AUDITORIA.DESPLIEGUE.BD.md)** - Auditoría técnica completa

## 🏗️ Arquitectura (ADRs)

**Ubicación**: [04.docs/arquitectura/](04.docs/arquitectura/)

1. **[ADR-001](04.docs/arquitectura/ADR-001-postgresql-vs-mongodb.md)** - PostgreSQL vs MongoDB
2. **[ADR-002](04.docs/arquitectura/ADR-002-docker-local-development.md)** - Docker para desarrollo local
3. **[ADR-003](04.docs/arquitectura/ADR-003-nomenclatura-scripts-sql.md)** - Nomenclatura scripts SQL
4. **[ADR-004](04.docs/arquitectura/ADR-004-postgis-opcional.md)** - PostGIS opcional (MVP)
5. **[ADR-005](04.docs/arquitectura/ADR-005-vistas-vs-queries-backend.md)** - Vistas SQL vs queries backend

## 📐 Especificaciones Técnicas

**Ubicación**: [04.docs/especificaciones/](04.docs/especificaciones/)

- **[03.BASE-DATOS.md](04.docs/especificaciones/03.BASE-DATOS.md)** - Especificación completa de BD
  - Sección 3: ENUMs
  - Sección 6: 9 vistas especializadas
  - Sección 7: 2 funciones SQL
  - Sección 8: 5 triggers de auditoría

## 🧪 Tests

**Ubicación**: [06.tests/](06.tests/)

### Guía de Tests
- 📚 **[06.tests/README.md](06.tests/README.md)** - Guía completa de ejecución y troubleshooting

### Tests de Integración (SQL)
- [test_01_schema_creation.sql](06.tests/integration/test_01_schema_creation.sql) - ✅ 9/9 PASS
- [test_02_data_integrity.sql](06.tests/integration/test_02_data_integrity.sql) - ✅ 10/10 PASS
- [test_03_views_functions.sql](06.tests/integration/test_03_views_functions.sql) - ⚠️ 7/10 PASS
- [test_04_triggers.sql](06.tests/integration/test_04_triggers.sql) - ⚠️ 2/5 PASS

### Tests E2E (Bash)
- [test_docker_compose.sh](06.tests/e2e/test_docker_compose.sh) - 10 tests
- [test_postgres_connection.sh](06.tests/e2e/test_postgres_connection.sh) - ✅ 10/10 PASS
- [test_data_persistence.sh](06.tests/e2e/test_data_persistence.sh) - 7 tests

### Ejecutar Tests
```bash
# Suite completa
cd /srv/FallApp/06.tests
bash run_tests.sh

# Test individual recomendado
bash e2e/test_postgres_connection.sh
```

## 🗄️ Base de Datos

### Scripts SQL
**Ubicación**: [07.datos/scripts/](07.datos/scripts/)

- [01.schema.sql](07.datos/scripts/01.schema.sql) - Esquema de BD (6 tablas, 4 ENUMs)
- [10.seed.usuarios.sql](07.datos/scripts/10.seed.usuarios.sql) - Usuarios de prueba
- [20.import.fallas.sql](07.datos/scripts/20.import.fallas.sql) - Importación de 346 fallas
- [30.vistas.consultas.sql](07.datos/scripts/30.vistas.consultas.sql) - 9 vistas + 2 funciones

### Datos
**Ubicación**: [07.datos/](07.datos/)

- [raw/falles-fallas.json](07.datos/raw/falles-fallas.json) - Datos municipales originales (346 fallas)

## 🐳 Docker

**Ubicación**: [05.docker/](05.docker/)

- **[docker-compose.yml](05.docker/docker-compose.yml)** - Configuración de servicios (PostgreSQL + pgAdmin)
- **[DESPLIEGUE.COMPLETADO.md](05.docker/DESPLIEGUE.COMPLETADO.md)** - Estado detallado del despliegue

### Comandos Rápidos
```bash
cd /srv/FallApp/05.docker

# Iniciar servicios
sudo docker-compose up -d

# Ver logs
sudo docker-compose logs -f postgres

# Acceder a psql
sudo docker exec -it fallapp-postgres psql -U fallapp_user -d fallapp

# Detener servicios
sudo docker-compose down
```

## 📚 Guías de Desarrollo

**Ubicación**: [04.docs/](04.docs/)

- **[LEEME.IA.md](04.docs/LEEME.IA.md)** - Guía para asistentes de IA
- **[01.GUIA-PROGRAMACION.md](04.docs/01.GUIA-PROGRAMACION.md)** - Estándares de código
- **[NOMENCLATURA.FICHEROS.md](04.docs/NOMENCLATURA.FICHEROS.md)** - Convenciones de nombres

## 🎯 Flujos de Trabajo Típicos

### 🆕 Onboarding Nuevo Desarrollador
1. Leer [RESUMEN.EJECUTIVO.md](RESUMEN.EJECUTIVO.md)
2. Leer [README.md](README.md) - Sección "Estado del Proyecto"
3. Revisar [05.docker/DESPLIEGUE.COMPLETADO.md](05.docker/DESPLIEGUE.COMPLETADO.md)
4. Ejecutar `cd 05.docker && sudo docker-compose up -d`
5. Ejecutar [test_postgres_connection.sh](06.tests/e2e/test_postgres_connection.sh)

### 🔍 Entender Arquitectura
1. Leer [04.docs/arquitectura/ADR-001](04.docs/arquitectura/ADR-001-postgresql-vs-mongodb.md) (PostgreSQL justification)
2. Leer [04.docs/arquitectura/ADR-005](04.docs/arquitectura/ADR-005-vistas-vs-queries-backend.md) (Views strategy)
3. Revisar [04.docs/especificaciones/03.BASE-DATOS.md](04.docs/especificaciones/03.BASE-DATOS.md)

### 🧪 Validar Cambios
1. Ejecutar tests SQL: `cd 06.tests && bash run_tests.sh`
2. Revisar [CHECKLIST.DESPLIEGUE.BD.md](CHECKLIST.DESPLIEGUE.BD.md)
3. Actualizar [CHANGELOG.md](CHANGELOG.md) si hay cambios

### 📝 Documentar Nueva Decisión
1. Crear nuevo ADR en [04.docs/arquitectura/](04.docs/arquitectura/) siguiendo formato de ADR-001 a ADR-005
2. Referenciar ADR en código afectado (header de archivos SQL)
3. Actualizar [README.md](README.md) si es relevante para usuarios

## 📞 Soporte

- **Issues**: Crear issue en GitHub con etiqueta `database` o `tests`
- **Documentación adicional**: [04.docs/](04.docs/)
- **Tests con errores**: Ver [06.tests/README.md](06.tests/README.md) sección "Issues Conocidos"

---

**Última actualización**: 2024-02-01  
**Versión del despliegue**: v0.1.0  
**Estado**: ✅ COMPLETADO

