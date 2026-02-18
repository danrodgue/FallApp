# 🐛 Bugfix 2026-02-18 - IA de Sentimiento (Hugging Face) + errores 403

> **Fecha**: 2026-02-18  
> **Tipo**: Bugfix funcional crítico (backend + docker + seguridad + integración IA)  
> **Estado**: ✅ Resuelto y validado en servidor

---

## 1) Síntomas reportados

- El panel de IA mostraba sentimiento en `0` aunque existían comentarios.
- El botón de reanálisis no conseguía persistir `positive/neutral/negative`.
- Aparecían errores `403` al acceder a datos/endpoint admin en ciertos flujos.

---

## 2) Causas raíz encontradas

### A. Endpoint de Hugging Face obsoleto

- Se estaba usando `https://api-inference.huggingface.co/...`.
- Hugging Face respondió `410` indicando migración a `router.huggingface.co`.

### B. Header `Accept` incompatible con router de Hugging Face

- El router devolvía `400 Bad Request` con error de tipo `Accept type not supported`.
- Resultado: ningún comentario recibía sentimiento y todo quedaba en `NULL`.

### C. Desalineación en seguridad/context path (403)

- En Docker estaba configurado `SERVER_SERVLET_CONTEXT_PATH=/api` mientras los controladores ya usan prefijo `/api`.
- Además faltaba abrir explícitamente preflight `OPTIONS` y rutas equivalentes sin prefijo.

### D. Reanálisis incompleto para datos heredados

- Había comentarios históricos con estados pendientes/no normalizados.
- Necesario ampliar selección de pendientes y normalizar etiquetas de salida.

---

## 3) Solución aplicada

## 3.1 Backend IA (Hugging Face)

Archivo: `01.backend/src/main/java/com/fallapp/service/SentimentAnalysisService.java`

- Migración de endpoint a router configurable:
  - `huggingface.api.base-url` (por defecto `https://router.huggingface.co/hf-inference/models`)
  - `huggingface.api.model`
- Header explícito `Accept: application/json` para compatibilidad router.
- Parseo robusto de respuestas y normalización de etiquetas (`positive|neutral|negative`).
- Modo síncrono para reanálisis manual (`analizarComentario`) y asíncrono para alta de comentarios (`analizarComentarioAsync`).

## 3.2 Reanálisis y estadísticas

Archivos:

- `01.backend/src/main/java/com/fallapp/repository/ComentarioRepository.java`
- `01.backend/src/main/java/com/fallapp/service/ComentarioService.java`
- `01.backend/src/main/java/com/fallapp/service/EstadisticasService.java`

Cambios:

- Reanálisis de pendientes ampliado: incluye `NULL`, vacío y valores no válidos.
- Soporte de texto heredado (`COALESCE(contenido, texto_comentario)`).
- Conteo de estadísticas consistente y normalizado.

## 3.3 Seguridad y CORS (403)

Archivo: `01.backend/src/main/java/com/fallapp/config/SecurityConfig.java`

- `OPTIONS /**` permitido para preflight CORS.
- Rutas públicas/admin contempladas con y sin prefijo `/api` para despliegues con distinto context path.
- Endpoints admin quedan autenticados, sin bloquear por preflight.

## 3.4 Docker / despliegue backend

Archivo: `05.docker/docker-compose.yml`

- `SERVER_SERVLET_CONTEXT_PATH` ajustado a `/` (evita duplicidad de `/api/api`).
- `SPRING_JPA_DATABASE_PLATFORM` corregido a `org.hibernate.dialect.PostgreSQLDialect`.
- `SPRING_JPA_HIBERNATE_DDL_AUTO` por defecto a `none` (entorno schema-first legado).
- Variables de Hugging Face añadidas para base URL/model.
- Ajustes de recursos para host con 1 CPU.

---

## 4) Validación realizada

- `GET /api/fallas` responde `200`.
- Preflight `OPTIONS` en endpoint admin responde `200` con cabeceras CORS correctas.
- Logs backend dejan de mostrar `410` del endpoint obsoleto.
- Tras fix de `Accept`, desaparece el `400` por tipo no soportado y se habilita persistencia de sentimiento.

Consulta SQL de control:

```sql
SELECT sentimiento, COUNT(*)
FROM comentarios
GROUP BY sentimiento
ORDER BY 2 DESC;
```

---

## 5) Archivos impactados (resumen)

- `01.backend/src/main/java/com/fallapp/service/SentimentAnalysisService.java`
- `01.backend/src/main/java/com/fallapp/service/ComentarioService.java`
- `01.backend/src/main/java/com/fallapp/service/EstadisticasService.java`
- `01.backend/src/main/java/com/fallapp/repository/ComentarioRepository.java`
- `01.backend/src/main/java/com/fallapp/controller/AdminSentimientoController.java`
- `01.backend/src/main/java/com/fallapp/config/SecurityConfig.java`
- `01.backend/src/main/resources/application.properties`
- `02.desktop/js/ia-sentiment.js`
- `05.docker/docker-compose.yml`
- `05.docker/.env.example`

---

## 6) Lecciones aprendidas

- Dependencias externas de IA pueden cambiar endpoints sin compatibilidad retroactiva.
- En integraciones HTTP externas, fijar explícitamente `Accept` y `Content-Type` evita fallos silenciosos.
- En despliegues Docker con context path, no duplicar prefijos que ya están en controladores.
- Para bases legacy, priorizar `ddl-auto=none`/migraciones explícitas sobre validaciones estrictas en runtime.
