#!/bin/bash
# =============================================================================
# test_postgres_connection.sh
# Test E2E de conexión a PostgreSQL
# =============================================================================

set -e

echo "========================================="
echo "TEST E2E: PostgreSQL Connection"
echo "========================================="
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Test 1: Verificar que el contenedor está corriendo
echo "Test 1: Contenedor PostgreSQL corriendo"
if sudo docker ps | grep -q "fallapp-postgres"; then
    echo -e "${GREEN}PASS${NC} | Contenedor fallapp-postgres UP"
else
    echo -e "${RED}FAIL${NC} | Contenedor no está corriendo"
    echo "Iniciando contenedor..."
    cd /srv/FallApp/05.docker
    sudo docker-compose up -d postgres
    sleep 5
fi

# Test 2: Conexión con psql
echo ""
echo "Test 2: Conexión con psql"
if sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${GREEN}PASS${NC} | Conexión psql exitosa"
else
    echo -e "${RED}FAIL${NC} | No se pudo conectar con psql"
    exit 1
fi

# Test 3: Listar bases de datos
echo ""
echo "Test 3: Listar bases de datos"
DBS=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d postgres -t -c "SELECT datname FROM pg_database WHERE datname='fallapp';")
if echo "$DBS" | grep -q "fallapp"; then
    echo -e "${GREEN}PASS${NC} | Base de datos 'fallapp' existe"
else
    echo -e "${RED}FAIL${NC} | Base de datos 'fallapp' no encontrada"
    exit 1
fi

# Test 4: Contar tablas
echo ""
echo "Test 4: Tablas en base de datos"
TABLE_COUNT=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';")
TABLE_COUNT=$(echo $TABLE_COUNT | tr -d ' ')

if [ "$TABLE_COUNT" -ge 6 ]; then
    echo -e "${GREEN}PASS${NC} | $TABLE_COUNT tablas encontradas (esperado >= 6)"
else
    echo -e "${RED}FAIL${NC} | Solo $TABLE_COUNT tablas encontradas (esperado >= 6)"
    exit 1
fi

# Test 5: Contar fallas
echo ""
echo "Test 5: Datos de fallas"
FALLA_COUNT=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM fallas;")
FALLA_COUNT=$(echo $FALLA_COUNT | tr -d ' ')

if [ "$FALLA_COUNT" -ge 300 ]; then
    echo -e "${GREEN}PASS${NC} | $FALLA_COUNT fallas encontradas (esperado >= 300)"
else
    echo -e "${RED}FAIL${NC} | Solo $FALLA_COUNT fallas (esperado >= 300)"
    exit 1
fi

# Test 6: Contar usuarios
echo ""
echo "Test 6: Datos de usuarios"
USER_COUNT=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM usuarios;")
USER_COUNT=$(echo $USER_COUNT | tr -d ' ')

if [ "$USER_COUNT" -ge 3 ]; then
    echo -e "${GREEN}PASS${NC} | $USER_COUNT usuarios encontrados (esperado >= 3)"
else
    echo -e "${RED}FAIL${NC} | Solo $USER_COUNT usuarios (esperado >= 3)"
    exit 1
fi

# Test 7: Verificar vistas
echo ""
echo "Test 7: Vistas SQL"
VIEW_COUNT=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM information_schema.views WHERE table_schema='public';")
VIEW_COUNT=$(echo $VIEW_COUNT | tr -d ' ')

if [ "$VIEW_COUNT" -ge 9 ]; then
    echo -e "${GREEN}PASS${NC} | $VIEW_COUNT vistas encontradas (esperado >= 9)"
else
    echo -e "${RED}FAIL${NC} | Solo $VIEW_COUNT vistas (esperado >= 9)"
    exit 1
fi

# Test 8: Verificar funciones
echo ""
echo "Test 8: Funciones SQL"
FUNC_COUNT=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM pg_proc WHERE proname IN ('buscar_fallas', 'obtener_ranking_fallas');")
FUNC_COUNT=$(echo $FUNC_COUNT | tr -d ' ')

if [ "$FUNC_COUNT" -ge 2 ]; then
    echo -e "${GREEN}PASS${NC} | $FUNC_COUNT funciones encontradas (esperado >= 2)"
else
    echo -e "${RED}FAIL${NC} | Solo $FUNC_COUNT funciones (esperado >= 2)"
    exit 1
fi

# Test 9: Query compleja (JOIN)
echo ""
echo "Test 9: Query compleja con JOIN"
if sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "SELECT f.nombre, COUNT(v.id_voto) FROM fallas f LEFT JOIN votos v ON f.id_falla = v.id_falla GROUP BY f.id_falla LIMIT 5;" > /dev/null 2>&1; then
    echo -e "${GREEN}PASS${NC} | Query compleja exitosa"
else
    echo -e "${RED}FAIL${NC} | Query compleja falló"
    exit 1
fi

# Test 10: Performance simple
echo ""
echo "Test 10: Performance básica"
START=$(date +%s%3N)
sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "SELECT COUNT(*) FROM fallas;" > /dev/null 2>&1
END=$(date +%s%3N)
DURATION=$((END - START))

if [ "$DURATION" -lt 1000 ]; then
    echo -e "${GREEN}PASS${NC} | Query completada en ${DURATION}ms (< 1000ms)"
else
    echo -e "${RED}WARN${NC} | Query lenta: ${DURATION}ms"
fi

echo ""
echo "========================================="
echo "TEST E2E POSTGRESQL: COMPLETADO"
echo "========================================="
echo -e "${GREEN}Todos los tests pasaron${NC}"
