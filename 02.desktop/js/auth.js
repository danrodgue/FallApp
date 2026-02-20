const API_BASE_URL =
  window._API_URL ||
  (window._API_BASE ? `${window._API_BASE}/api` : 'http://35.180.21.42:8080/api');
const API_AUTH = `${API_BASE_URL}/auth/login`;

document.addEventListener('DOMContentLoaded', () => {
  const formulario = document.getElementById('login-form');
  const cajaError = document.getElementById('error-msg');
  const campoEmail = document.getElementById('email');
  const campoPassword = document.getElementById('password');

  function mostrarError(mensaje) {
    if (window.validacionFormulario && window.validacionFormulario.pintarError) {
      window.validacionFormulario.pintarError(cajaError, mensaje);
      return;
    }
    cajaError.textContent = mensaje;
    cajaError.style.display = mensaje ? 'block' : 'none';
  }

  function limpiarError() {
    mostrarError('');
  }

  function validarFormulario() {
    const validacion = window.validacionFormulario;
    const email = campoEmail.value;
    const password = campoPassword.value;

    const emailOk = validacion
      ? validacion.validarCampo(campoEmail, validacion.emailValido(email), 'Escribe un email válido.')
      : !!email;

    const passwordOk = validacion
      ? validacion.validarCampo(campoPassword, validacion.passwordValida(password, 1), 'La contraseña es obligatoria.')
      : String(password || '').length > 0;
    if (!emailOk) {
      mostrarError('Escribe un email válido.');
      try { campoEmail.reportValidity(); } catch (e) {}
      return false;
    }

    if (!passwordOk) {
      mostrarError('La contraseña es obligatoria.');
      try { campoPassword.reportValidity(); } catch (e) {}
      return false;
    }

    return true;
  }

  async function autenticarUsuario(email, password) {
    try {
      const respuesta = await fetch(API_AUTH, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: email,
          contrasena: password
        })
      });

      if (!respuesta.ok) {
        if (respuesta.status === 401) {
          return { success: false, message: 'Email o contraseña incorrectos.' };
        }
        if (respuesta.status === 400) {
          return { success: false, message: 'Solicitud inválida. Revisa email y contraseña.' };
        }
        return { success: false, message: `Error del servidor (${respuesta.status})` };
      }

      const datos = await respuesta.json();
      if (!datos.exito || !datos.datos) {
        return { success: false, message: datos.mensaje || 'Error en la autenticación.' };
      }

      const loginData = datos.datos;
      const usuario = loginData.usuario;

      if (!loginData.token || !usuario) {
        return { success: false, message: 'Respuesta del servidor incompleta.' };
      }

      return {
        success: true,
        token: loginData.token,
        user: usuario
      };
    } catch (error) {
      return {
        success: false,
        message: `Error de conexión: ${error.message}`
      };
    }
  }

  formulario.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    limpiarError();

    if (!validarFormulario()) {
      return;
    }

    const email = campoEmail.value.trim();
    const password = campoPassword.value;

    try {
      const resultado = await autenticarUsuario(email, password);

      if (!resultado.success) {
        mostrarError(resultado.message || 'No se ha podido autenticar.');
        return;
      }

      try {
        localStorage.setItem('fallapp_token', resultado.token);
        localStorage.setItem('fallapp_user_id', resultado.user.idUsuario);
        localStorage.setItem('fallapp_user_email', resultado.user.email);
        localStorage.setItem('fallapp_user_nombre', resultado.user.nombreCompleto);
        localStorage.setItem('fallapp_user_rol', resultado.user.rol);
        localStorage.setItem('fallapp_user_idFalla', resultado.user.idFalla || '');
        localStorage.setItem('fallapp_user', resultado.user.email);
      } catch (errorStorage) {
        console.error('Error guardando datos en localStorage:', errorStorage);
      }

      if (resultado.user.rol !== 'casal') {
        mostrarError('Acceso denegado: Solo usuarios de casal pueden acceder.');
        return;
      }

      window.location.href = '../screens/home.html';
    } catch (error) {
      console.error('Error en autenticación:', error);
      mostrarError('Error al autenticar. Inténtalo de nuevo.');
    }
  });
});
