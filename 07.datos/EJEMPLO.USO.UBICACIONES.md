# Ejemplos de Uso - Ubicaciones GPS de Fallas

## 📍 Introducción

Tras la actualización del 2026-02-03, **253 fallas** (72.9%) tienen coordenadas GPS disponibles en los campos `latitud` y `longitud` de la API.

## 🌐 Ejemplos de Consultas API

### 1. Obtener falla con ubicación

```bash
curl -s http://35.180.21.42:8080/api/fallas/95 | jq '{
  id: .datos.idFalla,
  nombre: .datos.nombre,
  latitud: .datos.latitud,
  longitud: .datos.longitud,
  seccion: .datos.seccion
}'
```

**Respuesta:**
```json
{
  "id": 95,
  "nombre": "Plaza Sant Miquel-Vicent Iborra",
  "latitud": 39.48827564,
  "longitud": -0.37691843,
  "seccion": "8A"
}
```

### 2. Buscar fallas cercanas (1km desde Plaza del Ayuntamiento)

```bash
curl -s "http://35.180.21.42:8080/api/fallas/cercanas?lat=39.4699&lon=-0.3763&radio=1" \
  | jq '.datos[] | {nombre: .nombre, seccion: .seccion}'
```

### 3. Listar todas las fallas con ubicación

```bash
curl -s "http://35.180.21.42:8080/api/fallas?pagina=0&tamano=350" \
  | jq '.datos.contenido[] | select(.latitud != null) | {
      nombre: .nombre,
      lat: .latitud,
      lon: .longitud
    }' | head -20
```

## 🗺️ Integración con Mapas

### Leaflet.js (JavaScript)

```html
<!DOCTYPE html>
<html>
<head>
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <style>
    #map { height: 600px; }
  </style>
</head>
<body>
  <div id="map"></div>
  
  <script>
    // Inicializar mapa centrado en Valencia
    const map = L.map('map').setView([39.4699, -0.3763], 13);
    
    // Añadir capa de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(map);
    
    // Cargar fallas desde API
    fetch('http://35.180.21.42:8080/api/fallas?pagina=0&tamano=350')
      .then(res => res.json())
      .then(data => {
        data.datos.contenido.forEach(falla => {
          if (falla.latitud && falla.longitud) {
            // Crear marcador
            const marker = L.marker([falla.latitud, falla.longitud])
              .addTo(map);
            
            // Añadir popup con información
            marker.bindPopup(`
              <b>${falla.nombre}</b><br>
              Sección: ${falla.seccion}<br>
              Presidente: ${falla.presidente}<br>
              Fundación: ${falla.anyoFundacion}
            `);
          }
        });
      });
  </script>
</body>
</html>
```

### Google Maps (JavaScript)

```html
<!DOCTYPE html>
<html>
<head>
  <script src="https://maps.googleapis.com/maps/api/js?key=TU_API_KEY"></script>
  <style>
    #map { height: 600px; }
  </style>
</head>
<body>
  <div id="map"></div>
  
  <script>
    // Inicializar mapa
    const map = new google.maps.Map(document.getElementById('map'), {
      center: { lat: 39.4699, lng: -0.3763 },
      zoom: 13
    });
    
    // Cargar fallas
    fetch('http://35.180.21.42:8080/api/fallas?pagina=0&tamano=350')
      .then(res => res.json())
      .then(data => {
        data.datos.contenido.forEach(falla => {
          if (falla.latitud && falla.longitud) {
            const marker = new google.maps.Marker({
              position: { lat: falla.latitud, lng: falla.longitud },
              map: map,
              title: falla.nombre
            });
            
            const infoWindow = new google.maps.InfoWindow({
              content: `
                <h3>${falla.nombre}</h3>
                <p><strong>Sección:</strong> ${falla.seccion}</p>
                <p><strong>Presidente:</strong> ${falla.presidente}</p>
              `
            });
            
            marker.addListener('click', () => {
              infoWindow.open(map, marker);
            });
          }
        });
      });
  </script>
</body>
</html>
```

## 🐍 Python - Análisis de Datos

### Crear mapa con Folium

```python
import requests
import folium
from folium.plugins import MarkerCluster

# Obtener fallas de la API
response = requests.get('http://35.180.21.42:8080/api/fallas?pagina=0&tamano=350')
fallas = response.json()['datos']['contenido']

# Crear mapa centrado en Valencia
mapa = folium.Map(
    location=[39.4699, -0.3763],
    zoom_start=13,
    tiles='OpenStreetMap'
)

# Añadir cluster de marcadores
marker_cluster = MarkerCluster().add_to(mapa)

# Añadir marcador por cada falla con ubicación
for falla in fallas:
    if falla.get('latitud') and falla.get('longitud'):
        folium.Marker(
            location=[falla['latitud'], falla['longitud']],
            popup=f"""
                <b>{falla['nombre']}</b><br>
                Sección: {falla['seccion']}<br>
                Presidente: {falla['presidente']}<br>
                Fundada: {falla['anyoFundacion']}
            """,
            tooltip=falla['nombre']
        ).add_to(marker_cluster)

# Guardar mapa
mapa.save('mapa_fallas_valencia.html')
print(f"✅ Mapa creado con {len([f for f in fallas if f.get('latitud')])} fallas")
```

### Calcular distancia entre fallas

```python
import requests
from math import radians, sin, cos, sqrt, atan2

def calcular_distancia(lat1, lon1, lat2, lon2):
    """Calcular distancia en km usando fórmula Haversine"""
    R = 6371  # Radio de la Tierra en km
    
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    
    return R * c

# Obtener fallas
response = requests.get('http://35.180.21.42:8080/api/fallas?pagina=0&tamano=350')
fallas = [f for f in response.json()['datos']['contenido'] 
          if f.get('latitud') and f.get('longitud')]

# Encontrar falla más cercana a un punto
punto_referencia = (39.4699, -0.3763)  # Plaza Ayuntamiento

for falla in fallas:
    distancia = calcular_distancia(
        punto_referencia[0], punto_referencia[1],
        falla['latitud'], falla['longitud']
    )
    falla['distancia_km'] = round(distancia, 2)

# Ordenar por distancia
fallas_ordenadas = sorted(fallas, key=lambda f: f['distancia_km'])

print("🎭 5 Fallas más cercanas a Plaza del Ayuntamiento:")
for i, falla in enumerate(fallas_ordenadas[:5], 1):
    print(f"{i}. {falla['nombre']} - {falla['distancia_km']} km")
```

## 📊 SQL - Consultas Geoespaciales

### Fallas en un radio específico

```sql
-- Buscar fallas en 1km de radio desde un punto
SELECT 
  id_falla,
  nombre,
  seccion,
  ubicacion_lat,
  ubicacion_lon,
  -- Calcular distancia (aproximada)
  SQRT(
    POW((ubicacion_lat - 39.4699) * 111.32, 2) +
    POW((ubicacion_lon - (-0.3763)) * 111.32 * COS(RADIANS(39.4699)), 2)
  ) AS distancia_km
FROM fallas
WHERE ubicacion_lat IS NOT NULL
  AND ubicacion_lon IS NOT NULL
HAVING distancia_km < 1
ORDER BY distancia_km
LIMIT 10;
```

### Fallas por zona geográfica

```sql
-- Fallas en zona norte de Valencia (lat > 39.48)
SELECT 
  id_falla,
  nombre,
  seccion,
  ubicacion_lat,
  ubicacion_lon
FROM fallas
WHERE ubicacion_lat > 39.48
ORDER BY ubicacion_lat DESC;
```

### Densidad de fallas por cuadrante

```sql
-- Contar fallas por cuadrante
SELECT 
  CASE 
    WHEN ubicacion_lat >= 39.47 AND ubicacion_lon >= -0.37 THEN 'Noreste'
    WHEN ubicacion_lat >= 39.47 AND ubicacion_lon < -0.37 THEN 'Noroeste'
    WHEN ubicacion_lat < 39.47 AND ubicacion_lon >= -0.37 THEN 'Sureste'
    ELSE 'Suroeste'
  END AS cuadrante,
  COUNT(*) as total_fallas,
  ARRAY_AGG(seccion) as secciones
FROM fallas
WHERE ubicacion_lat IS NOT NULL
  AND ubicacion_lon IS NOT NULL
GROUP BY cuadrante
ORDER BY total_fallas DESC;
```

## 🎯 Casos de Uso

### 1. Ruta de visita de fallas

Crear ruta óptima para visitar las 5 fallas principales de una sección.

### 2. Mapa interactivo

Mostrar todas las fallas en mapa con filtros por sección, distintivo, año fundación.

### 3. Búsqueda por proximidad

"Mostrarme las 3 fallas más cercanas a mi ubicación actual".

### 4. Análisis geográfico

Densidad de fallas por barrio, distribución por secciones, cobertura territorial.

### 5. Planificador de eventos

Calcular tiempos de desplazamiento entre eventos de diferentes fallas.

## 📱 Apps Móviles

### React Native / Flutter

```javascript
// Obtener ubicación del usuario
navigator.geolocation.getCurrentPosition(async (position) => {
  const { latitude, longitude } = position.coords;
  
  // Buscar fallas cercanas
  const response = await fetch(
    `http://35.180.21.42:8080/api/fallas/cercanas?lat=${latitude}&lon=${longitude}&radio=2`
  );
  
  const data = await response.json();
  const fallasCercanas = data.datos;
  
  // Mostrar lista de fallas cercanas
  fallasCercanas.forEach(falla => {
    console.log(`${falla.nombre} - ${falla.distancia}km`);
  });
});
```

## ✅ Conclusión

Con **253 fallas geolocalizadas** (72.9%), FallApp ofrece datos suficientes para:
- 🗺️ Crear mapas interactivos completos
- 📍 Implementar búsquedas geoespaciales
- 📊 Realizar análisis de distribución territorial
- 🚶 Planificar rutas de visita optimizadas

---

**Documentación relacionada:**
- [ACTUALIZACION.UBICACIONES.FALLAS.md](ACTUALIZACION.UBICACIONES.FALLAS.md) - Proceso de actualización
- [04.docs/especificaciones/04.API-REST.md](../04.docs/especificaciones/04.API-REST.md) - Documentación completa de API
- [01.backend/README_API.md](../01.backend/README_API.md) - Guía de endpoints

---

**Última actualización**: 2026-02-03  
**API Base URL**: http://35.180.21.42:8080
