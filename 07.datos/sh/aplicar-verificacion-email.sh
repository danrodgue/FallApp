#!/bin/bash

set -e

echo "=========================================="
echo "  APLICANDO MIGRACIÓN SQL Y COMPILANDO"
echo "=========================================="
echo ""

echo "1. Aplicando migración SQL..."
cd /srv/FallApp/07.datos/scripts

export PGPASSWORD=fallapp_secure_password_2026
psql -U fallapp_user -d fallapp -h localhost -f 12.migracion.verificacion_email.sql

if [ $? -eq 0 ]; then
    echo "✅ Migración aplicada correctamente"
else
    echo "⚠️  Error en migración (puede que ya esté aplicada)"
fi

echo ""

echo "2. Verificando campos en BD..."
psql -U fallapp_user -d fallapp -h localhost -c "
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'usuarios'
  AND column_name IN ('verificado', 'token_verificacion', 'token_verificacion_expira')
ORDER BY ordinal_position;
" 2>&1 | grep -v "PGPASSWORD"

echo ""

echo "3. Compilando backend..."
cd /srv/FallApp/01.backend
./mvnw clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Backend compilado correctamente"
else
    echo "❌ Error al compilar backend"
    exit 1
fi

echo ""
echo "=========================================="
echo "  ✅ PROCESO COMPLETADO"
echo "=========================================="
echo ""
echo "Para iniciar el backend ejecuta:"
echo "  cd /srv/FallApp/01.backend"
echo "  ./mvnw spring-boot:run"
echo ""
