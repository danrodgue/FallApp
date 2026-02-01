# 👨‍💻 Bienvenido a FallApp - Guía para Desarrolladores

> **Proyecto**: FallApp - Sistema de Gestión de Fallas Valencianas  
> **Tipo**: Proyecto Intermodular DAM  
> **Duración**: 4 semanas (19 enero - 16 febrero 2026)  
> **Stack**: Spring Boot + PostgreSQL + Electron + Android

---

## 🚀 Inicio Rápido (5 minutos)

### 1. Lee la Guía de Programación
```
📄 01.GUIA-PROGRAMACION.md
```
**Aprenderás**:
- Filosofía del proyecto (KISS, DRY, Fail-Fast)
- Convenciones de código (Java, JavaScript, Kotlin)
- Estructura de carpetas
- Patrones de desarrollo
- Lista de verificación

⏱️ **Tiempo**: 10-15 minutos de lectura

---

### 2. Entiende la Visión General
```
📄 especificaciones/00.VISION-GENERAL.md
```
**Aprenderás**:
- Arquitectura del sistema
- Tecnologías utilizadas
- Modelo de dominio (Usuarios, Fallas, Eventos, Votos)
- Roles y permisos
- Casos de uso principales

⏱️ **Tiempo**: 10-12 minutos de lectura

---

### 3. Consulta las Especificaciones Técnicas

Antes de implementar **cualquier funcionalidad**, lee su especificación:

| Módulo | Especificación | Contenido |
|--------|----------------|-----------|
| **Usuarios y Autenticación** | `especificaciones/01.SISTEMA-USUARIOS.md` | Registro, login, JWT, roles, permisos |
| **Fallas/Casales** | `especificaciones/02.FALLAS.md` | Gestión de fallas, geolocalización, CRUD |
| **Eventos** | `especificaciones/03.EVENTOS.md` | Eventos por falla, calendario |
| **Votaciones** | `especificaciones/04.VOTACIONES.md` | Votos de ninots, restricciones |

⏱️ **Tiempo**: 5-8 minutos por especificación

---

## 📖 Flujo de Trabajo Recomendado

```
1. Tarea asignada
   ↓
2. Lee la especificación relevante
   ↓
3. Consulta 01.GUIA-PROGRAMACION.md para convenciones
   ↓
4. Implementa siguiendo la especificación
   ↓
5. Escribe pruebas
   ↓
6. Verifica checklist de 01.GUIA-PROGRAMACION.md
   ↓
7. Commit con mensaje descriptivo
```

---

## 🎯 Principios del Proyecto

### 1. Simplicidad (KISS)
> Keep It Simple, Stupid

- Preferir soluciones directas sobre arquitecturas complejas
- Si tiene >150 líneas, considerar dividir
- Código legible > Código "elegante"

**Ejemplo**:
```java
// ✅ BIEN - Simple y claro
public List<Falla> obtenerFallasActivas() {
    return fallaRepository.findByActiva(true);
}

// ❌ MAL - Sobreingeniería
public List<Falla> obtenerFallasActivas() {
    return fallaRepository.findAll()
        .stream()
        .filter(falla -> Optional.ofNullable(falla.getActiva()).orElse(false))
        .collect(Collectors.toList());
}
```

---

### 2. No Repetir (DRY)
> Don't Repeat Yourself

- **3+ copias** → Refactorizar y centralizar
- **1-2 copias** → Aceptable en fase inicial
- Crear utilidades compartidas cuando el patrón esté claro

---

### 3. Fallar Rápido (Fail-Fast)
> Preferir errores explícitos sobre comportamientos silenciosos

**❌ MAL - Valor por defecto oculta error**:
```java
String apiUrl = config.getApiUrl().orElse("http://localhost:8080");
```

**✅ BIEN - Error explícito**:
```java
String apiUrl = config.getApiUrl()
    .orElseThrow(() -> new ConfigurationException("API URL no configurada en application.properties"));
```

**Por qué**: Es mejor que falle en desarrollo que comportarse mal en producción.

---

## 🏗️ Estructura del Proyecto

### Backend (Spring Boot)
```
fallapp-backend/
├── src/main/java/com/fallapp/
│   ├── config/              # Configuración (Security, CORS)
│   ├── controller/          # Endpoints REST
│   ├── service/             # Lógica de negocio
│   ├── repository/          # Acceso a datos (JPA)
│   ├── model/               # Entidades JPA
│   ├── dto/                 # DTOs para API
│   ├── exception/           # Excepciones personalizadas
│   └── util/                # Utilidades
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/        # Migraciones SQL (Flyway)
└── src/test/               # Pruebas
```

### Frontend Escritorio (Electron)
```
fallapp-desktop/
├── src/
│   ├── main.js              # Proceso principal
│   ├── renderer/
│   │   ├── index.html
│   │   ├── styles/
│   │   └── scripts/
│   │       ├── api.js       # Cliente API
│   │       └── auth.js      # Autenticación
│   └── assets/
└── package.json
```

### Móvil (Android)
```
fallapp-android/
├── app/src/main/java/com/fallapp/
│   ├── data/                # Repositorios, Room
│   ├── domain/              # Casos de uso
│   ├── presentation/        # UI (Activities, ViewModels)
│   └── di/                  # Inyección de dependencias
└── app/src/main/res/        # Recursos
```

---

## 🔧 Configuración del Entorno

### Backend
1. **Java 17+** instalado
2. **PostgreSQL 15+** corriendo (o Docker)
3. **Maven** instalado
4. Configurar `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/fallapp
   spring.datasource.username=fallapp_user
   spring.datasource.password=tu_password
   jwt.secret=tu-clave-secreta-muy-larga
   ```

### Frontend Escritorio
1. **Node.js 18+** instalado
2. **npm** o **yarn**
3. `npm install` en el directorio del proyecto
4. `npm start` para ejecutar

### Móvil
1. **Android Studio** instalado
2. **SDK Android API 34+**
3. Configurar `local.properties` con rutas del SDK
4. Sync Gradle

---

## 📝 Convenciones de Código

### Java (Backend)
```java
// Clases: PascalCase
public class FallaService { }

// Métodos: camelCase (verbos)
public Falla crearFalla() { }
public List<Falla> listarFallas() { }

// Variables: camelCase (sustantivos descriptivos)
private Long fallaId;
private String nombreCompleto;

// Constantes: UPPER_SNAKE_CASE
private static final int MAX_INTENTOS = 3;
```

### JavaScript (Electron)
```javascript
// Variables: camelCase
const apiBaseUrl = 'http://localhost:8080/api';

// Funciones: camelCase (verbos)
async function cargarEventos() { }
function mostrarError(mensaje) { }

// Clases: PascalCase
class ApiClient { }
```

### Kotlin (Android)
```kotlin
// Clases: PascalCase
class EventoRepository { }

// Funciones: camelCase
suspend fun obtenerEventos(): List<Evento> { }

// Propiedades: camelCase
private val eventoDao: EventoDao
```

---

## ✅ Checklist Antes de Commit

- [ ] El código compila sin errores
- [ ] Las pruebas pasan (`mvn test` o equivalente)
- [ ] Sigue las convenciones de nomenclatura
- [ ] Usa fail-fast (sin fallbacks silenciosos)
- [ ] Logs informativos añadidos
- [ ] Manejo de errores explícito
- [ ] Documentación actualizada si fue necesario
- [ ] Revisé los cambios (`git diff`)
- [ ] Mensaje de commit descriptivo

---

## 🧪 Pruebas

### Estrategia
- **Pruebas de humo**: Verifican que el sistema arranca
- **Pruebas de integración**: Verifican endpoints completos
- **Objetivo**: ~100 pruebas en primera versión

### Ejemplo Prueba de Integración
```java
@SpringBootTest
@AutoConfigureMockMvc
public class FallaIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void crearFalla_DatosValidos_Exitoso() throws Exception {
        CrearFallaRequest request = new CrearFallaRequest();
        // ... configurar request
        
        mockMvc.perform(post("/api/fallas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
    }
}
```

---

## 🐛 Debugging

### Backend
- Logs con SLF4J: `log.info("Falla creada: id={}", id);`
- Activar logs de SQL: `spring.jpa.show-sql=true`
- Usa breakpoints en IntelliJ/Eclipse

### Frontend
- `console.log()` para debugging rápido
- Chrome DevTools (F12)
- Network tab para ver peticiones HTTP

### Móvil
- Logcat en Android Studio
- `Log.d(TAG, "mensaje")` para logs
- Layout Inspector para UI

---

## 📚 Recursos Útiles

### Documentación del Proyecto
- `00.INDICE.md` - Índice maestro
- `01.GUIA-PROGRAMACION.md` - Esta guía expandida
- `02.GUIA-PROMPTS-IA.md` - Si trabajas con IAs
- `especificaciones/` - Especificaciones técnicas detalladas

### Documentación Externa
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Electron Docs](https://www.electronjs.org/docs/latest)
- [Android Developers](https://developer.android.com/)

---

## 🆘 ¿Necesitas Ayuda?

1. **Consulta la especificación** del módulo en que trabajas
2. **Revisa** `01.GUIA-PROGRAMACION.md` para convenciones
3. **Busca ejemplos** en el código existente
4. **Pregunta** al equipo en el canal de Slack/Discord
5. **Documenta** la solución si fue compleja

---

## 🎓 Recordatorios del Contexto Académico

### Temporalización
- **Inicio**: 19 enero 2026
- **Duración**: 4 semanas
- **Entrega**: 16-20 febrero 2026
- **Exposición**: Semana del 16-20 febrero

### Evaluación
- **Contenido técnico**: 65% (distribuido en ADA, PMDP, DI, IPE2)
- **Memoria**: 10%
- **Exposición**: 15%
- **Trabajo en clase**: 10%

**Mínimo 50% en cada apartado para aprobar.**

### Asistencia
- Obligatoria y controlada por Itaca
- Penaliza faltas y retrasos
- Puede suponer suspenso

---

## 🏆 Objetivos de Calidad

- ✅ Sistema funcional end-to-end
- ✅ API REST completa y documentada
- ✅ Aplicación escritorio operativa
- ✅ Aplicación móvil con funcionalidades core
- ✅ ~100 pruebas automatizadas
- ✅ Desplegado en AWS EC2
- ✅ Memoria de ~15 páginas
- ✅ Demo lista para presentación

---

## 💪 Consejos Finales

1. **Lee las especificaciones** antes de codificar
2. **Comunica** si encuentras bloqueos
3. **Commitea frecuentemente** con mensajes claros
4. **Pide revisión** antes de merge a main
5. **Documenta** decisiones importantes
6. **Mantén la calma**: 4 semanas es poco tiempo, prioriza funcionalidad

---

> **¡Éxito en el proyecto FallApp! 🎊**  
> Recuerda: Funcionalidad completa > Código perfecto

---

**Última actualización**: 2026-02-01
