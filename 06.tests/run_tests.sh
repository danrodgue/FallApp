#!/bin/bash
# =============================================================================
# run_tests.sh
# Master test runner - Ejecuta todos los tests de FallApp
# =============================================================================

set -e

echo "======================================================="
echo "  FALLAPP - MASTER TEST RUNNER"
echo "======================================================="
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Contadores
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Función para ejecutar tests
run_test() {
    local test_name=$1
    local test_command=$2
    
    echo -e "${BLUE}▶${NC} Ejecutando: $test_name"
    echo "-------------------------------------------------------"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if eval "$test_command"; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${GREEN}✓${NC} $test_name: PASSED"
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${RED}✗${NC} $test_name: FAILED"
    fi
    
    echo ""
}

# Verificar que estamos en el directorio correcto
if [ ! -d "/srv/FallApp" ]; then
    echo -e "${RED}ERROR:${NC} Directorio /srv/FallApp no encontrado"
    exit 1
fi

cd /srv/FallApp

# ===========================
# TESTS E2E (End-to-End)
# ===========================
echo -e "${YELLOW}═══════════════════════════════════════════════${NC}"
echo -e "${YELLOW}  FASE 1: TESTS E2E (End-to-End)${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════${NC}"
echo ""

# Dar permisos de ejecución
chmod +x 06.tests/e2e/*.sh 2>/dev/null || true

run_test "E2E: Docker Compose" "bash 06.tests/e2e/test_docker_compose.sh"
run_test "E2E: PostgreSQL Connection" "bash 06.tests/e2e/test_postgres_connection.sh"
run_test "E2E: Data Persistence" "bash 06.tests/e2e/test_data_persistence.sh"

# ===========================
# TESTS DE INTEGRACIÓN (SQL)
# ===========================
echo -e "${YELLOW}═══════════════════════════════════════════════${NC}"
echo -e "${YELLOW}  FASE 2: TESTS DE INTEGRACIÓN (SQL)${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════${NC}"
echo ""

# Función auxiliar para ejecutar tests SQL
run_sql_test() {
    local test_name=$1
    local test_file=$2
    
    echo -e "${BLUE}▶${NC} Ejecutando: $test_name"
    echo "-------------------------------------------------------"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Ejecutar test SQL y capturar resultado
    RESULT=$(sudo docker exec fallapp-postgres psql -U fallapp_user -d fallapp -f "/docker-entrypoint-initdb.d/../../../srv/FallApp/$test_file" 2>&1)
    
    # Verificar si hay FAILs en el resultado
    if echo "$RESULT" | grep -q "FAIL"; then
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${RED}✗${NC} $test_name: FAILED"
        echo "$RESULT" | grep "FAIL"
    else
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${GREEN}✓${NC} $test_name: PASSED"
        echo "$RESULT" | grep "PASS" | tail -5
    fi
    
    echo ""
}

run_sql_test "INT: Schema Creation" "06.tests/integration/test_01_schema_creation.sql"
run_sql_test "INT: Data Integrity" "06.tests/integration/test_02_data_integrity.sql"
run_sql_test "INT: Views & Functions" "06.tests/integration/test_03_views_functions.sql"
run_sql_test "INT: Triggers" "06.tests/integration/test_04_triggers.sql"

# ===========================
# TESTS DE PERFORMANCE
# ===========================
echo -e "${YELLOW}═══════════════════════════════════════════════${NC}"
echo -e "${YELLOW}  FASE 3: TESTS DE PERFORMANCE (Opcional)${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════${NC}"
echo ""

if [ -d "06.tests/performance" ] && [ "$(ls -A 06.tests/performance 2>/dev/null)" ]; then
    echo "Tests de performance encontrados (ejecutar manualmente)"
    echo "  - cd 06.tests/performance && ./run_performance_tests.sh"
else
    echo -e "${YELLOW}SKIP${NC} No hay tests de performance configurados"
fi

echo ""

# ===========================
# RESUMEN FINAL
# ===========================
echo "======================================================="
echo "  RESUMEN DE TESTS"
echo "======================================================="
echo ""
echo "Total de tests ejecutados: $TOTAL_TESTS"
echo -e "${GREEN}Tests exitosos:${NC} $PASSED_TESTS"
echo -e "${RED}Tests fallidos:${NC} $FAILED_TESTS"
echo ""

# Calcular porcentaje de cobertura
if [ $TOTAL_TESTS -gt 0 ]; then
    COVERAGE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo "Cobertura: ${COVERAGE}%"
    echo ""
    
    if [ $COVERAGE -ge 80 ]; then
        echo -e "${GREEN}✓ Cobertura >= 80% - ACEPTABLE${NC}"
    else
        echo -e "${YELLOW}⚠ Cobertura < 80% - REVISAR${NC}"
    fi
fi

echo ""
echo "======================================================="

# Exit code basado en resultado
if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ TODOS LOS TESTS PASARON${NC}"
    echo "======================================================="
    exit 0
else
    echo -e "${RED}✗ ALGUNOS TESTS FALLARON${NC}"
    echo "======================================================="
    exit 1
fi
