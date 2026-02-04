# Checklist de Implementación - Reestructuración Ninots v2.0

> **Versión**: 0.5.0  
> **Fecha inicio**: TBD  
> **Estado**: 📝 Pendiente de Aprobación

---

## 📋 Pre-Requisitos

### Aprobaciones
- [ ] ✅ Tech Lead / Arquitecto revisó y aprobó
- [ ] ✅ Product Owner revisó y aprobó
- [ ] ✅ Equipo Frontend (Desktop) revisó
- [ ] ✅ Equipo Frontend (Mobile) revisó
- [ ] ✅ Fecha de implementación acordada

### Preparación
- [ ] Backup completo de BD de producción realizado
- [ ] Entorno de staging actualizado
- [ ] Tests E2E funcionando en staging
- [ ] Ventana de mantenimiento programada (si necesaria)

---

## 🗄️ Fase 1: Base de Datos

### 1.1 Desarrollo
- [ ] Ejecutar `10.migracion.ninots.simplificados.sql` en BD desarrollo
- [ ] Verificar migración exitosa (sin errores)
- [ ] Verificar integridad referencial
- [ ] Verificar que no hay registros huérfanos
- [ ] Probar rollback y restauración

### 1.2 Staging
- [ ] Backup de BD staging
- [ ] Ejecutar migración en staging
- [ ] Verificar datos migrados correctamente
- [ ] Verificar foreign keys actualizadas
- [ ] Validar índices creados

### 1.3 Producción
- [ ] Backup completo de BD producción
- [ ] Notificar usuarios de ventana de mantenimiento (si aplica)
- [ ] Ejecutar migración en producción
- [ ] Verificar migración exitosa
- [ ] `COMMIT` de migración
- [ ] Validar que tabla `ninots_backup_20260202` existe

**Criterios de aceptación BD**:
- ✅ Tabla `ninots` tiene exactamente 5 columnas
- ✅ Todas las foreign keys funcionan
- ✅ No hay registros huérfanos en `votos` o `comentarios`
- ✅ Backup disponible para rollback

---

## 💻 Fase 2: Backend

### 2.1 Modelo de Datos
- [ ] **Ninot.java**: Actualizar entidad a 5 campos
  - [ ] Eliminar campos obsoletos
  - [ ] Actualizar anotaciones JPA
  - [ ] Mantener relaciones `votos` y `comentarios`
  - [ ] Verificar compilación sin errores

- [ ] **NinotDTO.java**: Simplificar DTO
  - [ ] Reducir a campos esenciales
  - [ ] Actualizar validaciones Bean Validation
  - [ ] Mantener campos calculados (`totalVotos`, `totalComentarios`)

### 2.2 Servicios
- [ ] **NinotService.java**: Simplificar lógica
  - [ ] Actualizar método `convertirADTO()`
  - [ ] Actualizar método `mapearDTOAEntidad()`
  - [ ] Eliminar validaciones de campos obsoletos
  - [ ] Simplificar lógica de creación

- [ ] **VotoService.java**: Verificar funcionamiento
- [ ] **ComentarioService.java**: Verificar funcionamiento

### 2.3 Controladores
- [ ] **NinotController.java**: Adaptar endpoints
  - [ ] Verificar GET `/api/ninots` funciona
  - [ ] Verificar GET `/api/ninots/{id}` funciona
  - [ ] Verificar GET `/api/ninots/falla/{idFalla}` funciona
  - [ ] Verificar POST `/api/ninots` funciona
  - [ ] **Eliminar** endpoint PUT `/api/ninots/{id}` (o adaptar)
  - [ ] Verificar DELETE `/api/ninots/{id}` funciona

### 2.4 Tests
- [ ] Actualizar tests unitarios de `Ninot.java`
- [ ] Actualizar tests de `NinotService.java`
- [ ] Actualizar tests de `NinotController.java`
- [ ] Actualizar datos de prueba (fixtures)
- [ ] Todos los tests pasan (100%)

### 2.5 Documentación Código
- [ ] Actualizar JavaDoc en clases modificadas
- [ ] Actualizar comentarios inline
- [ ] Actualizar `README_API.md` con nuevo modelo

**Criterios de aceptación Backend**:
- ✅ Compilación sin errores
- ✅ 100% de tests unitarios pasan
- ✅ Endpoints funcionan correctamente
- ✅ Swagger UI actualizado

---

## 🎨 Fase 3: Frontend

### 3.1 Desktop (Electron)
- [ ] Actualizar llamadas a API de ninots
- [ ] Adaptar componentes de visualización
- [ ] Eliminar campos obsoletos de formularios
- [ ] Actualizar validaciones
- [ ] Probar flujo completo (crear, ver, eliminar ninot)

### 3.2 Mobile (Android/iOS)
- [ ] Actualizar modelos de datos
- [ ] Adaptar pantallas de ninots
- [ ] Eliminar campos obsoletos
- [ ] Actualizar validaciones
- [ ] Probar flujo completo

**Criterios de aceptación Frontend**:
- ✅ Aplicaciones compilan sin errores
- ✅ Visualización de ninots funciona
- ✅ Creación de ninots funciona
- ✅ Votación funciona
- ✅ Comentarios funcionan

---

## 🧪 Fase 4: Testing Integración

### 4.1 Tests E2E
- [ ] Test: Listar ninots (GET `/api/ninots`)
- [ ] Test: Ver ninot individual (GET `/api/ninots/{id}`)
- [ ] Test: Ninots por falla (GET `/api/ninots/falla/{idFalla}`)
- [ ] Test: Crear ninot (POST `/api/ninots`)
- [ ] Test: Eliminar ninot (DELETE `/api/ninots/{id}`)
- [ ] Test: Votar ninot
- [ ] Test: Comentar ninot
- [ ] Test: Estadísticas de ninots

### 4.2 Tests de Carga
- [ ] Test: 100 usuarios concurrentes
- [ ] Test: Paginación de ninots
- [ ] Test: Consultas optimizadas (sin N+1)

### 4.3 Tests de Regresión
- [ ] Verificar que fallas siguen funcionando
- [ ] Verificar que eventos siguen funcionando
- [ ] Verificar que usuarios siguen funcionando
- [ ] Verificar que votos siguen funcionando
- [ ] Verificar que comentarios siguen funcionando

**Criterios de aceptación Testing**:
- ✅ 100% de tests E2E pasan
- ✅ Tests de carga exitosos
- ✅ No regresiones detectadas

---

## 📚 Fase 5: Documentación

### 5.1 Actualizar Documentación Técnica
- [ ] **03.BASE-DATOS.md**: Actualizar sección 2.4 (tabla ninots)
- [ ] **04.API-REST.md**: Actualizar sección 4.5 (endpoints ninots)
- [ ] **README_API.md**: Actualizar modelo de datos
- [ ] **GUIA.API.FRONTEND.md**: Actualizar ejemplos

### 5.2 Actualizar Guías
- [ ] **01.GUIA-PROGRAMACION.md**: Añadir notas sobre cambio
- [ ] **QUICKSTART.md**: Actualizar ejemplos

### 5.3 Changelog
- [ ] Actualizar `CHANGELOG.md` con versión 0.5.0
- [ ] Marcar propuesta como implementada
- [ ] Documentar fecha de implementación

**Criterios de aceptación Documentación**:
- ✅ Toda la documentación actualizada
- ✅ Sin referencias a campos obsoletos
- ✅ Ejemplos funcionan correctamente

---

## 🚀 Fase 6: Despliegue

### 6.1 Staging
- [ ] Desplegar backend actualizado
- [ ] Desplegar frontend Desktop
- [ ] Desplegar frontend Mobile
- [ ] Smoke tests en staging
- [ ] Aprobación QA

### 6.2 Producción
- [ ] Notificar usuarios (si ventana mantenimiento)
- [ ] Desplegar BD (migración)
- [ ] Desplegar backend
- [ ] Desplegar frontend Desktop
- [ ] Desplegar frontend Mobile
- [ ] Verificar que todo funciona
- [ ] Monitorear logs (primeras 24h)

### 6.3 Post-Despliegue
- [ ] Enviar comunicado de cambios a usuarios
- [ ] Actualizar changelog público
- [ ] Monitorear métricas de rendimiento
- [ ] Validar que no hay errores

**Criterios de aceptación Despliegue**:
- ✅ 0 downtime (o dentro de ventana acordada)
- ✅ Todas las aplicaciones funcionan
- ✅ No errores críticos en logs
- ✅ Usuarios pueden usar el sistema normalmente

---

## 🔙 Plan de Rollback

### Si algo sale mal:

**Base de Datos**:
```sql
-- Restaurar tabla desde backup
DROP TABLE ninots;
ALTER TABLE ninots_backup_20260202 RENAME TO ninots;
-- Restaurar foreign keys
-- Restaurar índices
```

**Backend**:
- [ ] Revertir commit de código
- [ ] Redesplegar versión anterior

**Frontend**:
- [ ] Revertir a build anterior
- [ ] Redesplegar versión estable

**Tiempo estimado rollback**: 30 minutos

---

## 📊 Métricas de Éxito

Al completar la implementación:

| Métrica | Objetivo | Estado |
|---------|----------|--------|
| Campos en tabla `ninots` | 5 | ⏳ |
| Tests unitarios pasando | 100% | ⏳ |
| Tests E2E pasando | 100% | ⏳ |
| Downtime | 0 min (o < 5 min) | ⏳ |
| Errores críticos | 0 | ⏳ |
| Documentación actualizada | 100% | ⏳ |
| Quejas de usuarios | 0 | ⏳ |

---

## 📝 Notas de Implementación

### Fecha inicio: _______
### Fecha fin: _______
### Responsables:
- Backend: _______
- Frontend Desktop: _______
- Frontend Mobile: _______
- BD: _______
- QA: _______

### Incidencias encontradas:
```
Documentar aquí cualquier problema encontrado durante la implementación
```

### Lecciones aprendidas:
```
Documentar mejoras para futuras migraciones
```

---

## ✅ Sign-off Final

- [ ] Tech Lead aprueba implementación
- [ ] QA aprueba funcionalidad
- [ ] Product Owner aprueba cambios
- [ ] Documentación completa y actualizada

**Fecha de cierre**: _______

**Estado final**: 
- [ ] ✅ Implementado exitosamente
- [ ] ❌ Revertido (documentar razón)
- [ ] ⏸️ Pausado (documentar razón)

---

**Última actualización**: 2026-02-02  
**Responsable**: Equipo FallApp
