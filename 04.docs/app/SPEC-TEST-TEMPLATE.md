# 🧪 Plantilla de Especificación de Tests

> **Versión**: 1.0  
> **Última actualización**: 2026-02-01  
> **Propósito**: Plantilla estándar para documentar y planificar tests de robustez

---

## 📖 Instrucciones de Uso

1. Para cada SPEC de bloque, crear su SPEC de tests correspondiente
2. Nombrar: `TEST-[MÓDULO]-[NÚMERO].md`
3. Cada caso de test debe poder ejecutarse independientemente

---

## 🔲 PLANTILLA DE TESTS

```markdown
# TEST-[MÓDULO]-[NÚMERO]: Tests de [Funcionalidad]

> **Spec relacionada**: SPEC-[MÓDULO]-[NÚMERO]  
> **Módulo**: [Nombre del módulo]  
> **Tipo de tests**: [UNIT | INTEGRATION | E2E | UI]  
> **Estado**: [📝 PENDIENTE | 🔨 EN DESARROLLO | ✅ IMPLEMENTADO]

---

## 📊 Resumen de Cobertura

| Categoría | Total | Implementados | Pasando |
|-----------|-------|---------------|---------|
| Happy Path | X | 0 | 0 |
| Validación | X | 0 | 0 |
| Errores | X | 0 | 0 |
| Edge Cases | X | 0 | 0 |
| **TOTAL** | **X** | **0** | **0** |

---

## ✅ Tests de Happy Path

### T-001: [Nombre del test]
| Campo | Valor |
|-------|-------|
| **Descripción** | [Qué verifica este test] |
| **Precondiciones** | [Estado inicial requerido] |
| **Entrada** | [Datos de entrada] |
| **Acción** | [Qué ejecutar] |
| **Salida esperada** | [Resultado esperado] |
| **Postcondiciones** | [Estado final esperado] |

```kotlin
@Test
fun `descripcion en lenguaje natural`() {
    // Given: [precondiciones]
    
    // When: [acción]
    
    // Then: [verificación]
}
```

---

## ❌ Tests de Validación

### T-002: [Validación de campo X vacío]
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica error cuando campo X está vacío |
| **Entrada** | campoX = "" |
| **Salida esperada** | Error 400: "Campo X es requerido" |

### T-003: [Validación de campo Y formato inválido]
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica error cuando formato de Y es inválido |
| **Entrada** | campoY = "formato-malo" |
| **Salida esperada** | Error 400: "Formato inválido" |

---

## ⚠️ Tests de Errores

### T-010: [Error de autenticación]
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica rechazo sin token |
| **Precondiciones** | No hay token JWT |
| **Salida esperada** | Error 401: "No autenticado" |

### T-011: [Error de permisos]
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica rechazo con rol insuficiente |
| **Precondiciones** | Usuario con rol USUARIO intenta acción de ADMIN |
| **Salida esperada** | Error 403: "Permisos insuficientes" |

---

## 🔄 Tests de Edge Cases

### T-020: [Datos en límite inferior]
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica comportamiento con valores mínimos |
| **Entrada** | campo = valor_minimo_permitido |
| **Salida esperada** | Éxito (está en el límite, debe aceptar) |

### T-021: [Datos en límite superior]
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica comportamiento con valores máximos |
| **Entrada** | campo = valor_maximo_permitido |
| **Salida esperada** | Éxito (está en el límite, debe aceptar) |

### T-022: [Datos justo fuera del límite]
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica rechazo con valor fuera de rango |
| **Entrada** | campo = valor_maximo + 1 |
| **Salida esperada** | Error 400 |

---

## 🔁 Tests de Concurrencia (si aplica)

### T-030: [Operación simultánea]
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica manejo de peticiones simultáneas |
| **Precondiciones** | 2+ usuarios ejecutan misma operación |
| **Salida esperada** | Comportamiento consistente sin corrupción |

---

## 📎 Notas de Implementación

- [Framework de testing a usar]
- [Mocks necesarios]
- [Datos de prueba requeridos]
- [Orden de ejecución si importa]
```

---

## 📋 EJEMPLO COMPLETO: Tests de Login

```markdown
# TEST-AUTH-001: Tests de Login de Usuario

> **Spec relacionada**: SPEC-AUTH-001  
> **Módulo**: Autenticación  
> **Tipo de tests**: INTEGRATION  
> **Estado**: ✅ IMPLEMENTADO

---

## 📊 Resumen de Cobertura

| Categoría | Total | Implementados | Pasando |
|-----------|-------|---------------|---------|
| Happy Path | 2 | 2 | 2 |
| Validación | 4 | 4 | 4 |
| Errores | 3 | 3 | 3 |
| Edge Cases | 2 | 2 | 2 |
| **TOTAL** | **11** | **11** | **11** |

---

## ✅ Tests de Happy Path

### T-001: Login exitoso con credenciales válidas
| Campo | Valor |
|-------|-------|
| **Descripción** | Usuario con credenciales correctas recibe token |
| **Precondiciones** | Usuario existe en BD con password hasheada |
| **Entrada** | email: "test@test.com", password: "Pass123!" |
| **Acción** | POST /api/auth/login |
| **Salida esperada** | 200 OK + token JWT válido + datos usuario |
| **Postcondiciones** | Último acceso actualizado |

```kotlin
@Test
fun `login exitoso retorna token y datos de usuario`() {
    // Given: usuario existente
    val email = "test@test.com"
    val password = "Pass123!"
    
    // When: intenta login
    val response = authService.login(email, password)
    
    // Then: recibe token válido
    assertThat(response.isSuccess).isTrue()
    assertThat(response.data.token).isNotEmpty()
    assertThat(response.data.usuario.email).isEqualTo(email)
}
```

### T-002: Login exitoso actualiza último acceso
| Campo | Valor |
|-------|-------|
| **Descripción** | Al hacer login se actualiza timestamp de último acceso |
| **Precondiciones** | Usuario con ultimoAcceso = fecha anterior |
| **Acción** | POST /api/auth/login |
| **Salida esperada** | ultimoAcceso actualizado a ahora |

---

## ❌ Tests de Validación

### T-003: Email vacío retorna error 400
| Campo | Valor |
|-------|-------|
| **Descripción** | Verifica validación de email requerido |
| **Entrada** | email: "", password: "Pass123!" |
| **Salida esperada** | 400: "Email es requerido" |

```kotlin
@Test
fun `login con email vacio retorna error 400`() {
    // Given
    val email = ""
    val password = "Pass123!"
    
    // When
    val response = authService.login(email, password)
    
    // Then
    assertThat(response.isError).isTrue()
    assertThat(response.error.code).isEqualTo(400)
    assertThat(response.error.message).contains("Email es requerido")
}
```

### T-004: Email con formato inválido retorna error 400
| Campo | Valor |
|-------|-------|
| **Entrada** | email: "noesunmail", password: "Pass123!" |
| **Salida esperada** | 400: "Formato de email inválido" |

### T-005: Password vacía retorna error 400
| Campo | Valor |
|-------|-------|
| **Entrada** | email: "test@test.com", password: "" |
| **Salida esperada** | 400: "Contraseña es requerida" |

### T-006: Password muy corta retorna error 400
| Campo | Valor |
|-------|-------|
| **Entrada** | email: "test@test.com", password: "123" |
| **Salida esperada** | 400: "Contraseña debe tener mínimo 6 caracteres" |

---

## ⚠️ Tests de Errores

### T-007: Usuario no existe retorna error 401
| Campo | Valor |
|-------|-------|
| **Descripción** | Email no registrado debe dar mismo error que password incorrecta |
| **Entrada** | email: "noexiste@test.com", password: "Pass123!" |
| **Salida esperada** | 401: "Credenciales inválidas" |

```kotlin
@Test
fun `login con usuario inexistente retorna 401`() {
    // Given: email que no existe
    val email = "noexiste@test.com"
    val password = "Pass123!"
    
    // When
    val response = authService.login(email, password)
    
    // Then: mismo error que password incorrecta (seguridad)
    assertThat(response.isError).isTrue()
    assertThat(response.error.code).isEqualTo(401)
    assertThat(response.error.message).isEqualTo("Credenciales inválidas")
}
```

### T-008: Password incorrecta retorna error 401
| Campo | Valor |
|-------|-------|
| **Entrada** | email: "test@test.com", password: "PasswordIncorrecta" |
| **Salida esperada** | 401: "Credenciales inválidas" |

### T-009: Usuario baneado retorna error 403
| Campo | Valor |
|-------|-------|
| **Precondiciones** | Usuario existe con activo = false |
| **Entrada** | email: "banned@test.com", password: "Pass123!" |
| **Salida esperada** | 403: "Cuenta suspendida" |

---

## 🔄 Tests de Edge Cases

### T-010: Email con espacios se normaliza
| Campo | Valor |
|-------|-------|
| **Descripción** | Espacios antes/después del email se eliminan |
| **Entrada** | email: "  test@test.com  ", password: "Pass123!" |
| **Salida esperada** | 200 OK (funciona igual) |

### T-011: Password con exactamente 6 caracteres es válida
| Campo | Valor |
|-------|-------|
| **Descripción** | Límite inferior de longitud de password |
| **Entrada** | password: "123456" (exactamente 6) |
| **Salida esperada** | Acepta la validación (puede fallar por otras razones) |

---

## 📎 Notas de Implementación

- **Framework**: JUnit 5 + MockK (Kotlin)
- **Mocks necesarios**:
  - UsuarioRepository (para simular usuarios)
  - PasswordEncoder (para verificar hashes)
  - JwtService (para generar tokens de prueba)
- **Datos de prueba**:
  - Usuario válido: test@test.com / Pass123!
  - Usuario baneado: banned@test.com
- **Limpieza**: Cada test debe limpiar datos creados

---

## 🔧 Comandos de Ejecución

```bash
# Ejecutar todos los tests de auth
./gradlew test --tests "*.AuthTest*"

# Ejecutar un test específico
./gradlew test --tests "AuthTest.login exitoso retorna token"

# Ver reporte de cobertura
./gradlew jacocoTestReport
```
```

---

## 📋 TIPOS DE TESTS POR CAPA

### Tests Unitarios (Unit)

**Qué testean**: Una función/clase aislada  
**Mocks**: Todo excepto el sujeto de test  
**Velocidad**: Muy rápidos  
**Dónde**: `src/test/`

```kotlin
// Ejemplo: Test de UseCase
@Test
fun `GetFallasUseCase retorna lista de fallas`() {
    // Given: repository mockeado
    val mockRepo = mockk<FallaRepository>()
    coEvery { mockRepo.getFallas() } returns flowOf(Result.Success(listaDeFallas))
    
    val useCase = GetFallasUseCase(mockRepo)
    
    // When
    val result = useCase().first()
    
    // Then
    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrNull()?.size).isEqualTo(10)
}
```

### Tests de Integración (Integration)

**Qué testean**: Múltiples componentes juntos  
**Mocks**: Solo externos (BD real en memoria, API mockeada)  
**Velocidad**: Moderados  
**Dónde**: `src/test/integration/`

```kotlin
// Ejemplo: Repository con Room real
@Test
fun `FallaRepositoryImpl guarda y recupera fallas`() {
    // Given: Base de datos real en memoria
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.fallaDao()
    val repo = FallaRepositoryImpl(mockApi, dao, mapper)
    
    // When: guarda fallas
    repo.saveFallas(listaDeFallas)
    val result = repo.getFallas().first()
    
    // Then: las recupera correctamente
    assertThat(result.size).isEqualTo(listaDeFallas.size)
}
```

### Tests End-to-End (E2E)

**Qué testean**: Flujo completo del usuario  
**Mocks**: Ninguno (todo real)  
**Velocidad**: Lentos  
**Dónde**: `src/androidTest/`

```kotlin
// Ejemplo: Flujo de login completo
@Test
fun `usuario puede hacer login y ver dashboard`() {
    // Given: app iniciada
    launchActivity<MainActivity>()
    
    // When: hace login
    onView(withId(R.id.emailField)).perform(typeText("test@test.com"))
    onView(withId(R.id.passwordField)).perform(typeText("Pass123!"))
    onView(withId(R.id.loginButton)).perform(click())
    
    // Then: ve el dashboard
    onView(withId(R.id.dashboardTitle)).check(matches(isDisplayed()))
}
```

### Tests de UI (Compose)

**Qué testean**: Componentes de UI aislados  
**Mocks**: ViewModels/Estados  
**Velocidad**: Rápidos  
**Dónde**: `src/test/` o `src/androidTest/`

```kotlin
// Ejemplo: Test de Composable
@Test
fun `FallaCard muestra nombre y seccion`() {
    val falla = Falla(nombre = "Test Falla", seccion = "1A")
    
    composeTestRule.setContent {
        FallaCard(falla = falla, onClick = {})
    }
    
    composeTestRule.onNodeWithText("Test Falla").assertIsDisplayed()
    composeTestRule.onNodeWithText("1A").assertIsDisplayed()
}
```

---

## 🎯 PIRÁMIDE DE TESTS

```
                    /\
                   /  \
                  / E2E \       ← Pocos: Flujos críticos
                 /  (10%)  \
                /──────────\
               /            \
              / Integration  \   ← Moderados: Conexiones entre capas
             /    (30%)      \
            /──────────────────\
           /                    \
          /       Unit           \  ← Muchos: Lógica de negocio
         /        (60%)          \
        /──────────────────────────\
```

### Distribución recomendada

| Tipo | % del total | Qué cubrir |
|------|-------------|------------|
| **Unit** | 60% | UseCases, ViewModels, Mappers, Validaciones |
| **Integration** | 30% | Repositories, Room+API, Navegación |
| **E2E** | 10% | Flujos críticos: Login, Votación, Compra |

---

## ✅ Checklist de Tests

### Antes de implementar código

- [ ] ¿Existen tests definidos en la spec?
- [ ] ¿Tengo claros los casos de éxito?
- [ ] ¿Tengo claros los casos de error?
- [ ] ¿Qué mocks necesito?

### Al implementar tests

- [ ] ¿Nombre descriptivo del test?
- [ ] ¿Estructura Given-When-Then?
- [ ] ¿Un assert principal por test?
- [ ] ¿Test independiente de otros?
- [ ] ¿Limpia después de ejecutar?

### Antes de merge/commit

- [ ] ¿Todos los tests pasan?
- [ ] ¿Cobertura >= 80% en nueva funcionalidad?
- [ ] ¿Tests de edge cases incluidos?

---

> **Regla de oro**: Si la spec tiene N casos, deben existir N tests.
> Un caso sin test es un bug esperando a aparecer.
