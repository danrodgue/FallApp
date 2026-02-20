#!/bin/bash

set -e

echo "========================================="
echo "TEST E2E: Docker Compose"
echo "========================================="
echo ""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PROJECT_DIR="/srv/FallApp/05.docker"
cd "$PROJECT_DIR"

echo "Test 1: docker-compose.yml existe"
if [ -f "docker-compose.yml" ]; then
    echo -e "${GREEN}PASS${NC} | docker-compose.yml encontrado"
else
    echo -e "${RED}FAIL${NC} | docker-compose.yml no encontrado"
    exit 1
fi

echo ""
echo "Test 2: Archivo .env"
if [ ! -f ".env" ] && [ -f ".env.example" ]; then
    echo -e "${YELLOW}WARN${NC} | .env no existe, copiando desde .env.example"
    cp .env.example .env
fi

if [ -f ".env" ]; then
    echo -e "${GREEN}PASS${NC} | .env configurado"
else
    echo -e "${RED}FAIL${NC} | .env no encontrado"
    exit 1
fi

echo ""
echo "Test 3: Docker Compose down (limpieza)"
sudo docker-compose down > /dev/null 2>&1 || true
echo -e "${GREEN}PASS${NC} | Limpieza completada"

echo ""
echo "Test 4: Docker Compose up postgres"
sudo docker-compose up -d postgres
sleep 5  # Esperar a que inicie

if sudo docker-compose ps | grep -q "fallapp-postgres.*Up"; then
    echo -e "${GREEN}PASS${NC} | PostgreSQL iniciado correctamente"
else
    echo -e "${RED}FAIL${NC} | PostgreSQL no inició"
    sudo docker-compose logs postgres
    exit 1
fi

echo ""
echo "Test 5: Docker Compose up pgAdmin"
sudo docker-compose up -d pgadmin
sleep 3

if sudo docker-compose ps | grep -q "fallapp-pgadmin.*Up"; then
    echo -e "${GREEN}PASS${NC} | pgAdmin iniciado correctamente"
else
    echo -e "${YELLOW}WARN${NC} | pgAdmin no inició (puede ser opcional)"
fi

echo ""
echo "Test 6: Health check de PostgreSQL"
for i in {1..10}; do
    if sudo docker exec fallapp-postgres pg_isready -U fallapp_user -d fallapp > /dev/null 2>&1; then
        echo -e "${GREEN}PASS${NC} | PostgreSQL healthy (intento $i/10)"
        break
    else
        if [ $i -eq 10 ]; then
            echo -e "${RED}FAIL${NC} | PostgreSQL no está healthy después de 10 intentos"
            exit 1
        fi
        sleep 2
    fi
done

echo ""
echo "Test 7: Logs de PostgreSQL sin errores críticos"
if sudo docker-compose logs postgres | grep -i "FATAL\|ERROR" | grep -v "role \"postgres\" does not exist" > /dev/null 2>&1; then
    echo -e "${YELLOW}WARN${NC} | Se encontraron errores en los logs (revisar)"
    sudo docker-compose logs postgres | grep -i "FATAL\|ERROR" | tail -5
else
    echo -e "${GREEN}PASS${NC} | No hay errores críticos en logs"
fi

echo ""
echo "Test 8: Estado de servicios"
echo "-------------------------------"
sudo docker-compose ps
echo "-------------------------------"

echo ""
echo "Test 9: Docker Compose restart"
sudo docker-compose restart postgres > /dev/null 2>&1
sleep 5

if sudo docker-compose ps | grep -q "fallapp-postgres.*Up"; then
    echo -e "${GREEN}PASS${NC} | PostgreSQL reiniciado correctamente"
else
    echo -e "${RED}FAIL${NC} | PostgreSQL no reinició"
    exit 1
fi

echo ""
echo "Test 10: Docker Compose down"
sudo docker-compose down > /dev/null 2>&1

if ! sudo docker-compose ps | grep -q "fallapp-postgres.*Up"; then
    echo -e "${GREEN}PASS${NC} | Servicios detenidos correctamente"
else
    echo -e "${RED}FAIL${NC} | Servicios no se detuvieron"
    exit 1
fi

echo ""
echo "Reiniciando servicios..."
sudo docker-compose up -d postgres pgadmin > /dev/null 2>&1
sleep 3

echo ""
echo "========================================="
echo "TEST E2E DOCKER COMPOSE: COMPLETADO"
echo "========================================="
echo -e "${GREEN}Todos los tests pasaron${NC}"
