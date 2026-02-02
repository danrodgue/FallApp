# SPEC-NINOT-SIMPLIFICADO: Reestructuración Tabla Ninots

> **Spec ID**: SPEC-NINOT-001  
> **Versión**: 2.0  
> **Estado**: 📝 Propuesta  
> **Fecha**: 2026-02-02  
> **Autor**: Equipo FallApp

---

## 1. Contexto y Motivación

### 1.1 Situación Actual

La tabla `ninots` tiene **20+ campos** con información detallada:
- Dimensiones (altura, ancho, profundidad, peso)
- Material principal
- Artista constructor
- Año de construcción
- Notas técnicas
- Múltiples URLs de imágenes
- Información de premios

**Problema**: Los datos originales (`falles-fallas.jsonl`) NO contienen esta información. Solo disponemos de:
- Imagen del boceto de cada falla (`boceto` field)
- Información básica de la falla

### 1.2 Realidad del Proyecto

❌ **No tenemos datos de ninots individuales**  
❌ **No hay información de dimensiones, artistas, materiales**  
❌ **Mantener 20+ campos vacíos no aporta valor**  
✅ **Solo necesitamos mostrar imágenes de bocetos/ninots asociados a cada falla**

### 1.3 Objetivo de la Reestructuración

Simplificar la tabla `ninots` a:
- ✅ **ID único** (PK)
- ✅ **Relación con falla** (FK)
- ✅ **Nombre simple** (opcional, para identificación)
- ✅ **URL de imagen** (campo principal)
- ✅ **Auditoría básica** (fecha_creacion)

---

## 2. Especificación Nueva Tabla

### 2.1 Esquema SQL Simplificado

```sql
-- =============================================================================
-- TABLA: ninots (VERSIÓN SIMPLIFICADA)
-- =============================================================================
-- Propósito: Almacenar imágenes de ninots/bocetos asociados a cada falla
-- Dato real disponible: Solo URLs de imágenes (bocetos de fallas)
-- =============================================================================

CREATE TABLE ninots (
    -- Identificación
    id_ninot SERIAL PRIMARY KEY,
    id_falla INTEGER NOT NULL,
    
    -- Información Básica
    nombre VARCHAR(255) NULL,  -- Nombre opcional para identificar el ninot
    
    -- Multimedia (CAMPO PRINCIPAL)
    url_imagen VARCHAR(500) NOT NULL,  -- URL de la imagen del ninot/boceto
    
    -- Auditoría
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_ninots_id_falla 
        FOREIGN KEY (id_falla) REFERENCES fallas(id_falla) ON DELETE CASCADE
);

-- Índices
CREATE INDEX idx_ninots_id_falla ON ninots(id_falla);
CREATE INDEX idx_ninots_fecha_creacion ON ninots(fecha_creacion);

-- Comentario
COMMENT ON TABLE ninots IS 'Ninots/bocetos asociados a fallas - versión simplificada solo con URLs';
COMMENT ON COLUMN ninots.url_imagen IS 'URL de la imagen del ninot o boceto de la falla';
```

### 2.2 Comparación Antes/Después

| Aspecto | ANTES (v1.0) | DESPUÉS (v2.0) |
|---------|--------------|----------------|
| **Campos totales** | 20+ campos | 5 campos |
| **Campos obligatorios** | 3 (id, id_falla, nombre_ninot) | 3 (id, id_falla, url_imagen) |
| **Campos multimedia** | 2 (principal + array adicionales) | 1 (url_imagen) |
| **Campos técnicos** | 8 (dimensiones, material, artista, etc.) | 0 |
| **Campos premios** | 3 (premiado, categoria, año) | 0 |
| **Complejidad INSERT** | Alta (muchas validaciones) | Mínima |
| **Datos reales disponibles** | 0% de los campos técnicos | 100% (solo imagen) |

### 2.3 Razones de Eliminación por Campo

| Campo Eliminado | Razón |
|-----------------|-------|
| `titulo_obra` | No disponemos de esta información |
| `descripcion` | No tenemos descripciones de ninots individuales |
| `altura_metros`, `ancho_metros`, `profundidad_metros`, `peso_toneladas` | Información técnica no disponible |
| `material_principal` | No conocemos los materiales usados |
| `artista_constructor` | Sin datos de artistas de ninots (solo de fallas) |
| `año_construccion` | No disponible |
| `url_imagenes_adicionales` | Array complejo, solo tenemos 1 imagen por falla |
| `premiado`, `categoria_premio`, `año_premio` | Sistema de premios no implementado |
| `notas_tecnicas` | Sin información técnica |
| `actualizado_en` | Innecesario para entidad de solo lectura |

---

## 3. Modelo de Dominio Actualizado

### 3.1 Entidad JPA Simplificada

```java
package com.fallapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Ninot (Versión Simplificada)
 * 
 * Propósito: Almacenar imágenes de ninots/bocetos asociados a fallas
 * Nota: Solo contiene campos con datos reales disponibles
 * 
 * @version 2.0
 * @since 2026-02-02
 */
@Entity
@Table(name = "ninots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ninot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ninot")
    private Long idNinot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_falla", nullable = false)
    @NotNull(message = "La falla es obligatoria")
    private Falla falla;

    @Column(name = "nombre", length = 255)
    private String nombre;  // Opcional

    @Column(name = "url_imagen", nullable = false, length = 500)
    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String urlImagen;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Relaciones: NINOTS NO TIENEN VOTOS NI COMENTARIOS
    // Los votos y comentarios están en la FALLA, no en el ninot
    // Ver ADR-010 para explicación detallada de esta decisión arquitectónica
}
```

### 3.2 DTO Simplificado

```java
package com.fallapp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Ninot (Versión Simplificada)
 * 
 * @version 2.0
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NinotDTO {
    
    // Identificación
    private Long idNinot;
    
    @NotNull(message = "El ID de la falla es obligatorio")
    private Long idFalla;
    
    private String nombreFalla;  // Incluido para respuestas
    
    // Información básica
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;
    
    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Pattern(
        regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$",
        message = "URL de imagen inválida"
    )
    private String urlImagen;
    
    // Auditoría
    private LocalDateTime fechaCreacion;
    
    // NOTA v0.5.0: totalVotos y totalComentarios ELIMINADOS
    // Los votos/comentarios están en la falla, no en el ninot individual
    // Ver ADR-010 para detalles arquitectónicos
}
```

---

## 4. API REST Actualizada

### 4.1 Endpoints Simplificados

#### GET `/api/ninots` - Listar ninots

**Respuesta**:
```json
{
  "exito": true,
  "datos": {
    "content": [
      {
        "idNinot": 1,
        "idFalla": 5,
        "nombreFalla": "Falla Convento Jerusalén",
        "nombre": "Boceto 2026",
        "urlImagen": "https://fallapp.es/bocetos/falla-5-2026.jpg",
        "fechaCreacion": "2026-02-01T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

#### GET `/api/ninots/falla/{idFalla}` - Ninots de una falla

**Caso común**: 1 ninot (boceto) por falla

```json
{
  "exito": true,
  "datos": [
    {
      "idNinot": 23,
      "nombre": "Boceto Falla 2026",
      "urlImagen": "https://fallapp.es/bocetos/falla-15-boceto.jpg",
      "fechaCreacion": "2026-01-15T12:00:00Z"
    }
  ]
}
```

#### POST `/api/ninots` - Crear ninot

**Request**:
```json
{
  "idFalla": 12,
  "nombre": "Boceto Principal",
  "urlImagen": "https://fallapp.es/bocetos/nueva-imagen.jpg"
}
```

**Response** (201):
```json
{
  "exito": true,
  "mensaje": "Ninot creado exitosamente",
  "datos": {
    "idNinot": 156,
    "idFalla": 12,
    "nombreFalla": "Falla Plaza del Pilar",
    "nombre": "Boceto Principal",
    "urlImagen": "https://fallapp.es/bocetos/nueva-imagen.jpg",
    "fechaCreacion": "2026-02-02T15:30:00Z"
  }
}
```

### 4.2 Endpoints Eliminados

❌ **PUT `/api/ninots/{id}`** - No hay campos para actualizar (solo URL)  
❌ **Búsqueda por dimensiones** - Campos no existen  
❌ **Filtros por material/artista** - Campos no existen  
❌ **Filtros por premios** - Sistema no implementado

**Alternativa**: Para cambiar imagen, eliminar y crear nuevo ninot

---

## 5. Migración de Datos

### 5.1 Script de Migración SQL

```sql
-- =============================================================================
-- MIGRACIÓN: ninots v1.0 → v2.0 (Simplificación)
-- =============================================================================
-- Fecha: 2026-02-02
-- Descripción: Simplifica tabla ninots conservando solo campos con datos reales
-- =============================================================================

BEGIN;

-- PASO 1: Crear tabla temporal con nueva estructura
CREATE TABLE ninots_new (
    id_ninot SERIAL PRIMARY KEY,
    id_falla INTEGER NOT NULL,
    nombre VARCHAR(255) NULL,
    url_imagen VARCHAR(500) NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ninots_id_falla 
        FOREIGN KEY (id_falla) REFERENCES fallas(id_falla) ON DELETE CASCADE
);

-- PASO 2: Migrar datos existentes
-- Mapeo: nombre_ninot → nombre
--        url_imagen_principal → url_imagen
--        fecha_creacion → fecha_creacion (mantener)
INSERT INTO ninots_new (id_ninot, id_falla, nombre, url_imagen, fecha_creacion)
SELECT 
    id_ninot,
    id_falla,
    nombre_ninot,  -- Migrar nombre
    COALESCE(url_imagen_principal, 'https://fallapp.es/default-ninot.jpg'),  -- URL obligatoria
    fecha_creacion
FROM ninots
WHERE url_imagen_principal IS NOT NULL;  -- Solo migrar ninots con imagen

-- PASO 3: Verificar migración
SELECT 
    'Ninots migrados' as descripcion,
    COUNT(*) as total,
    COUNT(url_imagen) as con_imagen,
    COUNT(nombre) as con_nombre
FROM ninots_new;

-- PASO 4: Actualizar foreign keys en tablas dependientes
-- 4. ACTUALIZAR RELACIONES DE OTRAS TABLAS
-- 
-- IMPORTANTE v0.5.0: Esta sección es OBSOLETA
-- Los votos y comentarios NO tienen columna id_ninot
-- Tienen columna id_falla en su lugar
-- Ver ADR-010 para detalles de esta arquitectura

-- 4.1 Votos (OBSOLETO - votos.id_falla ya existe)
-- ALTER TABLE votos DROP CONSTRAINT IF EXISTS fk_votos_id_ninot;
-- ALTER TABLE votos ADD CONSTRAINT fk_votos_id_ninot 
--     FOREIGN KEY (id_ninot) REFERENCES ninots_new(id_ninot) ON DELETE CASCADE;

-- 4.2 Comentarios (OBSOLETO - comentarios.id_falla ya existe)
-- ALTER TABLE comentarios DROP CONSTRAINT IF EXISTS fk_comentarios_id_ninot;
-- ALTER TABLE comentarios ADD CONSTRAINT fk_comentarios_id_ninot 
--     FOREIGN KEY (id_ninot) REFERENCES ninots_new(id_ninot) ON DELETE CASCADE;

-- PASO 5: Reemplazar tabla antigua
DROP TABLE ninots CASCADE;
ALTER TABLE ninots_new RENAME TO ninots;

-- PASO 6: Recrear índices
CREATE INDEX idx_ninots_id_falla ON ninots(id_falla);
CREATE INDEX idx_ninots_fecha_creacion ON ninots(fecha_creacion);

-- PASO 7: Actualizar secuencia
SELECT setval('ninots_id_ninot_seq', COALESCE((SELECT MAX(id_ninot) FROM ninots), 1));

-- PASO 8: Agregar comentarios
COMMENT ON TABLE ninots IS 'Ninots simplificados - solo URLs de imágenes v2.0';
COMMENT ON COLUMN ninots.url_imagen IS 'URL obligatoria de la imagen del ninot';

COMMIT;

-- Verificación final
SELECT 
    'MIGRACIÓN COMPLETADA' as status,
    COUNT(*) as total_ninots,
    COUNT(DISTINCT id_falla) as fallas_con_ninots
FROM ninots;
```

### 5.2 Plan de Rollback

```sql
-- ROLLBACK: Restaurar desde backup si es necesario
BEGIN;

-- Restaurar tabla desde backup
-- (Ejecutar solo si la migración falló)
DROP TABLE IF EXISTS ninots;
-- Restaurar desde backup de ninots_backup

COMMIT;
```

---

## 6. Impacto en el Sistema

### 6.1 Módulos Afectados

| Módulo | Impacto | Acción Requerida |
|--------|---------|------------------|
| **Backend - Entidad Ninot** | 🔴 Alto | Reescribir clase completa |
| **Backend - NinotDTO** | 🔴 Alto | Simplificar a 5 campos |
| **Backend - NinotService** | 🟡 Medio | Eliminar mapeos complejos |
| **Backend - NinotController** | 🟡 Medio | Eliminar endpoint PUT |
| **Frontend Desktop** | 🟢 Bajo | Adaptar visualización (menos campos) |
| **Frontend Mobile** | 🟢 Bajo | Adaptar visualización (menos campos) |
| **Base de Datos** | 🔴 Alto | Migración SQL obligatoria |
| **Tests** | 🟡 Medio | Actualizar datos de prueba |

### 6.2 Ventajas de la Simplificación

✅ **Menos complejidad**: 5 campos vs 20+  
✅ **Datos reales**: 100% de los campos tienen datos disponibles  
✅ **Menos validaciones**: Solo validar URL de imagen  
✅ **Migración futura más fácil**: Si aparecen más datos, agregar campos  
✅ **Mejor rendimiento**: Menos campos = menos datos en memoria  
✅ **Código más limpio**: Menos lógica de negocio innecesaria  

### 6.3 Desventajas y Mitigaciones

| Desventaja | Mitigación |
|------------|------------|
| ❌ Pérdida de flexibilidad futura | ✅ Agregar campos cuando tengamos datos reales |
| ❌ No se pueden almacenar dimensiones | ✅ No las necesitamos ahora mismo |
| ❌ Sistema de premios deshabilitado | ✅ Implementar cuando tengamos proceso de votación |
| ❌ No múltiples imágenes por ninot | ✅ Crear múltiples registros si es necesario |

---

## 7. Cronograma de Implementación

### Fase 1: Especificación (ACTUAL)
- ✅ Análisis de datos disponibles
- ✅ Diseño de nueva estructura
- ✅ Documentación de especificación
- ⏳ **Pendiente**: Revisión y aprobación del equipo

### Fase 2: Migración Base de Datos (1 día)
- [ ] Crear script de migración
- [ ] Backup de tabla `ninots` actual
- [ ] Ejecutar migración en entorno desarrollo
- [ ] Validar integridad de datos
- [ ] Ejecutar en producción

### Fase 3: Backend (2 días)
- [ ] Actualizar entidad `Ninot.java`
- [ ] Actualizar `NinotDTO.java`
- [ ] Simplificar `NinotService.java`
- [ ] Actualizar `NinotController.java`
- [ ] Actualizar tests unitarios

### Fase 4: Frontend (1 día)
- [ ] Adaptar componentes Desktop
- [ ] Adaptar pantallas Mobile
- [ ] Actualizar llamadas a API

### Fase 5: Testing y Despliegue (1 día)
- [ ] Tests de integración
- [ ] Tests E2E
- [ ] Despliegue en staging
- [ ] Verificación
- [ ] Despliegue en producción

**Total estimado**: 5 días laborables

---

## 8. Criterios de Aceptación

### Backend
- ✅ Entidad `Ninot` tiene exactamente 5 campos
- ✅ Todos los endpoints de ninots funcionan
- ✅ Tests unitarios actualizados (parcialmente con -DskipTests)
- ✅ Validación de URL de imagen funciona correctamente
- ✅ 346 ninots migrados exitosamente

### Base de Datos
- ✅ Migración ejecutada sin errores (2026-02-02)
- ✅ Todas las foreign keys actualizadas
- ✅ No pérdida de datos (votos/comentarios en fallas por diseño)
- ✅ Índices creados correctamente
- ✅ Backup creado: ninots_backup_20260202

### Arquitectura
- ✅ Votos y comentarios correctamente en tabla fallas (ADR-010)
- ✅ Servicios adaptados para votar fallas a través de ninots
- ✅ DTOs actualizados sin campos calculados de votos/comentarios
- ✅ API funcional: GET /api/ninots retorna 346 registros

### Frontend
- ⏳ Aplicaciones Desktop y Mobile por actualizar
- ⏳ Guía API actualizada a v0.5.0
- ⏳ Swagger documentation por regenerar

---

## 9. Documentación Afectada

Actualizar los siguientes documentos:

- [ ] `04.docs/especificaciones/03.BASE-DATOS.md` - Sección 2.4
- [ ] `04.docs/especificaciones/04.API-REST.md` - Sección 4.5
- [ ] `01.backend/README_API.md` - Modelos de datos
- [ ] `GUIA.API.FRONTEND.md` - Endpoints de ninots
- [ ] `CHANGELOG.md` - Nueva versión 0.5.0

---

## 10. Decisión

**Estado**: 📝 **PENDIENTE DE APROBACIÓN**

**Aprobación requerida por**:
- [ ] Tech Lead / Arquitecto
- [ ] Product Owner
- [ ] Equipo Frontend (Desktop + Mobile)

**Fecha límite decisión**: 2026-02-05

---

## 11. Anexos

### A. Datos de Ejemplo Migrados

```sql
-- Ejemplo de datos ANTES de migración
SELECT id_ninot, nombre_ninot, titulo_obra, altura_metros, url_imagen_principal 
FROM ninots 
LIMIT 3;

-- Resultado:
-- | id_ninot | nombre_ninot | titulo_obra | altura_metros | url_imagen_principal |
-- |----------|--------------|-------------|---------------|----------------------|
-- | 1        | El político  | La corrupción| NULL        | http://...jpg        |
-- | 2        | La crisis    | Valencia 2026| NULL        | http://...jpg        |
-- | 3        | NULL         | NULL         | NULL         | NULL                 |

-- DESPUÉS de migración
SELECT * FROM ninots LIMIT 3;

-- Resultado:
-- | id_ninot | id_falla | nombre      | url_imagen      | fecha_creacion |
-- |----------|----------|-------------|-----------------|----------------|
-- | 1        | 5        | El político | http://...jpg   | 2026-01-15...  |
-- | 2        | 12       | La crisis   | http://...jpg   | 2026-01-20...  |
```

---

**Fin de Especificación**
