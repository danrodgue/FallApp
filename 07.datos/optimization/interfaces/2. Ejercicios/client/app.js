import fetch from 'node-fetch';
const recurso = "http://127.0.0.1:8080";

//Get para fecha:
fetch(recurso + '/fecha')
  .then(res => res.text())
  .then(body => console.log(body));

//Get para usuario:
fetch(recurso + '/usuario')
  .then(res => res.text())
  .then(body => console.log(body));
