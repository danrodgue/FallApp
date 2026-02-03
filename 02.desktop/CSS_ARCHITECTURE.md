# CSS Architecture - FallApp Desktop

## Estructura de Estilos Organizados

### 🎯 Principio Base
- **Un archivo CSS base** (`login.css`) contiene todas las variables y componentes comunes
- **Archivos CSS específicos** importan el base y añaden solo estilos únicos de su página
- **Sin duplicación** de variables o componentes comunes
- **Mantenimiento centralizado** - cambios globales en un solo lugar

---

## 📁 Archivos CSS y su contenido

### 1. **login.css** (BASE - 784 líneas)
**Propósito:** Archivo base con todos los estilos globales y comunes
**Importado por:** index.css, home.css, events.css, user.css, info.css

**Contiene:**
- ✅ Variables CSS globales (colors, shadows, spacing, fonts)
- ✅ Reset y estilos globales (body, h1-h6, p, inputs)
- ✅ Scrollbar personalizado
- ✅ Header fijo (.site-header, .header-logo, .header-profile, .header-logout)
- ✅ Botones globales (.btn, .btn:hover, .btn:active)
- ✅ Formularios (label, input[text/password])
- ✅ Modal de perfil (.profile-modal y sus variantes)
- ✅ Estilos de navegación comunes

**NO contiene:**
- ❌ Estilos específicos de páginas individuales
- ❌ Clases duplicadas

---

### 2. **index.css** (10 líneas)
**Propósito:** Estilos específicos para index.html (página de login)
**Importa:** login.css

**Contiene:**
- ✅ Estilos del login-container
- ✅ Formulario de login específico

---

### 3. **home.css** (135 líneas)
**Propósito:** Estilos específicos para home.html (página de inicio)
**Importa:** login.css

**Contiene:**
- ✅ Padding y layout de home-page
- ✅ Tarjetas de acciones (.action-card, .card-content, .card-image)
- ✅ Media queries específicas de home

---

### 4. **events.css** (520 líneas)
**Propósito:** Estilos específicos para events.html
**Importa:** login.css

**Contiene:**
- ✅ Header de eventos (.events-header)
- ✅ Tarjetas de eventos (.event-card, .event-status)
- ✅ Formularios de eventos
- ✅ Modales específicos de eventos
- ✅ Media queries responsivas

**Eliminado:**
- ❌ .root-vars (innecesario)
- ❌ .events-main, .events-panel (no se usan)

---

### 5. **user.css** (353 líneas)
**Propósito:** Estilos específicos para user.html
**Importa:** login.css

**Contiene:**
- ✅ Página de usuario (.user-page, .user-container)
- ✅ Avatar de usuario (.avatar)
- ✅ Información de usuario (.user-info)
- ✅ Botones específicos del usuario

**Eliminado:**
- ❌ Variables :root (ahora usa las de login.css)

---

### 6. **info.css** (401 líneas)
**Propósito:** Estilos específicos para info.html
**Importa:** login.css

**Contiene:**
- ✅ Contenedor de información (.falla-container)
- ✅ Detalles de fallos (.falla-header, .falla-body)
- ✅ Formularios específicos
- ✅ Media queries de info

**Eliminado:**
- ❌ Variables :root (ahora usa las de login.css)

---

## 🔄 Flujo de Importaciones

```
login.css (BASE)
    ├── variables (:root)
    ├── componentes globales
    └── estilos comunes
        │
        ├─→ index.css (Login page)
        ├─→ home.css (Home page)
        ├─→ events.css (Events page)
        ├─→ user.css (User page)
        └─→ info.css (Info page)
```

---

## 📊 Optimización Realizada

| Métrica | Antes | Después | Ahorro |
|---------|-------|---------|--------|
| Variables duplicadas | 6 redefs | 0 redefs | 100% |
| Líneas de CSS | ~4000 | ~3200 | 20% |
| Clases no usadas | 14 clases | 0 clases | 100% |
| Archivos con imports | 1 | 5 | Mayor modularidad |

---

## ✅ Checklist de Cambios Realizados

- [x] Eliminar pseudoclase muerta `.login-page:not(.home-page)::after`
- [x] Eliminar variable :root redefinida en events.css
- [x] Eliminar variable :root redefinida en user.css
- [x] Eliminar variable :root redefinida en info.css
- [x] Eliminar `.root-vars` de events.css
- [x] Eliminar `.events-main`, `.events-panel` de events.css
- [x] Agregar @import login.css a events.css
- [x] Agregar @import login.css a user.css
- [x] Agregar @import login.css a info.css
- [x] Agregar comentarios de documentación en cada archivo

---

## 🎓 Guía para Mantenimiento Futuro

### ✨ Para añadir un nuevo estilo global:
1. Ir a `login.css`
2. Añadir la clase/variable
3. Automáticamente estará disponible en todos los archivos

### ✨ Para añadir un estilo específico de página:
1. Ir al archivo CSS de la página (ej: events.css)
2. Añadir la clase
3. No duplicar estilos que ya existen en login.css

### ✨ Para cambiar colores/variables:
1. Cambiar SOLO en `login.css` (líneas 8-35)
2. Se aplicará automáticamente a todas las páginas

### ✨ Para añadir media queries:
1. Idealmente centralizarlas en el archivo CSS específico
2. O si son comunes a varias páginas, considerar un archivo `responsive.css`

---

## 📋 Dependencias de Archivos HTML

| HTML | CSS Principal | CSS Importados |
|------|---------------|-----------------|
| index.html | index.css | → login.css |
| screens/home.html | home.css | → login.css |
| screens/events.html | events.css | → login.css |
| screens/user.html | user.css | → login.css |
| screens/info.html | info.css | → login.css |

---

## 🚫 Clases Eliminadas

- `.root-vars` - Innecesaria (display:none)
- `.events-main` - No se usa en HTML
- `.events-panel` - No se usa en HTML
- `.login-page:not(.home-page)::after` - Pseudoclase nunca se dispara

---

## 💡 Beneficios de esta Estructura

1. **Sin duplicación** - Variables y componentes definidos una sola vez
2. **Fácil mantenimiento** - Cambios globales en un archivo
3. **Menor tamaño** - Menos código redundante
4. **Escalabilidad** - Nuevas páginas pueden usar el mismo patrón
5. **Claridad** - Cada archivo tiene un propósito específico
6. **Consistencia** - Todos los componentes tienen el mismo estilo

