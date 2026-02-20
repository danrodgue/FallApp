#!/bin/bash


set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

DB_CONTAINER="fallapp-postgres"
DB_USER="fallapp_user"
DB_NAME="fallapp"

EMAIL="fallappproyect@proton.me"

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  👤 Cambiar Rol a CASAL - FallApp${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

echo -e "${YELLOW}📧 Email del usuario a actualizar: ${EMAIL}${NC}"
echo ""

if ! docker ps | grep -q "$DB_CONTAINER"; then
    echo -e "${RED}❌ Error: El contenedor de PostgreSQL no está corriendo${NC}"
    exit 1
fi

echo -e "${BLUE}1️⃣  Buscando usuario en la base de datos...${NC}"

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

echo -e "${YELLOW}⚠️  ¿Estás seguro de que quieres cambiar el rol de este usuario a CASAL?${NC}"
echo ""
read -p "Escribe 'CASAL' para confirmar: " CONFIRMACION

if [ "$CONFIRMACION" != "CASAL" ]; then
    echo -e "${BLUE}ℹ️  Operación cancelada${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}2️⃣  Actualizando rol a CASAL...${NC}"

UPDATE_QUERY="UPDATE usuarios SET rol = 'CASAL' WHERE email = '$EMAIL';"
UPDATE_RESULT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "$UPDATE_QUERY" 2>&1)

if echo "$UPDATE_RESULT" | grep -q "UPDATE 1"; then
    echo -e "${GREEN}✅ Rol actualizado exitosamente a CASAL${NC}"
else
    echo -e "${RED}❌ Error al actualizar el rol del usuario${NC}"
    echo "$UPDATE_RESULT"
    exit 1
fi

echo ""
echo -e "${BLUE}3️⃣  Verificando cambio de rol...${NC}"

VERIFY_QUERY="SELECT email, rol FROM usuarios WHERE email = '$EMAIL';"
VERIFY_RESULT=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c "$VERIFY_QUERY")

if echo "$VERIFY_RESULT" | grep -q "CASAL"; then
    echo -e "${GREEN}✅ Verificación exitosa: El usuario ahora tiene rol CASAL${NC}"
    echo ""
    docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "$VERIFY_QUERY"
else
    echo -e "${RED}❌ Advertencia: No se pudo verificar el cambio de rol${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}  ✅ Proceso completado exitosamente${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
