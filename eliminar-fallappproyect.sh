#!/bin/bash

# Script para eliminar el usuario fallappproyect@proton.me
# El email está configurado directamente en el script eliminar-usuario.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Ejecutando script de eliminación de usuario..."
echo ""

# Ejecutar el script genérico (el email ya está configurado dentro)
bash "$SCRIPT_DIR/eliminar-usuario.sh"
