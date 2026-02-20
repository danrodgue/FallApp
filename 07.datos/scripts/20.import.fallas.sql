

INSERT INTO fallas (
    nombre,
    seccion,
    fallera,
    presidente,
    artista,
    lema,
    descripcion,
    anyo_fundacion,
    categoria,
    distintivo,
    ubicacion_lat,
    ubicacion_lon,
    web_oficial,
    telefono_contacto,
    email_contacto,
    experim,
    activa
) VALUES (
    'Falla Ejemplo - Importación de Prueba',
    'E',
    'Fallera Mayor de Ejemplo',
    'Presidente de Ejemplo',
    'Artista Constructor',
    'Tema: Las Fallas en la Era Digital',
    'Descripción de la falla de prueba para validar la estructura de importación',
    2024,
    'sin_categoria'::categoria_falla,
    NULL,
    39.47694,
    -0.37632,
    'https://example.com/falla',
    '+34-963-000-000',
    'contacto@example.com',
    false,
    true
)
ON CONFLICT (nombre) DO NOTHING;

SELECT
    COUNT(*) as total_fallas,
    COUNT(CASE WHEN activa THEN 1 END) as fallas_activas,
    MIN(anyo_fundacion) as anyo_fundacion_min,
    MAX(anyo_fundacion) as anyo_fundacion_max
FROM fallas;


SELECT
    categoria,
    COUNT(*) as cantidad
FROM fallas
GROUP BY categoria
ORDER BY cantidad DESC;


SELECT
    seccion,
    COUNT(*) as cantidad
FROM fallas
GROUP BY seccion
ORDER BY seccion;

