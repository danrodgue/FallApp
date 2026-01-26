const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const fetch = require('node-fetch');

// Leer config local (apiBase)
let API_BASE = 'http://127.0.0.1:8080';
try { const cfg = require('./config.json'); API_BASE = cfg.apiBase || API_BASE; } catch(e) {}

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    fullscreen: false,
    fullscreenable: false,
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true
    }
  });

  mainWindow.loadFile('index.html');
  mainWindow.maximize();
  mainWindow.setMenu(null);

  mainWindow.on('closed', () => { mainWindow = null; });
}

// IPC handlers: main -> backend HTTP
ipcMain.handle('get-events', async () => {
  const res = await fetch(`${API_BASE}/api/events`);
  return await res.json();
});
ipcMain.handle('create-event', async (evt, payload) => {
  const res = await fetch(`${API_BASE}/api/events`, { method: 'POST', body: JSON.stringify(payload), headers: {'Content-Type':'application/json'} });
  return await res.json();
});
ipcMain.handle('update-event', async (evt, payload) => {
  const res = await fetch(`${API_BASE}/api/events/${payload.id}`, { method: 'PUT', body: JSON.stringify(payload), headers: {'Content-Type':'application/json'} });
  return await res.json();
});
ipcMain.handle('delete-event', async (evt, id) => {
  const res = await fetch(`${API_BASE}/api/events/${id}`, { method: 'DELETE' });
  return res.status === 204 || res.ok;
});

// Falla endpoints
ipcMain.handle('get-falla', async (evt, id) => {
  const res = await fetch(`${API_BASE}/api/fallas/${id}`);
  if (!res.ok) throw new Error('Falla no encontrada');
  return await res.json();
});
ipcMain.handle('save-falla', async (evt, payload) => {
  const id = payload.id;
  const url = id ? `${API_BASE}/api/fallas/${id}` : `${API_BASE}/api/fallas`;
  const method = id ? 'PUT' : 'POST';
  const res = await fetch(url, { method: method, body: JSON.stringify(payload), headers: {'Content-Type':'application/json'} });
  return await res.json();
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
