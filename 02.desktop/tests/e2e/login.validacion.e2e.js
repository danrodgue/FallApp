const assert = require('node:assert/strict');

describe('Login - validación', () => {
  it('muestra error si el email no tiene formato válido', async () => {
    await browser.url('/js/index.html');

    const inputEmail = await $('#email');
    const inputPassword = await $('#password');
    const botonEnviar = await $('button[type="submit"]');
    const cajaError = await $('#error-msg');

    await inputEmail.setValue('correo-invalido');
    await inputPassword.setValue('123456');
    await botonEnviar.click();

    await cajaError.waitForDisplayed({ timeout: 4000 });
    const texto = await cajaError.getText();

    assert.match(texto, /email válido/i);
  });

  it('muestra error si la contraseña está vacía', async () => {
    await browser.url('/js/index.html');

    const inputEmail = await $('#email');
    const inputPassword = await $('#password');
    const botonEnviar = await $('button[type="submit"]');
    const cajaError = await $('#error-msg');

    await inputEmail.setValue('casal@fallapp.es');
    await inputPassword.setValue('');
    await botonEnviar.click();

    await cajaError.waitForDisplayed({ timeout: 4000 });
    const texto = await cajaError.getText();

    assert.match(texto, /contraseña/i);
  });
});
