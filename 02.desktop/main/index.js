const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const fetch = require('node-fetch');

// Leer config local (apiBase) desde la ubicación real del archivo
let API_BASE = 'http://127.0.0.1:8080';
try {
  const cfg = require(path.join(__dirname, '../js/config.json'));
  API_BASE = cfg.apiBase || API_BASE;
} catch (e) {}

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    fullscreen: false,
    fullscreenable: false,
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, '../preload/preload.js'),
      nodeIntegration: false,
      contextIsolation: true
    }
  });

  // Cargar la pantalla de login ubicada en js/index.html
  mainWindow.loadFile(path.join(__dirname, '../js/index.html'));
  mainWindow.maximize();
  mainWindow.setMenu(null);

  mainWindow.on('closed', () => { mainWindow = null; });
}

// IPC handlers: main -> backend HTTP
ipcMain.handle('get-events', async () => {
  try {
    const res = await fetch(`${API_BASE}/api/eventos/proximos?limite=100`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    return data.datos || data;
  } catch (error) {
    console.error('Error fetching events:', error);
    throw error;
  }
});

ipcMain.handle('create-event', async (evt, payload) => {
  try {
    const token = payload.token;
    const headers = {
      'Content-Type': 'application/json'
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Mapear los datos del formulario a los campos exactos de la tabla
    const eventData = {
      nombre: payload.nombre || payload.name,
      descripcion: payload.descripcion || payload.description,
      fecha_evento: payload.fecha_evento || new Date(payload.date + 'T' + (payload.time || '00:00')).toISOString(),
      ubicacion: payload.ubicacion || payload.place,
      id_falla: payload.id_falla || payload.idFalla,
      tipo: payload.tipo || 'otro',
      creado_por: payload.creado_por
    };
    
    const res = await fetch(`${API_BASE}/api/eventos`, { 
      method: 'POST', 
      body: JSON.stringify(eventData), 
      headers: headers 
    });
    
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: `Error HTTP ${res.status}` }));
      throw new Error(error.message || error.error || `Error HTTP ${res.status}`);
    }
    
    const data = await res.json();
    return data.datos || data;
  } catch (error) {
    console.error('Error creating event:', error);
    throw error;
  }
});

ipcMain.handle('update-event', async (evt, payload) => {
  try {
    const token = payload.token;
    const id = payload.id_evento || payload.idEvento || payload.id;
    const headers = {
      'Content-Type': 'application/json'
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Mapear los datos del formulario a los campos exactos de la tabla
    const eventData = {
      nombre: payload.nombre || payload.name,
      descripcion: payload.descripcion || payload.description,
      fecha_evento: payload.fecha_evento || new Date(payload.date + 'T' + (payload.time || '00:00')).toISOString(),
      ubicacion: payload.ubicacion || payload.place,
      id_falla: payload.id_falla || payload.idFalla,
      tipo: payload.tipo || 'otro',
      creado_por: payload.creado_por
    };
    
    const res = await fetch(`${API_BASE}/api/eventos/${id}`, { 
      method: 'PUT', 
      body: JSON.stringify(eventData), 
      headers: headers 
    });
    
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: `Error HTTP ${res.status}` }));
      throw new Error(error.message || error.error || `Error HTTP ${res.status}`);
    }
    
    const data = await res.json();
    return data.datos || data;
  } catch (error) {
    console.error('Error updating event:', error);
    throw error;
  }
});

ipcMain.handle('delete-event', async (evt, id) => {
  try {
    const res = await fetch(`${API_BASE}/api/eventos/${id}`, { 
      method: 'DELETE' 
    });
    return res.status === 204 || res.ok;
  } catch (error) {
    console.error('Error deleting event:', error);
    throw error;
  }
});

// Falla endpoints
ipcMain.handle('get-falla', async (evt, id) => {
  const res = await fetch(`${API_BASE}/api/fallas/${id}`);
  if (!res.ok) throw new Error('Falla no encontrada');
  return await res.json();
});
ipcMain.handle('save-falla', async (evt, payload) => {
  try {
    // Extraer token e id del payload
    const token = payload.token;
    const id = payload.id;
    
    // Crear copia del payload sin id y token para enviar al backend
    const bodyPayload = { ...payload };
    delete bodyPayload.id;
    delete bodyPayload.token;
    
    const url = id ? `${API_BASE}/api/fallas/${id}` : `${API_BASE}/api/fallas`;
    const method = id ? 'PUT' : 'POST';
    const headers = {
      'Content-Type': 'application/json'
    };
    
    // Si hay token, agregarlo a los headers
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    const res = await fetch(url, { 
      method: method, 
      body: JSON.stringify(bodyPayload), 
      headers: headers 
    });
    
    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: `Error HTTP ${res.status}` }));
      throw new Error(error.message || error.error || `Error HTTP ${res.status}`);
    }
    return await res.json();
  } catch (error) {
    console.error('Error en save-falla:', error);
    throw error;
  }
});
ipcMain.handle('delete-falla', async (evt, id) => {
  const res = await fetch(`${API_BASE}/api/fallas/${id}`, { method: 'DELETE' });
  return res.status === 204 || res.ok;
});

// Crear ventana cuando Electron esté listo
app.whenReady().then(createWindow);

// Cerrar app en todas las ventanas cerradas (excepto macOS)
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

// macOS: recrear ventana al hacer click en el dock
app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});
