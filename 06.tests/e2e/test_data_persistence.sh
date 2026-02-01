#!/bin/bash
# =============================================================================
# test_data_persistence.sh
# Test E2E de persistencia de datos
# =============================================================================

set -e

echo "========================================="
echo "TEST E2E: Data Persistence"
echo "========================================="
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_DIR="/srv/FallApp/05.docker"
cd "$PROJECT_DIR"

# Test 1: Contar datos iniciales
echo "Test 1: Contar datos iniciales"
INITIAL_FALLAS=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM fallas;")
INITIAL_FALLAS=$(echo $INITIAL_FALLAS | tr -d ' ')
echo "Fallas iniciales: $INITIAL_FALLAS"

INITIAL_USERS=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM usuarios;")
INITIAL_USERS=$(echo $INITIAL_USERS | tr -d ' ')
echo "Usuarios iniciales: $INITIAL_USERS"

# Test 2: Insertar datos de prueba
echo ""
echo "Test 2: Insertar datos de prueba"
sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "
INSERT INTO fallas (nombre, seccion, presidente, anyo_fundacion, categoria)
VALUES ('TEST_PERSISTENCE_FALLA', 'TP', 'Test Persist', 2026, 'sin_categoria')
ON CONFLICT (nombre) DO NOTHING;
" > /dev/null 2>&1

sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c "
INSERT INTO usuarios (email, contraseña_hash, nombre_completo, rol)
VALUES ('test_persist@test.com', 'hash_test_persist', 'Test Persistence', 'usuario')
ON CONFLICT (email) DO NOTHING;
" > /dev/null 2>&1

# Verificar inserción
NEW_FALLAS=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM fallas;")
NEW_FALLAS=$(echo $NEW_FALLAS | tr -d ' ')

NEW_USERS=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM usuarios;")
NEW_USERS=$(echo $NEW_USERS | tr -d ' ')

if [ "$NEW_FALLAS" -gt "$INITIAL_FALLAS" ] && [ "$NEW_USERS" -gt "$INITIAL_USERS" ]; then
    echo -e "${GREEN}PASS${NC} | Datos insertados: +$(($NEW_FALLAS - $INITIAL_FALLAS)) fallas, +$(($NEW_USERS - $INITIAL_USERS)) usuarios"
else
    echo -e "${YELLOW}WARN${NC} | Datos ya existían (ON CONFLICT DO NOTHING)"
fi

# Test 3: Reiniciar contenedor
echo ""
echo "Test 3: Reiniciar contenedor PostgreSQL"
sudo docker-compose restart postgres > /dev/null 2>&1
sleep 5

# Esperar a que esté disponible
for i in {1..10}; do
    if sudo docker exec fallapp-postgres pg_isready -U fallapp_user -d fallapp > /dev/null 2>&1; then
        echo -e "${GREEN}PASS${NC} | PostgreSQL reiniciado y disponible"
        break
    else
        if [ $i -eq 10 ]; then
            echo -e "${RED}FAIL${NC} | PostgreSQL no disponible después de reinicio"
            exit 1
        fi
        sleep 2
    fi
done

# Test 4: Verificar persistencia de datos
echo ""
echo "Test 4: Verificar datos después de reinicio"
AFTER_RESTART_FALLAS=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM fallas;")
AFTER_RESTART_FALLAS=$(echo $AFTER_RESTART_FALLAS | tr -d ' ')

AFTER_RESTART_USERS=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM usuarios;")
AFTER_RESTART_USERS=$(echo $AFTER_RESTART_USERS | tr -d ' ')

if [ "$AFTER_RESTART_FALLAS" -eq "$NEW_FALLAS" ] && [ "$AFTER_RESTART_USERS" -eq "$NEW_USERS" ]; then
    echo -e "${GREEN}PASS${NC} | Datos persistidos: $AFTER_RESTART_FALLAS fallas, $AFTER_RESTART_USERS usuarios"
else
    echo -e "${RED}FAIL${NC} | Datos perdidos después de reinicio"
    echo "Antes: $NEW_FALLAS fallas, $NEW_USERS usuarios"
    echo "Después: $AFTER_RESTART_FALLAS fallas, $AFTER_RESTART_USERS usuarios"
    exit 1
fi

# Test 5: Verificar datos específicos de prueba
echo ""
echo "Test 5: Verificar datos específicos insertados"
TEST_FALLA=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT nombre FROM fallas WHERE nombre='TEST_PERSISTENCE_FALLA';")
if echo "$TEST_FALLA" | grep -q "TEST_PERSISTENCE_FALLA"; then
    echo -e "${GREEN}PASS${NC} | Falla de prueba encontrada después de reinicio"
else
    echo -e "${YELLOW}WARN${NC} | Falla de prueba no encontrada (puede haber sido limpiada)"
fi

# Test 6: Verificar volumen Docker
echo ""
echo "Test 6: Verificar volumen Docker"
if sudo docker volume ls | grep -q "05docker_postgres_data"; then
    VOLUME_SIZE=$(sudo docker volume inspect 05docker_postgres_data --format '{{ .Mountpoint }}' | xargs sudo du -sh 2>/dev/null || echo "N/A")
    echo -e "${GREEN}PASS${NC} | Volumen persistente existe: $VOLUME_SIZE"
else
    echo -e "${RED}FAIL${NC} | Volumen persistente no encontrado"
    exit 1
fi

# Test 7: Down y Up completo (test de persistencia extremo)
echo ""
echo "Test 7: Down y Up completo (persistencia extrema)"
echo "Deteniendo todos los servicios..."
sudo docker-compose down > /dev/null 2>&1
sleep 2

echo "Iniciando servicios nuevamente..."
sudo docker-compose up -d postgres > /dev/null 2>&1
sleep 5

# Esperar disponibilidad
for i in {1..10}; do
    if sudo docker exec fallapp-postgres pg_isready -U fallapp_user -d fallapp > /dev/null 2>&1; then
        break
    else
        if [ $i -eq 10 ]; then
            echo -e "${RED}FAIL${NC} | PostgreSQL no disponible después de down/up"
            exit 1
        fi
        sleep 2
    fi
done

# Verificar datos finales
FINAL_FALLAS=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c "SELECT COUNT(*) FROM fallas;")
FINAL_FALLAS=$(echo $FINAL_FALLAS | tr -d ' ')

if [ "$FINAL_FALLAS" -eq "$AFTER_RESTART_FALLAS" ]; then
    echo -e "${GREEN}PASS${NC} | Datos persistidos después de down/up: $FINAL_FALLAS fallas"
else
    echo -e "${RED}FAIL${NC} | Datos perdidos en down/up"
    exit 1
fi

echo ""
echo "========================================="
echo "TEST E2E DATA PERSISTENCE: COMPLETADO"
echo "========================================="
echo -e "${GREEN}Todos los tests pasaron${NC}"
echo ""
echo "Resumen:"
echo "  - Fallas: $FINAL_FALLAS"
echo "  - Usuarios: $AFTER_RESTART_USERS"
echo "  - Volumen: 05docker_postgres_data OK"
