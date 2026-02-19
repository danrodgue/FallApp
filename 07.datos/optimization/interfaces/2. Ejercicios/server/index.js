/*
Ejercicio: creación de servicios GET básicos con Express:
/fecha para mostrar la fecha actual
/usuario para mostrar el usuario actual del sistema (usando la librería "os")
*/

// Carga de librerías necesarias
const express = require('express');
const os = require('os');

// Inicializar Express
let app = express();

// Servicio 1: GET /fecha para mostrar la fecha actual
app.get('/fecha', (req, res) => {
    res.send(new Date().toString());
});

// Servicio 2: GET /usuario para mostrar usuario actual
app.get('/usuario', (req, res) => {
    res.send(os.userInfo().username);
    
});

// Ponemos en marcha el servidor
app.listen(8080);