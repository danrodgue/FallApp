// API base URL - sincronizado con config.js
const API_BASE = 'http://35.180.21.42:8080/api';
const API_AUTH = `${API_BASE}/auth/login`;

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('login-form');
    const errorMsg = document.getElementById('error-msg');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideError();

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        try {
            const result = await authenticateUser(email, password);
            if (result.success) {
                // Guardar datos en localStorage
                try {
                    localStorage.setItem('fallapp_token', result.token);
                    localStorage.setItem('fallapp_user_id', result.user.idUsuario);
                    localStorage.setItem('fallapp_user_email', result.user.email);
                    localStorage.setItem('fallapp_user_nombre', result.user.nombreCompleto);
                    localStorage.setItem('fallapp_user_rol', result.user.rol);
                    localStorage.setItem('fallapp_user_idFalla', result.user.idFalla || '');
                    localStorage.setItem('fallapp_user', result.user.email); // Para compatibilidad
                } catch (e) {
                    console.error('Error guardando datos en localStorage:', e);
                }

                // Verificar que el usuario es casal
                if (result.user.rol !== 'casal') {
                    showError('Acceso denegado: Solo usuarios de casal pueden acceder.');
                    return;
                }

                // Redirigir a home.html
                window.location.href = '../screens/home.html';
            } else {
                showError(result.message || 'No se ha podido autenticar.');
            }
        } catch (err) {
            console.error('Error en autenticación:', err);
            showError('Error al autenticar. Inténtalo de nuevo.');
        }
    });

    function showError(msg) {
        errorMsg.textContent = msg;
        errorMsg.style.display = 'block';
    }

    function hideError() {
        errorMsg.textContent = '';
        errorMsg.style.display = 'none';
    }

    /**
     * Autentica el usuario contra la API real
     * @param {string} email Email del usuario
     * @param {string} password Contraseña
     * @returns {Promise<{success: boolean, token?: string, user?: object, message?: string}>}
     */
    async function authenticateUser(email, password) {
        try {
            const response = await fetch(API_AUTH, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    contrasena: password
                })
            });

            console.log('Response status:', response.status);

            if (!response.ok) {
                // Manejar errores HTTP
                if (response.status === 401) {
                    return { success: false, message: 'Email o contraseña incorrectos.' };
                } else if (response.status === 400) {
                    return { success: false, message: 'Solicitud inválida. Verifica email y contraseña.' };
                } else {
                    return { success: false, message: `Error del servidor (${response.status})` };
                }
            }

            const data = await response.json();
            console.log('Auth response:', data);

            // Verificar estructura de respuesta (ApiResponse)
            if (!data.exito || !data.datos) {
                return { success: false, message: data.mensaje || 'Error en la autenticación.' };
            }

            const loginData = data.datos;
            const usuario = loginData.usuario;

            // Validar que tenemos los datos necesarios
            if (!loginData.token || !usuario) {
                return { success: false, message: 'Respuesta del servidor incompleta.' };
            }

            return {
                success: true,
                token: loginData.token,
                user: usuario
            };
        } catch (err) {
            console.error('Error en fetch:', err);
            return {
                success: false,
                message: `Error de conexión: ${err.message}`
            };
        }
    }
});
