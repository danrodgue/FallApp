#!/bin/bash

set -e

echo "========================================="
echo "TEST PERFORMANCE: Endpoint /ubicacion"
echo "========================================="
echo ""

API_URL="http://localhost:8080"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "Test 1: Tiempo de respuesta individual"
echo "--------------------------------------"
RESPONSE_TIME=$(curl -s -o /dev/null -w "%{time_total}" $API_URL/api/fallas/95/ubicacion)
echo "Tiempo de respuesta: ${RESPONSE_TIME}s"

if (( $(echo "$RESPONSE_TIME < 0.5" | bc -l) )); then
    echo -e "${GREEN}PASS${NC} | Respuesta rápida (< 0.5s)"
elif (( $(echo "$RESPONSE_TIME < 1.0" | bc -l) )); then
    echo -e "${YELLOW}WARN${NC} | Respuesta aceptable (< 1.0s)"
else
    echo -e "${RED}FAIL${NC} | Respuesta lenta (>= 1.0s)"
fi

echo ""
echo "Test 2: 10 requests secuenciales"
echo "--------------------------------------"
TOTAL_TIME=0
for i in {1..10}; do
    TIME=$(curl -s -o /dev/null -w "%{time_total}" $API_URL/api/fallas/$((90 + i))/ubicacion)
    TOTAL_TIME=$(echo "$TOTAL_TIME + $TIME" | bc -l)
done

AVG_TIME=$(echo "scale=4; $TOTAL_TIME / 10" | bc -l)
echo "Tiempo promedio: ${AVG_TIME}s"
echo "Tiempo total: ${TOTAL_TIME}s"

if (( $(echo "$AVG_TIME < 0.3" | bc -l) )); then
    echo -e "${GREEN}PASS${NC} | Rendimiento excelente (< 0.3s promedio)"
elif (( $(echo "$AVG_TIME < 0.5" | bc -l) )); then
    echo -e "${YELLOW}WARN${NC} | Rendimiento aceptable (< 0.5s promedio)"
else
    echo -e "${RED}FAIL${NC} | Rendimiento deficiente (>= 0.5s promedio)"
fi

echo ""
echo "Test 3: 5 requests concurrentes"
echo "--------------------------------------"
START_TIME=$(date +%s.%N)

for i in {1..5}; do
    curl -s -o /dev/null $API_URL/api/fallas/$((100 + i))/ubicacion &
done

wait

END_TIME=$(date +%s.%N)
CONCURRENT_TIME=$(echo "$END_TIME - $START_TIME" | bc -l)

echo "Tiempo total (5 paralelos): ${CONCURRENT_TIME}s"

if (( $(echo "$CONCURRENT_TIME < 1.0" | bc -l) )); then
    echo -e "${GREEN}PASS${NC} | Maneja concurrencia eficientemente (< 1.0s)"
elif (( $(echo "$CONCURRENT_TIME < 2.0" | bc -l) )); then
    echo -e "${YELLOW}WARN${NC} | Concurrencia aceptable (< 2.0s)"
else
    echo -e "${RED}FAIL${NC} | Problemas de concurrencia (>= 2.0s)"
fi

echo ""
echo "Test 4: Tamaño de respuesta"
echo "--------------------------------------"
RESPONSE=$(curl -s $API_URL/api/fallas/95/ubicacion)
SIZE=$(echo "$RESPONSE" | wc -c)

echo "Tamaño de respuesta: ${SIZE} bytes"

if [ $SIZE -lt 500 ]; then
    echo -e "${GREEN}PASS${NC} | Respuesta compacta (< 500 bytes)"
elif [ $SIZE -lt 1000 ]; then
    echo -e "${YELLOW}WARN${NC} | Respuesta aceptable (< 1KB)"
else
    echo -e "${RED}FAIL${NC} | Respuesta grande (>= 1KB)"
fi

echo ""
echo "Test 5: Carga pesada (100 requests)"
echo "--------------------------------------"
START_TIME=$(date +%s.%N)

for i in {1..100}; do
    curl -s -o /dev/null $API_URL/api/fallas/$((i + 50))/ubicacion 2>/dev/null || true
done

END_TIME=$(date +%s.%N)
LOAD_TIME=$(echo "$END_TIME - $START_TIME" | bc -l)
AVG_LOAD=$(echo "scale=4; $LOAD_TIME / 100" | bc -l)

echo "Tiempo total (100 requests): ${LOAD_TIME}s"
echo "Tiempo promedio: ${AVG_LOAD}s"

if (( $(echo "$LOAD_TIME < 30.0" | bc -l) )); then
    echo -e "${GREEN}PASS${NC} | Soporta carga pesada eficientemente (< 30s)"
elif (( $(echo "$LOAD_TIME < 60.0" | bc -l) )); then
    echo -e "${YELLOW}WARN${NC} | Carga pesada aceptable (< 60s)"
else
    echo -e "${RED}FAIL${NC} | No soporta carga pesada (>= 60s)"
fi

echo ""
echo "Test 6: Uso de recursos del backend"
echo "--------------------------------------"
if command -v systemctl &> /dev/null; then
    MEMORY=$(sudo systemctl show fallapp --property=MemoryCurrent | cut -d= -f2)
    if [ "$MEMORY" != "[not set]" ] && [ ! -z "$MEMORY" ]; then
        MEMORY_MB=$((MEMORY / 1024 / 1024))
        echo "Memoria en uso: ${MEMORY_MB} MB"

        if [ $MEMORY_MB -lt 512 ]; then
            echo -e "${GREEN}PASS${NC} | Uso de memoria bajo (< 512 MB)"
        elif [ $MEMORY_MB -lt 768 ]; then
            echo -e "${YELLOW}WARN${NC} | Uso de memoria medio (< 768 MB)"
        else
            echo -e "${RED}FAIL${NC} | Uso de memoria alto (>= 768 MB)"
        fi
    else
        echo "INFO | No se pudo obtener uso de memoria"
    fi
fi

echo ""
echo "========================================="
echo "FIN TEST PERFORMANCE"
echo "========================================="
