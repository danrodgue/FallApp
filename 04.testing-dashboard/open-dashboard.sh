#!/bin/bash
# Quick launcher - Abre el dashboard directamente en el navegador

DASHBOARD_PATH="/srv/FallApp/05.testing-dashboard/index.html"

echo "🚀 Abriendo FallApp Testing Dashboard..."

# Detectar el comando para abrir el navegador según el OS
if command -v xdg-open > /dev/null; then
    xdg-open "$DASHBOARD_PATH"
elif command -v open > /dev/null; then
    open "$DASHBOARD_PATH"
elif command -v start > /dev/null; then
    start "$DASHBOARD_PATH"
else
    echo "ℹ️  Abre manualmente en tu navegador:"
    echo "   file://$DASHBOARD_PATH"
fi
