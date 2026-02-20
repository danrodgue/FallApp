

INSERT INTO usuarios (
    email,
    contraseña_hash,
    nombre_completo,
    rol,
    activo,
    verificado,
    fecha_registro
) VALUES (
    'admin@fallapp.es',
    '$2a$12$Gj3mGjm.Kj9nKj9nKj9nKoR8RzG7HsT8IuV9JwK0LxM1NyO2PzQ3Rx',
    'Administrador del Sistema',
    'admin'::rol_usuario,
    true,
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO usuarios (
    email,
    contraseña_hash,
    nombre_completo,
    rol,
    activo,
    verificado,
    fecha_registro
) VALUES (
    'demo@fallapp.es',
    '$2a$12$Kj0mKj0mKj9nKj9nKj9nKoR8RzG7HsT8IuV9JwK0LxM1NyO2PzQ3Rx',
    'Usuario Demostración',
    'usuario'::rol_usuario,
    true,
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO usuarios (
    email,
    contraseña_hash,
    nombre_completo,
    rol,
    activo,
    verificado,
    telefono,
    fecha_registro
) VALUES (
    'casal@fallapp.es',
    '$2a$12$Lk1nLk1nKj9nKj9nKj9nKoR8RzG7HsT8IuV9JwK0LxM1NyO2PzQ3Rx',
    'Responsable de Casal',
    'casal'::rol_usuario,
    true,
    false,
    '+34-963-123-456',
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

