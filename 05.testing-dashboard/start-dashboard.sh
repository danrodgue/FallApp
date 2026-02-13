#!/bin/bash
# =====================================================
# Script para verificar e iniciar el entorno necesario
# para el Testing Dashboard
# =====================================================

echo "🔍 Verificando entorno para FallApp Testing Dashboard..."
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 1. Verificar que Docker está corriendo
echo -e "${BLUE}[1/4]${NC} Verificando Docker..."
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}✗${NC} Docker no está corriendo"
    echo "   Inicia Docker primero: sudo systemctl start docker"
    exit 1
else
    echo -e "${GREEN}✓${NC} Docker está corriendo"
fi

# 2. Verificar que el contenedor PostgreSQL está activo
echo -e "${BLUE}[2/4]${NC} Verificando PostgreSQL..."
if ! docker ps | grep -q fallapp-postgres; then
    echo -e "${YELLOW}⚠${NC} PostgreSQL no está corriendo"
    echo "   Iniciando PostgreSQL..."
    cd /srv/FallApp/05.docker
    sudo docker-compose up -d postgres
    sleep 5
    if docker ps | grep -q fallapp-postgres; then
        echo -e "${GREEN}✓${NC} PostgreSQL iniciado"
    else
        echo -e "${RED}✗${NC} Error al iniciar PostgreSQL"
        exit 1
    fi
else
    echo -e "${GREEN}✓${NC} PostgreSQL está corriendo"
fi

# 3. Verificar que el backend API está corriendo
echo -e "${BLUE}[3/4]${NC} Verificando Backend API..."
if ! curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠${NC} Backend API no está corriendo"
    echo "   Inicia el backend desde: /srv/FallApp/01.backend"
    echo "   Comando: ./mvnw spring-boot:run"
    echo ""
    echo -e "${YELLOW}¿Deseas que intente iniciarlo ahora? (y/n)${NC}"
    read -r response
    if [[ "$response" == "y" ]]; then
        echo "   Iniciando backend en background..."
        cd /srv/FallApp/01.backend
        ./mvnw spring-boot:run > /tmp/fallapp-backend.log 2>&1 &
        echo "   Esperando 30 segundos para que arranque..."
        sleep 30
        if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} Backend API iniciado"
        else
            echo -e "${RED}✗${NC} Error al iniciar Backend API"
            echo "   Revisa el log: tail -f /tmp/fallapp-backend.log"
            exit 1
        fi
    else
        echo "   Inicia el backend manualmente antes de usar el dashboard"
        exit 1
    fi
else
    echo -e "${GREEN}✓${NC} Backend API está corriendo"
fi

# 4. Verificar que existe al menos un usuario ADMIN
echo -e "${BLUE}[4/4]${NC} Verificando usuario ADMIN..."
ADMIN_COUNT=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN';" 2>/dev/null | tr -d ' ')

if [ "$ADMIN_COUNT" == "0" ] || [ -z "$ADMIN_COUNT" ]; then
    echo -e "${YELLOW}⚠${NC} No existe ningún usuario ADMIN"
    echo ""
    echo "   Para crear un usuario admin, ejecuta:"
    echo "   cd /srv/FallApp/05.testing-dashboard"
    echo "   bash create-admin.sh"
    echo ""
else
    echo -e "${GREEN}✓${NC} Existen $ADMIN_COUNT usuario(s) ADMIN"
fi

echo ""
echo -e "${GREEN}✅ Verificación completada${NC}"
echo ""
echo "🚀 Para abrir el dashboard:"
echo "   1. Abre tu navegador"
echo "   2. Navega a: file:///srv/FallApp/05.testing-dashboard/index.html"
echo "   O ejecuta: xdg-open /srv/FallApp/05.testing-dashboard/index.html"
echo ""
echo "🔐 Credenciales:"
echo "   Usa el email y contraseña de un usuario ADMIN de tu base de datos"
echo ""
