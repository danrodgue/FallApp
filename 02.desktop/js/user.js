// API endpoint para obtener información del usuario
const API_USER_URL = 'http://35.180.21.42:8080/api/usuarios';

// Variable para rastrear si estamos en modo edición
let isEditing = false;
let currentUserId = null;
let originalUserData = null;

document.addEventListener('DOMContentLoaded', async () => {
  console.log('🔄 Inicializando página de usuario...');
  
  // Cargar información del usuario
  await loadUserData();

  // Cargar imagen del usuario si existe
  await loadUserImage();

  // Configurar botones
  const editBtn = document.getElementById('editBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelBtn = document.getElementById('cancelBtn');
  const logoutBtn = document.getElementById('logout');
  const uploadPhotoBtn = document.getElementById('uploadPhotoBtn');
  const userPhotoInput = document.getElementById('userPhotoInput');

  if (editBtn) {
    editBtn.addEventListener('click', toggleEditMode);
    console.log('✓ Event listener agregado a editBtn');
  } else {
    console.error('❌ No se encontró el elemento editBtn');
  }

  if (saveBtn) {
    saveBtn.addEventListener('click', saveUserData);
    console.log('✓ Event listener agregado a saveBtn');
  } else {
    console.error('❌ No se encontró el elemento saveBtn');
  }

  if (cancelBtn) {
    cancelBtn.addEventListener('click', toggleEditMode);
    console.log('✓ Event listener agregado a cancelBtn');
  } else {
    console.error('❌ No se encontró el elemento cancelBtn');
  }

  if (logoutBtn) {
    logoutBtn.addEventListener('click', handleLogout);
    console.log('✓ Event listener agregado a logoutBtn');
  } else {
    console.error('❌ No se encontró el elemento logoutBtn');
  }

  // Configurar botón de cambiar foto
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
    
    console.log('✓ Event listeners agregados al botón de cambiar foto');
  } else {
    console.error('❌ No se encontró uploadPhotoBtn o userPhotoInput');
  }
  
  console.log('✓ Página de usuario inicializada correctamente');
});

// Cargar datos del usuario desde el backend
async function loadUserData() {
  try {
    // Obtener el idUsuario desde localStorage (guardado en el login)
    const idUsuario = localStorage.getItem('fallapp_user_id');
    
    if (!idUsuario) {
      console.error('❌ No hay idUsuario en localStorage');
      redirectToLogin();
      return;
    }

    console.log(`📥 Cargando datos del usuario ID: ${idUsuario}`);
    currentUserId = idUsuario;

    // Obtener los datos del usuario específico desde la API
    const response = await obtenerUsuario(idUsuario);
    
    // Manejo de diferentes formatos de respuesta
    const userData = response.datos || response.data || response;
    
    if (!userData) {
      throw new Error('Usuario no encontrado');
    }
    
    // Guardar datos originales para comparación en cancelar
    originalUserData = JSON.parse(JSON.stringify(userData));
    
    console.log('✓ Datos del usuario cargados:', userData);
    populateUserForm(userData);
  } catch (error) {
    console.error('❌ Error loading user data:', error);
    showErrorMessage(`Error al cargar datos: ${error.message}`);
  }
}

// Rellenar el formulario con datos del usuario
function populateUserForm(userData) {
  if (!userData) {
    showErrorMessage('No se encontraron datos del usuario.');
    return;
  }

  // Establecer valores en los campos
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

  // Formatear fecha de creación si existe
  if (userData.fechaCreacion) {
    const fecha = new Date(userData.fechaCreacion);
    document.getElementById('userFechaRegistro').value = fecha.toLocaleDateString('es-ES');
  }

  // Actualizar avatar con iniciales del usuario
  const initials = (userData.nombreCompleto || userData.email || 'U')
    .split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
  
  document.getElementById('avatarInitials').textContent = initials;

  // Actualizar información en la barra lateral
  const sideStatus = document.getElementById('sideStatus');
  const estadoTexto = userData.activo ? 'activa' : 'inactiva';
  const fecha = userData.fechaCreacion 
    ? new Date(userData.fechaCreacion).toLocaleDateString('es-ES') 
    : 'desconocida';
  sideStatus.textContent = `Tu cuenta está ${estadoTexto}. Registrado desde ${fecha}.`;
}

// Alternar modo de edición
function toggleEditMode() {
  isEditing = !isEditing;

  const editBtn = document.getElementById('editBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelBtn = document.getElementById('cancelBtn');
  const uploadPhotoBtn = document.getElementById('uploadPhotoBtn');
  const fields = document.querySelectorAll('.field input');

  if (isEditing) {
    console.log('✏️ Entrando en modo edición...');
    // Habilitar campos editables (excepto algunos)
    // Los campos no editables son: id, rol, idFalla, estado, fechaRegistro
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
    
    console.log('✓ Modo edición activado');
  } else {
    console.log('🔒 Saliendo del modo edición...');
    // Deshabilitar campos
    fields.forEach((field) => {
      field.disabled = true;
    });

    editBtn.style.display = 'inline-flex';
    saveBtn.style.display = 'none';
    cancelBtn.style.display = 'none';
    if (uploadPhotoBtn) {
      uploadPhotoBtn.style.display = 'none';
    }

    // Recargar datos para descartar cambios (sin hacer petición si tenemos datos originales)
    if (originalUserData) {
      console.log('📄 Restaurando datos originales...');
      populateUserForm(originalUserData);
    } else {
      console.log('🔄 Recargando datos desde servidor...');
      loadUserData();
    }
    
    console.log('✓ Modo edición desactivado');
  }
}

// Validar datos del usuario
function validateUserData(formData) {
  const errors = [];
  
  // Validar que al menos someCompleto no esté vacío
  if (!formData.nombreCompleto || formData.nombreCompleto.length === 0) {
    errors.push('El nombre completo es requerido');
  }
  
  // Validar email
  if (formData.email && !formData.email.includes('@')) {
    errors.push('El email no es válido');
  } else if (!formData.email || formData.email.length === 0) {
    errors.push('El email es requerido');
  }
  
  // Validar teléfono (opcional pero si se proporciona debe tener formato)
  if (formData.telefono && formData.telefono.length > 0) {
    if (formData.telefono.replace(/\D/g, '').length < 9) {
      errors.push('El teléfono debe tener al menos 9 dígitos');
    }
  }
  
  // Validar código postal (opcional pero si se proporciona)
  if (formData.codigoPostal && formData.codigoPostal.length > 0) {
    if (!/^\d{5}$/.test(formData.codigoPostal)) {
      errors.push('El código postal debe tener 5 dígitos');
    }
  }
  
  return errors;
}

// Guardar cambios del usuario
async function saveUserData() {
  try {
    if (!currentUserId) {
      showErrorMessage('❌ No se puede identificar al usuario.');
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

    console.log('📤 Enviando datos del usuario:', formData);

    // Validar datos
    const validationErrors = validateUserData(formData);
    if (validationErrors.length > 0) {
      const errorMsg = '❌ Errores de validación:\n' + validationErrors.join('\n');
      console.error(errorMsg);
      showErrorMessage(validationErrors.join('\n'));
      return;
    }

    // Mostrar indicador de carga
    const saveBtn = document.getElementById('saveBtn');
    const originalText = saveBtn.textContent;
    saveBtn.textContent = 'Guardando...';
    saveBtn.disabled = true;

    console.log(`🔄 Actualizando usuario ${currentUserId}...`);
    
    // Enviar actualización utilizando la función mejorada de api.js
    const result = await actualizarUsuario(currentUserId, formData);

    console.log('✓ Usuario actualizado en el servidor:', result);
    
    if (!result.exito) {
      throw new Error(`Error del servidor: ${result.mensaje}`);
    }

    // Actualizar los datos originales con los nuevos para que cancelar funcione correctamente
    // La respuesta tiene estructura: { exito: true, mensaje: "...", datos: {...} }
    originalUserData = JSON.parse(JSON.stringify(result.datos || result));
    console.log('✓ Datos guardados en memoria:', originalUserData);

    // Deshabilitar modo edición
    isEditing = true;
    toggleEditMode();

    showSuccessMessage('✓ Perfil actualizado correctamente.');
    
    // Recargar datos del servidor para confirmar que se guardó
    await loadUserData();
  } catch (error) {
    console.error('❌ Error saving user data:', error);
    showErrorMessage(`Error al guardar: ${error.message}`);
  } finally {
    // Restaurar botón de guardar
    const saveBtn = document.getElementById('saveBtn');
    if (saveBtn) {
      saveBtn.textContent = 'Guardar';
      saveBtn.disabled = false;
    }
  }
}

// Mostrar mensaje de éxito
function showSuccessMessage(message) {
  const messageDiv = createMessageElement(message, 'success');
  document.body.appendChild(messageDiv);
  console.log('✓ ' + message);
  setTimeout(() => messageDiv.remove(), 4000);
}

// Mostrar mensaje de error
function showErrorMessage(message) {
  const messageDiv = createMessageElement(message, 'error');
  document.body.appendChild(messageDiv);
  console.error('❌ ' + message);
  setTimeout(() => messageDiv.remove(), 6000);
}

// Crear elemento de mensaje
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

// Manejar cierre de sesión
function handleLogout() {
  try {
    console.log('🚪 Cerrando sesión...');
    // Limpiar todos los datos de sesión
    localStorage.removeItem('fallapp_token');
    localStorage.removeItem('fallapp_user_id');
    localStorage.removeItem('fallapp_user_email');
    localStorage.removeItem('fallapp_user_nombre');
    localStorage.removeItem('fallapp_user_rol');
    localStorage.removeItem('fallapp_user_idFalla');
    localStorage.removeItem('fallapp_user');
    
    console.log('✓ Sesión cerrada, redirigiendo a login...');
    showSuccessMessage('Sesión cerrada correctamente');
    
    // Pequeño delay para que se vea el mensaje
    setTimeout(() => {
      window.location.href = '../js/index.html';
    }, 500);
  } catch (e) {
    console.error('❌ Error removing user from localStorage:', e);
    window.location.href = '../js/index.html';
  }
}

// Redirigir a login si no hay usuario
function redirectToLogin() {
  console.warn('⚠️ Redirigiendo a login...');
  window.location.href = '../js/index.html';
}

// Cargar imagen del usuario
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
        console.log('✓ Imagen de usuario cargada');
      }
    }
  } catch (e) {
    console.debug('No hay imagen de usuario o error cargándola:', e);
  }
}

// Subir imagen de usuario
// Wrapper para subir imagen de usuario - obtiene el archivo del input
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
    
    // Llamar a la función de api.js
    await subirImagenUsuario(idUsuario, archivo);
    
    showSuccessMessage('Imagen de perfil actualizada');
    
    // Recargar imagen - mostrar iniciales de nuevo y luego cargar la imagen
    const avatarImg = document.getElementById('avatarImg');
    const avatarInitials = document.getElementById('avatarInitials');
    if (avatarImg) {
      avatarImg.style.display = 'none';
      avatarImg.src = '';
    }
    if (avatarInitials) {
      avatarInitials.style.display = 'block';
    }
    
    // Esperar un poco y recargar la nueva imagen
    setTimeout(() => {
      loadUserImage();
    }, 500);
    
  } catch (e) {
    console.error('❌ Error al subir imagen:', e);
    showErrorMessage(`Error al subir imagen: ${e.message}`);
  }
}

// Agregar animación de deslizamiento
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


