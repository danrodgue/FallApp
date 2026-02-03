# Gestión de Usuarios de la Base de Datos

## 📋 Descripción General

Este documento describe cómo gestionar los usuarios de FallApp desde el servidor, incluyendo la visualización, creación, modificación y eliminación de cuentas de usuario.

## 🔐 Seguridad de Contraseñas

### ¿Por qué no se pueden "desencriptar" las contraseñas?

Las contraseñas en FallApp están protegidas con **BCrypt**, un algoritmo de **hashing unidireccional**:

- **NO es encriptación**: La encriptación es reversible, el hashing NO
- **Irreversible**: No existe forma matemática de obtener la contraseña original desde el hash
- **Por diseño**: Esta es una característica de seguridad intencional, no un error
- **Incluso con acceso a la BD**: Ni siquiera con acceso directo a PostgreSQL se puede recuperar la contraseña

### ¿Cómo funciona BCrypt?

```
Contraseña: "admin123"
        ↓
   BCrypt Hash
        ↓
"$2a$10$nOUIs5kJ7naTuTFkBy1veuK0kSxUFXfuaOKdOLAQ"

❌ No existe operación inversa
```

Cada vez que un usuario hace login:
1. Ingresa su contraseña (ej: "admin123")
2. El sistema la convierte a hash con BCrypt
3. Compara el nuevo hash con el almacenado en la BD
4. Si coinciden, el login es exitoso

## 🛠️ Script de Gestión de Usuarios

### Instalación

El script ya está instalado en el sistema:

```bash
# Ubicación del script
/srv/FallApp/fallapp-users.sh

# Enlace simbólico para ejecución rápida
/usr/local/bin/fallapp-users
```

### Uso Básico

```bash
# Ejecutar desde cualquier ubicación
fallapp-users

# O con la ruta completa
/srv/FallApp/fallapp-users.sh
```

### Salida del Script

El script muestra:

1. **Lista de usuarios**: ID, nombre, email, rol, estado activo, fechas
2. **Estadísticas**: Total de usuarios, usuarios activos, administradores
3. **Contraseñas conocidas**: Usuarios de prueba predefinidos
4. **Operaciones disponibles**: Comandos para crear, modificar y eliminar usuarios
5. **Explicación de seguridad**: Por qué las contraseñas no se pueden desencriptar

## 👥 Usuarios Predefinidos

### Usuarios de Prueba Iniciales

| Email | Contraseña | Rol | Propósito |
|-------|-----------|-----|-----------|
| admin@fallapp.es | admin123 | admin | Administración del sistema |
| demo@fallapp.es | demo123 | usuario | Demostración |
| casal@fallapp.es | casal123 | casal | Responsable de casal |

⚠️ **IMPORTANTE**: Estas son las únicas contraseñas conocidas del sistema. Para cualquier otro usuario, deberás conocer la contraseña que se utilizó al crearlo.

## 🔧 Operaciones con Usuarios

### 1. Ver Usuarios (Script)

```bash
fallapp-users
```

### 2. Ver Usuarios (PostgreSQL Directo)

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "SELECT id_usuario, nombre_completo, email, rol, activo FROM usuarios;"
```

### 3. Probar Login (Verificar Credenciales)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@fallapp.es","contrasena":"admin123"}' | jq
```

**Respuesta exitosa:**
```json
{
  "exito": true,
  "mensaje": "Login exitoso",
  "datos": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "usuario": {
      "idUsuario": 1,
      "email": "admin@fallapp.es",
      "nombreCompleto": "Administrador del Sistema",
      "rol": "admin"
    }
  }
}
```

### 4. Crear Nuevo Usuario (API)

```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H 'Content-Type: application/json' \
  -d '{
    "email":"nuevo@example.com",
    "contrasena":"MiPassword123",
    "nombreCompleto":"Usuario Nuevo",
    "rol":"usuario"
  }' | jq
```

**Nota**: Guarda la contraseña en un lugar seguro porque **no podrás recuperarla después**.

### 5. Resetear Contraseña

Si un usuario olvida su contraseña, debes generar un nuevo hash BCrypt:

#### Paso 1: Generar Hash BCrypt

Visita: https://bcrypt-generator.com/
- Ingresa la nueva contraseña
- Usa cost factor: 10 (default de Spring Security)
- Copia el hash generado (comenzará con `$2a$10$`)

#### Paso 2: Actualizar en Base de Datos

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "UPDATE usuarios 
   SET contraseña_hash = '\$2a\$10\$HASH_AQUI' 
   WHERE email = 'usuario@email.com';"
```

**Ejemplo real:**
```bash
# Nueva contraseña: "nuevaPass123"
# Hash generado: $2a$10$nOUIs5kJ7naTuTFkBy1veuK0kSxUFXfuaOKdOLAQ/lJjiVtmjT

docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "UPDATE usuarios 
   SET contraseña_hash = '\$2a\$10\$nOUIs5kJ7naTuTFkBy1veuK0kSxUFXfuaOKdOLAQ/lJjiVtmjT' 
   WHERE email = 'demo@fallapp.es';"
```

### 6. Desactivar Usuario (Sin Eliminar)

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "UPDATE usuarios SET activo = false WHERE email = 'usuario@desactivar.com';"
```

### 7. Reactivar Usuario

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "UPDATE usuarios SET activo = true WHERE email = 'usuario@reactivar.com';"
```

### 8. Eliminar Usuario Permanentemente

⚠️ **CUIDADO**: Esta operación es irreversible.

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "DELETE FROM usuarios WHERE email = 'usuario@eliminar.com';"
```

### 9. Cambiar Rol de Usuario

```bash
# Promover a admin
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "UPDATE usuarios SET rol = 'admin' WHERE email = 'usuario@email.com';"

# Degradar a usuario
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "UPDATE usuarios SET rol = 'usuario' WHERE email = 'admin@email.com';"
```

**Roles disponibles:**
- `admin`: Administrador del sistema
- `casal`: Responsable de casal
- `usuario`: Usuario normal

## 📊 Consultas Útiles

### Usuarios activos por rol

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "SELECT rol, COUNT(*) as total 
   FROM usuarios 
   WHERE activo = true 
   GROUP BY rol;"
```

### Usuarios que nunca han accedido

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "SELECT email, nombre_completo, fecha_registro 
   FROM usuarios 
   WHERE ultimo_acceso IS NULL 
   ORDER BY fecha_registro DESC;"
```

### Últimos usuarios registrados

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "SELECT email, nombre_completo, fecha_registro 
   FROM usuarios 
   ORDER BY fecha_registro DESC 
   LIMIT 10;"
```

### Usuarios con más actividad reciente

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "SELECT email, nombre_completo, ultimo_acceso 
   FROM usuarios 
   WHERE ultimo_acceso IS NOT NULL 
   ORDER BY ultimo_acceso DESC 
   LIMIT 10;"
```

## 🔍 Troubleshooting

### Error: "El contenedor de PostgreSQL no está corriendo"

```bash
# Iniciar PostgreSQL
cd /srv/FallApp/05.docker
docker-compose up -d

# Verificar que esté corriendo
docker ps | grep postgres
```

### Error al crear usuario: "Email ya existe"

El email debe ser único. Verifica si ya existe:

```bash
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
  "SELECT email FROM usuarios WHERE email = 'email@buscar.com';"
```

### Error de login: "Credenciales inválidas"

Posibles causas:
1. Contraseña incorrecta (verifica con las contraseñas conocidas)
2. Usuario inactivo (verifica con `fallapp-users`)
3. Email incorrecto (usa el email exacto con @fallapp.es, no .com)

### No puedo acceder a PostgreSQL directamente

El acceso directo está protegido. Usa siempre:

```bash
# ✅ CORRECTO
docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "SELECT..."

# ❌ INCORRECTO
psql -h localhost -U fallapp_user -d fallapp
```

## 🔗 Documentación Relacionada

- [Servicio Systemd](./SERVICIO-SYSTEMD.md) - Gestión del backend
- [API REST](/01.backend/README_API.md) - Documentación de endpoints
- [Base de Datos](/04.docs/especificaciones/03.BASE-DATOS.md) - Estructura de tablas

## 📝 Notas de Seguridad

1. **Nunca compartas las contraseñas de los usuarios de prueba en producción**
2. **Cambia las contraseñas predefinidas en un entorno de producción real**
3. **Genera hashes BCrypt fuertes** (minimum 10 rounds)
4. **Documenta las contraseñas de usuarios de prueba** en un lugar seguro
5. **No intentes "recuperar" contraseñas** - resetéalas en su lugar
6. **Las contraseñas en logs o backups siguen siendo seguras** porque están hasheadas

---

**Última actualización**: 2026-02-01  
**Versión del documento**: 1.0  
**Autor**: Sistema de documentación FallApp
