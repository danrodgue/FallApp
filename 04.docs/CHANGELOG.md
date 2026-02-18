# 📋 Changelog - Documentación FallApp

Todos los cambios notables en la documentación del proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Versionado Semántico](https://semver.org/lang/es/).

---

## [2.0.4] - 2026-02-18

### 🐛 Corregido

#### IA de Sentimiento (Hugging Face)
- **Endpoint Hugging Face actualizado**:
  - Migración desde `api-inference.huggingface.co` (obsoleto) a `router.huggingface.co/hf-inference/models`
  - Configuración externalizada en propiedades/variables:
    - `huggingface.api.base-url`
    - `huggingface.api.model`

- **Compatibilidad HTTP con router Hugging Face**:
  - Header `Accept: application/json` añadido explícitamente
  - Eliminado error `400 Accept type not supported`

- **Reanálisis de comentarios robustecido**:
  - Reprocesa pendientes con estados nulos/vacíos/no válidos
  - Soporte para texto heredado (`contenido`/`texto_comentario`)
  - Normalización de etiquetas a `positive|neutral|negative`

#### Seguridad / Acceso a Datos (403)
- **Spring Security**:
  - Permitido `OPTIONS /**` (preflight CORS)
  - Reglas alineadas para rutas con y sin prefijo `/api`
  - Endpoints admin protegidos por autenticación sin bloquear preflight

#### Docker / Arranque Backend
- Ajustes en `docker-compose.yml` para estabilidad en servidor:
  - `SERVER_SERVLET_CONTEXT_PATH=/` (evita duplicidad `/api/api`)
  - Dialecto: `org.hibernate.dialect.PostgreSQLDialect`
  - `SPRING_JPA_HIBERNATE_DDL_AUTO` por defecto `none`
  - Variables Hugging Face añadidas (`HUGGINGFACE_API_BASE_URL`, `HUGGINGFACE_API_MODEL`)

### 📚 Documentación

- Nuevo documento de incidente y resolución:
  - `BUGFIX-2026-02-18-ia-sentimiento-huggingface.md`

---

## [2.0.3] - 2026-02-16

### ✨ Agregado

#### Sistema de Verificación de Email
- **Verificación de cuentas por email** - Sistema completo implementado
  - Campo `verificado` en tabla `usuarios` (ya existía en schema)
  - Campos `token_verificacion` y `token_verificacion_expira` para gestión de tokens
  - Token único generado al registro (UUID sin guiones, 64 caracteres)
  - Expiración de token a 24 horas
  - Email automático de verificación en registro
  
- **Nuevos endpoints de autenticación**:
  - `GET /api/auth/verificar?token={token}` - Verificar email con token
  - `POST /api/auth/reenviar-verificacion?email={email}` - Reenviar email de verificación
  
- **EmailService.java** - Servicio completo de envío de emails
  - Integración con Brevo (antes Sendinblue) vía SMTP
  - Métodos: `sendSimpleEmail`, `sendHtmlEmail`, `sendVerificationEmail`
  - Plantillas HTML predefinidas (bienvenida, verificación, recuperación contraseña, notificaciones)
  
- **TestEmailController.java** - Controlador para testing de emails
  - Endpoints de prueba para todos los tipos de email
  - Solo para desarrollo (documentar seguridad en producción)

- **Migración SQL**: `12.migracion.verificacion_email.sql`
  - Agrega campos `token_verificacion` y `token_verificacion_expira`
  - Índices para búsquedas optimizadas
  - Migración automática de usuarios existentes como verificados

#### Documentación Email
- **CONFIGURACION.BREVO.EMAIL.md** - Guía completa de configuración Brevo
  - Paso a paso para obtener credenciales SMTP
  - Configuración en application.properties
  - Ejemplos de uso del EmailService
  - Solución de problemas comunes
  
- **QUICKSTART.BREVO.md** - Guía rápida (5 minutos)
  - Pasos esenciales para configurar Brevo
  - Comparativa Gmail vs Brevo
  - Checklist de configuración

### 🔄 Cambiado

#### Backend - Usuarios
- **Usuario.java** - Campos agregados:
  - `verificado` (Boolean) - Estado de verificación de email
  - `tokenVerificacion` (String) - Token único de verificación
  - `tokenVerificacionExpira` (LocalDateTime) - Fecha de expiración del token
  
- **UsuarioDTO.java** - Campo `verificado` expuesto en API

- **AuthController.java** - Actualizado registro:
  - Genera token de verificación al registrar usuario
  - Envía email de verificación automáticamente
  - Mensaje de respuesta indica necesidad de verificación
  - Manejo de errores de email (no falla el registro)

- **UsuarioService.java** - Método `convertirADTO()` actualizado
  - Incluye campo `verificado` en la conversión

#### Configuración
- **application.properties** - Configuración SMTP agregada:
  - Host: `smtp-relay.brevo.com`
  - Puerto: `587` (TLS)
  - Credenciales (username/password)
  - Propiedades SMTP (auth, starttls, timeouts)
  - Email remitente y nombre

- **pom.xml** - Dependencia agregada:
  - `spring-boot-starter-mail` para JavaMailSender

### 📚 Documentación Mejorada
- Guías de configuración de Brevo (completa y rápida)
- Documentación de nuevos endpoints de verificación
- Ejemplos de uso del servicio de email
- Proceso de verificación documentado

---

## [2.0.2] - 2026-02-13

### ✨ Agregado

#### API Eventos - Documentación Completa y Actualizada
- **GUIA.API.FRONTEND.md** - Sección de eventos completamente reescrita
  - Agregado endpoint `GET /api/eventos` - Listado con filtros y paginación completa
  - Agregado endpoint `GET /api/eventos/futuros` - Todos los eventos futuros
  - Agregado endpoint `GET /api/eventos/falla/{idFalla}` - Eventos por falla (paginado)
  - Actualizado `GET /api/eventos/proximos` - Corregidos parámetros (solo `limite`, no `dias`)
  - Actualizado `GET /api/eventos/tipo/{tipo}` - Lista completa de 10 tipos válidos
  - Tabla resumen con todos los endpoints de eventos
  - Formato de respuesta corregido (Spring Data Page con `content`, `totalElements`, `number`)

### 🔄 Cambiado

#### Backend - Eventos
- **EventoDTO.java** - Campos ampliados:
  - Agregados: `direccion`, `urlImagen`, `creadoPor`, `fechaCreacion`, `actualizadoEn`
  - DTO interno `UsuarioSimpleDTO` para auditoría
  
- **EventoService.java** - Nuevos métodos:
  - `listarConFiltros()` - Listado general con múltiples filtros opcionales
  - `obtenerPorTipo()` - Filtrado específico por tipo de evento
  - Conversión DTO mejorada con todos los campos incluyendo auditoría
  
- **EventoController.java** - Endpoints completos:
  - `GET /api/eventos` - Listado con paginación y filtros (id_falla, tipo, fechas, ordenar_por)
  - `GET /api/eventos/tipo/{tipo}` - Filtrado por tipo específico
  - Todos los endpoints retornan DTOs completos con campos de auditoría

#### Base de Datos
- **eventos.tipo** - Migrado de ENUM PostgreSQL a VARCHAR(30)
  - Razón: Compatibilidad con Hibernate EnumType.STRING
  - Sin pérdida de datos, conversión directa
  - Tipos soportados: planta, crema, ofrenda, infantil, concierto, exposicion, encuentro, cena, teatro, otro

#### Documentación
- **00.INDICE.md** - Actualizado a v2.0.2
  - Sección "Novedades v2.0.2" con resumen de cambios en API Eventos
  - Fecha de actualización: 2026-02-13

- **GUIA.API.FRONTEND.md** - Correcciones importantes:
  - ❌ Eliminados endpoints deshabilitados (`PUT/GET /api/eventos/{id}/imagen`)
  - ✅ Campos de respuesta corregidos (camelCase consistente, estructura Spring Page)
  - ✅ Parámetros de petición actualizados (nombres correctos)
  - ✅ Explicación de tipos de evento completa (10 tipos)
  - ✅ Notas sobre campos de auditoría (`creadoPor`, `fechaCreacion`, `actualizadoEn`)

### ❌ Eliminado

#### Endpoints Deshabilitados Documentados
- **Eliminada documentación de endpoints no funcionales:**
  - ❌ `PUT /api/eventos/{id}/imagen` - BD no tiene columnas `imagen`/`imagen_content_type`
  - ❌ `GET /api/eventos/{id}/imagen` - BD no soporta almacenamiento binario de imágenes
  - ℹ️ **Usar campo `urlImagen` en su lugar**

### 🐛 Corregido

#### Documentación - Correcciones de exactitud
- **GUIA.API.FRONTEND.md sección Eventos:**
  - Corregido: Estructura de respuesta paginada (era `contenido`, ahora `content`)
  - Corregido: Nombres de campos (era snake_case, ahora camelCase consistente)
  - Corregido: Parámetro `GET /api/eventos/proximos` (era `dias`, ahora solo `limite`)
  - Corregido: Permisos `DELETE /api/eventos/{id}` (era "ADMIN o CASAL", ahora "Solo ADMIN")
  - Corregido: Lista de tipos válidos (faltaban `encuentro` y `otro`)

### 📋 Cambios Técnicos

#### Implementación
1. **EventoDTO** - DTO enriquecido con campos completos
2. **EventoService** - Lógica de negocio para filtros avanzados
3. **EventoController** - Endpoints RESTful completos
4. **Migración BD** - ALTER TABLE eventos tipo → VARCHAR(30)
5. **Documentación** - GUIA.API.FRONTEND.md alineada con implementación real

#### Testing
- ✅ Verificados todos los endpoints GET públicos: HTTP 200
- ✅ Creado evento de prueba para validar estructura
- ✅ Confirmada compatibilidad con Electron y Android

### 📖 Notas de Migración

Si estás usando la API de eventos:
1. ✅ Actualiza estructura de respuesta paginada: `.datos.content` (antes `.datos.contenido`)
2. ✅ Usa nombres de campos en camelCase: `idEvento`, `fechaEvento`, etc.
3. ⚠️ Elimina referencias a endpoints de imagen (no funcionan, usar `urlImagen`)
4. ✅ Tipo de evento: VARCHAR en BD, sigue siendo enum en Java (sin cambios en cliente)

---

## [2.0.1] - 2026-02-13

### ✨ Agregado

#### Testing Dashboard - Documentación de Acceso Remoto
- `05.testing-dashboard/ACCESO_REMOTO.md` - Guía completa para configurar acceso remoto
  - Instrucciones paso a paso para AWS Security Groups
  - Comandos AWS CLI alternativos
  - Recomendaciones de seguridad
  - Troubleshooting de conectividad
  - Credenciales de acceso al dashboard

- `05.testing-dashboard/diagnostico.sh` - Script de diagnóstico automatizado
  - Verificación de servidor HTTP (puerto 8001)
  - Verificación de backend API (puerto 8080)
  - Test de acceso local
  - Estado de firewall (UFW)
  - Información de red pública
  - Guía de resolución de problemas

### 🔄 Cambiado

#### Testing Dashboard - Configuración
- `05.testing-dashboard/js/config.js`:
  - **ANTES:** `API_URL: 'http://localhost:8080/api'`
  - **AHORA:** `API_URL: 'http://35.180.21.42:8080/api'`
  - Configuración apunta a servidor remoto en producción

### 🐛 Corregido

#### Documentación de Bugs Resueltos
- **Bug Electron - Persistencia de datos:**
  - Documentado problema de datos de usuario que desaparecían
  - Causa raíz: Columnas faltantes en BD + manejo incorrecto de strings vacíos
  - Solución implementada en backend y base de datos
  - Ver CHANGELOG.md principal v0.5.11 para detalles técnicos

### 📖 Documentación Técnica

Esta actualización documenta:
1. **Corrección crítica** en persistencia de datos de usuario (backend + BD)
2. **Configuración de acceso remoto** al panel de testing
3. **Herramientas de diagnóstico** para troubleshooting

Para detalles de implementación, ver:
- `/srv/FallApp/CHANGELOG.md` - v0.5.11
- `/srv/FallApp/01.backend/src/main/java/com/fallapp/service/UsuarioService.java`
- Esquema de base de datos actualizado con columnas de direcciones

---

## [2.0.0] - 2026-02-10

### 🎯 Reorganización Mayor y Actualización de Documentación

**Objetivo**: Optimizar documentación para spec-driven development y reflejar el estado actual del proyecto (v0.5.8).

### ✨ Agregado

#### Estructura
- `old-docs/` - Carpeta para documentación histórica y deprecated
- `old-docs/README.md` - Guía sobre por qué los documentos están deprecated
- `CHANGELOG.md` - Este archivo, para trackear cambios en documentación
- `BREAKING-CHANGES.md` - Documento de cambios incompatibles entre versiones

#### Documentos Nuevos
- `apps/android/INTEGRACION-API.md` - Guía de integración API para Android
- `apps/electron/INTEGRACION-API.md` - Guía de integración API para Electron

### 🔄 Cambiado

#### Sistema de Votación (CRÍTICO)
- **05.SISTEMA-VOTACION.md**: v2.0 → v4.0
  - **Tipos de voto**: `favorito`, `ingenioso`, `critico`, `artistico` → `EXPERIMENTAL`, `INGENIO_Y_GRACIA`, `MONUMENTO`
  - **Modelo**: Votos a ninots individuales → Votos directos a fallas por categoría
  - **Endpoint**: `POST /api/votos` ahora usa `{idFalla, tipoVoto}` en lugar de `{idNinot, tipoVoto}`
  - **Constraint DB**: `(id_usuario, id_ninot, tipo_voto)` → `(id_usuario, id_falla, tipo_voto)`
  - ✅ Documentación completa con ejemplos Android/Electron
  - ✅ Flujos de usuario end-to-end
  - ✅ Casos de prueba con curl
  - ✅ Métricas y KPIs SQL

#### API REST
- **04.API-REST.md**: v1.0 → v2.0
  - Sección de votos completamente actualizada
  - Tipos de voto actualizados en todos los endpoints
  - Request/Response bodies reflejan modelo actual
  - Referencia cruzada a 05.SISTEMA-VOTACION.md
  - Validaciones y errores actualizados

#### Índice
- **00.INDICE.md**: Referencias actualizadas
  - Links a nueva estructura `old-docs/`
  - Referencias a CHANGELOG y BREAKING-CHANGES
  - Estado actualizado de especificaciones

### 🗄️ Movido a `old-docs/`

Documentos preservados por razones históricas pero no deben usarse para desarrollo:

- `05.SISTEMA-VOTACION-v2.0-deprecated.md` - Versión con tipos de voto obsoletos
- `04.API-REST-v1.0-deprecated.md` - Versión con endpoints desactualizados
- `03.BASE-DATOS-pre-v0.5.0.md` - Esquema pre-reestructuración de ninots
- `00.ARQUITECTURA-MOBILE-deprecated.md` - Documentación genérica de mobile
- `01.APP-ADMIN-SPEC-deprecated.md` - Spec sin separar Android/Electron
- `02.APP-USER-SPEC-deprecated.md` - Spec sin separar Android/Electron
- `NOTA.ACTUALIZACION.DOCUMENTACION.v0.5.0.md` - Nota histórica de cambios v0.5.0

### 🐛 Corregido

#### Inconsistencias Documentadas
- **Sistema de Votación**: Tipos de voto sincronizados entre especificaciones, API y código
- **Referencias Cruzadas**: Links rotos reparados, referencias actualizadas
- **Ejemplos de Código**: Android y Electron usan tipos actuales
- **Nomenclatura**: Consistencia en nombres de campos (`idFalla` vs `id_falla`)

---

## [1.0.0] - 2026-02-01

### Documentación Inicial v0.5.0

#### Estructura Original
- `00.INDICE.md` - Índice maestro
- `01.GUIA-PROGRAMACION.md` - Convenciones de código
- `02.GUIA-PROMPTS-IA.md` - Trabajo con IAs
- `03.CONVENCIONES-IDIOMA.md` - Uso del español

#### Especificaciones
- `especificaciones/00.VISION-GENERAL.md` - Visión del sistema
- `especificaciones/01.SISTEMA-USUARIOS.md` - Usuarios y autenticación
- `especificaciones/02.FALLAS.md` - Gestión de fallas
- `especificaciones/03.BASE-DATOS.md` - Esquema de base de datos
- `especificaciones/04.API-REST.md` - Endpoints API
- `especificaciones/05.SISTEMA-VOTACION.md` - Sistema de votos (v2.0)

#### ADRs (Architecture Decision Records)
- `arquitectura/ADR-001-postgresql-vs-mongodb.md`
- `arquitectura/ADR-002-docker-local-development.md`
- `arquitectura/ADR-003-nomenclatura-scripts-sql.md`
- `arquitectura/ADR-004-postgis-opcional.md`
- `arquitectura/ADR-005-vistas-vs-queries-backend.md`
- `arquitectura/ADR-006-autenticacion-jwt-pendiente.md`
- `arquitectura/ADR-007-formato-respuesta-api.md`
- `arquitectura/ADR-008-postgresql-enum-varchar.md`
- `arquitectura/ADR-009-simplificacion-ninots.md`
- `arquitectura/ADR-010-realineacion-relaciones-ninots.md`

#### Apps
- `app/00.ARQUITECTURA-MOBILE.md`
- `app/01.APP-ADMIN-SPEC.md`
- `app/02.APP-USER-SPEC.md`
- `app/03.PROMPT-GENERACION-IA.md`
- `app/04.PLANTILLA-ERRORES.md`

---

## Tipos de Cambios

- `✨ Agregado` - Nueva funcionalidad o documentación
- `🔄 Cambiado` - Cambios en funcionalidad existente
- `⚠️ Deprecated` - Funcionalidad que será eliminada
- `🗑️ Eliminado` - Funcionalidad eliminada
- `🐛 Corregido` - Corrección de bugs o inconsistencias
- `🔒 Seguridad` - Correcciones de seguridad
- `📚 Documentación` - Solo cambios en documentación
- `🗄️ Movido` - Archivos movidos o reorganizados

---

## Historial de Versiones del Sistema

| Versión | Fecha | Cambios Principales |
|---------|-------|---------------------|
| **v0.5.8** | 2026-02-10 | Sistema de votación v4.0, tipos actuales |
| **v0.5.0** | 2026-02-06 | Reestructuración de ninots, simplificación BD |
| **v0.4.1** | 2026-02-03 | Ajustes en modelo de datos |
| **v0.4.0** | 2026-02-01 | Backend completo, JWT, endpoints CRUD |
| **v0.3.0** | 2026-01-28 | Base de datos PostgreSQL, migraciones |
| **v0.2.0** | 2026-01-25 | Arquitectura inicial, ADRs |
| **v0.1.0** | 2026-01-19 | Inicio del proyecto intermodular |

---

## Convenciones de Versionado

### Documentación
- **MAJOR** (X.0.0): Cambios incompatibles en especificaciones, reestructuración
- **MINOR** (0.X.0): Nuevas especificaciones, actualizaciones significativas
- **PATCH** (0.0.X): Correcciones menores, typos, aclaraciones

### Sistema (Backend/Apps)
Seguimos [Semantic Versioning 2.0.0](https://semver.org/):
- **MAJOR**: Cambios incompatibles en API
- **MINOR**: Nueva funcionalidad compatible hacia atrás
- **PATCH**: Corrección de bugs compatible hacia atrás

---

## Links Útiles

- [BREAKING-CHANGES.md](BREAKING-CHANGES.md) - Cambios incompatibles detallados
- [00.INDICE.md](00.INDICE.md) - Índice maestro de documentación
- [old-docs/README.md](old-docs/README.md) - Documentación histórica
- [Especificaciones](especificaciones/) - Carpeta de especificaciones técnicas
- [ADRs](arquitectura/) - Decisiones arquitectónicas

---

## Cómo Contribuir al Changelog

Cuando actualices documentación significativa:

1. **Determina el tipo de cambio** (Agregado, Cambiado, Deprecated, etc.)
2. **Identifica la versión** (major, minor, patch según impacto)
3. **Agrega entrada** al inicio del archivo (más reciente primero)
4. **Usa formato consistente**:
   ```markdown
   ### Categoría
   - **Documento.md**: Descripción breve del cambio
     - Detalles adicionales si es necesario
     - Bullet points para múltiples cambios
   ```
5. **Referencia PRs/Issues** si aplica
6. **Actualiza fecha** en el encabezado de la versión

### Ejemplo
```markdown
## [2.1.0] - 2026-02-15

### ✨ Agregado
- **05.SISTEMA-VOTACION.md**: Agregado sistema de badges por actividad
  - 🥉 Bronce: 10 votos
  - 🥈 Plata: 50 votos
  - 🥇 Oro: 100 votos
```

---

**Última actualización**: 2026-02-10
**Mantenedor**: Equipo FallApp
**Siguiente revisión**: Tras próxima actualización mayor (v2.1.0)
