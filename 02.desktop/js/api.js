const API_BASE_URL = 'http://35.180.21.42:8080/api';
const API_URL = API_BASE_URL + '/fallas';
const API_USUARIOS_URL = API_BASE_URL + '/usuarios';
const API_EVENTOS_URL = API_BASE_URL + '/eventos';

// ============================================
// OBTENER TOKEN DE AUTENTICACIÓN
// ============================================
function getAuthHeaders() {
    const token = localStorage.getItem('fallapp_token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
        console.log('🔐 [API] Token de autorización encontrado');
    } else {
        console.warn('⚠️ [API] No hay token de autorización en localStorage');
    }
    return headers;
}

// ============================================
// MANEJO DE ERRORES MEJORADO
// ============================================
async function parseErrorResponse(response) {
    try {
        const data = await response.json();
        return data.message || data.error || `Error ${response.status}`;
    } catch (e) {
        return `Error del servidor (${response.status})`;
    }
}

// ============================================
// FUNCIONES DE FALLA
// ============================================

// Obtener todas las fallas
async function obtenerFallas() {
    try {
        const respuesta = await fetch(API_URL, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudieron obtener las fallas: ${error.message}`);
    }
}

// Obtener una falla por ID
async function obtenerFalla(id) {
    try {
        const respuesta = await fetch(`${API_URL}/${id}`, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo obtener la falla: ${error.message}`);
    }
}

// Crear una falla
async function crearFalla(datos) {
    try {
        const respuesta = await fetch(API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo crear la falla: ${error.message}`);
    }
}

// Actualizar una falla
async function actualizarFalla(id, datos) {
    try {
        const respuesta = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo actualizar la falla: ${error.message}`);
    }
}

// Eliminar una falla
async function eliminarFalla(id) {
    try {
        const respuesta = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo eliminar la falla: ${error.message}`);
    }
}

// ============================================
// FUNCIONES DE USUARIO
// ============================================

// Obtener un usuario por ID
async function obtenerUsuario(id) {
    try {
        const url = `${API_USUARIOS_URL}/${id}`;
        console.log(`📥 [API] Obteniendo usuario: ${id} desde ${url}`);
        
        const respuesta = await fetch(url, {
            headers: getAuthHeaders()
        });
        
        console.log(`📥 [API] Respuesta status: ${respuesta.status} ${respuesta.statusText}`);
        
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            console.error(`❌ [API] Error obteniendo usuario: ${error}`);
            throw new Error(error);
        }
        
        const resultado = await respuesta.json();
        console.log('------- USUARIO OBTENIDO -------');
        console.log(`✓ [API] Usuario obtenido:`, resultado);
        if (resultado.datos) {
            console.log(`✓ [API] Email: ${resultado.datos.email}`);
            console.log(`✓ [API] Nombre: ${resultado.datos.nombreCompleto}`);
        }
        console.log('------- FIN USUARIO -------');
        return resultado;
    } catch (error) {
        console.error(`❌ [API] Error en obtenerUsuario:`, error);
        throw new Error(`No se pudo obtener el usuario: ${error.message}`);
    }
}

// Actualizar un usuario
async function actualizarUsuario(id, datos) {
    try {
        const url = `${API_USUARIOS_URL}/${id}`;
        const headers = getAuthHeaders();
        const body = JSON.stringify(datos);
        
        console.log(`📤 [API] Actualizando usuario: ${id}`);
        console.log(`📤 [API] URL: ${url}`);
        console.log(`📤 [API] Headers:`, headers);
        console.log(`📤 [API] Body:`, body);
        console.log('------- ENVIANDO PETICIÓN PUT -------');
        
        const respuesta = await fetch(url, {
            method: 'PUT',
            headers: headers,
            body: body
        });
        
        console.log(`📥 [API] Respuesta status: ${respuesta.status} ${respuesta.statusText}`);
        
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            console.error(`❌ [API] Error en actualizar usuario: ${error}`);
            console.error(`❌ [API] Status ${respuesta.status} - Body: ${error}`);
            throw new Error(error);
        }
        
        const resultado = await respuesta.json();
        console.log('------- RESPUESTA RECIBIDA -------');
        console.log(`✓ [API] Usuario actualizado exitosamente:`, resultado);
        console.log(`✓ [API] exito: ${resultado.exito}`);
        console.log(`✓ [API] mensaje: ${resultado.mensaje}`);
        console.log(`✓ [API] datos:`, resultado.datos);
        console.log('------- FIN RESPUESTA -------');
        
        return resultado;
    } catch (error) {
        console.error(`❌ [API] Error en actualizarUsuario:`, error);
        throw new Error(`No se pudo actualizar el usuario: ${error.message}`);
    }
}

// ============================================
// FUNCIONES DE EVENTO
// ============================================

// Obtener eventos por falla
async function obtenerEventosPorFalla(idFalla, page = 0, size = 100) {
    try {
        const url = `${API_EVENTOS_URL}/falla/${idFalla}?page=${page}&size=${size}`;
        const respuesta = await fetch(url, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudieron obtener los eventos de la falla: ${error.message}`);
    }
}

// Obtener un evento por ID
async function obtenerEvento(id) {
    try {
        const respuesta = await fetch(`${API_EVENTOS_URL}/${id}`, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo obtener el evento: ${error.message}`);
    }
}

// Crear un evento
async function crearEvento(datos) {
    try {
        console.log('📤 [API] Enviando evento:', datos);
        const respuesta = await fetch(API_EVENTOS_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        
        console.log('📥 [API] Respuesta status:', respuesta.status);
        
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            console.error('❌ [API] Error al crear evento:', error);
            throw new Error(error);
        }
        const resultado = await respuesta.json();
        console.log('✅ [API] Evento creado:', resultado);
        return resultado;
    } catch (error) {
        console.error('❌ [API] Error en crearEvento:', error.message);
        throw new Error(`No se pudo crear el evento: ${error.message}`);
    }
}

// Actualizar un evento
async function actualizarEvento(id, datos) {
    try {
        const respuesta = await fetch(`${API_EVENTOS_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo actualizar el evento: ${error.message}`);
    }
}

// Eliminar un evento
async function eliminarEvento(id) {
    try {
        const respuesta = await fetch(`${API_EVENTOS_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo eliminar el evento: ${error.message}`);
    }
}

// ============================================
// FUNCIONES DE IMAGEN
// ============================================

// Subir imagen de evento
async function subirImagenEvento(idEvento, archivo) {
    try {
        const formData = new FormData();
        formData.append('imagen', archivo);
        
        const headers = {};
        const token = localStorage.getItem('fallapp_token');
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const respuesta = await fetch(`${API_EVENTOS_URL}/${idEvento}/imagen`, {
            method: 'POST',
            headers: headers,
            body: formData
        });
        
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo subir la imagen del evento: ${error.message}`);
    }
}

// Descargar imagen de evento
async function descargarImagenEvento(idEvento) {
    try {
        const respuesta = await fetch(`${API_EVENTOS_URL}/${idEvento}/imagen`, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            throw new Error(`Error HTTP ${respuesta.status}`);
        }
        return await respuesta.blob();
    } catch (error) {
        throw new Error(`No se pudo descargar la imagen del evento: ${error.message}`);
    }
}

// Eliminar imagen de evento
async function eliminarImagenEvento(idEvento) {
    try {
        const respuesta = await fetch(`${API_EVENTOS_URL}/${idEvento}/imagen`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo eliminar la imagen del evento: ${error.message}`);
    }
}

// Subir imagen de usuario
async function subirImagenUsuario(idUsuario, archivo) {
    try {
        const formData = new FormData();
        formData.append('imagen', archivo);
        
        const headers = {};
        const token = localStorage.getItem('fallapp_token');
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const respuesta = await fetch(`${API_USUARIOS_URL}/${idUsuario}/imagen`, {
            method: 'POST',
            headers: headers,
            body: formData
        });
        
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo subir la imagen del usuario: ${error.message}`);
    }
}

// Descargar imagen de usuario
async function descargarImagenUsuario(idUsuario) {
    try {
        const respuesta = await fetch(`${API_USUARIOS_URL}/${idUsuario}/imagen`, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            throw new Error(`Error HTTP ${respuesta.status}`);
        }
        return await respuesta.blob();
    } catch (error) {
        throw new Error(`No se pudo descargar la imagen del usuario: ${error.message}`);
    }
}

// Eliminar imagen de usuario
async function eliminarImagenUsuario(idUsuario) {
    try {
        const respuesta = await fetch(`${API_USUARIOS_URL}/${idUsuario}/imagen`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo eliminar la imagen del usuario: ${error.message}`);
    }
}