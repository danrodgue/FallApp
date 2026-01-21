const { app, BrowserWindow } = require('electron');

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    fullscreen: false,         // No usar el modo fullscreen (tipo F11)
    fullscreenable: false,     // Evita entrar en fullscreen desde el SO
    autoHideMenuBar: true,      // Oculta el menú
    webPreferences: {
      nodeIntegration: true,   // Permite usar Node en el renderer
      contextIsolation: false  // Simplifica el arranque (ok para proyectos simples)
    }
  });

  mainWindow.loadFile('index.html');

  // Abrir la ventana en modo maximizado (pantalla completa pero mostrando barra de tareas)
  mainWindow.maximize();

  // Quitar menú completamente (Windows/Linux)
  mainWindow.setMenu(null);

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

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
