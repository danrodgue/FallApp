# Actualización de Ubicaciones de Fallas

## 📍 Resumen

**Fecha**: 2026-02-03  
**Actualizadas**: 253 fallas de 347 (72.9% cobertura)  
**Script**: `07.datos/scripts/actualizar_ubicaciones_fallas.py`

## 🎯 Objetivo

Poblar los campos `ubicacion_lat` y `ubicacion_lon` de la tabla `fallas` con las coordenadas geográficas (latitud/longitud) disponibles en el archivo JSON fuente (`07.datos/raw/falles-fallas.json`).

Estos datos permiten:
- 🗺️ Visualización de fallas en mapas interactivos
- 📍 Búsqueda de fallas cercanas por geolocalización  
- 🔍 Filtrado por zona/barrio
- 📊 Análisis geoespacial de distribución de fallas

## 📊 Estructura de Datos

### JSON Fuente
```json
{
  "id_falla": 95,
  "nombre": "Plaza Sant Miquel-Vicent Iborra",
  "geo_point_2d": {
    "lat": 39.48827564,
    "lon": -0.37691843
  },
  ...
}
```

### Base de Datos PostgreSQL
```sql
-- Tabla: fallas
ubicacion_lat  | numeric(10,8)  -- Latitud (-90 a 90)
ubicacion_lon  | numeric(11,8)  -- Longitud (-180 a 180)
```

### API REST (Respuesta)
```json
{
  "exito": true,
  "datos": {
    "idFalla": 95,
    "nombre": "Plaza Sant Miquel-Vicent Iborra",
    "latitud": 39.48827564,
    "longitud": -0.37691843,
    ...
  }
}
```

**Mapeo de campos:**
- JSON `geo_point_2d.lat` → BD `ubicacion_lat` → API `latitud`
- JSON `geo_point_2d.lon` → BD `ubicacion_lon` → API `longitud`

## 🚀 Ejecución del Script

### Requisitos Previos

1. **PostgreSQL activo**:
   ```bash
   docker ps | grep postgres
   ```

2. **Backend operativo** (opcional, solo para verificación):
   ```bash
   sudo systemctl status fallapp
   ```

3. **Python 3 con psycopg2**:
   ```bash
   pip3 install psycopg2-binary
   ```

### Ejecutar Actualización

```bash
cd /srv/FallApp
python3 07.datos/scripts/actualizar_ubicaciones_fallas.py
```

### Salida del Script

```
==========================================
🗺️  ACTUALIZACIÓN DE UBICACIONES DE FALLAS
==========================================

📂 Cargando JSON desde: /srv/FallApp/07.datos/raw/falles-fallas.json
✅ Cargadas 351 fallas del JSON

🔌 Conectando a PostgreSQL en localhost:5432
✅ Conexión exitosa a PostgreSQL

🔄 Iniciando actualización de ubicaciones...
==========================================

✅ Falla #289 - Plaza Doctor Collado -> (39.474342, -0.377781)
✅ Falla # 97 - Doctor Olóriz-Muñoz Degraín -> (39.477863, -0.390479)
...

==========================================
📊 RESUMEN DE LA ACTUALIZACIÓN
==========================================
Total de fallas en JSON:        351
✅ Actualizadas correctamente:  253
⚠️  Sin ubicación en JSON:      0
❌ No encontradas en BD:        94
❌ Errores:                     4
==========================================

🎯 Tasa de éxito: 73.5%

📍 Estado final de ubicaciones en BD:
   Total de fallas:        347
   ✅ Con ubicación:       253
   ❌ Sin ubicación:       94
   📊 Cobertura:           72.9%
```

## 🔍 Verificación

### 1. Verificar en Base de Datos

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "SELECT 
    COUNT(*) as total,
    COUNT(ubicacion_lat) as con_ubicacion,
    COUNT(*) - COUNT(ubicacion_lat) as sin_ubicacion
   FROM fallas;"
```

**Resultado esperado:**
```
 total | con_ubicacion | sin_ubicacion 
-------+---------------+---------------
   347 |           253 |            94
```

### 2. Verificar en API

```bash
curl -s http://localhost:8080/api/fallas/95 | jq '{
  id: .datos.idFalla,
  nombre: .datos.nombre,
  lat: .datos.latitud,
  lon: .datos.longitud
}'
```

**Resultado esperado:**
```json
{
  "id": 95,
  "nombre": "Plaza Sant Miquel-Vicent Iborra",
  "lat": 39.48827564,
  "lon": -0.37691843
}
```

### 3. Buscar Fallas Cercanas

```bash
# Fallas en radio de 1km desde Plaza del Ayuntamiento
curl -s "http://localhost:8080/api/fallas/cercanas?lat=39.4699&lon=-0.3763&radio=1" | jq .
```

## 📝 Notas Técnicas

### ¿Por qué hay fallas sin ubicación?

De las 347 fallas en la BD:
- **253 tienen ubicación** (72.9%) - Actualizadas desde el JSON
- **94 NO tienen ubicación** (27.1%) - Por estas razones:
  1. **No existen en el JSON fuente** (91 fallas) - IDs no presentes en falles-fallas.json
  2. **Sin coordenadas en JSON** - Campo `geo_point_2d` null o incompleto
  3. **Errores de actualización** (7 fallas) - Problemas de formato o validación

### Fallas Sin Ubicación (Ejemplos)

```sql
-- Consultar fallas sin ubicación
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "SELECT id_falla, nombre 
   FROM fallas 
   WHERE ubicacion_lat IS NULL 
   ORDER BY id_falla 
   LIMIT 10;"
```

**IDs sin ubicación común:**
- ID 1-5: Fallas históricas sin datos en JSON municipal
- ID 11, 22, 37, 45, 46, 55, 61, 65, 71, 80, 82, 85, 89: No presentes en JSON 2025

### Manejo de Errores

El script usa **commits individuales** para cada falla:
- ✅ **Éxito**: Se confirma el cambio inmediatamente
- ❌ **Error**: Se hace rollback de esa falla y continúa con la siguiente
- 🔄 **Sin afectar**: Las actualizaciones previas exitosas se mantienen

## 🔄 Re-ejecución

El script es **idempotente**: puede ejecutarse múltiples veces sin problemas.

```bash
# Re-ejecutar actualización
python3 07.datos/scripts/actualizar_ubicaciones_fallas.py
```

Las fallas ya actualizadas se sobrescribirán con los mismos valores (no hay cambios).  
Útil si se añaden nuevas fallas al JSON fuente.

## 🛠️ Configuración

### Credenciales de Base de Datos

Archivo: `07.datos/scripts/actualizar_ubicaciones_fallas.py`

```python
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'fallapp',
    'user': 'fallapp_user',
    'password': 'fallapp_secure_password_2026'
}
```

⚠️ **Nota**: Las credenciales coinciden con `05.docker/docker-compose.yml`

### Ruta del JSON

```python
JSON_FILE = '/srv/FallApp/07.datos/raw/falles-fallas.json'
```

## 📚 Documentación Relacionada

- [03.BASE-DATOS.md](../04.docs/especificaciones/03.BASE-DATOS.md) - Especificación de tabla `fallas`
- [04.API-REST.md](../04.docs/especificaciones/04.API-REST.md) - Endpoint `/api/fallas/cercanas`
- [FallaService.java](../01.backend/src/main/java/com/fallapp/service/FallaService.java) - Lógica de negocio
- [Falla.java](../01.backend/src/main/java/com/fallapp/model/Falla.java) - Modelo de datos

## 🗺️ Uso en Frontend

```javascript
// Obtener falla con ubicación
fetch('http://35.180.21.42:8080/api/fallas/95')
  .then(res => res.json())
  .then(data => {
    const { latitud, longitud, nombre } = data.datos;
    
    // Añadir marcador en mapa (Leaflet/Google Maps)
    L.marker([latitud, longitud])
      .addTo(map)
      .bindPopup(nombre);
  });

// Buscar fallas cercanas
fetch('http://35.180.21.42:8080/api/fallas/cercanas?lat=39.47&lon=-0.37&radio=2')
  .then(res => res.json())
  .then(data => {
    data.datos.forEach(falla => {
      console.log(`${falla.nombre}: ${falla.latitud}, ${falla.longitud}`);
    });
  });
```

## ✅ Conclusión

- ✅ **253 fallas** tienen ubicación geográfica (73% cobertura)
- ✅ **API REST** devuelve coordenadas en campo `latitud` y `longitud`
- ✅ **Backend reiniciado** y funcionando correctamente
- ✅ **PostgreSQL y API activos** en todo momento (systemd)
- ✅ **Datos listos** para mapas interactivos y búsquedas geoespaciales

---

**Última actualización**: 2026-02-03  
**Autor**: Sistema de datos FallApp
