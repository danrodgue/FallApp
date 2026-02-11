// =====================================================
// API CLIENT - FallApp Testing Dashboard
// Funciones para comunicarse con el Backend
// =====================================================

const API = {
    // ===== UTILIDADES =====
    
    /**
     * Obtiene el token JWT almacenado
     */
    getToken() {
        return sessionStorage.getItem(CONFIG.STORAGE.TOKEN_KEY);
    },
    
    /**
     * Guarda el token JWT
     */
    setToken(token) {
        sessionStorage.setItem(CONFIG.STORAGE.TOKEN_KEY, token);
    },
    
    /**
     * Elimina el token JWT
     */
    clearToken() {
        sessionStorage.removeItem(CONFIG.STORAGE.TOKEN_KEY);
        sessionStorage.removeItem(CONFIG.STORAGE.USER_KEY);
    },
    
    /**
     * Obtiene la información del usuario
     */
    getUser() {
        const userStr = sessionStorage.getItem(CONFIG.STORAGE.USER_KEY);
        return userStr ? JSON.parse(userStr) : null;
    },
    
    /**
     * Guarda la información del usuario
     */
    setUser(user) {
        sessionStorage.setItem(CONFIG.STORAGE.USER_KEY, JSON.stringify(user));
    },
    
    /**
     * Realiza una petición HTTP a la API
     */
    async request(endpoint, options = {}) {
        const url = `${CONFIG.API_URL}${endpoint}`;
        const token = this.getToken();
        
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...options.headers,
        };
        
        // Añadir token si existe
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const config = {
            ...options,
            headers,
        };
        
        try {
            const response = await fetch(url, config);
            const data = await response.json().catch(() => ({}));
            
            if (!response.ok) {
                throw {
                    status: response.status,
                    message: data.mensaje || data.error?.mensaje || 'Error en la petición',
                    data: data,
                };
            }
            
            return data;
        } catch (error) {
            console.error('Error en petición API:', error);
            throw error;
        }
    },
    
    // ===== AUTENTICACIÓN =====
    
    /**
     * Inicia sesión con email y contraseña
     */
    async login(email, password) {
        try {
            const response = await this.request(CONFIG.ENDPOINTS.LOGIN, {
                method: 'POST',
                body: JSON.stringify({ email, contrasena: password }),
            });
            
            if (response.exito && response.datos) {
                // Guardar token y usuario
                this.setToken(response.datos.token);
                this.setUser({
                    email: email,
                    rol: response.datos.rol,
                    nombre: response.datos.nombre || email.split('@')[0],
                });
                
                return { success: true, data: response.datos };
            }
            
            return { success: false, error: 'Respuesta inválida del servidor' };
        } catch (error) {
            return { 
                success: false, 
                error: error.message || 'Error al iniciar sesión' 
            };
        }
    },
    
    /**
     * Cierra sesión
     */
    logout() {
        this.clearToken();
    },
    
    /**
     * Verifica si hay una sesión activa
     */
    isAuthenticated() {
        return !!this.getToken();
    },
    
    // ===== ESTADO DEL SERVIDOR =====
    
    /**
     * Verifica el estado del servidor API
     */
    async checkServerStatus() {
        try {
            const controller = new AbortController();
            const timeout = setTimeout(() => controller.abort(), CONFIG.TIMEOUTS.API_CHECK);
            
            const response = await fetch(CONFIG.API_URL + CONFIG.ENDPOINTS.HEALTH, {
                method: 'GET',
                signal: controller.signal,
            });
            
            clearTimeout(timeout);
            
            return {
                online: response.ok,
                status: response.status,
                timestamp: new Date().toISOString(),
            };
        } catch (error) {
            return {
                online: false,
                status: 0,
                error: error.name === 'AbortError' ? 'Timeout' : error.message,
                timestamp: new Date().toISOString(),
            };
        }
    },
    
    /**
     * Obtiene información del servidor
     */
    async getServerInfo() {
        try {
            const data = await this.request(CONFIG.ENDPOINTS.INFO);
            return { success: true, data };
        } catch (error) {
            return { success: false, error: error.message };
        }
    },
    
    // ===== ENDPOINTS =====
    
    /**
     * Verifica el estado de un endpoint específico
     */
    async checkEndpoint(method, path) {
        try {
            const controller = new AbortController();
            const timeout = setTimeout(() => controller.abort(), CONFIG.TIMEOUTS.API_CHECK);
            
            const url = `${CONFIG.API_URL}${path}`;
            
            // Para GET hacemos la petición directamente
            // Para otros métodos, solo verificamos que la ruta existe (OPTIONS)
            const response = await fetch(url, {
                method: method === 'GET' ? 'GET' : 'OPTIONS',
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                },
                signal: controller.signal,
            });
            
            clearTimeout(timeout);
            
            return {
                online: response.ok || response.status === 404, // 404 significa que la ruta existe
                status: response.status,
                latency: 0, // Podríamos medir esto si queremos
            };
        } catch (error) {
            return {
                online: false,
                status: 0,
                error: error.message,
            };
        }
    },
    
    /**
     * Verifica todos los endpoints configurados
     */
    async checkAllEndpoints() {
        const results = [];
        
        for (const endpoint of CONFIG.API_ENDPOINTS) {
            const result = await this.checkEndpoint(endpoint.method, endpoint.path);
            results.push({
                ...endpoint,
                ...result,
            });
        }
        
        return results;
    },
    
    // ===== TESTS =====
    
    /**
     * Ejecuta un test específico
     * NOTA: Esto requeriría un endpoint en el backend para ejecutar tests
     * Por ahora, simularemos la ejecución
     */
    async runTest(testId) {
        // En un escenario real, esto haría una petición al backend
        // que ejecutaría el test y devolvería los resultados
        
        return new Promise((resolve) => {
            setTimeout(() => {
                const success = Math.random() > 0.3; // 70% de éxito simulado
                resolve({
                    testId,
                    success,
                    output: success 
                        ? `✓ Test ${testId} completado exitosamente\n\nTodos los casos de prueba pasaron.`
                        : `✗ Test ${testId} falló\n\nError: Algunos casos de prueba no pasaron.`,
                    duration: Math.floor(Math.random() * 3000) + 500,
                    timestamp: new Date().toISOString(),
                });
            }, Math.random() * 2000 + 1000); // Simular ejecución de 1-3 segundos
        });
    },
    
    /**
     * Ejecuta todos los tests de una categoría
     */
    async runTestsByCategory(category) {
        const tests = CONFIG.TESTS[category] || [];
        const results = [];
        
        for (const test of tests) {
            const result = await this.runTest(test.id);
            results.push(result);
        }
        
        return results;
    },
    
    /**
     * Ejecuta todos los tests
     */
    async runAllTests() {
        const allTests = [
            ...CONFIG.TESTS.integration,
            ...CONFIG.TESTS.e2e,
            ...CONFIG.TESTS.performance,
        ];
        
        const results = [];
        
        for (const test of allTests) {
            const result = await this.runTest(test.id);
            results.push(result);
        }
        
        return results;
    },
    
    // ===== RECURSOS (Ejemplos) =====
    
    /**
     * Obtiene la lista de fallas
     */
    async getFallas() {
        try {
            const data = await this.request(CONFIG.ENDPOINTS.FALLAS);
            return { success: true, data: data.datos || [] };
        } catch (error) {
            return { success: false, error: error.message };
        }
    },
    
    /**
     * Obtiene la lista de ubicaciones
     */
    async getUbicaciones() {
        try {
            const data = await this.request(CONFIG.ENDPOINTS.UBICACIONES);
            return { success: true, data: data.datos || [] };
        } catch (error) {
            return { success: false, error: error.message };
        }
    },
};

// Hacer API disponible globalmente
window.API = API;
