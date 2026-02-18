(function () {
  const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const regexTelefono = /^[+()\-\s\d]{7,20}$/;
  const regexCodigoPostal = /^\d{5}$/;

  function texto(valor) {
    return String(valor || '').trim();
  }

  function emailValido(valor) {
    const limpio = texto(valor);
    return limpio.length > 0 && regexEmail.test(limpio);
  }

  function telefonoValido(valor) {
    const limpio = texto(valor);
    if (!limpio) return true;
    const totalDigitos = limpio.replace(/\D/g, '').length;
    return regexTelefono.test(limpio) && totalDigitos >= 9;
  }

  function codigoPostalValido(valor) {
    const limpio = texto(valor);
    if (!limpio) return true;
    return regexCodigoPostal.test(limpio);
  }

  function passwordValida(valor, minimo) {
    const limite = Number(minimo || 6);
    return String(valor || '').length >= limite;
  }

  function ponerError(campo, mensaje) {
    if (!campo) return false;
    campo.setCustomValidity(mensaje || 'Campo inválido');
    return false;
  }

  function limpiarError(campo) {
    if (!campo) return true;
    campo.setCustomValidity('');
    return true;
  }

  function validarCampo(campo, esValido, mensaje) {
    if (esValido) {
      return limpiarError(campo);
    }
    return ponerError(campo, mensaje);
  }

  function pintarError(contenedor, mensaje) {
    if (!contenedor) return;
    contenedor.textContent = mensaje || '';
    contenedor.style.display = mensaje ? 'block' : 'none';
  }

  window.validacionFormulario = {
    texto,
    emailValido,
    telefonoValido,
    codigoPostalValido,
    passwordValida,
    validarCampo,
    pintarError
  };
})();