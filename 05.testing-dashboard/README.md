# 🎯 FallApp Testing Dashboard

Dashboard de monitoreo y testing para FallApp. Panel HTML/CSS/JS simple que se puede abrir desde cualquier navegador sin necesidad de servidor web.

## 📋 Características

### ✅ Funcionalidades Principales

- **🔐 Autenticación Admin**: Login con JWT para acceso seguro
- **🖥️ Monitor del Servidor**: Estado en tiempo real de API y base de datos
- **🔌 Estado de Endpoints**: Verificación de disponibilidad de endpoints
- **🧪 Ejecución de Tests**: Suite completa de tests (Integration, E2E, Performance)
- **📊 Resultados Detallados**: Visualización de resultados con logs
- **📋 Registro de Actividad**: Log de todas las operaciones
- **🔄 Auto-Refresh**: Actualización automática cada 30 segundos

### 🎨 Diseño

- HTML/CSS/JavaScript puro (sin frameworks)
- Estilos reutilizados del proyecto desktop (02.desktop/css/)
- Responsive design
- Animaciones fluidas
- Sin dependencias externas (excepto fuente Google Fonts)

## 🚀 Uso

### Opción 1: Abrir Directamente en Navegador

```bash
# Simplemente abre el archivo en tu navegador
xdg-open /srv/FallApp/05.testing-dashboard/index.html

# O en Windows
start /srv/FallApp/05.testing-dashboard/index.html

# O en macOS
open /srv/FallApp/05.testing-dashboard/index.html
```

### Opción 2: Usar con Python HTTP Server (Opcional)

Si prefieres servir el dashboard con un servidor local:

```bash
cd /srv/FallApp/05.testing-dashboard
python3 -m http.server 8000

# Luego abre en el navegador:
# http://localhost:8000
```

### Opción 3: Usar con VSCode Live Server (Opcional)

1. Instala la extensión "Live Server" en VSCode
2. Haz clic derecho en `index.html`
3. Selecciona "Open with Live Server"

## 🔐 Credenciales de Acceso

El dashboard requiere autenticación de administrador. Usa las credenciales de un usuario con rol `ADMIN` de tu base de datos FallApp.

**Ejemplo de credenciales por defecto:**
```
Email: admin@fallapp.com
Contraseña: [tu contraseña de admin]
```

> ⚠️ **Nota**: Asegúrate de tener al menos un usuario con rol ADMIN en la base de datos.

## ⚙️ Configuración

### Cambiar la URL de la API

Edita el archivo `js/config.js`:

```javascript
const CONFIG = {
    API_URL: 'http://localhost:8080/api',  // Cambia esto si tu API está en otro puerto/host
    // ... resto de configuración
};
```

### Ajustar Timeouts

En `js/config.js`:

```javascript
TIMEOUTS: {
    API_CHECK: 5000,        // 5 segundos - Timeout para verificar endpoints
    TEST_EXECUTION: 120000, // 2 minutos - Timeout para ejecutar tests
    AUTO_REFRESH: 30000,    // 30 segundos - Intervalo de auto-refresh
},
```

### Añadir Nuevos Endpoints

En `js/config.js`, modifica el array `API_ENDPOINTS`:

```javascript
API_ENDPOINTS: [
    { 
        method: 'GET', 
        path: '/mi-endpoint', 
        public: true, 
        description: 'Mi nuevo endpoint' 
    },
    // ... más endpoints
],
```

### Añadir Nuevos Tests

En `js/config.js`, modifica el objeto `TESTS`:

```javascript
TESTS: {
    integration: [
        { 
            id: 'mi_test', 
            name: 'Mi Nuevo Test', 
            file: 'mi_test.sql', 
            category: 'integration' 
        },
        // ... más tests
    ],
},
```

## 📁 Estructura de Archivos

```
05.testing-dashboard/
├── index.html              # Página principal del dashboard
├── css/
│   └── dashboard.css       # Estilos del dashboard
├── js/
│   ├── config.js          # Configuración (API URL, endpoints, tests)
│   ├── api.js             # Cliente API (funciones para comunicarse con backend)
│   └── dashboard.js       # Lógica principal del dashboard
└── README.md              # Este archivo
```

## 🔧 Funcionalidades Detalladas

### 1. Monitor del Servidor

- **Estado de la API**: Verifica si el backend está online
- **Estado de la BD**: Indica si la base de datos está conectada
- **Puerto**: Muestra el puerto en el que corre la API
- **Última Verificación**: Timestamp de la última comprobación

### 2. Endpoints API

- Lista todos los endpoints configurados
- Muestra método HTTP (GET, POST, PUT, DELETE)
- Indica si son públicos o requieren autenticación
- Muestra rol requerido (USUARIO, CASAL, ADMIN)
- Verificación de disponibilidad en tiempo real

### 3. Suite de Tests

#### Tests de Integración (SQL)
- Schema Creation
- Data Integrity
- Views & Functions
- Triggers
- Ubicaciones GPS

#### Tests End-to-End (Bash)
- Docker Compose
- PostgreSQL Connection
- Data Persistence
- API Ubicaciones

#### Tests de Performance
- Endpoint Ubicaciones Performance

### 4. Resultados de Tests

- **Resumen**: Contador de tests pasados/fallados/total
- **Duración**: Tiempo total de ejecución
- **Output Detallado**: Log de cada test con timestamps
- **Colores**: Verde (pass), Rojo (fail), Naranja (running)

### 5. Registro de Actividad

- Log cronológico de todas las operaciones
- Tipos: Info, Success, Error, Warning
- Timestamps precisos
- Auto-scroll al final
- Límite de 100 entradas

## 🔒 Seguridad

### Implementada

- ✅ Autenticación JWT requerida
- ✅ Token almacenado en `sessionStorage` (se borra al cerrar navegador)
- ✅ Solo usuarios con rol ADMIN pueden acceder
- ✅ Token incluido en todas las peticiones protegidas
- ✅ Logout limpia el token automáticamente

### Recomendaciones Adicionales

- 🔸 Usa HTTPS en producción
- 🔸 Configura CORS en el backend correctamente
- 🔸 Implementa rate limiting en el backend
- 🔸 Monitorea los logs de acceso
- 🔸 Usa contraseñas fuertes para cuentas admin

## 🐛 Troubleshooting

### El dashboard no se conecta a la API

1. **Verifica que el backend esté corriendo:**
   ```bash
   curl http://localhost:8080/api/health
   ```

2. **Verifica la URL en `js/config.js`:**
   ```javascript
   API_URL: 'http://localhost:8080/api'
   ```

3. **Verifica CORS en el backend:**
   El backend debe permitir peticiones desde el origen del dashboard.

### Error de autenticación

1. **Verifica que tienes un usuario ADMIN:**
   ```sql
   SELECT * FROM usuarios WHERE rol = 'ADMIN';
   ```

2. **Verifica que las credenciales son correctas**

3. **Verifica que el endpoint de login funciona:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/iniciar-sesion \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@fallapp.com","contrasena":"tu_password"}'
   ```

### Los tests no se ejecutan

> ⚠️ **Nota Importante**: La ejecución de tests desde el dashboard requiere un endpoint en el backend que actualmente **NO está implementado**.

Por ahora, los tests se simulan en el frontend. Para ejecutar tests reales, usa:

```bash
cd /srv/FallApp/06.tests
bash run_tests.sh
```

### El dashboard no carga estilos

Asegúrate de que la estructura de carpetas es correcta:
```
05.testing-dashboard/
├── index.html
├── css/
│   └── dashboard.css
└── js/
    ├── config.js
    ├── api.js
    └── dashboard.js
```

## 📊 Próximas Mejoras

### Backend

- [ ] Endpoint `/admin/tests/run` para ejecutar tests reales
- [ ] Endpoint `/admin/tests/results` para obtener resultados históricos
- [ ] Endpoint `/admin/system/metrics` para métricas del sistema
- [ ] Webhook para notificaciones de tests fallidos

### Frontend

- [ ] Gráficos de tendencias de tests
- [ ] Historial de ejecuciones
- [ ] Exportar resultados a PDF/JSON
- [ ] Modo oscuro
- [ ] Notificaciones de escritorio
- [ ] Filtros y búsqueda en logs

## 📝 Notas

- El dashboard está diseñado para ser simple y portable
- No requiere compilación ni dependencias npm
- Se puede copiar a cualquier servidor web estático
- Compatible con todos los navegadores modernos
- El token JWT se almacena en `sessionStorage` por seguridad

## 🔗 Enlaces Relacionados

- [Documentación API REST](../04.docs/especificaciones/04.API-REST.md)
- [Guía de Tests](../06.tests/README.md)
- [Guía de Programación](../04.docs/01.GUIA-PROGRAMACION.md)

## 📞 Soporte

Si encuentras problemas o tienes sugerencias, contacta al equipo de desarrollo.

---

**FallApp Testing Dashboard v1.0.0** | © 2026
