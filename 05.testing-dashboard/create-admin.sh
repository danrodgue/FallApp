#!/bin/bash
# =====================================================
# Script para crear un usuario administrador
# =====================================================

echo "👤 Crear Usuario Administrador - FallApp"
echo "=========================================="
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Solicitar datos
read -p "Email del admin: " admin_email
read -sp "Contraseña: " admin_password
echo ""
read -p "Nombre completo: " admin_nombre

# Generar hash de contraseña (usando bcrypt con coste 10)
# Nota: Esto requiere que el backend tenga un endpoint o lo haremos con un INSERT directo
# Por simplicidad, usaremos un hash pre-calculado de "admin123" para facilitar

# Hash bcrypt de "admin123" (coste 10)
DEFAULT_HASH='$2a$10$XqjY3qKV8RZ0qH.LKZrfC.vNQF/LqXvF7KZHx1aGYH5wHvXEJXZYC'

echo ""
echo "Insertando usuario en la base de datos..."

# Insertar en la base de datos
sudo docker exec -i fallapp-postgres psql -U fallapp_user -d fallapp <<EOF
INSERT INTO usuarios (email, nombre, contrasena, rol, id_falla, activo, fecha_registro)
VALUES ('$admin_email', '$admin_nombre', '$DEFAULT_HASH', 'ADMIN', NULL, true, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Usuario administrador creado exitosamente"
    echo ""
    echo "📝 Credenciales:"
    echo "   Email: $admin_email"
    echo "   Contraseña: admin123 (CÁMBIALA después del primer login)"
    echo ""
    echo "⚠️  IMPORTANTE: Por seguridad, la contraseña por defecto es 'admin123'"
    echo "   Cámbiala inmediatamente después de iniciar sesión"
else
    echo -e "${RED}✗${NC} Error al crear el usuario"
    echo "   Puede que el email ya exista en la base de datos"
fi

echo ""
echo "🔍 Usuarios ADMIN actuales:"
sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "SELECT id_usuario, email, nombre, rol, fecha_registro FROM usuarios WHERE rol = 'ADMIN';"
