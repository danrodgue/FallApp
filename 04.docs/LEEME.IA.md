# 🤖 LEE ESTO PRIMERO - Guía para Asistentes IA

> **Propósito**: Instrucciones para trabajar eficientemente en el proyecto FallApp  
> **Fecha**: 2026-02-01

---

## 📖 Secuencia de Lectura Obligatoria

Antes de realizar CUALQUIER tarea en este proyecto, **lee estos documentos en este orden**:

### 1️⃣ Contexto General
```
Lee: 00.INDICE.md
```
**Por qué**: Entenderás la estructura completa de la documentación y dónde encontrar cada cosa.

**Tiempo estimado**: 2 minutos

---

### 2️⃣ Visión del Sistema
```
Lee: especificaciones/00.VISION-GENERAL.md
```
**Por qué**: Comprenderás la arquitectura, tecnologías, modelo de dominio y objetivos del proyecto.

**Puntos clave a retener**:
- Stack tecnológico: Spring Boot + PostgreSQL + Electron + Android
- Roles del sistema: ADMIN, CASAL, USUARIO
- Entidades principales: Usuario, Falla, Evento, Ninot, Voto
- Contexto académico: 4 semanas, proyecto intermodular

**Tiempo estimado**: 5-7 minutos

---

### 3️⃣ Especificación Relevante
```
Lee la especificación según tu tarea:
- especificaciones/01.SISTEMA-USUARIOS.md    → Para tareas de autenticación/usuarios
- especificaciones/02.FALLAS.md              → Para tareas de fallas/casales
- especificaciones/03.EVENTOS.md             → Para tareas de eventos
- especificaciones/04.VOTACIONES.md          → Para tareas de votación
```
**Por qué**: Tendrás los detalles técnicos exactos (tablas SQL, endpoints, DTOs, lógica de negocio).

**Tiempo estimado**: 3-5 minutos por especificación

---

### 4️⃣ Patrones de Trabajo
```
Lee: 02.GUIA-PROMPTS-IA.md
```
**Por qué**: Aprenderás los patrones de prompts efectivos y cómo estructurar tu trabajo.

**Tiempo estimado**: 3-4 minutos (puedes consultar secciones específicas según necesites)

---

## 🎯 Después de Leer

Una vez hayas leído la documentación necesaria:

### ✅ Confirma que has entendido
```
Confirma explícitamente que has leído los documentos:
"He leído 00.INDICE.md, 00.VISION-GENERAL.md y [especificación X]. 
Entiendo que el proyecto usa [stack], tiene [roles], y voy a [descripción de tu tarea]."
```

### ✅ Plantea dudas si las tienes
```
Si algo no está claro, pregunta ANTES de implementar:
"En la especificación 02.FALLAS.md, ¿el campo 'anoFundacion' es obligatorio u opcional?"
```

### ✅ Sigue las convenciones
```
Consulta: 01.GUIA-PROGRAMACION.md para:
- Nomenclatura (camelCase, PascalCase, etc.)
- Estructura de clases
- Patrones de error handling
- Principios fail-fast
```

---

## 🚫 NO Hacer

### ❌ No implementar sin leer las especificaciones
**MAL**:
```
Usuario: "Crea el endpoint de eventos"
IA: [genera código sin leer especificaciones]
```

**BIEN**:
```
Usuario: "Crea el endpoint de eventos"
IA: "Primero voy a leer especificaciones/03.EVENTOS.md..."
     [lee la spec]
     "He leído la especificación. El endpoint POST /api/eventos debe..."
     [implementa según la spec]
```

### ❌ No usar valores por defecto silenciosos (Fail-Fast)
**MAL**:
```java
String apiUrl = config.getApiUrl().orElse("http://localhost:8080");
```

**BIEN**:
```java
String apiUrl = config.getApiUrl()
    .orElseThrow(() -> new ConfigurationException("API URL no configurada"));
```

### ❌ No inventar estructura de datos
**MAL**: Crear tus propios campos en las entidades

**BIEN**: Usar EXACTAMENTE los campos definidos en la especificación

### ❌ No ignorar los roles y permisos
**MAL**: Permitir que cualquier usuario haga cualquier acción

**BIEN**: Verificar permisos según la matriz de la especificación 00.VISION-GENERAL.md

---

## 📋 Checklist Pre-Implementación

Antes de escribir código, verifica:

- [ ] He leído 00.INDICE.md
- [ ] He leído especificaciones/00.VISION-GENERAL.md
- [ ] He leído la especificación relevante para mi tarea
- [ ] Entiendo el stack tecnológico (Spring Boot + PostgreSQL)
- [ ] Sé qué roles están involucrados en esta funcionalidad
- [ ] Conozco los endpoints/tablas/entidades relacionadas
- [ ] He consultado 01.GUIA-PROGRAMACION.md para convenciones

---

## 💡 Patrones de Prompt Efectivos

### Patrón 1: Implementar desde Especificación
```
Siguiendo la especificación especificaciones/02.FALLAS.md, 
implementa [componente específico].

Requisitos:
- Usa las convenciones de 01.GUIA-PROGRAMACION.md
- Aplica fail-fast para validaciones
- Incluye logs informativos
- Respeta los permisos definidos
```

### Patrón 2: Modificar Código Existente
```
Necesito modificar [archivo] para [objetivo].

Contexto:
- Lee especificaciones/[NUM].[NOMBRE].md sección [X.Y]
- La modificación debe [requisitos específicos]
- Mantener compatibilidad con [componentes relacionados]

Muéstrame solo los cambios necesarios.
```

### Patrón 3: Debugging
```
Tengo este error:
[stacktrace completo]

Contexto:
- Endpoint/Función: [nombre]
- Datos de entrada: [JSON]
- Comportamiento esperado según especificaciones/[X].md: [descripción]

Analiza la causa raíz y propón solución.
```

### Patrón 4: Crear Pruebas
```
Crea pruebas de integración para [funcionalidad]:

Especificación: especificaciones/[X].md sección [Y]
Escenarios según la spec:
1. [Caso exitoso]
2. [Caso de error 1]
3. [Caso de error 2]

Usa @SpringBootTest y MockMvc.
```

---

## 🔄 Flujo de Trabajo Típico

```
1. Usuario solicita tarea
   ↓
2. Lees documentación relevante
   ↓
3. Confirmas que entiendes el contexto
   ↓
4. Implementas siguiendo la especificación
   ↓
5. Verificas que cumple con convenciones
   ↓
6. Entregas el resultado
```

---

## 📚 Referencias Rápidas

| Necesito... | Consultar... |
|-------------|--------------|
| Entender el proyecto | `00.INDICE.md` + `especificaciones/00.VISION-GENERAL.md` |
| Convenciones de código | `01.GUIA-PROGRAMACION.md` |
| Modelo de datos de usuarios | `especificaciones/01.SISTEMA-USUARIOS.md` |
| Modelo de datos de fallas | `especificaciones/02.FALLAS.md` |
| Patrones de prompts | `02.GUIA-PROMPTS-IA.md` |
| Roles y permisos | `especificaciones/00.VISION-GENERAL.md` sección 5 |
| Stack tecnológico | `especificaciones/00.VISION-GENERAL.md` sección 3 |

---

## 🎓 Recordatorios Importantes

### 🔴 CRÍTICO: Fail-Fast
Este proyecto sigue el principio **fail-fast**: 
- Lanzar excepciones claras en lugar de usar valores por defecto
- No ocultar errores con fallbacks silenciosos
- Fallar temprano y explícitamente

### 🔴 CRÍTICO: Especificaciones son Ley
- Si la especificación dice que un campo es `VARCHAR(200)`, es `VARCHAR(200)`
- Si la especificación dice que solo ADMIN puede hacer X, solo ADMIN puede hacer X
- Si la especificación define un endpoint como `POST /api/eventos`, es exactamente eso

### 🟡 IMPORTANTE: Stack Tecnológico
- **Backend**: Spring Boot 3.x + Java 17 + PostgreSQL
- **Frontend Escritorio**: Electron + JavaScript
- **Móvil**: Android + Kotlin + Room + Retrofit
- No uses tecnologías diferentes sin consultar

### 🟡 IMPORTANTE: Contexto Académico
- Proyecto de 4 semanas
- Prioridad: funcionalidad completa > arquitectura perfecta
- Documentación concisa pero suficiente
- ~100 pruebas máximo en primera versión

---

## ✅ Checklist Post-Implementación

Después de implementar algo, verifica:

- [ ] El código compila sin errores
- [ ] Sigue las convenciones de nomenclatura
- [ ] Aplica fail-fast (sin fallbacks silenciosos)
- [ ] Incluye logs informativos (no excesivos)
- [ ] Maneja errores explícitamente
- [ ] Respeta los permisos según roles
- [ ] Coincide con la especificación técnica
- [ ] Las pruebas existentes siguen pasando
- [ ] Añadí pruebas para la nueva funcionalidad

---

## 🆘 En Caso de Duda

Si algo no está claro:

1. **Revisa la especificación** relevante
2. **Consulta** 01.GUIA-PROGRAMACION.md
3. **Pregunta** al usuario antes de asumir
4. **No inventes**: Mejor preguntar que implementar incorrectamente

---

> 💡 **Recuerda**: La documentación existe para garantizar consistencia. Úsala siempre como referencia, no como sugerencia opcional.

---

**¡Éxito en tu trabajo en FallApp! 🚀**
