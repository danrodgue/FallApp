const assert = require('node:assert/strict');

describe('Registro - validación', () => {
  it('rechaza teléfono con pocos dígitos', async () => {
    await browser.url('/js/register.html');

    const nombre = await $('#nombreCompleto');
    const email = await $('#email');
    const password = await $('#password');
    const telefono = await $('#telefono');
    const buscarFalla = await $('#fallaSearch');
    const idFalla = await $('#idFalla');
    const boton = await $('#registerBtn');
    const error = await $('#register-error-msg');

    await browser.execute(() => {
      const hidden = document.getElementById('idFalla');
      hidden.value = '1';
      const campo = document.getElementById('fallaSearch');
      campo.value = 'Falla de prueba';
    });

    await nombre.setValue('Casal Prueba');
    await email.setValue('casal@fallapp.es');
    await password.setValue('123456');
    await telefono.setValue('123');

    await buscarFalla.click();
    await idFalla.getValue();
    await boton.click();

    const textoValidacion = await browser.execute(() => {
      return document.getElementById('telefono').validationMessage;
    });

    const visibleError = await error.isDisplayed();
    assert.equal(visibleError, false);
    assert.ok(textoValidacion && textoValidacion.length > 0);
  });

  it('rechaza código postal inválido', async () => {
    await browser.url('/js/register.html');

    await browser.execute(() => {
      document.getElementById('idFalla').value = '1';
      document.getElementById('fallaSearch').value = 'Falla de prueba';
    });

    await $('#nombreCompleto').setValue('Casal Prueba');
    await $('#email').setValue('casal@fallapp.es');
    await $('#password').setValue('123456');
    await $('#codigoPostal').setValue('12');
    await $('#registerBtn').click();

    const textoValidacion = await browser.execute(() => {
      return document.getElementById('codigoPostal').validationMessage;
    });

    assert.ok(textoValidacion && textoValidacion.length > 0);
  });
});
