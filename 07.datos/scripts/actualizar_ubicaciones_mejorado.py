#!/usr/bin/env python3

"""
Script mejorado para actualizar ubicaciones GPS de fallas en PostgreSQL
Usa matching normalizado de nombres para mayor cobertura
"""

import json
import psycopg2
import sys
from datetime import datetime
import re


DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'fallapp',
    'user': 'fallapp_user',
    'password': 'fallapp_secure_password_2026'
}

def normalizar_nombre(nombre):
    """Normaliza el nombre para comparación flexible"""
    if not nombre:
        return ""

    nombre = nombre.lower()

    replacements = {
        'á': 'a', 'é': 'e', 'í': 'i', 'ó': 'o', 'ú': 'u',
        'à': 'a', 'è': 'e', 'ì': 'i', 'ò': 'o', 'ù': 'u',
        'ä': 'a', 'ë': 'e', 'ï': 'i', 'ö': 'o', 'ü': 'u',
        'ñ': 'n', 'ç': 'c'
    }
    for old, new in replacements.items():
        nombre = nombre.replace(old, new)

    nombre = re.sub(r'[^\w\s]', ' ', nombre)
    nombre = re.sub(r'\s+', ' ', nombre)
    return nombre.strip()

def cargar_datos_json(ruta_json):
    """Carga el archivo JSON con datos de fallas"""
    try:
        with open(ruta_json, 'r', encoding='utf-8') as f:
            data = json.load(f)
        print(f"✅ JSON cargado: {len(data)} registros")
        return data
    except Exception as e:
        print(f"❌ Error cargando JSON: {e}")
        sys.exit(1)

def obtener_fallas_bd(cursor):
    """Obtiene todas las fallas de la BD con sus nombres normalizados"""
    cursor.execute("""
        SELECT id_falla, nombre
        FROM fallas
        ORDER BY id_falla
    """)

    fallas = {}
    for row in cursor.fetchall():
        id_falla, nombre = row
        nombre_norm = normalizar_nombre(nombre)
        fallas[id_falla] = {
            'nombre_original': nombre,
            'nombre_normalizado': nombre_norm
        }

    print(f"✅ Fallas en BD: {len(fallas)}")
    return fallas

def crear_indice_json(datos_json):
    """Crea índice de fallas del JSON por nombre normalizado"""
    indice = {}

    for falla in datos_json:
        if not falla.get('geo_point_2d'):
            continue

        nombre = falla.get('nombre', '')
        nombre_norm = normalizar_nombre(nombre)

        geo = falla['geo_point_2d']

        if nombre_norm:
            indice[nombre_norm] = {
                'nombre_original': nombre,
                'latitud': geo.get('lat'),
                'longitud': geo.get('lon'),
                'seccion': falla.get('seccion', ''),
                'anyo_fundacion': falla.get('anyo_fundacion', '')
            }

    print(f"✅ Fallas en JSON con ubicación: {len(indice)}")
    return indice

def actualizar_ubicaciones(cursor, fallas_bd, indice_json):
    """Actualiza ubicaciones con matching mejorado"""

    stats = {
        'actualizadas': 0,
        'sin_match': 0,
        'errores': 0,
        'sin_ubicacion': []
    }

    for id_falla, info_bd in fallas_bd.items():
        nombre_norm = info_bd['nombre_normalizado']
        nombre_orig = info_bd['nombre_original']


        if nombre_norm in indice_json:
            datos = indice_json[nombre_norm]
            lat = datos['latitud']
            lon = datos['longitud']

            if lat and lon:
                try:
                    cursor.execute("""
                        UPDATE fallas
                        SET ubicacion_lat = %s,
                            ubicacion_lon = %s,
                            actualizado_en = CURRENT_TIMESTAMP
                        WHERE id_falla = %s
                    """, (lat, lon, id_falla))

                    if cursor.rowcount > 0:
                        print(f"✅ {id_falla:3d} | {nombre_orig[:50]:<50} | ({lat:.6f}, {lon:.6f})")
                        stats['actualizadas'] += 1
                    else:
                        print(f"❌ {id_falla:3d} | {nombre_orig[:50]:<50} | UPDATE sin efecto")
                        stats['errores'] += 1


                    cursor.connection.commit()

                except Exception as e:
                    print(f"❌ {id_falla:3d} | {nombre_orig[:50]:<50} | Error: {e}")
                    cursor.connection.rollback()
                    stats['errores'] += 1
            else:
                print(f"⚠️  {id_falla:3d} | {nombre_orig[:50]:<50} | Sin coordenadas en JSON")
                stats['sin_match'] += 1
                stats['sin_ubicacion'].append({
                    'id': id_falla,
                    'nombre': nombre_orig
                })
        else:
            print(f"⚠️  {id_falla:3d} | {nombre_orig[:50]:<50} | No encontrada en JSON")
            stats['sin_match'] += 1
            stats['sin_ubicacion'].append({
                'id': id_falla,
                'nombre': nombre_orig
            })

    return stats

def main():
    """Función principal"""
    print("=" * 80)
    print("🗺️  ACTUALIZACIÓN MEJORADA DE UBICACIONES GPS - FallApp")
    print("=" * 80)
    print(f"Inicio: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()


    ruta_json = '/srv/FallApp/07.datos/raw/falles-fallas.json'
    datos_json = cargar_datos_json(ruta_json)


    indice_json = crear_indice_json(datos_json)
    print()


    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print("✅ Conectado a PostgreSQL")
        print()
    except Exception as e:
        print(f"❌ Error de conexión: {e}")
        sys.exit(1)


    fallas_bd = obtener_fallas_bd(cursor)
    print()


    print("📍 Actualizando ubicaciones...")
    print("-" * 80)
    stats = actualizar_ubicaciones(cursor, fallas_bd, indice_json)


    cursor.close()
    conn.close()


    print()
    print("=" * 80)
    print("📊 RESUMEN")
    print("=" * 80)
    print(f"✅ Actualizadas correctamente:  {stats['actualizadas']}")
    print(f"⚠️  Sin match en JSON:          {stats['sin_match']}")
    print(f"❌ Errores:                     {stats['errores']}")
    print()


    total_bd = len(fallas_bd)
    cobertura = (stats['actualizadas'] / total_bd * 100) if total_bd > 0 else 0
    print(f"📊 Cobertura: {stats['actualizadas']}/{total_bd} ({cobertura:.1f}%)")


    if stats['sin_ubicacion']:
        print()
        print(f"⚠️  Fallas sin ubicación ({len(stats['sin_ubicacion'])}):")
        for falla in stats['sin_ubicacion'][:10]:
            print(f"   - ID {falla['id']}: {falla['nombre']}")
        if len(stats['sin_ubicacion']) > 10:
            print(f"   ... y {len(stats['sin_ubicacion']) - 10} más")

    print()
    print(f"Fin: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 80)

if __name__ == "__main__":
    main()
