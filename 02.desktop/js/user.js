let isEditing = false;
let currentUserId = null;
let originalUserData = null;

document.addEventListener('DOMContentLoaded', async () => {
  await loadUserData();
  await loadUserImage();

  const editBtn = document.getElementById('editBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelBtn = document.getElementById('cancelBtn');
  const logoutBtn = document.getElementById('logout');
  const uploadPhotoBtn = document.getElementById('uploadPhotoBtn');
  const userPhotoInput = document.getElementById('userPhotoInput');

  if (editBtn) {
    editBtn.addEventListener('click', toggleEditMode);
  } else {
    console.error('No se encontró editBtn');
  }

  if (saveBtn) {
    saveBtn.addEventListener('click', saveUserData);
  } else {
    console.error('No se encontró saveBtn');
  }

  if (cancelBtn) {
    cancelBtn.addEventListener('click', toggleEditMode);
  } else {
    console.error('No se encontró cancelBtn');
  }

  if (logoutBtn) {
    logoutBtn.addEventListener('click', handleLogout);
  } else {
    console.error('No se encontró logoutBtn');
  }

  ['userNombreCompleto', 'userEmail', 'userTelefono', 'userCodigoPostal'].forEach((idCampo) => {
    const campo = document.getElementById(idCampo);
    if (!campo) return;
    campo.addEventListener('input', () => {
      campo.setCustomValidity('');
    });
  });

  if (uploadPhotoBtn && userPhotoInput) {
    uploadPhotoBtn.addEventListener('click', () => {
      userPhotoInput.click();
    });
    
    userPhotoInput.addEventListener('change', async (e) => {
      const file = e.target.files[0];
      if (file) {
        try {
          await subirImagenUsuarioLocal();
        } catch (error) {
          showErrorMessage('Error al subir la foto: ' + error.message);
        }
      }
    });
    
  } else {
    console.error('No se encontró uploadPhotoBtn o userPhotoInput');
  }
});

async function loadUserData() {
  try {
    const idUsuario = localStorage.getItem('fallapp_user_id');
    
    if (!idUsuario) {
      console.error('No hay idUsuario en localStorage');
      redirectToLogin();
      return;
    }

    currentUserId = idUsuario;

    const response = await obtenerUsuario(idUsuario);
    const userData = response.datos || response.data || response;
    
    if (!userData) {
      throw new Error('Usuario no encontrado');
    }
    
    originalUserData = JSON.parse(JSON.stringify(userData));

    populateUserForm(userData);
  } catch (error) {
    console.error('Error loading user data:', error);
    showErrorMessage(`Error al cargar datos: ${error.message}`);
  }
}

function populateUserForm(userData) {
  if (!userData) {
    showErrorMessage('No se encontraron datos del usuario.');
    return;
  }

  document.getElementById('userId').value = userData.idUsuario || '';
  document.getElementById('userName').value = userData.email || '';
  document.getElementById('userEmail').value = userData.email || '';
  document.getElementById('userNombreCompleto').value = userData.nombreCompleto || '';
  document.getElementById('userTelefono').value = userData.telefono || '';
  document.getElementById('userRol').value = userData.rol || '';
  document.getElementById('userDireccion').value = userData.direccion || '';
  document.getElementById('userCiudad').value = userData.ciudad || '';
  document.getElementById('userCodigoPostal').value = userData.codigoPostal || '';
  document.getElementById('userIdFalla').value = userData.idFalla || '';
  document.getElementById('userEstado').value = userData.activo ? 'Activo' : 'Inactivo';

  if (userData.fechaCreacion) {
    const fecha = new Date(userData.fechaCreacion);
    document.getElementById('userFechaRegistro').value = fecha.toLocaleDateString('es-ES');
  }

  const initials = (userData.nombreCompleto || userData.email || 'U')
    .split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
  
  document.getElementById('avatarInitials').textContent = initials;

  const sideStatus = document.getElementById('sideStatus');
  const estadoTexto = userData.activo ? 'activa' : 'inactiva';
  const fecha = userData.fechaCreacion 
    ? new Date(userData.fechaCreacion).toLocaleDateString('es-ES') 
    : 'desconocida';
  sideStatus.textContent = `Tu cuenta está ${estadoTexto}. Registrado desde ${fecha}.`;
}

function toggleEditMode() {
  isEditing = !isEditing;

  const editBtn = document.getElementById('editBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelBtn = document.getElementById('cancelBtn');
  const uploadPhotoBtn = document.getElementById('uploadPhotoBtn');
  const fields = document.querySelectorAll('.field input');

  if (isEditing) {
    const nonEditableFields = ['userId', 'userRol', 'userIdFalla', 'userEstado', 'userFechaRegistro', 'userName'];
    
    fields.forEach((field) => {
      if (!nonEditableFields.includes(field.id)) {
        field.disabled = false;
      } else {
        field.disabled = true;
      }
    });

    editBtn.style.display = 'none';
    saveBtn.style.display = 'inline-flex';
    cancelBtn.style.display = 'inline-flex';
    if (uploadPhotoBtn) {
      uploadPhotoBtn.style.display = 'inline-flex';
    }
    
  } else {
    fields.forEach((field) => {
      field.disabled = true;
    });

    editBtn.style.display = 'inline-flex';
    saveBtn.style.display = 'none';
    cancelBtn.style.display = 'none';
    if (uploadPhotoBtn) {
      uploadPhotoBtn.style.display = 'none';
    }

    if (originalUserData) {
      populateUserForm(originalUserData);
    } else {
      loadUserData();
    }
  }
}

function validateUserData(formData) {
  const errors = [];
  const v = window.validacionFormulario;
  
  if (!formData.nombreCompleto || formData.nombreCompleto.length === 0) {
    errors.push('El nombre completo es requerido');
  }
  
  if (v && !v.emailValido(formData.email || '')) {
    errors.push('El email no es válido');
  } else if (formData.email && !formData.email.includes('@')) {
    errors.push('El email no es válido');
  } else if (!formData.email || formData.email.length === 0) {
    errors.push('El email es requerido');
  }
  
  if (formData.telefono && formData.telefono.length > 0) {
    if (v && !v.telefonoValido(formData.telefono)) {
      errors.push('El teléfono debe tener al menos 9 dígitos');
    } else if (formData.telefono.replace(/\D/g, '').length < 9) {
      errors.push('El teléfono debe tener al menos 9 dígitos');
    }
  }
  
  if (formData.codigoPostal && formData.codigoPostal.length > 0) {
    if (v && !v.codigoPostalValido(formData.codigoPostal)) {
      errors.push('El código postal debe tener 5 dígitos');
    } else if (!/^\d{5}$/.test(formData.codigoPostal)) {
      errors.push('El código postal debe tener 5 dígitos');
    }
  }
  
  return errors;
}

function validarCamposPerfil(formData) {
  const v = window.validacionFormulario;
  if (!v) {
    return true;
  }

  const campoNombre = document.getElementById('userNombreCompleto');
  const campoEmail = document.getElementById('userEmail');
  const campoTelefono = document.getElementById('userTelefono');
  const campoCodigoPostal = document.getElementById('userCodigoPostal');

  const nombreOk = v.validarCampo(campoNombre, v.texto(formData.nombreCompleto).length >= 3, 'El nombre completo es requerido');
  const emailOk = v.validarCampo(campoEmail, v.emailValido(formData.email), 'El email no es válido');
  const telefonoOk = v.validarCampo(campoTelefono, v.telefonoValido(formData.telefono), 'El teléfono debe tener al menos 9 dígitos');
  const cpOk = v.validarCampo(campoCodigoPostal, v.codigoPostalValido(formData.codigoPostal), 'El código postal debe tener 5 dígitos');

  if (!nombreOk) return campoNombre.reportValidity();
  if (!emailOk) return campoEmail.reportValidity();
  if (!telefonoOk) return campoTelefono.reportValidity();
  if (!cpOk) return campoCodigoPostal.reportValidity();

  return true;
}

async function saveUserData() {
  try {
    if (!currentUserId) {
      showErrorMessage('No se puede identificar al usuario.');
      console.error('No hay currentUserId');
      return;
    }

    const formData = {
      nombreCompleto: document.getElementById('userNombreCompleto').value.trim(),
      email: document.getElementById('userEmail').value.trim(),
      telefono: document.getElementById('userTelefono').value.trim(),
      direccion: document.getElementById('userDireccion').value.trim(),
      ciudad: document.getElementById('userCiudad').value.trim(),
      codigoPostal: document.getElementById('userCodigoPostal').value.trim(),
    };

    if (!validarCamposPerfil(formData)) {
      return;
    }

    const validationErrors = validateUserData(formData);
    if (validationErrors.length > 0) {
      const errorMsg = 'Errores de validación:\n' + validationErrors.join('\n');
      console.error(errorMsg);
      showErrorMessage(validationErrors.join('\n'));
      return;
    }

    const saveBtn = document.getElementById('saveBtn');
    const originalText = saveBtn.textContent;
    saveBtn.textContent = 'Guardando...';
    saveBtn.disabled = true;

    const result = await actualizarUsuario(currentUserId, formData);
    
    if (!result.exito) {
      throw new Error(`Error del servidor: ${result.mensaje}`);
    }

    originalUserData = JSON.parse(JSON.stringify(result.datos || result));

    isEditing = true;
    toggleEditMode();

    showSuccessMessage('Perfil actualizado correctamente.');

    await loadUserData();
  } catch (error) {
    console.error('Error saving user data:', error);
    showErrorMessage(`Error al guardar: ${error.message}`);
  } finally {
    const saveBtn = document.getElementById('saveBtn');
    if (saveBtn) {
      saveBtn.textContent = 'Guardar';
      saveBtn.disabled = false;
    }
  }
}

function showSuccessMessage(message) {
  const messageDiv = createMessageElement(message, 'success');
  document.body.appendChild(messageDiv);
  setTimeout(() => messageDiv.remove(), 4000);
}

function showErrorMessage(message) {
  const messageDiv = createMessageElement(message, 'error');
  document.body.appendChild(messageDiv);
  setTimeout(() => messageDiv.remove(), 6000);
}

function createMessageElement(message, type) {
  const div = document.createElement('div');
  div.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 16px 24px;
    border-radius: 8px;
    font-weight: 600;
    font-size: 14px;
    z-index: 9999;
    animation: slideIn 0.3s ease-out;
    max-width: 400px;
    min-width: 300px;
    word-wrap: break-word;
    white-space: pre-wrap;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    ${type === 'success' 
      ? 'background: #10b981; color: white;' 
      : 'background: #ef4444; color: white;'}
  `;
  div.textContent = message;
  return div;
}

function handleLogout() {
  try {
    localStorage.removeItem('fallapp_token');
    localStorage.removeItem('fallapp_user_id');
    localStorage.removeItem('fallapp_user_email');
    localStorage.removeItem('fallapp_user_nombre');
    localStorage.removeItem('fallapp_user_rol');
    localStorage.removeItem('fallapp_user_idFalla');
    localStorage.removeItem('fallapp_user');
    
    showSuccessMessage('Sesión cerrada correctamente');

    setTimeout(() => {
      window.location.href = '../js/index.html';
    }, 500);
  } catch (e) {
    console.error('Error removing user from localStorage:', e);
    window.location.href = '../js/index.html';
  }
}

function redirectToLogin() {
  window.location.href = '../js/index.html';
}

async function loadUserImage() {
  try {
    const idUsuario = localStorage.getItem('fallapp_user_id');
    if (!idUsuario) return;
    
    const urlImagen = `http://35.180.21.42:8080/api/usuarios/${idUsuario}/imagen`;
    const token = localStorage.getItem('fallapp_token');
    
    const headers = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await fetch(urlImagen, { headers });
    
    if (response.ok) {
      const blob = await response.blob();
      const objectUrl = URL.createObjectURL(blob);
      const avatarImg = document.getElementById('avatarImg');
      if (avatarImg) {
        avatarImg.src = objectUrl;
        avatarImg.style.display = 'block';
        const initials = document.getElementById('avatarInitials');
        if (initials) initials.style.display = 'none';
      }
    }
  } catch (e) {
    console.debug('No hay imagen de usuario o error cargándola:', e);
  }
}

async function subirImagenUsuarioLocal() {
  try {
    const idUsuario = localStorage.getItem('fallapp_user_id');
    if (!idUsuario) {
      showErrorMessage('No hay usuario cargado');
      return;
    }
    
    const inputImagen = document.getElementById('userPhotoInput');
    if (!inputImagen || !inputImagen.files.length) {
      showErrorMessage('Selecciona una imagen');
      return;
    }
    
    const archivo = inputImagen.files[0];
    
    await subirImagenUsuario(idUsuario, archivo);
    
    showSuccessMessage('Imagen de perfil actualizada');
    
    const avatarImg = document.getElementById('avatarImg');
    const avatarInitials = document.getElementById('avatarInitials');
    if (avatarImg) {
      avatarImg.style.display = 'none';
      avatarImg.src = '';
    }
    if (avatarInitials) {
      avatarInitials.style.display = 'block';
    }
    
    setTimeout(() => {
      loadUserImage();
    }, 500);
    
  } catch (e) {
    console.error('Error al subir imagen:', e);
    showErrorMessage(`Error al subir imagen: ${e.message}`);
  }
}

const style = document.createElement('style');
style.textContent = `
  @keyframes slideIn {
    from {
      transform: translateX(400px);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
`;
document.head.appendChild(style);


