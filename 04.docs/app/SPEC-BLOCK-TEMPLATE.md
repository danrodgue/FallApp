# 📋 Plantilla de Especificación de Bloque

> **Versión**: 1.0  
> **Última actualización**: 2026-02-01  
> **Propósito**: Plantilla estándar para documentar funcionalidades de forma consistente

---

## 📖 Instrucciones de Uso

1. Copia esta plantilla
2. Rellena cada sección
3. Guarda con nombre: `SPEC-[MÓDULO]-[NÚMERO].md`
4. Ejemplo: `SPEC-AUTH-001.md`, `SPEC-FALLAS-003.md`

---

## 🔲 PLANTILLA DE BLOQUE

```markdown
# [ID]: [Nombre de la Funcionalidad]

> **Módulo**: [Nombre del módulo]  
> **Tipo**: [FEATURE | ENDPOINT | COMPONENT | USECASE]  
> **Prioridad**: [CRÍTICA | ALTA | MEDIA | BAJA]  
> **Estado**: [📝 SPEC | 🔨 EN DESARROLLO | ✅ COMPLETADO | 🧪 EN TEST]

---

## 📝 Descripción (máx 3 líneas)

Línea 1: ¿QUÉ hace esta funcionalidad?
Línea 2: ¿PARA QUÉ sirve?
Línea 3: ¿CUÁNDO se usa? (contexto)

---

## 📥 Entrada

| Campo | Tipo | Requerido | Validación | Ejemplo |
|-------|------|-----------|------------|---------|
| campo1 | String | ✅ | max 100 chars | "ejemplo" |
| campo2 | Int | ❌ | > 0 | 42 |
| campo3 | Boolean | ❌ | - | true |

### Ejemplo de entrada
```json
{
  "campo1": "valor1",
  "campo2": 42
}
```

---

## 📤 Salida

### Éxito (código 2XX)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| campo1 | String | Descripción del campo |
| campo2 | Object | Descripción del campo |

```json
{
  "exito": true,
  "datos": {
    "campo1": "valor",
    "campo2": { }
  }
}
```

### Error

| Código | Caso | Mensaje |
|--------|------|---------|
| 400 | Validación fallida | "Campo X es requerido" |
| 401 | No autenticado | "Token inválido" |
| 404 | No encontrado | "Recurso no existe" |
| 500 | Error interno | "Error del servidor" |

---

## 🔀 Flujos

### Flujo Principal (Happy Path)
1. [Paso 1]
2. [Paso 2]
3. [Paso 3]
4. → Retorna éxito

### Flujos Alternativos
- **Si [condición A]**: [qué pasa]
- **Si [condición B]**: [qué pasa]
- **Si [condición C]**: [qué pasa]

---

## 🔗 Dependencias

| Dependencia | Tipo | Descripción |
|-------------|------|-------------|
| NombreRepository | Repository | Acceso a datos de X |
| NombreService | Service | Lógica de Y |
| OtraFeature | Feature | Requiere Z completado |

---

## 🧪 Casos de Test

| ID | Caso | Entrada | Salida Esperada |
|----|------|---------|-----------------|
| T01 | Happy path | entrada válida | éxito con datos |
| T02 | Campo vacío | campo1 = "" | error 400 |
| T03 | No autorizado | sin token | error 401 |
| T04 | No encontrado | id = 99999 | error 404 |

---

## 📎 Notas Adicionales

- [Nota 1]
- [Nota 2]
- [Decisiones de diseño]
```

---

## 📋 EJEMPLOS COMPLETOS

### Ejemplo 1: Endpoint de Login

```markdown
# FEAT-AUTH-001: Login de Usuario

> **Módulo**: Autenticación  
> **Tipo**: ENDPOINT  
> **Prioridad**: CRÍTICA  
> **Estado**: ✅ COMPLETADO

---

## 📝 Descripción

Autentica un usuario mediante email y contraseña.
Retorna token JWT válido por 24 horas si credenciales correctas.
Se usa en pantalla de login de todas las aplicaciones.

---

## 📥 Entrada

| Campo | Tipo | Requerido | Validación | Ejemplo |
|-------|------|-----------|------------|---------|
| email | String | ✅ | Formato email válido | "user@mail.com" |
| contrasena | String | ✅ | Mínimo 6 caracteres | "Pass123!" |

```json
{
  "email": "usuario@ejemplo.com",
  "contrasena": "MiPassword123"
}
```

---

## 📤 Salida

### Éxito (200)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| token | String | JWT válido por 24h |
| tipo | String | Siempre "Bearer" |
| expiraEn | Int | Segundos hasta expiración |
| usuario | Object | Datos del usuario |

```json
{
  "exito": true,
  "datos": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "tipo": "Bearer",
    "expiraEn": 86400,
    "usuario": {
      "id": 42,
      "email": "usuario@ejemplo.com",
      "nombreCompleto": "Juan Pérez",
      "rol": "USUARIO"
    }
  }
}
```

### Error

| Código | Caso | Mensaje |
|--------|------|---------|
| 400 | Email vacío | "Email es requerido" |
| 400 | Email inválido | "Formato de email inválido" |
| 400 | Password vacía | "Contraseña es requerida" |
| 400 | Password corta | "Contraseña debe tener mínimo 6 caracteres" |
| 401 | Credenciales incorrectas | "Credenciales inválidas" |
| 403 | Usuario baneado | "Cuenta suspendida" |

---

## 🔀 Flujos

### Flujo Principal
1. Usuario envía email y contraseña
2. Sistema valida formato de campos
3. Sistema busca usuario por email
4. Sistema verifica contraseña
5. Sistema genera token JWT
6. → Retorna token + datos usuario

### Flujos Alternativos
- **Si email vacío/inválido**: Error 400 sin consultar BD
- **Si usuario no existe**: Error 401 (mismo que password incorrecta)
- **Si password incorrecta**: Error 401
- **Si usuario baneado**: Error 403 con mensaje específico

---

## 🔗 Dependencias

| Dependencia | Tipo | Descripción |
|-------------|------|-------------|
| UsuarioRepository | Repository | Buscar usuario por email |
| PasswordEncoder | Service | Verificar hash de password |
| JwtService | Service | Generar token JWT |

---

## 🧪 Casos de Test

| ID | Caso | Entrada | Salida Esperada |
|----|------|---------|-----------------|
| T01 | Login exitoso | email+pass válidos | 200 + token |
| T02 | Email vacío | email = "" | 400 |
| T03 | Email inválido | email = "noesmail" | 400 |
| T04 | Password vacía | password = "" | 400 |
| T05 | Password corta | password = "123" | 400 |
| T06 | Usuario no existe | email inexistente | 401 |
| T07 | Password incorrecta | pass incorrecta | 401 |
| T08 | Usuario baneado | usuario.activo=false | 403 |
```

---

### Ejemplo 2: Use Case de Votación

```markdown
# FEAT-NINOTS-003: Votar Ninot

> **Módulo**: Ninots  
> **Tipo**: USECASE  
> **Prioridad**: ALTA  
> **Estado**: 📝 SPEC

---

## 📝 Descripción

Permite a un usuario autenticado votar un ninot.
Existen 3 tipos de voto: Ingenioso, Crítico, Artístico.
Solo se permite un voto de cada tipo por ninot por usuario.

---

## 📥 Entrada

| Campo | Tipo | Requerido | Validación | Ejemplo |
|-------|------|-----------|------------|---------|
| ninotId | Long | ✅ | Debe existir | 42 |
| tipoVoto | Enum | ✅ | INGENIOSO/CRITICO/ARTISTICO | "INGENIOSO" |
| usuarioId | Long | ✅ | Del token JWT | 1 |

```json
{
  "ninotId": 42,
  "tipoVoto": "INGENIOSO"
}
```

---

## 📤 Salida

### Éxito (201)

```json
{
  "exito": true,
  "mensaje": "Voto registrado",
  "datos": {
    "ninotId": 42,
    "tipoVoto": "INGENIOSO",
    "totalVotosTipo": 156
  }
}
```

### Error

| Código | Caso | Mensaje |
|--------|------|---------|
| 400 | Tipo inválido | "Tipo de voto inválido" |
| 401 | No autenticado | "Autenticación requerida" |
| 404 | Ninot no existe | "Ninot no encontrado" |
| 409 | Ya votó este tipo | "Ya has votado este tipo en este ninot" |

---

## 🔀 Flujos

### Flujo Principal
1. Usuario autenticado envía voto
2. Sistema valida que ninot existe
3. Sistema verifica que no haya votado este tipo
4. Sistema registra voto
5. Sistema actualiza contador
6. → Retorna confirmación

### Flujos Alternativos
- **Si no autenticado**: Error 401 antes de procesar
- **Si ninot no existe**: Error 404
- **Si ya votó este tipo**: Error 409 con mensaje claro

---

## 🔗 Dependencias

| Dependencia | Tipo | Descripción |
|-------------|------|-------------|
| NinotRepository | Repository | Verificar existencia ninot |
| VotoRepository | Repository | Guardar y consultar votos |
| AuthService | Service | Obtener usuario actual |

---

## 🧪 Casos de Test

| ID | Caso | Entrada | Salida Esperada |
|----|------|---------|-----------------|
| T01 | Voto exitoso | datos válidos | 201 + confirmación |
| T02 | Sin auth | sin token | 401 |
| T03 | Ninot inexistente | ninotId = 99999 | 404 |
| T04 | Tipo inválido | tipoVoto = "OTRO" | 400 |
| T05 | Voto duplicado | mismo tipo+ninot | 409 |
| T06 | Diferente tipo OK | otro tipo mismo ninot | 201 |
```

---

## 📁 Organización de Archivos

```
04.docs/
├── specs/
│   ├── auth/
│   │   ├── SPEC-AUTH-001-login.md
│   │   ├── SPEC-AUTH-002-registro.md
│   │   └── SPEC-AUTH-003-logout.md
│   ├── fallas/
│   │   ├── SPEC-FALLAS-001-listar.md
│   │   ├── SPEC-FALLAS-002-detalle.md
│   │   └── SPEC-FALLAS-003-buscar.md
│   ├── ninots/
│   │   ├── SPEC-NINOTS-001-listar.md
│   │   └── SPEC-NINOTS-003-votar.md
│   └── eventos/
│       └── SPEC-EVENTOS-001-listar.md
├── SPEC-BLOCK-TEMPLATE.md    ← Este archivo
├── SPEC-TEST-TEMPLATE.md     ← Plantilla de tests
└── gautier.leeme.md          ← Tu guía personal
```

---

## ✅ Checklist antes de dar por buena una spec

- [ ] ¿ID único y descriptivo?
- [ ] ¿Descripción en máx 3 líneas?
- [ ] ¿Entrada con tipos y validaciones?
- [ ] ¿Ejemplo de entrada en JSON?
- [ ] ¿Salida exitosa documentada?
- [ ] ¿TODOS los casos de error listados?
- [ ] ¿Flujo principal claro?
- [ ] ¿Flujos alternativos cubiertos?
- [ ] ¿Dependencias identificadas?
- [ ] ¿Casos de test definidos?

---

> **Recuerda**: Una spec incompleta = código incompleto.
> Tómate el tiempo de especificar bien.
