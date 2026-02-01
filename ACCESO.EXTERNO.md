# Acceso Externo a la API - FallApp

**Fecha configuración**: 2026-02-01  
**IP Pública EC2**: 35.180.21.42  
**Puerto API**: 8080

---

## ✅ Configuración Completada

### 1. Spring Boot
- [x] **server.address=0.0.0.0** configurado en application.properties
- [x] **CORS** actualizado con allowedOriginPatterns("*") para desarrollo
- [x] **Aplicación escuchando** en todas las interfaces (verificado con netstat)

### 2. Firewall Local (UFW)
- [x] **Estado**: Inactivo (no requiere configuración)

---

## 🔧 Configuración Pendiente en AWS

### Security Group - Abrir Puerto 8080

**IMPORTANTE**: Necesitas configurar el Security Group en AWS Console para permitir acceso al puerto 8080.

#### Paso a Paso:

1. **Accede a AWS Console**
   - Ir a: https://console.aws.amazon.com/ec2/

2. **Navega a Security Groups**
   - En el panel izquierdo → **Network & Security** → **Security Groups**
   - Encuentra el security group asociado a tu instancia (probablemente `sg-xxxxx`)

3. **Editar Inbound Rules**
   - Selecciona tu security group
   - Click en **Inbound rules** (pestaña)
   - Click en **Edit inbound rules**

4. **Añadir Regla Nueva**
   - Click **Add rule**
   - Configuración:
     ```
     Type:        Custom TCP
     Port range:  8080
     Source:      0.0.0.0/0  (para acceso desde cualquier IP)
     Description: FallApp API Backend
     ```
   
5. **Guardar cambios**
   - Click **Save rules**

#### Regla Recomendada (Más Segura)

Si solo necesitas acceso desde IPs específicas:
```
Type:        Custom TCP
Port range:  8080
Source:      TU_IP/32  (ejemplo: 185.123.45.67/32)
Description: FallApp API - Solo mi IP
```

Para encontrar tu IP: https://www.whatismyip.com/

---

## 🧪 Pruebas de Acceso

### Desde el Servidor (Local)
```bash
curl -s http://localhost:8080/api/estadisticas/resumen | jq .
```

### Desde Internet (Una vez configurado Security Group)
```bash
curl -s http://35.180.21.42:8080/api/estadisticas/resumen | jq .
```

### Desde Desktop App (Electron)
```javascript
const API_URL = 'http://35.180.21.42:8080';

fetch(`${API_URL}/api/estadisticas/resumen`)
  .then(res => res.json())
  .then(data => console.log(data));
```

### Desde App Móvil (Android)
```kotlin
val apiUrl = "http://35.180.21.42:8080"

// AndroidManifest.xml - Permitir cleartext HTTP
<application
    android:usesCleartextTraffic="true">
```

---

## 📱 URLs de la API

**Base URL**: `http://35.180.21.42:8080`

### Endpoints Públicos (Sin autenticación)
- GET http://35.180.21.42:8080/api/fallas
- GET http://35.180.21.42:8080/api/eventos
- GET http://35.180.21.42:8080/api/ninots
- GET http://35.180.21.42:8080/api/estadisticas/resumen
- GET http://35.180.21.42:8080/swagger-ui.html

### Autenticación
```bash
# Login
curl -X POST http://35.180.21.42:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fallapp.es","contrasena":"Admin2026!"}'

# Respuesta (copiar el token)
{
  "exito": true,
  "mensaje": "Login exitoso",
  "datos": {
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

### Endpoints Protegidos (Con JWT)
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# Crear falla
curl -X POST http://35.180.21.42:8080/api/fallas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Falla Convento Jerusalén",
    "seccion": "Especial",
    "presidente": "Juan García"
  }'
```

---

## 🔒 Seguridad

### Configuración Actual (Desarrollo)
- ✅ JWT authentication implementado
- ✅ CORS permitiendo todos los orígenes (`allowedOriginPatterns: "*"`)
- ✅ HTTP sin SSL (no apto para producción)

### Para Producción
- [ ] Configurar HTTPS con certificado SSL
- [ ] Restringir CORS a dominios específicos
- [ ] Configurar rate limiting
- [ ] Implementar API key adicional
- [ ] Considerar usar Nginx como reverse proxy

---

## 📊 Verificación de Configuración

### Check 1: Spring Boot escuchando en 0.0.0.0
```bash
netstat -tlnp | grep :8080
# Debe mostrar: *:8080 (no 127.0.0.1:8080)
```
**Estado**: ✅ CORRECTO

### Check 2: Firewall local
```bash
sudo ufw status
```
**Estado**: ✅ Inactivo (no bloquea)

### Check 3: Security Group AWS
**Estado**: ⏳ PENDIENTE DE CONFIGURAR

### Check 4: Aplicación corriendo
```bash
curl http://localhost:8080/actuator/health
```
**Estado**: ✅ {"status":"UP"}

---

## 🐛 Troubleshooting

### Error: "Connection refused"
- Verificar que la aplicación está corriendo: `ps aux | grep spring-boot`
- Verificar puerto: `netstat -tlnp | grep 8080`
- Revisar logs: `tail -100 /tmp/spring-boot.log`

### Error: "Connection timeout"
- Verificar Security Group en AWS (puerto 8080 abierto)
- Verificar IP pública: `curl http://checkip.amazonaws.com`

### Error: "CORS policy blocked"
- Verificar SecurityConfig.java tiene `allowedOriginPatterns("*")`
- Verificar headers en request incluyen `Origin`

### Error: "403 Forbidden"
- Endpoint requiere JWT token
- Obtener token con POST /api/auth/login
- Incluir header: `Authorization: Bearer {token}`

---

## 📞 Información de Contacto

**Servidor**: Amazon EC2  
**Región**: eu-west-3 (Paris)  
**IP Pública**: 35.180.21.42  
**IP Privada**: 172.31.3.84  
**Puerto**: 8080  
**Protocolo**: HTTP (desarrollo)

---

## 🚀 Próximos Pasos

1. **Inmediato**: Configurar Security Group en AWS
2. **Esta semana**: Probar desde desktop y móvil
3. **Futuro**: Implementar HTTPS con Let's Encrypt + Nginx

---

**Última actualización**: 2026-02-01  
**Mantenido por**: Backend Development Team
