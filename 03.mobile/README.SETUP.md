# Configuración inicial (móvil)

## local.properties (obligatorio en cada máquina)

El archivo `local.properties` **no se sube a Git** porque contiene la ruta del SDK de Android de cada desarrollador (por ejemplo `C:\Users\7J\...` en un PC y `/home/otro_usuario/...` en otro).

**Primera vez que clonas el repo o cambias de ordenador:**

1. Copia `local.properties.example` a `local.properties`.
2. Edita `local.properties` y pon la ruta de tu Android SDK:
   - **Windows:** `sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk`
   - **Mac:** `sdk.dir=/Users/tu_usuario/Library/Android/sdk`
   - **Linux:** `sdk.dir=/home/tu_usuario/Android/Sdk`
3. Guarda el archivo. Gradle usará esta ruta para compilar.

Si `local.properties` se subió a Git por error en el pasado, quítalo del índice (sin borrarlo en disco):

```bash
git rm --cached 03.mobile/local.properties
git commit -m "Dejar de trackear local.properties"
```

A partir de ahí, cada desarrollador tendrá su propio `local.properties` local y no se volverá a subir.
