# 🔐 Guía Práctica: Probar Autenticación API - FallApp

**Fecha Creación:** 2026-02-03  
**Última Actualización:** 2026-02-03 (Validación BCrypt)  
**Versión:** 0.5.3  
**URL Base:** http://localhost:8080 (o http://35.180.21.42:8080)  
**Estado:** ✅ Sistema JWT y BCrypt completamente funcional

---

## 📋 Índice Rápido

1. [Conceptos Básicos de Autenticación](#conceptos-básicos)
2. [Paso 1: Registro de Usuario](#paso-1-registro)
3. [Paso 2: Login (Obtener Token)](#paso-2-login)
4. [Paso 3: Usar Token en Requests](#paso-3-usar-token)
5. [Ejemplos de Endpoints Protegidos](#ejemplos-protegidos)
6. [Script Automatizado de Pruebas](#script-automatizado)
7. [Troubleshooting](#troubleshooting)

---

## 🎓 Conceptos Básicos

### ¿Qué es JWT?
**JWT (JSON Web Token)** es un token de autenticación que se genera cuando haces login. Este token:
- ✅ Dura **24 horas** (86400 segundos)
- ✅ Debe incluirse en el header `Authorization: Bearer TOKEN`
- ✅ Permite acceder a endpoints protegidos
- ✅ **Algoritmo**: HS512 (validado 2026-02-03)

### Seguridad de Contraseñas
- ✅ **BCrypt**: Hashing unidireccional (no encriptación reversible)
- ✅ **Validado**: Sistema operativo desde 2026-02-03
- ✅ **Backend**: Recompilado con Java 17 y reiniciado
- ✅ No se almacenan contraseñas en texto plano

### Niveles de Acceso

| Nivel | Descripción | Endpoints |
|-------|-------------|-----------|
| **🌐 PÚBLICO** | Sin token | GET (browse), /auth/registro, /auth/login |
| **🔐 AUTENTICADO** | Con token | POST/PUT fallas, eventos, ninots, comentarios |
| **👑 ADMIN** | Token + rol ADMIN | DELETE (eliminar recursos) |

---

## 📝 Paso 1: Registro

### Opción A: Con cURL (Terminal)

```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "email": "miusuario@example.com",
    "contrasena": "password123",
    "nombreCompleto": "Juan Pérez",
    "idFalla": 95
  }' | jq
```

### Opción B: Con HTTPie (más fácil)

```bash
# Instalar HTTPie si no lo tienes: sudo apt install httpie
http POST localhost:8080/api/auth/registro \
  email=miusuario@example.com \
  contrasena=password123 \
  nombreCompleto="Juan Pérez" \
  idFalla:=95
```

### Respuesta Esperada

```json
{
  "exito": true,
  "mensaje": "Usuario registrado exitosamente",
  "datos": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA...",
    "tipo": "Bearer",
    "expiraEn": 86400,
    "usuario": {
      "idUsuario": 10,
      "email": "miusuario@example.com",
      "nombreCompleto": "Juan Pérez",
      "rol": "usuario"
    }
  }
}
```

**⚠️ IMPORTANTE:** Guarda el `token` que te devuelve, lo necesitarás para el siguiente paso.

---

## 🔑 Paso 2: Login

Si ya tienes un usuario registrado, puedes hacer login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "miusuario@example.com",
    "contrasena": "password123"
  }' | jq
```

### Guardar Token Automáticamente (Bash)

```bash
# Guardar token en variable de entorno
export TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "miusuario@example.com",
    "contrasena": "password123"
  }' | jq -r '.datos.token')

# Verificar que se guardó
echo "Token: $TOKEN"
```

---

## 🚀 Paso 3: Usar Token en Requests

Una vez que tienes el token, debes incluirlo en el header `Authorization`:

### Formato del Header

```
Authorization: Bearer TU_TOKEN_AQUI
```

### Ejemplo con cURL

```bash
curl -X POST http://localhost:8080/api/fallas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Falla Prueba",
    "seccion": "9Z",
    "presidente": "Test User",
    "anyoFundacion": 2020,
    "latitud": 39.47,
    "longitud": -0.38
  }' | jq
```

### Ejemplo con HTTPie

```bash
http POST localhost:8080/api/fallas \
  "Authorization: Bearer $TOKEN" \
  nombre="Falla Prueba" \
  seccion=9Z \
  presidente="Test User" \
  anyoFundacion:=2020 \
  latitud:=39.47 \
  longitud:=-0.38
```

---

## 🔐 Ejemplos de Endpoints Protegidos

### 1. Crear Falla (Autenticado)

```bash
curl -X POST http://localhost:8080/api/fallas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Falla Test API",
    "seccion": "9Z",
    "presidente": "Test User",
    "anyoFundacion": 2025,
    "latitud": 39.47,
    "longitud": -0.38,
    "categoria": "tercera"
  }' | jq
```

### 2. Actualizar Falla (Autenticado)

```bash
curl -X PUT http://localhost:8080/api/fallas/95 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Falla Actualizada",
    "seccion": "1A",
    "presidente": "Nuevo Presidente",
    "anyoFundacion": 1942
  }' | jq
```

### 3. Crear Evento (Autenticado)

```bash
curl -X POST http://localhost:8080/api/eventos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "idFalla": 95,
    "tipo": "PAELLA",
    "nombre": "Paella Popular",
    "descripcion": "Paella para todos los falleros",
    "fechaEvento": "2026-03-19T13:00:00",
    "ubicacion": "Plaza del casal",
    "participantesEstimado": 200
  }' | jq
```

### 4. Votar por Ninot (Autenticado)

```bash
curl -X POST http://localhost:8080/api/votos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "idNinot": 1,
    "tipoVoto": "ARTISTICO"
  }' | jq
```

### 5. Crear Comentario (Autenticado)

```bash
# Necesitas primero tu idUsuario (lo obtienes del login)
export USER_ID=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "miusuario@example.com", "contrasena": "password123"}' \
  | jq -r '.datos.usuario.idUsuario')

curl -X POST http://localhost:8080/api/comentarios \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "idUsuario": '$USER_ID',
    "idFalla": 95,
    "contenido": "¡Excelente falla! Felicidades al casal"
  }' | jq
```

### 6. Eliminar Falla (Solo ADMIN)

```bash
# Este endpoint requiere rol ADMIN
curl -X DELETE http://localhost:8080/api/fallas/999 \
  -H "Authorization: Bearer $TOKEN" \
  | jq
```

---

## 🤖 Script Automatizado de Pruebas

He creado un script que prueba todo automáticamente:

```bash
# Ejecutar script de pruebas completo
bash /srv/FallApp/06.tests/e2e/test_api_auth.sh
```

O prueba manualmente con este script inline:

```bash
#!/bin/bash
echo "🔐 PRUEBA COMPLETA DE AUTENTICACIÓN"
echo "=================================="

# 1. Registro
echo ""
echo "1️⃣ Registrando nuevo usuario..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test_'$(date +%s)'@example.com",
    "contrasena": "password123",
    "nombreCompleto": "Usuario Test",
    "idFalla": 95
  }')

echo "$REGISTER_RESPONSE" | jq

# 2. Extraer token
TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.datos.token')
USER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.datos.usuario.idUsuario')

if [ "$TOKEN" != "null" ]; then
  echo "✅ Token obtenido: ${TOKEN:0:50}..."
  echo "✅ User ID: $USER_ID"
else
  echo "❌ Error: No se pudo obtener el token"
  exit 1
fi

# 3. Probar endpoint autenticado
echo ""
echo "2️⃣ Probando crear falla (autenticado)..."
curl -s -X POST http://localhost:8080/api/fallas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Falla Test '$(date +%H%M%S)'",
    "seccion": "9Z",
    "presidente": "Test User",
    "anyoFundacion": 2025,
    "latitud": 39.47,
    "longitud": -0.38
  }' | jq

# 4. Probar endpoint sin token (debe fallar)
echo ""
echo "3️⃣ Probando sin token (debe fallar con 401)..."
curl -s -X POST http://localhost:8080/api/fallas \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test"}' | jq

# 5. Probar votar
echo ""
echo "4️⃣ Probando votar por ninot..."
curl -s -X POST http://localhost:8080/api/votos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "idNinot": 1,
    "tipoVoto": "ARTISTICO"
  }' | jq

echo ""
echo "✅ PRUEBAS COMPLETADAS"
```

---

## 🛠️ Troubleshooting

### Error: 401 Unauthorized

**Causa:** Token inválido, expirado o no incluido

**Solución:**
```bash
# 1. Verifica que el token esté guardado
echo $TOKEN

# 2. Si está vacío, haz login de nuevo
export TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "tu@email.com", "contrasena": "tupassword"}' \
  | jq -r '.datos.token')

# 3. Verifica el formato del header
curl -v http://localhost:8080/api/fallas \
  -H "Authorization: Bearer $TOKEN"
```

### Error: 403 Forbidden

**Causa:** Token válido pero sin permisos (ej: intentas DELETE sin ser ADMIN)

**Solución:** Este endpoint requiere rol ADMIN, usa endpoints de usuario normal.

### Error: 400 Bad Request

**Causa:** Datos inválidos en el request

**Solución:** Verifica que todos los campos requeridos estén presentes:
```bash
# Ver detalles del error
curl -s -X POST http://localhost:8080/api/fallas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test"}' | jq
```

### Error: Token expiró

**Causa:** El token tiene 24h de validez

**Solución:**
```bash
# Hacer login de nuevo
export TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "tu@email.com", "contrasena": "tupassword"}' \
  | jq -r '.datos.token')
```

### Error: Email ya existe

**Causa:** Ya hay un usuario registrado con ese email

**Solución:** Usa un email diferente o haz login con el existente.

---

## 🧪 Verificar Estado de la API

```bash
# 1. Health check
curl http://localhost:8080/actuator/health

# 2. Ver endpoints disponibles (Swagger)
xdg-open http://localhost:8080/swagger-ui.html

# 3. Ver estadísticas
curl http://localhost:8080/api/estadisticas/resumen | jq
```

---

## 📚 Endpoints Públicos (Sin Token)

Estos endpoints **NO requieren** autenticación:

```bash
# Listar fallas
curl http://localhost:8080/api/fallas | jq

# Ver falla específica
curl http://localhost:8080/api/fallas/95 | jq

# Ubicación GPS
curl http://localhost:8080/api/fallas/95/ubicacion | jq

# Buscar fallas
curl "http://localhost:8080/api/fallas/buscar?texto=convento" | jq

# Eventos futuros
curl http://localhost:8080/api/eventos/futuros | jq

# Estadísticas
curl http://localhost:8080/api/estadisticas/resumen | jq
```

---

## 🎯 Resumen Rápido

### Flujo Básico

1. **Registrarse:**
   ```bash
   curl -X POST localhost:8080/api/auth/registro -H "Content-Type: application/json" -d '{"email":"tu@email.com","contrasena":"pass123","nombreCompleto":"Tu Nombre"}'
   ```

2. **Guardar Token:**
   ```bash
   export TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"tu@email.com","contrasena":"pass123"}' | jq -r '.datos.token')
   ```

3. **Usar Token:**
   ```bash
   curl -X POST localhost:8080/api/fallas -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{...}'
   ```

### Comandos Útiles

```bash
# Ver tu token actual
echo $TOKEN

# Verificar si el token es válido
curl -X GET localhost:8080/api/usuarios \
  -H "Authorization: Bearer $TOKEN" | jq

# Limpiar token (logout)
unset TOKEN
```

---

## 📞 Ayuda Adicional

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs JSON:** http://localhost:8080/v3/api-docs
- **Guía Completa:** [GUIA.API.FRONTEND.md](GUIA.API.FRONTEND.md)
- **Tests Automatizados:** `bash 06.tests/e2e/test_api_auth.sh`

---

**Última actualización:** 2026-02-03  
**Versión:** 0.5.2  
**¿Preguntas?** Revisa [GUIA.API.FRONTEND.md](GUIA.API.FRONTEND.md) para más ejemplos
