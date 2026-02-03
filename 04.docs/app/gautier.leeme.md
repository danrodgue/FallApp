# 🎯 Guía Personal de Spec-Driven Development - Gautier

> **Fecha**: 2026-02-01  
> **Propósito**: Aprender y dominar Spec-Driven Development para desarrollo eficiente

---

## 📚 Índice

1. [¿Qué es Spec-Driven Development?](#1-qué-es-spec-driven-development)
2. [Por qué funciona](#2-por-qué-funciona)
3. [El flujo completo](#3-el-flujo-completo)
4. [Anatomía de una buena especificación](#4-anatomía-de-una-buena-especificación)
5. [Trabajando con IA](#5-trabajando-con-ia)
6. [Errores comunes y cómo evitarlos](#6-errores-comunes-y-cómo-evitarlos)
7. [Mejores prácticas](#7-mejores-prácticas)
8. [Checklist diario](#8-checklist-diario)
9. [Recursos y referencias](#9-recursos-y-referencias)

---

## 1. ¿Qué es Spec-Driven Development?

### Definición Simple

**Spec-Driven Development (SDD)** es una metodología donde **primero escribes QUÉ vas a construir** (especificación) y **después escribes el código**.

```
❌ Forma tradicional: Idea → Código → Documentación
✅ Spec-Driven:       Idea → Especificación → Código (guiado por spec)
```

### Analogía

Piensa en construir una casa:
- ❌ **Sin spec**: Empiezas a poner ladrillos y "ya veremos cómo queda"
- ✅ **Con spec**: Primero tienes planos detallados, luego construyes siguiendo los planos

### Beneficios Principales

| Beneficio | Descripción |
|-----------|-------------|
| **Claridad mental** | Sabes exactamente qué construir antes de escribir código |
| **Menos bugs** | Los errores de diseño se detectan en papel, no en código |
| **Mejor comunicación** | El equipo (y la IA) entienden lo mismo |
| **Documentación gratis** | La spec ES la documentación |
| **Tests claros** | Los tests salen directamente de la spec |
| **Refactoring seguro** | La spec te dice si rompiste algo |

---

## 2. Por qué funciona

### El problema del código primero

```
Situación típica:
1. Tienes una idea vaga
2. Empiezas a codear
3. A mitad de camino te das cuenta de que no pensaste X
4. Reescribes parte del código
5. Aparecen bugs porque cambiaste cosas
6. Al final no recuerdas por qué hiciste ciertas decisiones
7. No hay documentación (o está desactualizada)
```

### La solución Spec-Driven

```
Con Spec-Driven:
1. Tienes una idea
2. Escribes QUÉ debe hacer (spec) ← Aquí piensas TODO
3. Revisas la spec y detectas problemas ANTES de codear
4. Codeas siguiendo la spec (sin improvisar)
5. Los tests verifican que cumples la spec
6. La spec ES la documentación actualizada
7. Si algo cambia, primero actualizas la spec
```

### Regla de oro

> **"Si no está en la spec, no se construye. Si cambió, primero se actualiza la spec."**

---

## 3. El flujo completo

### Diagrama del proceso

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SPEC-DRIVEN DEVELOPMENT FLOW                      │
└─────────────────────────────────────────────────────────────────────┘

    ┌─────────┐
    │  IDEA   │  ← Funcionalidad que quieres construir
    └────┬────┘
         │
         ▼
    ┌─────────────────────────────────────────────────────────────┐
    │  1. ESCRIBIR SPEC                                           │
    │  • Describir QUÉ hace (no CÓMO)                             │
    │  • Definir entradas y salidas                               │
    │  • Especificar casos de éxito y error                       │
    │  • Identificar dependencias                                  │
    └─────────────────────────────────────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────────────────────────────────────┐
    │  2. REVISAR SPEC                                            │
    │  • ¿Está completa?                                          │
    │  • ¿Es implementable?                                       │
    │  • ¿Hay ambigüedades?                                       │
    │  • ¿Falta algún caso?                                       │
    └─────────────────────────────────────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────────────────────────────────────┐
    │  3. ESCRIBIR TESTS (desde la spec)                          │
    │  • Un test por cada caso especificado                       │
    │  • Tests de éxito (happy path)                              │
    │  • Tests de error (edge cases)                              │
    └─────────────────────────────────────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────────────────────────────────────┐
    │  4. IMPLEMENTAR CÓDIGO                                      │
    │  • Seguir la spec al pie de la letra                        │
    │  • NO improvisar ni añadir features                         │
    │  • Si algo no encaja, VOLVER a la spec                      │
    └─────────────────────────────────────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────────────────────────────────────┐
    │  5. VERIFICAR                                               │
    │  • ¿Los tests pasan?                                        │
    │  • ¿El código cumple la spec?                               │
    │  • ¿Hay algo que falte?                                     │
    └─────────────────────────────────────────────────────────────┘
         │
         ▼
    ┌─────────────┐
    │   HECHO ✅  │
    └─────────────┘
```

### Tiempo invertido

| Fase | % del tiempo | Descripción |
|------|--------------|-------------|
| Especificación | 30% | Pensar y documentar |
| Tests | 20% | Escribir casos de prueba |
| Código | 40% | Implementar siguiendo spec |
| Verificación | 10% | Comprobar que todo funciona |

**Parece mucho tiempo en spec, pero AHORRAS tiempo porque:**
- Menos bugs
- Menos reescrituras
- Menos confusión
- Documentación lista

---

## 4. Anatomía de una buena especificación

### Los 7 elementos esenciales

Toda spec de un bloque/función debe tener:

```
┌─────────────────────────────────────────────────────────────┐
│  1. IDENTIFICADOR      │ ID único (ej: FEAT-001)           │
├─────────────────────────────────────────────────────────────┤
│  2. NOMBRE             │ Nombre descriptivo en 3-5 palabras│
├─────────────────────────────────────────────────────────────┤
│  3. DESCRIPCIÓN        │ QUÉ hace en 1-3 líneas            │
├─────────────────────────────────────────────────────────────┤
│  4. ENTRADA            │ Qué recibe (parámetros, datos)    │
├─────────────────────────────────────────────────────────────┤
│  5. SALIDA             │ Qué devuelve (formato, tipo)      │
├─────────────────────────────────────────────────────────────┤
│  6. CASOS              │ Happy path + Edge cases           │
├─────────────────────────────────────────────────────────────┤
│  7. DEPENDENCIAS       │ Qué necesita para funcionar       │
└─────────────────────────────────────────────────────────────┘
```

### Ejemplo práctico

```markdown
## FEAT-AUTH-001: Login de Usuario

**Descripción**: Autentica un usuario con email y contraseña,
retornando un token JWT si las credenciales son válidas.

**Entrada**:
| Campo | Tipo | Requerido | Validación |
|-------|------|-----------|------------|
| email | String | Sí | Formato email válido |
| password | String | Sí | Mínimo 6 caracteres |

**Salida exitosa** (200):
```json
{
  "exito": true,
  "datos": {
    "token": "eyJ...",
    "usuario": { "id": 1, "email": "...", "rol": "USUARIO" }
  }
}
```

**Casos de error**:
| Caso | Código | Mensaje |
|------|--------|---------|
| Email no existe | 401 | "Credenciales inválidas" |
| Password incorrecta | 401 | "Credenciales inválidas" |
| Usuario baneado | 403 | "Cuenta suspendida" |
| Email vacío | 400 | "Email es requerido" |

**Dependencias**: AuthRepository, JwtService
```

---

## 5. Trabajando con IA

### Por qué SDD + IA es poderoso

La IA es excelente generando código, pero necesita **contexto claro**. Las specs proporcionan ese contexto perfectamente.

```
Sin spec:    "Hazme un login" → IA adivina, tú corriges, pierdes tiempo
Con spec:    [Spec detallada] → IA genera exactamente lo que necesitas
```

### El flujo con IA

```
1. TÚ escribes la spec (pensamiento humano)
2. TÚ pasas la spec a la IA
3. IA genera código siguiendo la spec
4. TÚ verificas que cumple la spec
5. Si hay errores, documentas y la IA corrige
```

### Estructura del prompt para IA

```markdown
# CONTEXTO
[Cargar contexto del proyecto - arquitectura, convenciones]

# SPEC A IMPLEMENTAR
[Pegar la especificación completa]

# REQUISITOS ADICIONALES
- Seguir convenciones de [X]
- Usar patrón [Y]
- Documentar con [Z]

# ENTREGABLE ESPERADO
[Qué archivos debe generar]
```

### Tips para mejores resultados con IA

1. **Sé específico**: Cuanto más detalle en la spec, mejor código
2. **Incluye ejemplos**: Entrada/salida esperada ayuda mucho
3. **Menciona restricciones**: "NO usar X", "SIEMPRE hacer Y"
4. **Pide en partes**: Una feature a la vez, no todo junto
5. **Verifica siempre**: La IA puede equivocarse, revisa el código

---

## 6. Errores comunes y cómo evitarlos

### ❌ Error 1: Spec demasiado vaga

```markdown
# MAL
"El sistema debe permitir login de usuarios"

# BIEN
"El endpoint POST /api/auth/login recibe {email, password},
valida contra la BD, y retorna JWT con duración de 24h.
Si falla, retorna 401 con mensaje 'Credenciales inválidas'."
```

### ❌ Error 2: No especificar casos de error

```markdown
# MAL
Entrada: email, password
Salida: token

# BIEN
Entrada: email, password
Salida éxito: { token, usuario }
Salida error:
  - 400: Validación fallida
  - 401: Credenciales inválidas
  - 403: Usuario suspendido
  - 500: Error interno
```

### ❌ Error 3: Mezclar QUÉ con CÓMO

```markdown
# MAL (dice CÓMO)
"Usar SHA-256 para hashear la password y compararla
con BCrypt.checkpw() llamando a UserRepository.findByEmail()"

# BIEN (dice QUÉ)
"Verificar que la password proporcionada coincide con
la almacenada para el usuario con ese email"
```

### ❌ Error 4: No actualizar la spec cuando cambia algo

```markdown
# MAL
- Spec dice: "El token dura 24h"
- Código cambia a 12h
- Spec no se actualiza
- → Confusión futura

# BIEN
- Primero actualizar spec: "El token dura 12h"
- Luego cambiar código
- → Spec y código siempre sincronizados
```

### ❌ Error 5: Specs enormes y monolíticas

```markdown
# MAL
Un documento de 50 páginas con todo el sistema

# BIEN
- SPEC-AUTH.md: Solo autenticación
- SPEC-FALLAS.md: Solo fallas
- SPEC-VOTOS.md: Solo votación
- Cada uno pequeño y enfocado
```

---

## 7. Mejores prácticas

### 7.1 Regla de las 3 líneas

> Cada funcionalidad debe poder describirse en máximo 3 líneas.
> Si necesitas más, probablemente debe dividirse.

```markdown
✅ BIEN: "Obtiene lista de fallas paginada.
          Acepta filtros por categoría y sección.
          Retorna máximo 20 elementos por página."

❌ MAL:  [10 líneas de descripción]
         → Dividir en funcionalidades más pequeñas
```

### 7.2 Nomenclatura consistente

Usa un sistema de IDs claro:

```
FEAT-[MÓDULO]-[NÚMERO]: Funcionalidad
TEST-[MÓDULO]-[NÚMERO]: Test
ERR-[NÚMERO]: Error documentado

Ejemplos:
- FEAT-AUTH-001: Login de usuario
- FEAT-AUTH-002: Registro de usuario
- FEAT-FALLAS-001: Listar fallas
- TEST-AUTH-001: Test de login válido
- ERR-042: Error de timeout en API
```

### 7.3 Versionado de specs

```markdown
> **Versión**: 1.2
> **Última actualización**: 2026-02-01
> **Changelog**:
> - v1.2: Añadido campo "telefono" a registro
> - v1.1: Cambiado duración token a 24h
> - v1.0: Versión inicial
```

### 7.4 Especifica el "camino feliz" primero

```markdown
## Flujo principal (Happy Path)
1. Usuario envía email y password
2. Sistema valida formato
3. Sistema verifica credenciales
4. Sistema genera token
5. Sistema retorna token + datos usuario

## Flujos alternativos (Edge Cases)
- Si formato inválido → Error 400
- Si credenciales incorrectas → Error 401
- Si usuario baneado → Error 403
```

### 7.5 Tests desde la spec

Cada caso en la spec = 1 test

```markdown
# SPEC
Casos de error:
| Caso | Código | Mensaje |
| Email vacío | 400 | "Email requerido" |
| Email inválido | 400 | "Formato inválido" |
| Password corta | 400 | "Mínimo 6 caracteres" |

# TESTS (se generan automáticamente de la spec)
- test_login_email_vacio_retorna_400()
- test_login_email_invalido_retorna_400()
- test_login_password_corta_retorna_400()
```

---

## 8. Checklist diario

### Al empezar el día

```
□ ¿Qué voy a construir hoy?
□ ¿Existe spec para eso?
  → Si NO: Escribir spec primero
  → Si SÍ: Revisar que esté actualizada
□ ¿Tengo claro el entregable?
```

### Antes de escribir código

```
□ ¿La spec está completa?
□ ¿Entiendo todos los casos?
□ ¿Sé qué tests necesito?
□ ¿Hay dependencias que resolver?
```

### Al terminar una funcionalidad

```
□ ¿El código cumple la spec?
□ ¿Los tests pasan?
□ ¿Documenté los errores encontrados?
□ ¿La spec necesita actualizarse?
```

### Al encontrar un error

```
□ Documentar en 04.PLANTILLA-ERRORES.md
□ Incluir: causa, solución, prevención
□ ¿Afecta a la spec? → Actualizar
```

---

## 9. Recursos y referencias

### Archivos del proyecto

| Archivo | Propósito |
|---------|-----------|
| `04.docs/SPEC-BLOCK-TEMPLATE.md` | Plantilla para specs de bloques |
| `04.docs/SPEC-TEST-TEMPLATE.md` | Plantilla para tests |
| `03.mobile/docs/03.PROMPT-GENERACION-IA.md` | Prompts para IA |
| `03.mobile/docs/04.PLANTILLA-ERRORES.md` | Registro de errores |

### Flujo recomendado

```
1. Abrir SPEC-BLOCK-TEMPLATE.md
2. Copiar plantilla
3. Rellenar para tu funcionalidad
4. Guardar en carpeta apropiada
5. Pasar spec a IA para generar código
6. Verificar y documentar
```

### Mantra del SDD

> **"Especifica antes de codear.**
> **Testea lo que especificaste.**
> **Documenta lo que aprendiste."**

---

## 🎓 Conclusión

**Spec-Driven Development no es más trabajo, es trabajo más inteligente.**

Al principio puede parecer lento escribir specs antes de codear, pero:

- **Reduces bugs** en un 60-80%
- **Reduces reescrituras** en un 50%
- **Mejoras comunicación** con el equipo y la IA
- **Tienes documentación** siempre actualizada
- **Aprendes más** porque reflexionas antes de hacer

### Tu siguiente paso

1. Lee `SPEC-BLOCK-TEMPLATE.md`
2. Elige una funcionalidad pequeña
3. Escribe su spec usando la plantilla
4. Pásala a la IA
5. Implementa y verifica

**¡Buena suerte, Gautier! 🚀**

---

> *"Dame seis horas para cortar un árbol y pasaré las primeras cuatro afilando el hacha."*
> — Abraham Lincoln (sobre la preparación)
