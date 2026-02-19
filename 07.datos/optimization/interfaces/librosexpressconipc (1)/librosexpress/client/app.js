

const nf=require('node-fetch');
const { ipcRenderer } = require('electron');


const recurso = "http://127.0.0.1:8080";

let representaLibros = (books => {
    let cadenaDOM = "";
    books.forEach(book => {
        cadenaDOM +=
            `<div>
                
                    <img src="${recurso}/public/${book.img}" height="170" width="108">
                    <br>
                    <label><strong>${book.title}</strong></label>
                    <br>
                    <label>${book.author}</label>
                
            </div>`;
    });
    document.getElementById("wrapper").innerHTML = cadenaDOM;
});



let buscarTodos = () => {
 
console.log("Pintando todo")
//Get para todos los libros:
nf(recurso + '/libros')
    .then(res => res.json())
    .then(json =>representaLibros(json));


   
}


//escuchador del boton buscar libro por autor

document.getElementById("btnBuscarAutor").addEventListener('click', () => {
    let txtBuscarAutor = document.getElementById("txtBuscarAutor").value;
    if (txtBuscarAutor == "") {
        let notification = document.querySelector("#notification");
        notification.innerHTML = "Debe escribir algo";
        notification.opened = true;
    } else {
        console.log(recurso+'/libros2/'+txtBuscarAutor)
        //buscamos el libro o libros
        nf(recurso+'/libros2/'+txtBuscarAutor)
        .then(res => res.json())
        .then(json => representaLibros(json));
    }
});
//escuchador del boton buscar libro por titulo
document.getElementById("btnBuscarTitulo").addEventListener('click', () => {
    console.log("buscando")
    let txtBuscar = document.getElementById("txtBuscarTitulo").value;
    if (txtBuscar == "") {
        let notification = document.querySelector("#notification");
        notification.innerHTML = "Debe escribir algo";
        notification.opened = true;
    } else {
        console.log(recurso+'/libros/'+txtBuscar)
        //buscamos el libro o libros
        nf(recurso+'/libros/'+txtBuscar)
        .then(res => res.json())
        .then(json => representaLibros(json));
    
    }
});
document.getElementById("btnNuevoLibro").addEventListener('click', () => {
    let txtNuevoTitulo = document.getElementById("txtNuevotitulo").value;
    let txtNuevoAutor = document.getElementById("txtNuevoAutor").value;
    let txtNuevaImagen = document.getElementById("txtNuevaImagen").value;

    if (txtNuevoTitulo == "" || txtNuevoAutor == "" || txtNuevaImagen == "") {
        let notification = document.querySelector("#notification2");
        notification.innerHTML = "Debe escribir algo";
        notification.opened = true;
    } else {
        //Insertamos el libro
        let nuevo = {
            title: txtNuevoTitulo,
            author: txtNuevoAutor,
            img: txtNuevaImagen
        };

        nf(recurso + '/libros', {
            method: 'post',
            body: JSON.stringify(nuevo),
            headers: { 'Content-Type': 'application/json' },
          })
            .then(res => res.json())
            .then(json => {
                let notification = document.querySelector("#notification2");
                 notification.innerHTML = "Libro Añadido";
                 notification.opened = true;
                 buscarTodos();
                console.log(json)}).catch(error => {
                    let notification = document.querySelector("#notification2");
                    notification.innerHTML = "NO se ha podido añadir el libro";
                    notification.opened = true;
                });;

    }
});
document.getElementById("btnBorrarLibro").addEventListener('click', () => {
    let txtBorrar = document.getElementById("txtBorrarTitulo").value;
    if (txtBorrar == "") {
        let notification = document.querySelector("#notification3");
        notification.innerHTML = "Debe escribir algo";
        notification.opened = true;
    } else {
        console.log(recurso+'/libros/'+txtBorrar)
        nf(recurso+'/libros/'+txtBorrar,{
            method:'delete',
            headers: { 'Content-Type': 'application/json' }

        }).then(result => {
            let notification = document.querySelector("#notification3");
            notification.innerHTML = "Libro Borrado";
            notification.opened = true;
        }).catch(error => {
            let notification = document.querySelector("#notification");
            notification.innerHTML = "NO se ha podido borrar el libro";
            notification.opened = true;
        });
        buscarTodos();
    }
})

//escuchador del boton buscar todos
document.getElementById("btnTodos").addEventListener('click', () => {
    buscarTodos();
});

document.getElementById("btnVer").addEventListener('click', () => {
    let txtVerTitulo = document.getElementById("txtVer").value;
    nf(recurso+'/libros/'+txtVerTitulo)
        .then(res => res.json())
        .then(json => ipcRenderer.send("verLibro",json))
        .then(()=>ipcRenderer.on("pintarLibro",(event,arg)=>
        {
            let win=window.open();
            win.document.write('<h1>El titulo del libro es</h1>'+arg[0].title)
            console.log(arg[0].title)
        }));
    
    
});


buscarTodos()