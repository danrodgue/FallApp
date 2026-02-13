#!/usr/bin/env python3
"""
Script para actualizar las ubicaciones (lat/lon) de las fallas desde el JSON fuente.

Actualiza los campos ubicacion_lat y ubicacion_lon de la tabla fallas
con los datos de geo_point_2d del archivo falles-fallas.json.
"""

import json
import psycopg2
from psycopg2.extras import execute_values
import os
import sys

# Configuración de la base de datos
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'fallapp',
    'user': 'fallapp_user',
    'password': 'fallapp_secure_password_2026'
}

# Ruta del archivo JSON
JSON_FILE = '/srv/FallApp/07.datos/raw/falles-fallas.json'

def cargar_json():
    """Carga el archivo JSON de fallas."""
    print(f"📂 Cargando JSON desde: {JSON_FILE}")
    try:
        with open(JSON_FILE, 'r', encoding='utf-8') as f:
            data = json.load(f)
        print(f"✅ Cargadas {len(data)} fallas del JSON")
        return data
    except FileNotFoundError:
        print(f"❌ Error: No se encuentra el archivo {JSON_FILE}")
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"❌ Error al parsear JSON: {e}")
        sys.exit(1)

def conectar_db():
    """Establece conexión con PostgreSQL."""
    print(f"🔌 Conectando a PostgreSQL en {DB_CONFIG['host']}:{DB_CONFIG['port']}")
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        print("✅ Conexión exitosa a PostgreSQL")
        return conn
    except psycopg2.Error as e:
        print(f"❌ Error al conectar a PostgreSQL: {e}")
        sys.exit(1)

def actualizar_ubicaciones(conn, fallas_json):
    """Actualiza las ubicaciones de las fallas en la base de datos."""
    cursor = conn.cursor()
    
    # Estadísticas
    stats = {
        'total': len(fallas_json),
        'actualizadas': 0,
        'sin_ubicacion': 0,
        'no_encontradas': 0,
        'errores': 0
    }
    
    print("\n🔄 Iniciando actualización de ubicaciones...")
    print("=" * 80)
    
    for falla in fallas_json:
        id_falla = falla.get('id_falla')
        nombre = falla.get('nombre', 'Sin nombre')
        geo_point = falla.get('geo_point_2d', {})
        
        # Validar datos
        if not id_falla:
            stats['errores'] += 1
            continue
        
        lat = geo_point.get('lat')
        lon = geo_point.get('lon')
        
        if lat is None or lon is None:
            stats['sin_ubicacion'] += 1
            print(f"⚠️  Falla #{id_falla:3d} - {nombre[:50]:<50} [SIN UBICACIÓN]")
            continue
        
        try:
            # Actualizar en base de datos
            cursor.execute("""
                UPDATE fallas 
                SET ubicacion_lat = %s,
                    ubicacion_lon = %s,
                    actualizado_en = CURRENT_TIMESTAMP
                WHERE id_falla = %s
                RETURNING id_falla, nombre
            """, (lat, lon, id_falla))
            
            resultado = cursor.fetchone()
            
            if resultado:
                stats['actualizadas'] += 1
                print(f"✅ Falla #{id_falla:3d} - {nombre[:50]:<50} -> ({lat:.6f}, {lon:.6f})")
                # Commit después de cada actualización exitosa
                conn.commit()
            else:
                stats['no_encontradas'] += 1
                print(f"❌ Falla #{id_falla:3d} - {nombre[:50]:<50} [NO EXISTE EN BD]")
                conn.rollback()
                
        except psycopg2.Error as e:
            stats['errores'] += 1
            print(f"❌ Error actualizando falla #{id_falla}: {e}")
            conn.rollback()
            continue
        except Exception as e:
            stats['errores'] += 1
            print(f"❌ Error inesperado actualizando falla #{id_falla}: {e}")
            conn.rollback()
            continue
    
    cursor.close()
    
    return stats

def mostrar_estadisticas(stats):
    """Muestra un resumen de la actualización."""
    print("\n" + "=" * 80)
    print("📊 RESUMEN DE LA ACTUALIZACIÓN")
    print("=" * 80)
    print(f"Total de fallas en JSON:        {stats['total']}")
    print(f"✅ Actualizadas correctamente:  {stats['actualizadas']}")
    print(f"⚠️  Sin ubicación en JSON:      {stats['sin_ubicacion']}")
    print(f"❌ No encontradas en BD:        {stats['no_encontradas']}")
    print(f"❌ Errores:                     {stats['errores']}")
    print("=" * 80)
    
    # Calcular porcentaje de éxito
    if stats['total'] > 0:
        exito = (stats['actualizadas'] / stats['total']) * 100
        print(f"\n🎯 Tasa de éxito: {exito:.1f}%")

def verificar_actualizacion(conn):
    """Verifica cuántas fallas tienen ubicación después de la actualización."""
    cursor = conn.cursor()
    
    print("\n🔍 Verificando resultado...")
    
    try:
        # Contar fallas con ubicación
        cursor.execute("""
            SELECT 
                COUNT(*) as total,
                COUNT(ubicacion_lat) as con_ubicacion,
                COUNT(*) - COUNT(ubicacion_lat) as sin_ubicacion
            FROM fallas
        """)
        
        total, con_ubicacion, sin_ubicacion = cursor.fetchone()
        
        print(f"\n📍 Estado final de ubicaciones en BD:")
        print(f"   Total de fallas:        {total}")
        print(f"   ✅ Con ubicación:       {con_ubicacion}")
        print(f"   ❌ Sin ubicación:       {sin_ubicacion}")
        
        if con_ubicacion > 0:
            porcentaje = (con_ubicacion / total) * 100
            print(f"   📊 Cobertura:           {porcentaje:.1f}%")
            
    except psycopg2.Error as e:
        print(f"❌ Error en verificación: {e}")
    finally:
        cursor.close()

def main():
    """Función principal."""
    print("=" * 80)
    print("🗺️  ACTUALIZACIÓN DE UBICACIONES DE FALLAS")
    print("=" * 80)
    
    # Cargar datos
    fallas_json = cargar_json()
    
    # Conectar a DB
    conn = conectar_db()
    
    try:
        # Actualizar ubicaciones
        stats = actualizar_ubicaciones(conn, fallas_json)
        
        # Mostrar estadísticas
        mostrar_estadisticas(stats)
        
        # Verificar resultado
        verificar_actualizacion(conn)
        
        print("\n✅ Proceso completado exitosamente\n")
        
    except Exception as e:
        print(f"\n❌ Error inesperado: {e}")
        sys.exit(1)
    finally:
        conn.close()
        print("🔌 Conexión cerrada")

if __name__ == "__main__":
    main()
