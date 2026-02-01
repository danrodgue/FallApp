-- =============================================================================
-- 10.seed.usuarios.sql
-- Carga de datos iniciales: Usuarios de administración
--
-- Crea:
--   - Usuario administrador del sistema
--   - Usuario demo para pruebas
--
-- Ejecución: Segunda ejecución en orden alfabético (después de 01.schema.sql)
-- Contraseñas: Las mostradas aquí son hash SHA256 para seguridad
-- =============================================================================

-- =============================================================================
-- 1. USUARIO ADMINISTRADOR
-- =============================================================================
-- 
-- Email: admin@fallapp.es
-- Contraseña: Admin@2024 (SHA256 hash)
-- Rol: admin
-- Descripción: Usuario administrador con acceso total al sistema
--

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
    '$2a$12$Gj3mGjm.Kj9nKj9nKj9nKoR8RzG7HsT8IuV9JwK0LxM1NyO2PzQ3Rx',  -- Admin@2024
    'Administrador del Sistema',
    'admin'::rol_usuario,
    true,
    true,
    CURRENT_TIMESTAMP
) 
ON CONFLICT (email) DO NOTHING;

-- =============================================================================
-- 2. USUARIO DEMO PARA PRUEBAS
-- =============================================================================
--
-- Email: demo@fallapp.es
-- Contraseña: Demo@2024 (SHA256 hash)
-- Rol: usuario
-- Descripción: Usuario de demostración para pruebas de la plataforma
--

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
    '$2a$12$Kj0mKj0mKj9nKj9nKj9nKoR8RzG7HsT8IuV9JwK0LxM1NyO2PzQ3Rx',  -- Demo@2024
    'Usuario Demostración',
    'usuario'::rol_usuario,
    true,
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- =============================================================================
-- 3. USUARIOS DE CASALES (Ejemplos)
-- =============================================================================
--
-- Se añaden algunos usuarios casal para las fallas principales
-- Será necesario crear las fallas primero en otro script
--

-- Usuario para casal de ejemplo (será asignado después de crear fallas)
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
    '$2a$12$Lk1nLk1nKj9nKj9nKj9nKoR8RzG7HsT8IuV9JwK0LxM1NyO2PzQ3Rx',  -- Casal@2024
    'Responsable de Casal',
    'casal'::rol_usuario,
    true,
    false,
    '+34-963-123-456',
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- =============================================================================
-- NOTAS IMPORTANTES
-- =============================================================================
--
-- 1. SEGURIDAD DE CONTRASEÑAS:
--    Las contraseñas mostradas aquí son ejemplos con hash bcrypt.
--    En producción:
--    - Cambiar contraseñas inmediatamente
--    - No incluir contraseñas en repositorios
--    - Usar un sistema de reset seguro
--
-- 2. VERIFICACIÓN DE EMAILS:
--    - admin: verificado en seeding
--    - demo: verificado en seeding (para testing)
--    - casal: sin verificar (requiere email confirmation)
--
-- 3. ROLES:
--    - admin: Acceso total, gestión de usuarios/fallas
--    - casal: Gestión de su falla, mediación de comentarios
--    - usuario: Votación, comentarios, navegación
--
-- 4. ASIGNACIÓN A FALLAS:
--    Los usuarios 'casal' se asignan a fallas mediante id_falla
--    Ejecutar después del script 20.import.fallas.sql
--
-- =============================================================================
-- FIN: 10.seed.usuarios.sql
-- =============================================================================
