#!/bin/bash

set -e

echo "========================================="
echo "TEST E2E: API Endpoint /ubicacion"
echo "========================================="
echo ""

API_URL="http://localhost:8080"
TIMEOUT=5

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_result="$3"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "Test $TOTAL_TESTS: $test_name ... "

    if eval "$test_command"; then
        echo -e "${GREEN}PASS${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}FAIL${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

echo "=== Tests de Conectividad ==="
run_test "Backend API activo" \
    "curl -s -o /dev/null -w '%{http_code}' --connect-timeout $TIMEOUT $API_URL/actuator/health | grep -q '200'" \
    "200"

run_test "GET /api/fallas/95/ubicacion (HTTP 200)" \
    "curl -s -o /dev/null -w '%{http_code}' --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | grep -q '200'" \
    "200"

echo ""
echo "=== Tests de Estructura JSON ==="
run_test "Respuesta JSON válida" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e . >/dev/null 2>&1" \
    "valid"

run_test "Campo 'exito' es true" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.exito == true' >/dev/null 2>&1" \
    "true"

run_test "Campo 'datos' existe" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.datos' >/dev/null 2>&1" \
    "exists"

run_test "Campo 'datos.idFalla' es número" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.datos.idFalla | type == \"number\"' >/dev/null 2>&1" \
    "number"

run_test "Campo 'datos.nombre' es string" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.datos.nombre | type == \"string\"' >/dev/null 2>&1" \
    "string"

run_test "Campo 'datos.latitud' es número" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.datos.latitud | type == \"number\"' >/dev/null 2>&1" \
    "number"

run_test "Campo 'datos.longitud' es número" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.datos.longitud | type == \"number\"' >/dev/null 2>&1" \
    "number"

run_test "Campo 'datos.tieneUbicacion' es boolean" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.datos.tieneUbicacion | type == \"boolean\"' >/dev/null 2>&1" \
    "boolean"

echo ""
echo "=== Tests de Validación de Datos ==="
run_test "Latitud en rango válido (38-40)" \
    "lat=\$(curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -r '.datos.latitud'); [ \$(echo \"\$lat >= 38 && \$lat <= 40\" | bc) -eq 1 ]" \
    "valid"

run_test "Longitud en rango válido (-1 a 0)" \
    "lon=\$(curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -r '.datos.longitud'); [ \$(echo \"\$lon >= -1 && \$lon <= 0\" | bc) -eq 1 ]" \
    "valid"

run_test "tieneUbicacion=true para falla con GPS" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.datos.tieneUbicacion == true' >/dev/null 2>&1" \
    "true"

echo ""
echo "=== Tests de Casos Especiales ==="
run_test "Falla sin GPS: tieneUbicacion=false" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/442/ubicacion | jq -e '.datos.tieneUbicacion == false' >/dev/null 2>&1" \
    "false"

run_test "ID inexistente devuelve HTTP 404" \
    "curl -s -o /dev/null -w '%{http_code}' --connect-timeout $TIMEOUT $API_URL/api/fallas/99999/ubicacion | grep -q '404'" \
    "404"

run_test "Acceso sin token JWT (público)" \
    "curl -s -o /dev/null -w '%{http_code}' --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | grep -q '200'" \
    "200"

echo ""
echo "=== Tests de Múltiples Fallas ==="
for id in 100 150 200 250 300; do
    run_test "Falla ID $id responde correctamente" \
        "curl -s -o /dev/null -w '%{http_code}' --connect-timeout $TIMEOUT $API_URL/api/fallas/$id/ubicacion | grep -q '200'" \
        "200"
done

echo ""
echo "=== Tests de Metadatos ==="
run_test "Campo 'timestamp' existe" \
    "curl -s --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | jq -e '.timestamp' >/dev/null 2>&1" \
    "exists"

run_test "Content-Type es application/json" \
    "curl -s -I --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion | grep -i 'content-type' | grep -q 'application/json'" \
    "json"

run_test "Tiempo de respuesta < 1 segundo" \
    "time_ms=\$(curl -s -o /dev/null -w '%{time_total}' --connect-timeout $TIMEOUT $API_URL/api/fallas/95/ubicacion); [ \$(echo \"\$time_ms < 1.0\" | bc) -eq 1 ]" \
    "fast"

echo ""
echo "========================================="
echo "RESUMEN DE TESTS"
echo "========================================="
echo -e "Total:  $TOTAL_TESTS tests"
echo -e "${GREEN}Passed: $PASSED_TESTS tests${NC}"
if [ $FAILED_TESTS -gt 0 ]; then
    echo -e "${RED}Failed: $FAILED_TESTS tests${NC}"
else
    echo -e "Failed: $FAILED_TESTS tests"
fi
echo ""

PERCENTAGE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
if [ $PERCENTAGE -eq 100 ]; then
    echo -e "${GREEN}✓ TODOS LOS TESTS PASARON ($PERCENTAGE%)${NC}"
    exit 0
elif [ $PERCENTAGE -ge 80 ]; then
    echo -e "${YELLOW}⚠ $PERCENTAGE% de tests pasaron${NC}"
    exit 1
else
    echo -e "${RED}✗ TESTS FALLIDOS ($PERCENTAGE%)${NC}"
    exit 1
fi
