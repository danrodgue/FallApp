document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('login-form');
    const errorMsg = document.getElementById('error-msg');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideError();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        try {
            const ok = await fakeAuthenticate(username, password);
            if (ok) {
                // Guardar el usuario logueado para usarlo en otras pantallas (ej. eventos)
                try{ localStorage.setItem('fallapp_user', username); }catch(e){}
                window.location.href = 'screens/home.html';
            } else {
                showError('No se ha podido autenticar. Usuario o contraseña incorrectos.');
            }
        } catch (err) {
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

    // Simula una llamada async al servidor. Cambia la lógica por tu API real.
    function fakeAuthenticate(user, pass) {
        return new Promise((resolve) => {
            setTimeout(() => {
                // Reglas de prueba: usuario 'admin' + '1234' o cualquier usuario con contraseña 'password'
                if ((user === 'admin' && pass === '1234') || pass === 'password') {
                    resolve(true);
                } else {
                    resolve(false);
                }
            }, 700);
        });
    }
});
