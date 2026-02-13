// =====================================================
// CONFIGURACIÓN - FallApp Testing Dashboard
// =====================================================

const CONFIG = {
    // URL de la API Backend
    API_URL: 'http://35.180.21.42:8080/api',
    
    // Endpoints principales
    ENDPOINTS: {
        // Autenticación
        LOGIN: '/auth/iniciar-sesion',
        LOGOUT: '/auth/cerrar-sesion',
        
        // Salud del sistema
        HEALTH: '/health',
        INFO: '/actuator/info',
        
        // Recursos principales
        FALLAS: '/fallas',
        USUARIOS: '/usuarios',
        EVENTOS: '/eventos',
        NINOTS: '/ninots',
        VOTACIONES: '/votaciones',
        UBICACIONES: '/ubicaciones',
        
        // Tests
        TESTS_RUN: '/admin/tests/run',
        TESTS_RESULTS: '/admin/tests/results',
    },
    
    // Lista completa de endpoints para monitoreo
    API_ENDPOINTS: [
        // Públicos
        { method: 'POST', path: '/auth/iniciar-sesion', public: true, description: 'Iniciar sesión' },
        { method: 'POST', path: '/auth/registrar', public: true, description: 'Registrar usuario' },
        { method: 'GET', path: '/fallas', public: true, description: 'Listar fallas' },
        { method: 'GET', path: '/fallas/{id}', public: true, description: 'Obtener falla por ID' },
        { method: 'GET', path: '/eventos', public: true, description: 'Listar eventos' },
        { method: 'GET', path: '/ninots', public: true, description: 'Listar ninots' },
        { method: 'GET', path: '/ubicaciones', public: true, description: 'Listar ubicaciones GPS' },
        
        // Protegidos - Usuario
        { method: 'POST', path: '/votaciones', public: false, role: 'USUARIO', description: 'Votar ninot' },
        { method: 'GET', path: '/votaciones/mis-votos', public: false, role: 'USUARIO', description: 'Mis votos' },
        { method: 'GET', path: '/usuarios/perfil', public: false, role: 'USUARIO', description: 'Ver perfil' },
        
        // Protegidos - Casal
        { method: 'POST', path: '/eventos', public: false, role: 'CASAL', description: 'Crear evento' },
        { method: 'PUT', path: '/eventos/{id}', public: false, role: 'CASAL', description: 'Actualizar evento' },
        { method: 'DELETE', path: '/eventos/{id}', public: false, role: 'CASAL', description: 'Eliminar evento' },
        { method: 'POST', path: '/ninots', public: false, role: 'CASAL', description: 'Crear ninot' },
        { method: 'PUT', path: '/ninots/{id}', public: false, role: 'CASAL', description: 'Actualizar ninot' },
        
        // Protegidos - Admin
        { method: 'GET', path: '/usuarios', public: false, role: 'ADMIN', description: 'Listar usuarios' },
        { method: 'POST', path: '/fallas', public: false, role: 'ADMIN', description: 'Crear falla' },
        { method: 'PUT', path: '/fallas/{id}', public: false, role: 'ADMIN', description: 'Actualizar falla' },
        { method: 'DELETE', path: '/fallas/{id}', public: false, role: 'ADMIN', description: 'Eliminar falla' },
        { method: 'DELETE', path: '/usuarios/{id}', public: false, role: 'ADMIN', description: 'Eliminar usuario' },
    ],
    
    // Tests disponibles
    TESTS: {
        integration: [
            { id: 'test_01', name: 'Schema Creation', file: 'test_01_schema_creation.sql', category: 'integration' },
            { id: 'test_02', name: 'Data Integrity', file: 'test_02_data_integrity.sql', category: 'integration' },
            { id: 'test_03', name: 'Views & Functions', file: 'test_03_views_functions.sql', category: 'integration' },
            { id: 'test_04', name: 'Triggers', file: 'test_04_triggers.sql', category: 'integration' },
            { id: 'test_05', name: 'Ubicaciones GPS', file: 'test_05_ubicaciones_gps.sql', category: 'integration' },
        ],
        e2e: [
            { id: 'test_docker', name: 'Docker Compose', file: 'test_docker_compose.sh', category: 'e2e' },
            { id: 'test_postgres', name: 'PostgreSQL Connection', file: 'test_postgres_connection.sh', category: 'e2e' },
            { id: 'test_persistence', name: 'Data Persistence', file: 'test_data_persistence.sh', category: 'e2e' },
            { id: 'test_ubicaciones_api', name: 'API Ubicaciones', file: 'test_api_ubicaciones.sh', category: 'e2e' },
        ],
        performance: [
            { id: 'test_perf_ubicaciones', name: 'Endpoint Ubicaciones Performance', file: 'test_ubicaciones_performance.sh', category: 'performance' },
        ],
    },
    
    // Configuración de timeouts
    TIMEOUTS: {
        API_CHECK: 5000,        // 5 segundos
        TEST_EXECUTION: 120000, // 2 minutos
        AUTO_REFRESH: 30000,    // 30 segundos
    },
    
    // Almacenamiento
    STORAGE: {
        TOKEN_KEY: 'fallapp_dashboard_token',
        USER_KEY: 'fallapp_dashboard_user',
    },
};

// Hacer CONFIG disponible globalmente
window.CONFIG = CONFIG;
