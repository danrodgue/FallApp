# 📂 Documentación Histórica (old-docs)

> **Propósito:** Esta carpeta contiene versiones antiguas de documentación que han sido reemplazadas por versiones actualizadas.

**Fecha de creación:** 2026-02-10
**Política:** Preservar para referencia histórica, pero NO usar para desarrollo actual

---

## ⚠️ IMPORTANTE

**NO USES ESTOS DOCUMENTOS PARA DESARROLLO ACTUAL**

Esta documentación está desactualizada y ha sido reemplazada por versiones mejoradas en el directorio principal `/04.docs/`.

---

## 📋 Contenido de esta Carpeta

### Especificaciones Técnicas (Versiones Antiguas)

| Archivo | Versión Original | Reemplazado Por | Fecha Deprecación | Razón |
|---------|-----------------|----------------|-------------------|-------|
| `05.SISTEMA-VOTACION-v2.0-deprecated.md` | v2.0 | `especificaciones/05.SISTEMA-VOTACION.md` v3.0 | 2026-02-10 | Tipos de voto obsoletos, modelo incompleto |
| `04.API-REST-v1.0-deprecated.md` | v1.0 | `especificaciones/04.API-REST.md` v2.0 | 2026-02-10 | Endpoints y DTOs desactualizados |
| `03.BASE-DATOS-pre-v0.5.0.md` | Pre-v0.5.0 | `especificaciones/03.BASE-DATOS.md` v0.5.0+ | 2026-02-10 | Modelo de votos antiguo |

### Documentación de Apps (Versiones Antiguas)

| Archivo | Deprecación | Reemplazado Por | Razón |
|---------|------------|----------------|-------|
| `00.ARQUITECTURA-MOBILE-deprecated.md` | 2026-02-10 | `apps/android/ARQUITECTURA.md` | Reorganización por plataforma |
| `01.APP-ADMIN-SPEC-deprecated.md` | 2026-02-10 | `apps/electron/SPEC-ADMIN.md` | Separación Desktop/Mobile |
| `02.APP-USER-SPEC-deprecated.md` | 2026-02-10 | `apps/android/SPEC-USER.md` | Separación Desktop/Mobile |

### Notas Históricas

| Archivo | Tipo | Fecha | Contenido |
|---------|------|-------|-----------|
| `NOTA.ACTUALIZACION.DOCUMENTACION.v0.5.0.md` | Histórica | 2026-02-02 | Resumen de cambios v0.5.0 |

---

## 🔍 ¿Por Qué Están Aquí?

### Cambio de Modelo de Votación (v0.5.0)

**Antes (hasta v0.4.x):**
- Tipos de voto: `favorito`, `ingenioso`, `critico`, `artistico`, `rating`
- Modelo: Votos directos a ninots individuales
- Endpoint: `POST /api/votos` con `idNinot`

**Ahora (v0.5.0+):**
- Tipos de voto: `EXPERIMENTAL`, `INGENIO_Y_GRACIA`, `MONUMENTO`
- Modelo: Votos a fallas (con relación a ninot)
- Endpoint: `POST /api/votos` con `idFalla`, `tipoVoto`

**Documentos afectados:**
- Sistema de Votación (spec completa reescrita)
- API REST (endpoints y DTOs actualizados)
- Base de Datos (ENUM tipo_voto cambiado)

### Reestructuración de Documentación de Apps

**Antes:**
- Todo mezclado en `/04.docs/app/`
- Sin diferenciación clara entre Android/Electron
- Especificaciones genéricas

**Ahora:**
- Separación por plataforma: `/apps/android/`, `/apps/electron/`
- Guías de integración específicas
- Casos de uso por app

---

## 📖 Uso de Esta Documentación

### ✅ Casos Válidos de Uso

1. **Investigación histórica**: Entender decisiones pasadas
2. **Auditoría**: Revisar cambios entre versiones
3. **Debugging**: Comparar comportamiento antiguo vs nuevo
4. **Aprendizaje**: Ver evolución del proyecto

### ❌ NO Uses Esta Documentación Para

1. ❌ Implementar nuevas funcionalidades
2. ❌ Integrar con la API actual
3. ❌ Crear tests
4. ❌ Guiar desarrollo de apps
5. ❌ Documentar código nuevo

---

## 🔄 Cambios Principales Documentados

### v0.5.0 (2026-02-02)

**Backend:**
- Reestructuración de tabla `ninots` (simplificación a 5 campos)
- Cambio de relaciones: votos/comentarios ahora en fallas, no ninots
- Nuevo modelo de votación por categorías

**Impacto en Documentación:**
- ADR-009: Simplificación de ninots
- ADR-010: Realineación de relaciones
- CHANGELOG: 15 archivos modificados
- GUIA.API.FRONTEND: Breaking changes documentados

**Ver:** `NOTA.ACTUALIZACION.DOCUMENTACION.v0.5.0.md` (en esta carpeta)

---

## 📚 Documentación Actualizada

Para desarrollo actual, consulta:

### Especificaciones Técnicas
- [00.VISION-GENERAL.md](../especificaciones/00.VISION-GENERAL.md) - Visión del sistema
- [01.SISTEMA-USUARIOS.md](../especificaciones/01.SISTEMA-USUARIOS.md) - Autenticación y usuarios
- [02.FALLAS.md](../especificaciones/02.FALLAS.md) - Gestión de fallas
- [03.BASE-DATOS.md](../especificaciones/03.BASE-DATOS.md) - **ACTUALIZADO v0.5.0+**
- [04.API-REST.md](../especificaciones/04.API-REST.md) - **ACTUALIZADO v2.0**
- [05.SISTEMA-VOTACION.md](../especificaciones/05.SISTEMA-VOTACION.md) - **ACTUALIZADO v3.0**

### Guías de Desarrollo
- [01.GUIA-PROGRAMACION.md](../01.GUIA-PROGRAMACION.md)
- [02.GUIA-PROMPTS-IA.md](../02.GUIA-PROMPTS-IA.md)
- [03.CONVENCIONES-IDIOMA.md](../03.CONVENCIONES-IDIOMA.md)

### Documentación por Plataforma
- [apps/android/](../apps/android/) - Android Kotlin + Compose
- [apps/electron/](../apps/electron/) - Desktop Electron

### Decisiones Arquitectónicas
- [arquitectura/](../arquitectura/) - ADRs actualizados

---

## 🗄️ Política de Retención

- **Mantener:** Documentos de versiones principales (v0.4.0, v0.5.0, etc.)
- **Eliminar tras 1 año:** Notas de actualización menores
- **Preservar indefinidamente:** ADRs históricos, decisiones arquitectónicas

---

## 📞 Contacto

¿Preguntas sobre documentación histórica?

- Consulta [00.INDICE.md](../00.INDICE.md) para navegación actual
- Revisa [CHANGELOG.md](../../CHANGELOG.md) para historial completo
- Lee ADRs en [arquitectura/](../arquitectura/) para decisiones arquitectónicas

---

**Última actualización:** 2026-02-10
**Mantenedor:** Equipo FallApp
