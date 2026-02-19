const express = require('express');
const bodyParser = require('body-parser');
let app = express();
const model = require('./model')
let Libro = model.Libro
app.use(bodyParser.json());

app.use('/public', express.static(__dirname + '/public'));

app.get('/libros', (req, res) => {
    Libro.find().then(libros =>
        res.send(libros)).catch(error => {
            console.log("ERROR:", error);
        });
});



app.get('/libros2/:author', (req, res) => {
    //buscamos el libro o libros
    console.log(req.params.author)
    Libro.find({ author: { $regex: req.params.author } }).then(resultado => {
        console.log(resultado)
        res.send(resultado);
        //creamos el DOM para esos libros
    }).catch(error => {
        res.send("ERROR al buscar por título");
    });

});



app.get('/libros/:title', (req, res) => {
    //buscamos el libro o libros

    Libro.find({ title: { $regex: req.params.title } }).then(resultado => {
        res.send(resultado);
        //creamos el DOM para esos libros
    }).catch(error => {
        res.send("ERROR al buscar por título");
    });

});

app.post('/libros', (req, res) => {
    try {
        let nuevoLibro = new Libro(req.body);
        nuevoLibro.save()
        res.send({ ok: true });
    }
    catch (err) {
        res.send({ ok: false });
    }
});

app.delete('/libros/:titulo', (req, res) => {
    try {
        Libro.deleteOne({ title: req.params.titulo }).then(result => console.log(result))
        res.send({ ok: true });
    }
    catch (err) {
        res.send({ ok: false });
    }
})
app.listen(8080)

