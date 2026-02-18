// Base de la API: preferimos la misma configuración que usa el resto de la app (config.js)
const API_BASE_URL =
  window._API_URL || // normalmente algo como "http://HOST:8080/api"
  (window._API_BASE ? `${window._API_BASE}/api` : 'http://35.180.21.42:8080/api');

const API_AUTH_REGISTRO = `${API_BASE_URL}/auth/registro`;
const API_FALLAS_URL = `${API_BASE_URL}/fallas`;
const API_FALLAS_BUSCAR = `${API_BASE_URL}/fallas/buscar`;
const API_USUARIOS_BASE = `${API_BASE_URL}/usuarios`;

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('register-form');
  const errorMsg = document.getElementById('register-error-msg');
  const registerBtn = document.getElementById('registerBtn');

  const fallaSearchInput = document.getElementById('fallaSearch');
  const fallaResultsEl = document.getElementById('fallaResults');
  const fallaSelectedEl = document.getElementById('fallaSelected');
  const idFallaInput = document.getElementById('idFalla');

  const avatarFileInput = document.getElementById('avatarFile');
  const avatarPreview = document.getElementById('avatarPreview');
  const avatarInitialsPreview = document.getElementById('avatarInitialsPreview');

  let allFallasCache = null;
  let fallaSearchTimeout = null;

  function showError(message) {
    errorMsg.textContent = message;
    errorMsg.style.display = 'block';
  }

  function clearError() {
    errorMsg.textContent = '';
    errorMsg.style.display = 'none';
  }

  function updateAvatarInitialsFromName() {
    const nombre = document.getElementById('nombreCompleto').value.trim();
    if (!nombre) {
      avatarInitialsPreview.textContent = 'C';
      return;
    }
    const initials = nombre
      .split(' ')
      .filter(Boolean)
      .map(p => p[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
    avatarInitialsPreview.textContent = initials || 'C';
  }

  // Avatar: previsualización básica, se puede reutilizar más adelante en la app
  if (avatarFileInput && avatarPreview) {
    avatarFileInput.addEventListener('change', () => {
      const file = avatarFileInput.files[0];
      if (!file) {
        avatarPreview.innerHTML = '';
        avatarPreview.appendChild(avatarInitialsPreview);
        return;
      }
      const img = document.createElement('img');
      img.onload = () => {
        avatarPreview.innerHTML = '';
        avatarPreview.appendChild(img);
      };
      img.onerror = () => {
        avatarPreview.innerHTML = '';
        avatarPreview.appendChild(avatarInitialsPreview);
      };
      img.src = URL.createObjectURL(file);
    });
  }

  const nombreInput = document.getElementById('nombreCompleto');
  if (nombreInput) {
    nombreInput.addEventListener('input', updateAvatarInitialsFromName);
  }

  async function loadAllFallas() {
    if (allFallasCache) {
      return allFallasCache;
    }

    try {
      const url = `${API_FALLAS_URL}?pagina=0&tamano=400`;
      console.log('[Registro Casal] Cargando fallas desde:', url);
      const res = await fetch(url);

      if (!res.ok) {
        console.error('[Registro Casal] Error HTTP al obtener fallas:', res.status, res.statusText);
        throw new Error(`HTTP ${res.status}`);
      }

      const data = await res.json();
      const contenido = data.datos && data.datos.contenido ? data.datos.contenido : [];
      allFallasCache = Array.isArray(contenido) ? contenido : [];
      console.log('[Registro Casal] Fallas cargadas para búsqueda local:', allFallasCache.length);
    } catch (err) {
      console.error('[Registro Casal] Error cargando listado de fallas:', err);
      allFallasCache = [];
      // No mostramos error aquí para no bloquear el formulario; se usará solo como fallback
    }

    return allFallasCache;
  }

  // Búsqueda de fallas con autocompletado usando /fallas/buscar
  async function buscarFallas(texto) {
    const query = (texto || '').trim().toLowerCase();
    if (!query) {
      if (fallaResultsEl) {
        fallaResultsEl.style.display = 'none';
        fallaResultsEl.innerHTML = '';
      }
      return;
    }

    let resultados = [];
    try {
      const url = `${API_FALLAS_BUSCAR}?texto=${encodeURIComponent(query)}`;
      console.log('[Registro Casal] Buscando fallas en:', url);
      const res = await fetch(url);
      const data = await res.json();
      const lista = data.datos || data.data || [];

      if (Array.isArray(lista)) {
        const empiezaPor = lista.filter(f => (f.nombre || '').toLowerCase().startsWith(query));
        const contiene = lista.filter(
          f => (f.nombre || '').toLowerCase().includes(query) && !empiezaPor.includes(f)
        );
        resultados = [...empiezaPor, ...contiene];
      }
    } catch (err) {
      console.error('[Registro Casal] Error en /fallas/buscar, usando fallback local si existe:', err);
      // Fallback local en caso de que el endpoint falle (por ejemplo, mientras se arregla el backend)
      const todas = await loadAllFallas();
      if (Array.isArray(todas) && todas.length) {
        const empiezaPor = todas.filter(f => (f.nombre || '').toLowerCase().startsWith(query));
        const contiene = todas.filter(
          f => (f.nombre || '').toLowerCase().includes(query) && !empiezaPor.includes(f)
        );
        resultados = [...empiezaPor, ...contiene];
      }
    }

    if (!Array.isArray(resultados) || resultados.length === 0) {
      if (fallaResultsEl) {
        fallaResultsEl.style.display = 'none';
        fallaResultsEl.innerHTML = '';
      }
      return;
    }

    if (!fallaResultsEl) return;

    fallaResultsEl.innerHTML = '';
    resultados.slice(0, 25).forEach(f => {
      const item = document.createElement('div');
      item.className = 'falla-result-item';
      item.innerHTML = `
        <strong>${f.nombre}</strong>
        <span>Sección: ${f.seccion || '-'}</span>
      `;
      item.addEventListener('click', () => {
        idFallaInput.value = f.idFalla;
        if (fallaSearchInput) fallaSearchInput.value = f.nombre;
        if (fallaSelectedEl) {
          fallaSelectedEl.textContent = `Falla seleccionada: ${f.nombre} (${f.seccion || 'sin sección'})`;
        }
        fallaResultsEl.style.display = 'none';
      });
      fallaResultsEl.appendChild(item);
    });

    fallaResultsEl.style.display = 'block';
  }

  if (fallaSearchInput) {
    fallaSearchInput.addEventListener('input', (e) => {
      const value = e.target.value;
      idFallaInput.value = '';
      if (fallaSelectedEl) fallaSelectedEl.textContent = '';

      if (fallaSearchTimeout) {
        clearTimeout(fallaSearchTimeout);
      }
      fallaSearchTimeout = setTimeout(() => buscarFallas(value), 250);
    });

    fallaSearchInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        buscarFallas(fallaSearchInput.value);
      }
    });

    document.addEventListener('click', (ev) => {
      if (!fallaResultsEl) return;
      if (!fallaResultsEl.contains(ev.target) && ev.target !== fallaSearchInput) {
        fallaResultsEl.style.display = 'none';
      }
    });
  }

  async function registrarCasal(payload) {
    try {
      const response = await fetch(API_AUTH_REGISTRO, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const data = await response.json().catch(() => null);
        const mensaje = data && data.mensaje ? data.mensaje : `Error del servidor (${response.status})`;
        return { success: false, message: mensaje };
      }

      const data = await response.json();
      if (!data.exito || !data.datos) {
        return { success: false, message: data.mensaje || 'Error en el registro.' };
      }

      const loginData = data.datos;
      const usuario = loginData.usuario;

      if (!loginData.token || !usuario) {
        return { success: false, message: 'Respuesta del servidor incompleta.' };
      }

      return {
        success: true,
        token: loginData.token,
        user: usuario
      };
    } catch (err) {
      console.error('Error en registro:', err);
      return {
        success: false,
        message: `Error de conexión: ${err.message}`
      };
    }
  }

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearError();

      const nombreCompleto = nombreInput.value.trim();
      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;
      const telefono = document.getElementById('telefono').value.trim();
      const direccion = document.getElementById('direccion').value.trim();
      const ciudad = document.getElementById('ciudad').value.trim();
      const codigoPostal = document.getElementById('codigoPostal').value.trim();
      const idFalla = idFallaInput.value ? parseInt(idFallaInput.value, 10) : null;

      if (!idFalla) {
        showError('Debes seleccionar una falla de la lista para registrar el casal.');
        return;
      }

      if (password.length < 6) {
        showError('La contraseña debe tener al menos 6 caracteres.');
        return;
      }

      registerBtn.disabled = true;
      const originalText = registerBtn.textContent;
      registerBtn.textContent = 'Creando cuenta...';

      const payload = {
        email,
        contrasena: password,
        nombreCompleto,
        idFalla,
        rol: 'casal',  // Especificar explícitamente que es un casal
        telefono: telefono || undefined,
        direccion: direccion || undefined,
        ciudad: ciudad || undefined,
        codigoPostal: codigoPostal || undefined
      };

      const result = await registrarCasal(payload);

      if (!result.success) {
        showError(result.message || 'No se ha podido completar el registro.');
        registerBtn.disabled = false;
        registerBtn.textContent = originalText;
        return;
      }

      try {
        localStorage.setItem('fallapp_token', result.token);
        localStorage.setItem('fallapp_user_id', result.user.idUsuario);
        localStorage.setItem('fallapp_user_email', result.user.email);
        localStorage.setItem('fallapp_user_nombre', result.user.nombreCompleto);
        localStorage.setItem('fallapp_user_rol', result.user.rol);
        localStorage.setItem('fallapp_user_idFalla', result.user.idFalla || '');
        localStorage.setItem('fallapp_user', result.user.email);
      } catch (eStorage) {
        console.error('Error guardando datos de sesión:', eStorage);
      }

      // Si el usuario ha introducido datos adicionales, hacer una actualización inicial del perfil
      if (telefono || direccion || ciudad || codigoPostal) {
        try {
          const updatePayload = {
            nombreCompleto,
            telefono: telefono || null,
            direccion: direccion || null,
            ciudad: ciudad || null,
            codigoPostal: codigoPostal || null,
            idFalla: idFalla
          };

          await fetch(`${API_USUARIOS_BASE}/${result.user.idUsuario}`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${result.token}`
            },
            body: JSON.stringify(updatePayload)
          }).catch(err => {
            console.warn('No se pudo completar la actualización inicial del perfil:', err);
          });
        } catch (eUpdate) {
          console.warn('Error en actualización inicial del perfil:', eUpdate);
        }
      }

      if (result.user.rol !== 'casal') {
        showError('La cuenta creada no tiene rol CASAL. Contacta con soporte.');
        registerBtn.disabled = false;
        registerBtn.textContent = originalText;
        return;
      }

      alert('¡Registro exitoso! 🎉\n\n' +
            'Te hemos enviado un correo de bienvenida a: ' + result.user.email);

      window.location.href = '../screens/home.html';
    });
  }
});

