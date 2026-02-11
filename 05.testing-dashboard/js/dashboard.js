// =====================================================
// DASHBOARD - FallApp Testing Dashboard
// Lógica principal de la interfaz
// =====================================================

const Dashboard = {
    // Estado
    state: {
        serverStatus: null,
        endpoints: [],
        tests: [],
        testResults: [],
        autoRefresh: true,
        refreshInterval: null,
    },
    
    // ===== INICIALIZACIÓN =====
    
    init() {
        console.log('🚀 Inicializando Dashboard...');
        
        // Verificar autenticación
        if (API.isAuthenticated()) {
            this.showDashboard();
        } else {
            this.showLogin();
        }
        
        // Event listeners
        this.setupEventListeners();
    },
    
    setupEventListeners() {
        // Login
        document.getElementById('login-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleLogin();
        });
        
        // Logout
        document.getElementById('logout-btn').addEventListener('click', () => {
            this.handleLogout();
        });
        
        // Refresh
        document.getElementById('refresh-btn').addEventListener('click', () => {
            this.refreshAll();
        });
        
        // Endpoints
        document.getElementById('test-all-endpoints').addEventListener('click', () => {
            this.testAllEndpoints();
        });
        
        // Tests
        document.getElementById('run-all-tests').addEventListener('click', () => {
            this.runAllTests();
        });
        
        document.getElementById('clear-results').addEventListener('click', () => {
            this.clearTestResults();
        });
        
        document.getElementById('clear-logs').addEventListener('click', () => {
            this.clearLogs();
        });
        
        // Test por categoría
        document.querySelectorAll('.test-group-header button').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const category = e.target.getAttribute('data-category');
                this.runTestsByCategory(category);
            });
        });
    },
    
    // ===== NAVEGACIÓN =====
    
    showLogin() {
        document.getElementById('login-screen').classList.add('active');
        document.getElementById('dashboard-screen').classList.remove('active');
    },
    
    showDashboard() {
        document.getElementById('login-screen').classList.remove('active');
        document.getElementById('dashboard-screen').classList.add('active');
        
        // Cargar datos iniciales
        this.loadDashboardData();
        
        // Setup auto-refresh
        this.setupAutoRefresh();
        
        // Mostrar info del usuario
        this.updateUserInfo();
    },
    
    // ===== AUTENTICACIÓN =====
    
    async handleLogin() {
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const errorEl = document.getElementById('login-error');
        
        errorEl.style.display = 'none';
        
        this.log('info', `Intentando iniciar sesión: ${email}`);
        
        const result = await API.login(email, password);
        
        if (result.success) {
            this.log('success', `Sesión iniciada correctamente`);
            this.showDashboard();
        } else {
            errorEl.textContent = result.error;
            errorEl.style.display = 'block';
            this.log('error', `Error al iniciar sesión: ${result.error}`);
        }
    },
    
    handleLogout() {
        API.logout();
        this.log('info', 'Sesión cerrada');
        this.showLogin();
        this.stopAutoRefresh();
    },
    
    updateUserInfo() {
        const user = API.getUser();
        if (user) {
            document.getElementById('user-info').textContent = `${user.nombre} (${user.rol})`;
        }
    },
    
    // ===== CARGA DE DATOS =====
    
    async loadDashboardData() {
        this.log('info', 'Cargando datos del dashboard...');
        
        await Promise.all([
            this.checkServerStatus(),
            this.loadEndpoints(),
            this.loadTests(),
        ]);
        
        this.log('success', 'Dashboard cargado correctamente');
    },
    
    async refreshAll() {
        this.log('info', 'Actualizando todos los datos...');
        
        // Rotar el botón
        const btn = document.getElementById('refresh-btn');
        btn.style.transform = 'rotate(360deg)';
        setTimeout(() => btn.style.transform = '', 300);
        
        await this.loadDashboardData();
    },
    
    // ===== SERVIDOR =====
    
    async checkServerStatus() {
        const status = await API.checkServerStatus();
        this.state.serverStatus = status;
        
        // Actualizar UI
        const badge = document.getElementById('server-status-badge');
        const apiStatus = document.getElementById('api-status');
        const dbStatus = document.getElementById('db-status');
        const portEl = document.getElementById('api-port');
        const lastCheck = document.getElementById('last-check');
        
        if (status.online) {
            badge.className = 'badge online';
            badge.textContent = 'Online';
            apiStatus.textContent = '✅ Activo';
            apiStatus.style.color = 'var(--color-success)';
            dbStatus.textContent = '✅ Conectada';
            dbStatus.style.color = 'var(--color-success)';
            this.log('success', 'Servidor API: Online');
        } else {
            badge.className = 'badge offline';
            badge.textContent = 'Offline';
            apiStatus.textContent = '❌ Inactivo';
            apiStatus.style.color = 'var(--color-error)';
            dbStatus.textContent = '❓ Desconocido';
            dbStatus.style.color = 'var(--color-text-muted)';
            this.log('error', `Servidor API: Offline (${status.error || 'Sin respuesta'})`);
        }
        
        portEl.textContent = CONFIG.API_URL.split(':').pop().split('/')[0];
        lastCheck.textContent = new Date().toLocaleTimeString('es-ES');
    },
    
    // ===== ENDPOINTS =====
    
    async loadEndpoints() {
        const container = document.getElementById('endpoints-list');
        container.innerHTML = '<div class="loading">Verificando endpoints...</div>';
        
        // Verificar solo algunos endpoints principales para no saturar
        const mainEndpoints = CONFIG.API_ENDPOINTS.slice(0, 10);
        
        const results = [];
        for (const endpoint of mainEndpoints) {
            const result = await API.checkEndpoint(endpoint.method, endpoint.path);
            results.push({ ...endpoint, ...result });
        }
        
        this.state.endpoints = results;
        this.renderEndpoints(results);
    },
    
    renderEndpoints(endpoints) {
        const container = document.getElementById('endpoints-list');
        
        if (endpoints.length === 0) {
            container.innerHTML = '<div class="loading">No hay endpoints configurados</div>';
            return;
        }
        
        container.innerHTML = endpoints.map(ep => `
            <div class="endpoint-item ${ep.online ? 'active' : 'inactive'}">
                <div class="endpoint-info">
                    <span class="endpoint-method ${ep.method.toLowerCase()}">${ep.method}</span>
                    <span class="endpoint-path">${ep.path}</span>
                    <small style="color: var(--color-text-muted);">${ep.description}</small>
                </div>
                <div class="endpoint-status">
                    <span class="status-indicator ${ep.online ? 'online' : 'offline'}"></span>
                    <span style="font-size: 12px; color: var(--color-text-muted);">
                        ${ep.online ? 'Disponible' : 'No disponible'}
                    </span>
                </div>
            </div>
        `).join('');
    },
    
    async testAllEndpoints() {
        this.log('info', 'Probando todos los endpoints...');
        await this.loadEndpoints();
        this.log('success', 'Verificación de endpoints completada');
    },
    
    // ===== TESTS =====
    
    loadTests() {
        this.renderTestList('integration-tests', CONFIG.TESTS.integration);
        this.renderTestList('e2e-tests', CONFIG.TESTS.e2e);
        this.renderTestList('performance-tests', CONFIG.TESTS.performance);
    },
    
    renderTestList(containerId, tests) {
        const container = document.getElementById(containerId);
        
        if (tests.length === 0) {
            container.innerHTML = '<div style="color: var(--color-text-muted); font-size: 14px;">No hay tests disponibles</div>';
            return;
        }
        
        container.innerHTML = tests.map(test => `
            <div class="test-item" data-test-id="${test.id}">
                <span class="test-name">${test.name}</span>
                <span class="test-status" data-status="pending">Pendiente</span>
            </div>
        `).join('');
    },
    
    async runTestsByCategory(category) {
        const tests = CONFIG.TESTS[category] || [];
        
        if (tests.length === 0) {
            this.log('warning', `No hay tests en la categoría: ${category}`);
            return;
        }
        
        this.log('info', `Ejecutando tests de categoría: ${category}`);
        
        for (const test of tests) {
            await this.runTest(test);
        }
        
        this.log('success', `Tests de ${category} completados`);
    },
    
    async runAllTests() {
        this.log('info', 'Ejecutando todos los tests...');
        
        const allTests = [
            ...CONFIG.TESTS.integration,
            ...CONFIG.TESTS.e2e,
            ...CONFIG.TESTS.performance,
        ];
        
        // Mostrar resumen
        document.getElementById('test-summary').style.display = 'grid';
        document.getElementById('test-results').style.display = 'block';
        
        // Reset contadores
        this.state.testResults = [];
        this.updateTestSummary();
        
        const startTime = Date.now();
        
        for (const test of allTests) {
            await this.runTest(test);
        }
        
        const duration = Date.now() - startTime;
        
        // Actualizar duración
        document.getElementById('tests-duration').textContent = `${duration}ms`;
        
        this.log('success', `Todos los tests completados en ${duration}ms`);
    },
    
    async runTest(test) {
        // Marcar test como running
        const testEl = document.querySelector(`[data-test-id="${test.id}"]`);
        if (testEl) {
            testEl.classList.add('running');
            const statusEl = testEl.querySelector('.test-status');
            statusEl.textContent = '⏳ Ejecutando...';
            statusEl.className = 'test-status running';
        }
        
        this.log('info', `Ejecutando test: ${test.name}`);
        
        // Ejecutar test
        const result = await API.runTest(test.id);
        this.state.testResults.push(result);
        
        // Actualizar UI
        if (testEl) {
            testEl.classList.remove('running');
            testEl.classList.add(result.success ? 'passed' : 'failed');
            
            const statusEl = testEl.querySelector('.test-status');
            statusEl.textContent = result.success ? '✅ Pasado' : '❌ Fallado';
            statusEl.className = `test-status ${result.success ? 'passed' : 'failed'}`;
        }
        
        // Añadir resultado a output
        this.addTestOutput(test, result);
        
        // Actualizar resumen
        this.updateTestSummary();
        
        if (result.success) {
            this.log('success', `Test ${test.name}: PASS (${result.duration}ms)`);
        } else {
            this.log('error', `Test ${test.name}: FAIL`);
        }
    },
    
    addTestOutput(test, result) {
        const outputEl = document.getElementById('test-output');
        
        const timestamp = new Date(result.timestamp).toLocaleTimeString('es-ES');
        const icon = result.success ? '✅' : '❌';
        const status = result.success ? 'PASS' : 'FAIL';
        
        const output = `
[${timestamp}] ${icon} ${test.name} - ${status} (${result.duration}ms)
${result.output}

---------------------------------------------------
`;
        
        outputEl.textContent += output;
        outputEl.scrollTop = outputEl.scrollHeight;
    },
    
    updateTestSummary() {
        const passed = this.state.testResults.filter(r => r.success).length;
        const failed = this.state.testResults.filter(r => !r.success).length;
        const total = this.state.testResults.length;
        
        document.getElementById('tests-passed').textContent = passed;
        document.getElementById('tests-failed').textContent = failed;
        document.getElementById('tests-total').textContent = total;
    },
    
    clearTestResults() {
        this.state.testResults = [];
        document.getElementById('test-output').textContent = '';
        document.getElementById('test-summary').style.display = 'none';
        document.getElementById('test-results').style.display = 'none';
        
        // Reset UI de tests
        document.querySelectorAll('.test-item').forEach(el => {
            el.className = 'test-item';
            const statusEl = el.querySelector('.test-status');
            statusEl.textContent = 'Pendiente';
            statusEl.className = 'test-status';
        });
        
        this.log('info', 'Resultados de tests limpiados');
    },
    
    // ===== LOGS =====
    
    log(type, message) {
        const logEl = document.getElementById('activity-log');
        const time = new Date().toLocaleTimeString('es-ES');
        
        const entry = document.createElement('div');
        entry.className = `log-entry ${type}`;
        entry.innerHTML = `
            <span class="log-time">${time}</span>
            <span class="log-message">${message}</span>
        `;
        
        logEl.appendChild(entry);
        logEl.scrollTop = logEl.scrollHeight;
        
        // Limitar a 100 entradas
        while (logEl.children.length > 100) {
            logEl.removeChild(logEl.firstChild);
        }
        
        console.log(`[${type.toUpperCase()}] ${message}`);
    },
    
    clearLogs() {
        document.getElementById('activity-log').innerHTML = '';
        this.log('info', 'Registro de actividad limpiado');
    },
    
    // ===== AUTO-REFRESH =====
    
    setupAutoRefresh() {
        if (this.state.refreshInterval) {
            clearInterval(this.state.refreshInterval);
        }
        
        this.state.refreshInterval = setInterval(() => {
            if (this.state.autoRefresh) {
                this.checkServerStatus();
            }
        }, CONFIG.TIMEOUTS.AUTO_REFRESH);
    },
    
    stopAutoRefresh() {
        if (this.state.refreshInterval) {
            clearInterval(this.state.refreshInterval);
            this.state.refreshInterval = null;
        }
    },
};

// ===== INICIALIZACIÓN =====

// Esperar a que el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => Dashboard.init());
} else {
    Dashboard.init();
}

// Hacer Dashboard disponible globalmente
window.Dashboard = Dashboard;
