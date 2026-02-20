#!/bin/bash

echo "=========================================="
echo "  USUARIOS DE FALLAPP - Gestión de Base de Datos"
echo "=========================================="
echo ""

if ! docker ps | grep -q fallapp-postgres; then
    echo "❌ Error: El contenedor de PostgreSQL no está corriendo"
    echo "   Inicia el contenedor con: cd /srv/FallApp/05.docker && docker-compose up -d"
    exit 1
fi

echo "📊 USUARIOS REGISTRADOS EN LA BASE DE DATOS"
echo "=========================================="
echo ""

docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -A -F'|' -c \
"SELECT
    id_usuario,
    nombre_completo,
    email,
    rol,
    CASE WHEN activo THEN 'Sí' ELSE 'No' END as activo,
    TO_CHAR(fecha_registro, 'YYYY-MM-DD HH24:MI:SS') as fecha_registro,
    CASE
        WHEN ultimo_acceso IS NOT NULL
        THEN TO_CHAR(ultimo_acceso, 'YYYY-MM-DD HH24:MI:SS')
        ELSE 'Nunca'
    END as ultimo_acceso
FROM usuarios
ORDER BY id_usuario;" | while IFS='|' read -r id nombre email rol activo fecha ultimo; do
    echo "👤 Usuario #$id"
    echo "   Nombre: $nombre"
    echo "   Email: $email"
    echo "   Rol: $rol"
    echo "   Activo: $activo"
    echo "   Registrado: $fecha"
    echo "   Último acceso: $ultimo"
    echo ""
done

echo "=========================================="
echo "📊 ESTADÍSTICAS"
echo "=========================================="
TOTAL=$(docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -A -c "SELECT COUNT(*) FROM usuarios;")
ACTIVOS=$(docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -A -c "SELECT COUNT(*) FROM usuarios WHERE activo = true;")
ADMINS=$(docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -A -c "SELECT COUNT(*) FROM usuarios WHERE rol = 'admin';")

echo "Total usuarios: $TOTAL"
echo "Usuarios activos: $ACTIVOS"
echo "Administradores: $ADMINS"
echo ""

echo "=========================================="
echo "🔒 IMPORTANTE: SEGURIDAD DE CONTRASEÑAS"
echo "=========================================="
echo ""
echo "⚠️  Las contraseñas están encriptadas con BCrypt"
echo "    BCrypt es un algoritmo de HASH UNIDIRECCIONAL"
echo "    NO se pueden 'desencriptar' - esto es intencional por seguridad"
echo ""
echo "🔐 ¿Por qué no se pueden desencriptar?"
echo "    - BCrypt no es encriptación, es 'hashing'"
echo "    - Convierte la contraseña en un hash irreversible"
echo "    - Incluso con acceso a la BD no se puede obtener la contraseña original"
echo "    - Esta es una característica de seguridad, no un bug"
echo ""
echo "👥 CONTRASEÑAS CONOCIDAS (usuarios de prueba iniciales):"
echo "=========================================="
echo ""
echo "   1. admin@fallapp.es"
echo "      Contraseña: admin123"
echo "      Rol: admin"
echo "      Uso: Administración del sistema"
echo ""
echo "   2. demo@fallapp.es"
echo "      Contraseña: demo123"
echo "      Rol: usuario"
echo "      Uso: Demostración"
echo ""
echo "   3. casal@fallapp.es"
echo "      Contraseña: casal123"
echo "      Rol: casal"
echo "      Uso: Responsable de casal"
echo ""
echo "=========================================="
echo "🔧 OPERACIONES DISPONIBLES"
echo "=========================================="
echo ""
echo "✅ Probar login (verificar que funciona):"
echo "   curl -X POST http://localhost:8080/api/auth/login \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"email\":\"admin@fallapp.es\",\"contrasena\":\"admin123\"}' | jq"
echo ""
echo "➕ Crear nuevo usuario:"
echo "   curl -X POST http://localhost:8080/api/auth/registro \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"email\":\"nuevo@example.com\",\"contrasena\":\"MiPassword123\",\"nombreCompleto\":\"Usuario Nuevo\"}' | jq"
echo ""
echo "🔄 Resetear contraseña (desde PostgreSQL):"
echo "   # Primero generar hash BCrypt para nueva contraseña en: https://bcrypt-generator.com/"
echo "   # Luego actualizar:"
echo "   docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \\"
echo "     \"UPDATE usuarios SET contraseña_hash = '\$2a\$10\$HASH_GENERADO' WHERE email = 'usuario@email.com';\""
echo ""
echo "🗑️  Eliminar usuario:"
echo "   docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \\"
echo "     \"DELETE FROM usuarios WHERE email = 'usuario@eliminar.com';\""
echo ""
echo "=========================================="
