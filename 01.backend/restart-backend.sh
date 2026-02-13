#!/bin/bash

# ============================================================================
# Script: restart-backend.sh
# Descripción: Reinicia el backend de FallApp (Spring Boot)
# Uso: bash restart-backend.sh
# ============================================================================

set -e  # Salir si hay error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  🔄 Reiniciando Backend FallApp${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Verificar si estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
    echo -e "${YELLOW}⚠️  No estás en el directorio del backend${NC}"
    echo -e "${YELLOW}   Cambiando a /srv/FallApp/01.backend...${NC}"
    cd /srv/FallApp/01.backend
fi

# Paso 1: Detener el servicio
echo -e "${YELLOW}1️⃣  Deteniendo servicio fallapp...${NC}"
sudo systemctl stop fallapp

if [ $? -eq 0 ]; then
    echo -e "${GREEN}   ✅ Servicio detenido${NC}"
else
    echo -e "${RED}   ❌ Error al detener el servicio${NC}"
    exit 1
fi

# Esperar para asegurar que se detuvo completamente
echo -e "${YELLOW}   ⏳ Esperando 3 segundos...${NC}"
sleep 3

# Paso 2: Verificar que no hay procesos Java del backend corriendo
echo -e "${YELLOW}2️⃣  Verificando procesos...${NC}"
JAVA_PID=$(ps aux | grep '[F]allapp-0.0.1-SNAPSHOT.jar' | awk '{print $2}')

if [ -n "$JAVA_PID" ]; then
    echo -e "${YELLOW}   ⚠️  Proceso Java detectado (PID: $JAVA_PID)${NC}"
    echo -e "${YELLOW}   🔨 Matando proceso...${NC}"
    sudo kill -9 $JAVA_PID 2>/dev/null
    sleep 2
    echo -e "${GREEN}   ✅ Proceso eliminado${NC}"
else
    echo -e "${GREEN}   ✅ No hay procesos residuales${NC}"
fi

# Paso 3: Iniciar el servicio
echo -e "${YELLOW}3️⃣  Iniciando servicio fallapp...${NC}"
sudo systemctl start fallapp

if [ $? -eq 0 ]; then
    echo -e "${GREEN}   ✅ Servicio iniciado${NC}"
else
    echo -e "${RED}   ❌ Error al iniciar el servicio${NC}"
    exit 1
fi

# Esperar para que el servicio inicie
echo -e "${YELLOW}   ⏳ Esperando inicialización (10 segundos)...${NC}"
sleep 10

# Paso 4: Verificar estado del servicio
echo -e "${YELLOW}4️⃣  Verificando estado...${NC}"
STATUS=$(systemctl is-active fallapp)

if [ "$STATUS" = "active" ]; then
    echo -e "${GREEN}   ✅ Servicio: ${STATUS}${NC}"
    
    # Obtener PID del nuevo proceso
    NEW_PID=$(ps aux | grep '[F]allapp-0.0.1-SNAPSHOT.jar' | awk '{print $2}')
    if [ -n "$NEW_PID" ]; then
        echo -e "${GREEN}   ✅ PID: ${NEW_PID}${NC}"
    fi
    
    # Verificar puerto 8080
    PORT_CHECK=$(ss -tlnp 2>/dev/null | grep ':8080')
    if [ -n "$PORT_CHECK" ]; then
        echo -e "${GREEN}   ✅ Puerto 8080: Escuchando${NC}"
    else
        echo -e "${YELLOW}   ⚠️  Puerto 8080: No detectado aún${NC}"
    fi
else
    echo -e "${RED}   ❌ Servicio: ${STATUS}${NC}"
    echo -e "${RED}   ❌ El servicio no está activo${NC}"
    exit 1
fi

# Paso 5: Mostrar logs recientes
echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  📋 Últimos logs del backend${NC}"
echo -e "${BLUE}============================================${NC}"
sudo journalctl -u fallapp --no-pager -n 20 --since "1 minute ago" | tail -15

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}  ✅ Backend reiniciado correctamente${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""
echo -e "${YELLOW}💡 Comandos útiles:${NC}"
echo -e "   Ver logs en tiempo real: ${BLUE}sudo journalctl -u fallapp -f${NC}"
echo -e "   Ver estado del servicio: ${BLUE}systemctl status fallapp${NC}"
echo -e "   Test de API: ${BLUE}curl http://localhost:8080/api/fallas${NC}"
echo ""
