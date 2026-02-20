#!/usr/bin/env python3
"""
Script: Generador de INSERT SQL para todas las fallas
Fecha: 2026-02-04
Descripción: Lee el JSON original y genera el SQL completo para insertar las 351 fallas
"""

import json
import os


JSONL_PATH = "/srv/FallApp/07.datos/raw/falles-fallas.jsonl"
OUTPUT_SQL = "/srv/FallApp/07.datos/scripts/03.insertar_351_fallas_completo.sql"

def escape_sql(value):
    """Escapa comillas simples para SQL"""
    if value is None or value == '' or (isinstance(value, str) and value.lower() == 'none'):
        return "NULL"
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, (int, float)):
        return str(value)

    return f"'{str(value).replace(chr(39), chr(39)+chr(39))}'"

def generate_insert_sql():
    """Genera el SQL completo de inserción"""

    print(f"📖 Leyendo {JSONL_PATH}...")

    fallas = []
    with open(JSONL_PATH, 'r', encoding='utf-8') as f:
        for line in f:
            if line.strip():
                fallas.append(json.loads(line))

    print(f"✅ {len(fallas)} fallas leídas")


    sql_lines = []
    sql_lines.append("-- ========================================================================")
    sql_lines.append("-- INSERCIÓN COMPLETA DE FALLAS - Generado automáticamente")
    sql_lines.append(f"-- Total: {len(fallas)} fallas")
    sql_lines.append("-- Fuente: falles-fallas.jsonl")
    sql_lines.append("-- Fecha: 2026-02-04")
    sql_lines.append("-- ========================================================================")
    sql_lines.append("")
    sql_lines.append("BEGIN;")
    sql_lines.append("")
    sql_lines.append("-- Limpiar tabla")
    sql_lines.append("TRUNCATE TABLE fallas RESTART IDENTITY CASCADE;")
    sql_lines.append("")
    sql_lines.append("-- Insertar todas las fallas")
    sql_lines.append("INSERT INTO fallas (")
    sql_lines.append("    nombre, seccion, fallera, presidente, artista, lema,")
    sql_lines.append("    anyo_fundacion, distintivo, url_boceto, experim,")
    sql_lines.append("    ubicacion_lat, ubicacion_lon, categoria, datos_json")
    sql_lines.append(") VALUES")


    for idx, falla in enumerate(fallas):

        nombre = falla.get('nombre') or f'Falla sin nombre (ID: {falla.get("id_falla", idx)})'
        seccion = falla.get('seccion') or 'FC'


        fallera_raw = falla.get('fallera')
        fallera = None if fallera_raw in [None, 'NO HAY', ''] else fallera_raw


        presidente = falla.get('presidente') or 'Sin presidente'
        artista = falla.get('artista') or None
        lema = falla.get('lema') or None
        anyo_fundacion = falla.get('anyo_fundacion') or 1900
        distintivo = falla.get('distintivo') or None
        boceto = falla.get('boceto', '')
        experim = falla.get('experim', 0) == 1


        geo_point = falla.get('geo_point_2d', {})
        lat = geo_point.get('lat')
        lon = geo_point.get('lon')


        objectid = falla.get('objectid')
        id_falla_original = falla.get('id_falla')
        datos_json = json.dumps({
            'objectid': objectid,
            'id_falla_original': id_falla_original
        }, ensure_ascii=False)


        values = f"({escape_sql(nombre)}, {escape_sql(seccion)}, {escape_sql(fallera)}, " \
                 f"{escape_sql(presidente)}, {escape_sql(artista)}, {escape_sql(lema)}, " \
                 f"{anyo_fundacion}, {escape_sql(distintivo)}, {escape_sql(boceto)}, " \
                 f"{escape_sql(experim)}, {escape_sql(lat)}, {escape_sql(lon)}, " \
                 f"'sin_categoria', '{datos_json}'::jsonb)"


        if idx < len(fallas) - 1:
            values += ","
        else:
            values += ";"

        sql_lines.append(values)


        if (idx + 1) % 50 == 0:
            print(f"  Procesadas {idx + 1}/{len(fallas)} fallas...")

    sql_lines.append("")
    sql_lines.append("-- Verificar")
    sql_lines.append("SELECT COUNT(*) as total_insertado FROM fallas;")
    sql_lines.append("")
    sql_lines.append("COMMIT;")
    sql_lines.append("")
    sql_lines.append("-- ========================================================================")


    with open(OUTPUT_SQL, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_lines))

    print(f"✅ SQL generado: {OUTPUT_SQL}")
    print(f"📊 {len(fallas)} registros INSERT creados")


    con_fallera = sum(1 for f in fallas if f.get('fallera') not in [None, 'NO HAY'])
    con_gps = sum(1 for f in fallas if f.get('geo_point_2d', {}).get('lat'))

    print(f"\n📈 Estadísticas:")
    print(f"   - Total fallas: {len(fallas)}")
    print(f"   - Con fallera: {con_fallera} ({con_fallera/len(fallas)*100:.1f}%)")
    print(f"   - Con GPS: {con_gps} ({con_gps/len(fallas)*100:.1f}%)")

    return OUTPUT_SQL

if __name__ == "__main__":
    try:
        sql_file = generate_insert_sql()
        print(f"\n✅ Listo! Ahora ejecuta:")
        print(f"   docker exec -i fallapp-postgres psql -U fallapp_user -d fallapp < {sql_file}")
    except Exception as e:
        print(f"❌ Error: {e}")
        raise
