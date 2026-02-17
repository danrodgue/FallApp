#!/bin/bash

# ============================================================================
# Script: eliminar-usuario.sh
# Descripción: Elimina un usuario de la base de datos de FallApp
# Uso: bash eliminar-usuario.sh
# Configuración: Edita la variable EMAIL dentro del script (línea 27)
# ============================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración de la base de datos
DB_CONTAINER="fallapp-postgres"
DB_USER="fallapp_user"
DB_NAME="fallapp"

# ============================================================================
# CONFIGURAR AQUÍ EL EMAIL DEL USUARIO A ELIMINAR
# ============================================================================
EMAIL="fallappproyect@proton.me"
# ============================================================================

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  🗑️  Eliminar Usuario - FallApp${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

echo -e "${YELLOW}📧 Email del usuario a eliminar: ${EMAIL}${NC}"
echo ""

# Verificar que el contenedor de PostgreSQL está corriendo
if ! docker ps | grep -q "$DB_CONTAINER"; then
    echo -e "${RED}❌ Error: El contenedor de PostgreSQL no está corriendo${NC}"
    exit 1
fi

echo -e "${BLUE}1️⃣  Buscando usuario en la base de datos...${NC}"

# Buscar el usuario
QUERY="SELECT id_usuario, email, nombre_completo, rol, verificado, fecha_registro FROM usuarios WHERE email = '$EMAIL';"
RESULT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "$QUERY" 2>&1)

if [ -z "$(echo "$RESULT" | tr -d '[:space:]')" ]; then
    echo -e "${RED}❌ Usuario no encontrado con email: ${EMAIL}${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Usuario encontrado:${NC}"
echo ""
docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "$QUERY"
echo ""

# Pedir confirmación
echo -e "${YELLOW}⚠️  ¿Estás seguro de que quieres eliminar este usuario?${NC}"
echo -e "${YELLOW}   Esta acción NO se puede deshacer.${NC}"
echo ""
read -p "Escribe 'ELIMINAR' para confirmar: " CONFIRMACION

if [ "$CONFIRMACION" != "ELIMINAR" ]; then
    echo -e "${BLUE}ℹ️  Operación cancelada${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}2️⃣  Eliminando usuario...${NC}"

# Eliminar el usuario
DELETE_QUERY="DELETE FROM usuarios WHERE email = '$EMAIL';"
DELETE_RESULT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "$DELETE_QUERY" 2>&1)

if echo "$DELETE_RESULT" | grep -q "DELETE 1"; then
    echo -e "${GREEN}✅ Usuario eliminado exitosamente${NC}"
else
    echo -e "${RED}❌ Error al eliminar el usuario${NC}"
    echo "$DELETE_RESULT"
    exit 1
fi

echo ""
echo -e "${BLUE}3️⃣  Verificando eliminación...${NC}"

# Verificar que el usuario fue eliminado
VERIFY_QUERY="SELECT COUNT(*) FROM usuarios WHERE email = '$EMAIL';"
COUNT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "$VERIFY_QUERY" | tr -d ' ')

if [ "$COUNT" -eq 0 ]; then
    echo -e "${GREEN}✅ Verificación exitosa: El usuario ya no existe en la base de datos${NC}"
else
    echo -e "${RED}❌ Advertencia: El usuario todavía aparece en la base de datos${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}  ✅ Proceso completado exitosamente${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
