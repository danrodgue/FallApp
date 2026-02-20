const API_BASE_URL =
  window._API_URL ||
  (window._API_BASE ? `${window._API_BASE}/api` : 'http://35.180.21.42:8080/api');

const API_AUTH_REGISTRO = `${API_BASE_URL}/auth/registro`;
const API_FALLAS_URL = `${API_BASE_URL}/fallas`;
const API_FALLAS_BUSCAR = `${API_BASE_URL}/fallas/buscar`;
const API_USUARIOS_BASE = `${API_BASE_URL}/usuarios`;

document.addEventListener('DOMContentLoaded', () => {
  const formulario = document.getElementById('register-form');
  const cajaError = document.getElementById('register-error-msg');
  const botonRegistro = document.getElementById('registerBtn');

  const inputNombre = document.getElementById('nombreCompleto');
  const inputEmail = document.getElementById('email');
  const inputPassword = document.getElementById('password');
  const inputTelefono = document.getElementById('telefono');
  const inputDireccion = document.getElementById('direccion');
  const inputCiudad = document.getElementById('ciudad');
  const inputCodigoPostal = document.getElementById('codigoPostal');

  const inputBuscarFalla = document.getElementById('fallaSearch');
  const cajaResultadosFalla = document.getElementById('fallaResults');
  const textoFallaSeleccionada = document.getElementById('fallaSelected');
  const inputIdFalla = document.getElementById('idFalla');

  const inputAvatar = document.getElementById('avatarFile');
  const previewAvatar = document.getElementById('avatarPreview');
  const inicialesAvatar = document.getElementById('avatarInitialsPreview');

  let cacheFallas = null;
  let timeoutBusqueda = null;

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

  function actualizarInicialesAvatar() {
    const nombre = inputNombre.value.trim();
    if (!nombre) {
      inicialesAvatar.textContent = 'C';
      return;
    }

    const iniciales = nombre
      .split(' ')
      .filter(Boolean)
      .map((parte) => parte[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);

    inicialesAvatar.textContent = iniciales || 'C';
  }

  function configurarPreviewAvatar() {
    if (!inputAvatar || !previewAvatar) {
      return;
    }

    inputAvatar.addEventListener('change', () => {
      const archivo = inputAvatar.files[0];
      if (!archivo) {
        previewAvatar.innerHTML = '';
        previewAvatar.appendChild(inicialesAvatar);
        return;
      }

      const imagen = document.createElement('img');
      imagen.onload = () => {
        previewAvatar.innerHTML = '';
        previewAvatar.appendChild(imagen);
      };
      imagen.onerror = () => {
        previewAvatar.innerHTML = '';
        previewAvatar.appendChild(inicialesAvatar);
      };
      imagen.src = URL.createObjectURL(archivo);
    });
  }

  async function cargarFallas() {
    if (cacheFallas) {
      return cacheFallas;
    }

    try {
      const respuesta = await fetch(`${API_FALLAS_URL}?pagina=0&tamano=400`);
      if (!respuesta.ok) {
        throw new Error(`HTTP ${respuesta.status}`);
      }

      const datos = await respuesta.json();
      const contenido = datos.datos && datos.datos.contenido ? datos.datos.contenido : [];
      cacheFallas = Array.isArray(contenido) ? contenido : [];
    } catch (error) {
      console.error('Error cargando fallas:', error);
      cacheFallas = [];
    }

    return cacheFallas;
  }

  function ocultarResultadosFalla() {
    if (!cajaResultadosFalla) {
      return;
    }
    cajaResultadosFalla.style.display = 'none';
    cajaResultadosFalla.innerHTML = '';
  }

  function pintarResultadosFalla(lista, query) {
    if (!cajaResultadosFalla) {
      return;
    }

    if (!Array.isArray(lista) || lista.length === 0) {
      ocultarResultadosFalla();
      return;
    }

    const inicio = lista.filter((falla) => (falla.nombre || '').toLowerCase().startsWith(query));
    const contiene = lista.filter(
      (falla) => (falla.nombre || '').toLowerCase().includes(query) && !inicio.includes(falla)
    );

    cajaResultadosFalla.innerHTML = '';
    [...inicio, ...contiene].slice(0, 25).forEach((falla) => {
      const item = document.createElement('div');
      item.className = 'falla-result-item';
      item.innerHTML = `<strong>${falla.nombre}</strong><span>Sección: ${falla.seccion || '-'}</span>`;

      item.addEventListener('click', () => {
        inputIdFalla.value = falla.idFalla;
        if (inputBuscarFalla) {
          inputBuscarFalla.value = falla.nombre;
          inputBuscarFalla.setCustomValidity('');
        }
        if (textoFallaSeleccionada) {
          textoFallaSeleccionada.textContent = `Falla seleccionada: ${falla.nombre} (${falla.seccion || 'sin sección'})`;
        }
        ocultarResultadosFalla();
      });

      cajaResultadosFalla.appendChild(item);
    });

    cajaResultadosFalla.style.display = 'block';
  }

  async function buscarFallas(textoBusqueda) {
    const query = String(textoBusqueda || '').trim().toLowerCase();
    if (!query) {
      ocultarResultadosFalla();
      return;
    }

    try {
      const url = `${API_FALLAS_BUSCAR}?texto=${encodeURIComponent(query)}`;
      const respuesta = await fetch(url);
      const datos = await respuesta.json();
      const lista = datos.datos || datos.data || [];
      if (Array.isArray(lista)) {
        pintarResultadosFalla(lista, query);
        return;
      }
    } catch (error) {
      console.warn('Fallo en /fallas/buscar, usando fallback local:', error);
    }

    const listaLocal = await cargarFallas();
    pintarResultadosFalla(listaLocal, query);
  }

  function validarFormulario() {
    const v = window.validacionFormulario;
    const nombre = inputNombre.value;
    const email = inputEmail.value;
    const password = inputPassword.value;
    const telefono = inputTelefono.value;
    const codigoPostal = inputCodigoPostal.value;

    const nombreOk = v
      ? v.validarCampo(inputNombre, v.texto(nombre).length >= 3, 'Escribe tu nombre completo.')
      : String(nombre || '').trim().length >= 3;

    const emailOk = v
      ? v.validarCampo(inputEmail, v.emailValido(email), 'Escribe un email válido.')
      : String(email || '').trim().length > 0;

    const passOk = v
      ? v.validarCampo(inputPassword, v.passwordValida(password, 6), 'La contraseña debe tener al menos 6 caracteres.')
      : String(password || '').length >= 6;

    const telefonoOk = v
      ? v.validarCampo(inputTelefono, v.telefonoValido(telefono), 'El teléfono debe tener al menos 9 dígitos.')
      : true;

    const cpOk = v
      ? v.validarCampo(inputCodigoPostal, v.codigoPostalValido(codigoPostal), 'El código postal debe tener 5 dígitos.')
      : true;

    const fallaOk = inputIdFalla.value && Number(inputIdFalla.value) > 0;
    if (!fallaOk && inputBuscarFalla) {
      inputBuscarFalla.setCustomValidity('Selecciona una falla de la lista.');
    } else if (inputBuscarFalla) {
      inputBuscarFalla.setCustomValidity('');
    }

    if (!nombreOk) {
      mostrarError('Escribe tu nombre completo.');
      try { inputNombre.reportValidity(); } catch (e) {}
      return false;
    }
    if (!emailOk) {
      mostrarError('Escribe un email válido.');
      try { inputEmail.reportValidity(); } catch (e) {}
      return false;
    }
    if (!passOk) {
      mostrarError('La contraseña debe tener al menos 6 caracteres.');
      try { inputPassword.reportValidity(); } catch (e) {}
      return false;
    }
    if (!telefonoOk) {
      mostrarError('El teléfono debe tener al menos 9 dígitos. Usa sólo números y signos +, () o - si lo deseas.');
      try { inputTelefono.reportValidity(); } catch (e) {}
      return false;
    }
    if (!cpOk) {
      mostrarError('El código postal debe tener 5 dígitos.');
      try { inputCodigoPostal.reportValidity(); } catch (e) {}
      return false;
    }
    if (!fallaOk && inputBuscarFalla) {
      mostrarError('Selecciona una falla de la lista.');
      try { inputBuscarFalla.reportValidity(); } catch (e) {}
      return false;
    }

    // Clear any previous error message
    limpiarError();

    return true;
  }

  async function registrarCasal(payload) {
    try {
      const respuesta = await fetch(API_AUTH_REGISTRO, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (!respuesta.ok) {
        const dataError = await respuesta.json().catch(() => null);
        const mensaje = dataError && dataError.mensaje ? dataError.mensaje : `Error del servidor (${respuesta.status})`;
        return { success: false, message: mensaje };
      }

      const datos = await respuesta.json();
      if (!datos.exito || !datos.datos) {
        return { success: false, message: datos.mensaje || 'Error en el registro.' };
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

  async function guardarSesionRegistro(resultado) {
    try {
      localStorage.setItem('fallapp_token', resultado.token);
      localStorage.setItem('fallapp_user_id', resultado.user.idUsuario);
      localStorage.setItem('fallapp_user_email', resultado.user.email);
      localStorage.setItem('fallapp_user_nombre', resultado.user.nombreCompleto);
      localStorage.setItem('fallapp_user_rol', resultado.user.rol);
      localStorage.setItem('fallapp_user_idFalla', resultado.user.idFalla || '');
      localStorage.setItem('fallapp_user', resultado.user.email);
    } catch (errorStorage) {
      console.error('Error guardando datos de sesión:', errorStorage);
    }
  }

  async function actualizarPerfilInicialSiHaceFalta(resultado, datosPerfil) {
    const { nombreCompleto, telefono, direccion, ciudad, codigoPostal, idFalla } = datosPerfil;
    if (!telefono && !direccion && !ciudad && !codigoPostal) {
      return;
    }

    try {
      const payload = {
        nombreCompleto,
        telefono: telefono || null,
        direccion: direccion || null,
        ciudad: ciudad || null,
        codigoPostal: codigoPostal || null,
        idFalla: idFalla
      };

      await fetch(`${API_USUARIOS_BASE}/${resultado.user.idUsuario}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${resultado.token}`
        },
        body: JSON.stringify(payload)
      });
    } catch (error) {
      console.warn('No se pudo completar la actualización inicial del perfil:', error);
    }
  }

  function conectarEventos() {
    if (inputNombre) {
      inputNombre.addEventListener('input', actualizarInicialesAvatar);
    }

    if (inputBuscarFalla) {
      inputBuscarFalla.addEventListener('input', (evento) => {
        inputIdFalla.value = '';
        if (textoFallaSeleccionada) {
          textoFallaSeleccionada.textContent = '';
        }

        if (timeoutBusqueda) {
          clearTimeout(timeoutBusqueda);
        }

        timeoutBusqueda = setTimeout(() => {
          buscarFallas(evento.target.value);
        }, 250);
      });

      inputBuscarFalla.addEventListener('keydown', (evento) => {
        if (evento.key === 'Enter') {
          evento.preventDefault();
          buscarFallas(inputBuscarFalla.value);
        }
      });

      document.addEventListener('click', (evento) => {
        if (!cajaResultadosFalla) {
          return;
        }
        if (!cajaResultadosFalla.contains(evento.target) && evento.target !== inputBuscarFalla) {
          ocultarResultadosFalla();
        }
      });
    }

    [inputEmail, inputPassword, inputNombre, inputTelefono, inputCodigoPostal].forEach((campo) => {
      if (!campo) return;
      campo.addEventListener('input', () => {
        campo.setCustomValidity('');
      });
    });
  }

  formulario.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    limpiarError();

    if (!validarFormulario()) {
      return;
    }

    const nombreCompleto = inputNombre.value.trim();
    const email = inputEmail.value.trim();
    const password = inputPassword.value;
    const telefono = inputTelefono.value.trim();
    const direccion = inputDireccion.value.trim();
    const ciudad = inputCiudad.value.trim();
    const codigoPostal = inputCodigoPostal.value.trim();
    const idFalla = inputIdFalla.value ? parseInt(inputIdFalla.value, 10) : null;

    botonRegistro.disabled = true;
    const textoOriginalBoton = botonRegistro.textContent;
    botonRegistro.textContent = 'Creando cuenta...';

    // normalize telefono to digits only for backend
    const telefonoNormalized = telefono ? telefono.replace(/\D/g, '') : undefined;

    const payloadRegistro = {
      email,
      contrasena: password,
      nombreCompleto,
      idFalla,
      rol: 'casal',
      telefono: telefonoNormalized || undefined,
      direccion: direccion || undefined,
      ciudad: ciudad || undefined,
      codigoPostal: codigoPostal || undefined
    };

    const resultado = await registrarCasal(payloadRegistro);
    if (!resultado.success) {
      mostrarError(resultado.message || 'No se ha podido completar el registro.');
      botonRegistro.disabled = false;
      botonRegistro.textContent = textoOriginalBoton;
      return;
    }

    await guardarSesionRegistro(resultado);

    await actualizarPerfilInicialSiHaceFalta(resultado, {
      nombreCompleto,
      telefono,
      direccion,
      ciudad,
      codigoPostal,
      idFalla
    });

    if (resultado.user.rol !== 'casal') {
      mostrarError('La cuenta creada no tiene rol CASAL. Contacta con soporte.');
      botonRegistro.disabled = false;
      botonRegistro.textContent = textoOriginalBoton;
      return;
    }

    alert(`¡Registro exitoso! 🎉\n\nTe hemos enviado un correo de bienvenida a: ${resultado.user.email}`);
    window.location.href = '../screens/home.html';
  });

  configurarPreviewAvatar();
  actualizarInicialesAvatar();
  conectarEventos();
});
