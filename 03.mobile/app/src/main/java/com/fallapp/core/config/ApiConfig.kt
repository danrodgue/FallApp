package com.fallapp.core.config

/**
 * Configuración centralizada de la API REST.
 * 
 * IMPORTANTE: Cuando cambie la IP del servidor AWS, 
 * solo necesitas modificar BASE_URL aquí.
 * 
 * @author Equipo FallApp
 * @since 1.0
 */
object ApiConfig {
    
    // ============================================================
    // URL BASE DEL SERVIDOR
    // ============================================================
    
    /**
     * URL base de la API REST.
     * 
     * DESARROLLO: http://35.180.21.42:8080
     * PRODUCCIÓN: https://api.fallapp.es (futuro)
     * 
     * Para cambiar el servidor, modifica SOLO esta constante.
     */
    const val BASE_URL = "http://35.180.21.42:8080"
    
    /**
     * Path base de la API (se añade a BASE_URL)
     */
    const val API_PATH = "/api"
    
    /**
     * URL completa de la API (BASE_URL + API_PATH)
     */
    const val API_URL = "$BASE_URL$API_PATH"
    
    // ============================================================
    // TIMEOUTS (en milisegundos)
    // ============================================================
    
    /** Timeout para establecer conexión */
    const val CONNECT_TIMEOUT_MS = 30_000L
    
    /** Timeout para leer respuesta */
    const val READ_TIMEOUT_MS = 30_000L
    
    /** Timeout para enviar request */
    const val WRITE_TIMEOUT_MS = 30_000L
    
    /** Timeout total de la request */
    const val REQUEST_TIMEOUT_MS = 60_000L
    
    // ============================================================
    // CONFIGURACIÓN JWT
    // ============================================================
    
    /** Prefijo del token en el header Authorization */
    const val TOKEN_PREFIX = "Bearer "
    
    /** Nombre del header de autorización */
    const val AUTH_HEADER = "Authorization"
    
    /** Duración del token en horas (para referencia) */
    const val TOKEN_EXPIRATION_HOURS = 24
    
    // ============================================================
    // PAGINACIÓN
    // ============================================================
    
    /** Tamaño de página por defecto */
    const val DEFAULT_PAGE_SIZE = 20
    
    /** Tamaño máximo de página permitido */
    const val MAX_PAGE_SIZE = 100
    
    /** Índice de la primera página (0-indexed) */
    const val FIRST_PAGE_INDEX = 0
    
    // ============================================================
    // ENDPOINTS
    // ============================================================
    
    /**
     * Objeto con todas las rutas de la API.
     * Uso: ApiConfig.Endpoints.LOGIN
     */
    object Endpoints {
        
        // -------------------- AUTH --------------------
        
        /** POST - Login de usuario */
        const val LOGIN = "$API_PATH/auth/login"
        
        /** POST - Registro de nuevo usuario */
        const val REGISTER = "$API_PATH/auth/registro"
        
        // -------------------- FALLAS --------------------
        
        /** GET - Listar fallas (paginado) */
        const val FALLAS = "$API_PATH/fallas"
        
        /** GET - Buscar fallas por texto */
        const val FALLAS_BUSCAR = "$API_PATH/fallas/buscar"
        
        /** GET - Fallas cercanas a ubicación */
        const val FALLAS_CERCANAS = "$API_PATH/fallas/cercanas"
        
        /** GET - Fallas por sección */
        const val FALLAS_SECCION = "$API_PATH/fallas/seccion"
        
        /** GET - Fallas por categoría */
        const val FALLAS_CATEGORIA = "$API_PATH/fallas/categoria"
        
        /** Construye ruta para falla específica */
        fun fallaById(id: Long) = "$FALLAS/$id"
        
        // -------------------- EVENTOS --------------------
        
        /** GET - Listar eventos */
        const val EVENTOS = "$API_PATH/eventos"
        
        /** GET - Eventos futuros */
        const val EVENTOS_FUTUROS = "$API_PATH/eventos/futuros"
        
        /** GET - Próximos N eventos */
        const val EVENTOS_PROXIMOS = "$API_PATH/eventos/proximos"
        
        /** Construye ruta para evento específico */
        fun eventoById(id: Long) = "$EVENTOS/$id"
        
        /** Construye ruta para eventos de una falla */
        fun eventosByFalla(fallaId: Long) = "$EVENTOS/falla/$fallaId"
        
        // -------------------- NINOTS --------------------
        
        /** GET - Listar ninots */
        const val NINOTS = "$API_PATH/ninots"
        
        /** GET - Ninots premiados */
        const val NINOTS_PREMIADOS = "$API_PATH/ninots/premiados"
        
        /** Construye ruta para ninot específico */
        fun ninotById(id: Long) = "$NINOTS/$id"
        
        /** Construye ruta para ninots de una falla */
        fun ninotsByFalla(fallaId: Long) = "$NINOTS/falla/$fallaId"
        
        // -------------------- VOTOS --------------------
        
        /** POST - Crear voto / GET - Mis votos */
        const val VOTOS = "$API_PATH/votos"
        
        /** GET - Mis votos */
        const val MIS_VOTOS = "$API_PATH/votos/mis-votos"
        
        // -------------------- COMENTARIOS --------------------
        
        /** GET/POST - Comentarios */
        const val COMENTARIOS = "$API_PATH/comentarios"
        
        /** Construye ruta para comentario específico */
        fun comentarioById(id: Long) = "$COMENTARIOS/$id"
        
        // -------------------- USUARIOS --------------------
        
        /** GET/PUT - Perfil del usuario actual */
        const val PERFIL = "$API_PATH/usuarios/perfil"
        
        // -------------------- ADMIN --------------------
        
        /** GET - Health check del servidor */
        const val HEALTH = "/actuator/health"
        
        /** GET - Métricas del servidor */
        const val METRICS = "/actuator/metrics"
        
        /** GET - Info de la aplicación */
        const val INFO = "/actuator/info"
        
        /** GET - Listar usuarios (solo admin) */
        const val ADMIN_USUARIOS = "$API_PATH/admin/usuarios"
        
        /** GET - Estadísticas del sistema (solo admin) */
        const val ADMIN_STATS = "$API_PATH/admin/stats"
        
        /** Construye ruta para usuario específico (admin) */
        fun adminUsuarioById(id: Long) = "$ADMIN_USUARIOS/$id"
        
        /** Construye ruta para banear usuario */
        fun adminBanUsuario(id: Long) = "$ADMIN_USUARIOS/$id/ban"
        
        /** Construye ruta para desbanear usuario */
        fun adminUnbanUsuario(id: Long) = "$ADMIN_USUARIOS/$id/unban"
    }
    
    // ============================================================
    // CONFIGURACIÓN DE MAPAS
    // ============================================================
    
    object Maps {
        /** Latitud por defecto (Valencia centro) */
        const val DEFAULT_LATITUDE = 39.4699
        
        /** Longitud por defecto (Valencia centro) */
        const val DEFAULT_LONGITUDE = -0.3763
        
        /** Zoom por defecto para el mapa */
        const val DEFAULT_ZOOM = 14f
        
        /** Zoom para ver detalle de falla */
        const val DETAIL_ZOOM = 17f
        
        /** Radio de búsqueda por defecto (km) */
        const val DEFAULT_SEARCH_RADIUS_KM = 5.0
    }
    
    // ============================================================
    // CONFIGURACIÓN DE CACHÉ
    // ============================================================
    
    object Cache {
        /** Tiempo de validez del caché de fallas (milisegundos) */
        const val FALLAS_CACHE_VALIDITY_MS = 15 * 60 * 1000L  // 15 minutos
        
        /** Tiempo de validez del caché de eventos (milisegundos) */
        const val EVENTOS_CACHE_VALIDITY_MS = 5 * 60 * 1000L  // 5 minutos
        
        /** Tiempo de validez del caché de ninots (milisegundos) */
        const val NINOTS_CACHE_VALIDITY_MS = 30 * 60 * 1000L  // 30 minutos
    }
}
