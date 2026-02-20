#!/bin/bash


SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Ejecutando script de eliminación de usuario..."
echo ""

bash "$SCRIPT_DIR/eliminar-usuario.sh"
