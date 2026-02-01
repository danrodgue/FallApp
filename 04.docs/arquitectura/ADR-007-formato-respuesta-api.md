# ADR-007: Formato de Respuesta API - ApiResponse vs Especificación

**Estado**: Propuesta  
**Fecha**: 2026-02-01  
**Decisores**: Equipo de desarrollo  
**Contexto técnico**: Backend Spring Boot API REST  

## Contexto

Existe una discrepancia entre la **especificación API** y la **implementación actual** en cuanto al formato de respuestas JSON:

### Especificación (04.API-REST.md)
```json
{
  "exito": true,
  "mensaje": "Operación exitosa",
  "datos": { ... },
  "timestamp": "2026-02-01T14:30:00Z"
}
```

**Campos en español**: `exito`, `mensaje`, `datos`

### Implementación Actual (ApiResponse.java)
```java
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
```

**Campos en inglés**: `success`, `message`, `data`

### Impacto

- ❌ Frontend esperará campos en español según especificación
- ❌ Swagger UI muestra ejemplos incorrectos
- ❌ Inconsistencia entre docs y código
- ⚠️ Requiere cambio en **todos los controllers** (6 archivos)
- ⚠️ Rompe compatibilidad si ya hay clientes consumiendo API

## Opciones Evaluadas

### Opción A: Mantener Inglés (Implementación Actual)

**Razones a favor**:
- ✅ Estándar de facto en APIs REST internacionales
- ✅ Código Java usa convenciones en inglés
- ✅ Librerías como Jackson usan inglés (`success`, `data`)
- ✅ Facilita integración con herramientas de terceros
- ✅ No requiere cambios en código (42 archivos)
- ✅ Consistente con ejemplos de Spring Boot

**Razones en contra**:
- ❌ No cumple especificación original
- ❌ Puede confundir a desarrolladores frontend españoles
- ❌ Inconsistente con nombres de endpoints en español (`/fallas`, `/eventos`)

**Cambios necesarios**:
- Actualizar especificación 04.API-REST.md (1 archivo)
- Actualizar ejemplos de Swagger (OpenAPIConfig.java)

### Opción B: Cambiar a Español (Según Especificación)

**Razones a favor**:
- ✅ Cumple especificación original
- ✅ Consistente con dominio de negocio (Fallas es contexto español)
- ✅ API completamente en español (endpoints + respuestas)
- ✅ Más natural para desarrolladores hispanohablantes

**Razones en contra**:
- ❌ No es estándar internacional
- ❌ Requiere cambios en 6 Controllers + ApiResponse
- ❌ Rompe convenciones de Java (campos en inglés)
- ❌ Puede causar problemas con librerías esperando `success`/`data`

**Cambios necesarios**:
```java
@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCaseStrategy.class)
public class ApiResponse<T> {
    @JsonProperty("exito")
    private boolean exito;
    
    @JsonProperty("mensaje")
    private String mensaje;
    
    @JsonProperty("datos")
    private T datos;
    
    private LocalDateTime timestamp;
    
    // Métodos de conveniencia
    public static <T> ApiResponse<T> exitoso(String mensaje, T datos) {
        return new ApiResponse<>(true, mensaje, datos, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String mensaje) {
        return new ApiResponse<>(false, mensaje, null, LocalDateTime.now());
    }
}
```

### Opción C: Híbrido con @JsonProperty

**Razones a favor**:
- ✅ Código Java en inglés (convenciones Java)
- ✅ JSON en español (cumple especificación)
- ✅ Mejor de ambos mundos

**Razones en contra**:
- ⚠️ Confusión entre nombre de campo y nombre JSON
- ⚠️ Requiere documentación clara de la diferencia

**Implementación**:
```java
public class ApiResponse<T> {
    @JsonProperty("exito")
    private boolean success;  // Nombre Java: success, JSON: exito
    
    @JsonProperty("mensaje")
    private String message;
    
    @JsonProperty("datos")
    private T data;
    
    private LocalDateTime timestamp;
}
```

## Decisión

**PROPUESTA: Opción A - Mantener Inglés**

### Justificación

1. **Estándar de industria**: La mayoría de APIs REST públicas usan inglés
   - GitHub API: `success`, `message`, `data`
   - Stripe API: `success`, `error`, `data`
   - Google APIs: `success`, `items`, `error`

2. **Ecosistema Spring Boot**: Todos los ejemplos y tutoriales usan inglés
   - Spring HATEOAS: `_embedded`, `_links`
   - Spring Data REST: `content`, `page`, `size`

3. **Pragmatismo**: La API está al 52% de implementación
   - Ya existen 24 endpoints devolviendo formato inglés
   - Cambiar ahora requiere reescribir 6 controllers
   - Sin clientes reales aún consumiendo la API

4. **Internacionalización futura**: Si la app crece internacionalmente
   - Inglés facilita adopción fuera de España
   - Valencia tiene turismo internacional
   - Aplicación podría expandirse a otras ciudades

### Implementación

1. **Actualizar especificación**:
```markdown
## Formato de Respuesta Estándar

Todas las respuestas de la API siguen este formato:

{
  "success": true,          // Indica si la operación fue exitosa
  "message": "string",      // Mensaje descriptivo
  "data": object | array,   // Datos de respuesta (puede ser null)
  "timestamp": "ISO8601"    // Fecha/hora de la respuesta
}
```

2. **Actualizar README_API.md**: Ya usa inglés ✅

3. **Mantener ApiResponse.java**: Sin cambios ✅

### Excepciones

Campos de dominio **sí permanecen en español**:
```json
{
  "success": true,
  "data": {
    "nombre": "Falla Plaza del Ayuntamiento",  // ✅ Español
    "lema": "Valencia en Fallas",              // ✅ Español
    "seccion": "ESPECIAL"                      // ✅ Español
  }
}
```

**Razón**: El dominio es inherentemente español (Fallas de Valencia)

## Consecuencias

### Positivas
- ✅ No requiere refactorización de código
- ✅ Consistente con estándares internacionales
- ✅ Facilita integración con librerías de terceros
- ✅ Documentación (README_API.md) ya usa formato correcto

### Negativas
- ❌ Especificación original queda desactualizada
- ❌ Mezcla inglés (estructura) con español (dominio)
- ⚠️ Requiere explicación clara en docs

### Mitigaciones
- Actualizar 04.API-REST.md con formato definitivo
- Agregar sección en README explicando la decisión
- Documentar en ADR para referencia futura
- Mantener consistencia: estructura en inglés, dominio en español

## Alternativas Rechazadas

### Por qué NO Opción B (Todo Español)
- Requiere reescribir 6 controllers + todos los tests
- Rompe convenciones de Java (campos en español)
- No es estándar en ecosistema Spring Boot
- Dificulta integración con herramientas de terceros

### Por qué NO Opción C (Híbrido @JsonProperty)
- Confusión innecesaria entre nombre Java y JSON
- No aporta valor suficiente vs complejidad
- Requiere explicación constante a nuevos desarrolladores

## Validación

**Criterios de aceptación**:
- [ ] Especificación 04.API-REST.md actualizada con formato inglés
- [ ] Ejemplos en Swagger UI muestran formato correcto
- [ ] README_API.md documenta la decisión
- [ ] ADR-007 publicado
- [ ] Sin cambios en código (mantener ApiResponse.java actual)

## Plan de Implementación

1. **Actualizar 04.API-REST.md** (30 min)
   - Cambiar todos los ejemplos JSON
   - Actualizar sección de "Formato de Respuesta"
   - Agregar nota sobre campos de dominio en español

2. **Actualizar OpenAPIConfig.java** (15 min)
   - Ejemplos de Swagger con formato correcto

3. **Agregar nota en README_API.md** (10 min)
   - Sección "Convenciones de Nomenclatura"
   - Explicar estructura inglés + dominio español

4. **Crear test de validación** (30 min)
   - Test que valida estructura de ApiResponse
   - Garantizar campos `success`, `message`, `data`

**Tiempo total**: 1.5 horas

## Referencias

- **Código actual**: [ApiResponse.java](../../01.backend/src/main/java/com/fallapp/dto/ApiResponse.java)
- **Especificación original**: [04.API-REST.md líneas 50-100](../especificaciones/04.API-REST.md)
- **Controllers afectados**: [controller/](../../01.backend/src/main/java/com/fallapp/controller/)
- **Estándares de industria**: 
  - [GitHub API](https://docs.github.com/en/rest)
  - [Google JSON Style Guide](https://google.github.io/styleguide/jsoncstyleguide.xml)

## Revisión Futura

**Condiciones para reconsiderar**:
- Si cliente importante requiere formato en español
- Si aplicación se limita permanentemente a usuarios españoles
- Si equipo de frontend reporta problemas de usabilidad

**Próxima revisión**: Tras feedback de primer usuario frontend (estimado 2-3 semanas)

---

**Decisión tomada por**: Equipo backend  
**Fecha**: 2026-02-01  
**Estado**: ✅ Propuesta (pendiente aprobación)
