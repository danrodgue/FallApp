const { contextBridge, ipcRenderer } = require('electron');

// Exponer una API mínima y segura al renderer
contextBridge.exposeInMainWorld('api', {
  // Events
  getEvents: () => ipcRenderer.invoke('get-events'),
  createEvent: (payload) => ipcRenderer.invoke('create-event', payload),
  updateEvent: (payload) => ipcRenderer.invoke('update-event', payload),
  deleteEvent: (id) => ipcRenderer.invoke('delete-event', id),
  // Falla (info)
  getFalla: (id) => ipcRenderer.invoke('get-falla', id),
  saveFalla: (payload) => ipcRenderer.invoke('save-falla', payload),
  deleteFalla: (id) => ipcRenderer.invoke('delete-falla', id)
});
