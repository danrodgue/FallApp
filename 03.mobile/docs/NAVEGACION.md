# 🧭 Sistema de Navegación - FallApp Mobile

**Versión:** 2.0.0  
**Fecha:** 2026-02-04  
**Estado:** ✅ Completado y Funcional

---

## 📋 Tabla de Contenidos

1. [Resumen](#resumen)
2. [Estructura de Navegación](#estructura-de-navegación)
3. [Rutas (Screens)](#rutas-screens)
4. [Bottom Navigation](#bottom-navigation)
5. [NavGraph](#navgraph)
6. [MainScreen](#mainscreen)
7. [Flujo de Usuario](#flujo-de-usuario)
8. [Código de Ejemplo](#código-de-ejemplo)

---

## 📌 Resumen

El sistema de navegación usa **Jetpack Compose Navigation** con una arquitectura de dos niveles:

1. **Navegación Principal (Bottom Navigation)**: 4 tabs principales
2. **Navegación Secundaria**: Pantallas de detalle y flujos específicos

### Características Principales

✅ **Bottom Navigation Bar** con 4 secciones principales  
✅ **Navegación con tipo seguro** usando sealed classes  
✅ **Gestión de back stack** correcta  
✅ **Navegación condicional** según autenticación  
✅ **Deep linking** preparado para URLs directas

---

## 🏗️ Estructura de Navegación

```
Login/Register
     ↓
MainScreen (Bottom Navigation)
├── Tab 0: Mapa 📍
│   └── Detalle de Falla
├── Tab 1: Lista de Fallas 📋
│   └── Detalle de Falla
├── Tab 2: Votos ⭐
│   ├── Votar → Detalle de Falla
│   ├── Mis Votos → Detalle de Falla
│   └── Ranking → Detalle de Falla
└── Tab 3: Perfil 👤
    └── Cerrar Sesión → Login

Detalle de Falla
└── Sistema de Votos
```

---

## 🎯 Rutas (Screens)

### Screen.kt (Sealed Class)

```kotlin
sealed class Screen(val route: String) {
    
    // Auth
    data object Login : Screen("login")
    data object Register : Screen("register")
    
    // Main Screen con Bottom Navigation
    data object Main : Screen("main")
    
    // Main (legacy, no se usa)
    data object Home : Screen("home")
    
    // Fallas
    data object FallasList : Screen("fallas_list")
    data object FallaDetail : Screen("fallas_detail/{fallaId}") {
        fun createRoute(fallaId: Long) = "fallas_detail/$fallaId"
    }
    
    // Mapa
    data object Map : Screen("map")
    
    // Profile
    data object Profile : Screen("profile")
}
```

### Uso de Rutas

```kotlin
// Navegación simple
navController.navigate(Screen.Main.route)

// Navegación con parámetros
navController.navigate(Screen.FallaDetail.createRoute(123))

// Navegación con limpieza de back stack
navController.navigate(Screen.Main.route) {
    popUpTo(Screen.Login.route) { inclusive = true }
}
```

---

## 📱 Bottom Navigation

### MainScreen.kt

La pantalla principal (`MainScreen`) contiene el Bottom Navigation Bar con 4 tabs:

| Index | Label | Icono | Pantalla |
|-------|-------|-------|----------|
| 0 | Mapa | 📍 `LocationOn` | `MapScreen` |
| 1 | Fallas | 📋 `List` | `FallasListScreen` |
| 2 | Votos | ⭐ `Star` | `VotosScreen` |
| 3 | Perfil | 👤 `Person` | `ProfileTab` |

### Características del Bottom Navigation

- **Estado persistente**: El tab seleccionado se guarda con `rememberSaveable`
- **Iconos y labels**: Cada tab tiene icono y texto descriptivo
- **Navegación interna**: Cada tab puede navegar a pantallas secundarias
- **Back button oculto**: Las pantallas principales no muestran flecha de retorno

```kotlin
@Composable
fun MainScreen(navController: NavHostController) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedItem) {
            0 -> MapScreen(...)
            1 -> FallasListScreen(...)
            2 -> VotosScreen(...)
            3 -> ProfileTab(...)
        }
    }
}
```

---

## 🗺️ NavGraph

### NavGraph.kt

Define todas las rutas de navegación y sus transiciones:

```kotlin
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Flow
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        
        // Main Screen con Bottom Navigation
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        
        // Falla Detail
        composable(Screen.FallaDetail.route) { backStackEntry ->
            val fallaId = backStackEntry.arguments?.getString("fallaId")?.toLongOrNull()
            if (fallaId != null) {
                FallaDetailScreen(
                    fallaId = fallaId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
```

---

## 🎨 MainScreen Detallado

### Estructura

```kotlin
MainScreen
├── Scaffold
│   ├── bottomBar: NavigationBar
│   │   └── NavigationBarItem x4
│   └── content: when(selectedItem)
│       ├── 0: MapScreen
│       ├── 1: FallasListScreen
│       ├── 2: VotosScreen
│       └── 3: ProfileTab
```

### Navegación desde Tabs

Cada tab puede navegar a pantallas secundarias:

```kotlin
// Desde Mapa (Tab 0)
MapScreen(
    onFallaClick = { fallaId ->
        navController.navigate(Screen.FallaDetail.createRoute(fallaId))
    },
    hideBackButton = true  // Oculta flecha de retorno
)

// Desde Lista de Fallas (Tab 1)
FallasListScreen(
    onFallaClick = { fallaId ->
        navController.navigate(Screen.FallaDetail.createRoute(fallaId))
    },
    hideBackButton = true
)

// Desde Votos (Tab 2)
VotosScreen(
    onFallaClick = { fallaId ->
        navController.navigate(Screen.FallaDetail.createRoute(fallaId))
    }
)

// Desde Perfil (Tab 3)
ProfileTab(
    onLogout = {
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }
)
```

---

## 🔄 Flujo de Usuario

### 1. Inicio de Sesión

```
Usuario abre app
    ↓
LoginScreen (startDestination)
    ↓ onLoginSuccess
navController.navigate(Screen.Main.route) {
    popUpTo(Screen.Login.route) { inclusive = true }
}
    ↓
MainScreen (Tab 0: Mapa por defecto)
```

### 2. Navegación Principal

```
MainScreen (Bottom Navigation)
    ├── Click Tab Mapa → MapScreen
    ├── Click Tab Fallas → FallasListScreen
    ├── Click Tab Votos → VotosScreen
    └── Click Tab Perfil → ProfileTab
```

### 3. Navegación a Detalle

```
Usuario en cualquier tab
    ↓
Click en una falla
    ↓
navController.navigate(Screen.FallaDetail.createRoute(fallaId))
    ↓
FallaDetailScreen
    ↓ onBackClick
navController.popBackStack()
    ↓
Vuelve al tab anterior
```

### 4. Cierre de Sesión

```
ProfileTab
    ↓
Click "Cerrar Sesión"
    ↓
navController.navigate(Screen.Login.route) {
    popUpTo(0) { inclusive = true }
}
    ↓
LoginScreen (se limpia todo el back stack)
```

---

## 💻 Código de Ejemplo

### Crear NavController en MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FallAppTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = Screen.Login.route
                )
            }
        }
    }
}
```

### Navegación Simple

```kotlin
// Navegar a otra pantalla
navController.navigate(Screen.Main.route)

// Navegar con parámetros
navController.navigate(Screen.FallaDetail.createRoute(123))

// Volver atrás
navController.popBackStack()

// Volver a pantalla específica
navController.popBackStack(Screen.Main.route, inclusive = false)
```

### Navegación con Limpieza de Stack

```kotlin
// Navegar eliminando Login del stack
navController.navigate(Screen.Main.route) {
    popUpTo(Screen.Login.route) { inclusive = true }
}

// Navegar eliminando todo el stack
navController.navigate(Screen.Login.route) {
    popUpTo(0) { inclusive = true }
}

// Evitar múltiples instancias
navController.navigate(Screen.Main.route) {
    launchSingleTop = true
}
```

### Parámetros Opcionales con hideBackButton

Las pantallas principales (`MapScreen`, `FallasListScreen`) aceptan un parámetro `hideBackButton`:

```kotlin
@Composable
fun MapScreen(
    onBackClick: () -> Unit,
    onFallaClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    hideBackButton: Boolean = false,  // ← Parámetro opcional
    viewModel: MapViewModel = koinViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Fallas") },
                navigationIcon = {
                    if (!hideBackButton) {  // ← Solo muestra si no está oculto
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Volver")
                        }
                    }
                }
            )
        }
    )
}
```

---

## 🎯 Gestión de Back Stack

### Escenarios Comunes

**Escenario 1: Desde Login a Main**
```kotlin
// ❌ MAL (Login queda en el stack)
navController.navigate(Screen.Main.route)

// ✅ BIEN (Login se elimina del stack)
navController.navigate(Screen.Main.route) {
    popUpTo(Screen.Login.route) { inclusive = true }
}
```

**Escenario 2: Navegación entre Tabs**
```kotlin
// ✅ Usar selectedItem en lugar de navigate
var selectedItem by remember { mutableIntStateOf(0) }

NavigationBarItem(
    selected = selectedItem == index,
    onClick = { selectedItem = index }  // ← Cambiar tab
)
```

**Escenario 3: Detalle y Volver**
```kotlin
// ✅ Navigate a detalle
navController.navigate(Screen.FallaDetail.createRoute(123))

// ✅ Volver con back button
FallaDetailScreen(
    onBackClick = { navController.popBackStack() }
)
```

---

## 🐛 Troubleshooting

### Problema: Back button cierra la app

**Causa:** No hay manejo especial del back button en MainActivity.

**Solución:** Agregar `BackHandler` en `MainScreen`:

```kotlin
BackHandler {
    // Comportamiento personalizado
    if (selectedItem != 0) {
        selectedItem = 0  // Volver al primer tab
    } else {
        // Salir de la app
        activity?.finish()
    }
}
```

### Problema: Tab se resetea al volver

**Causa:** `selectedItem` no está usando `rememberSaveable`.

**Solución:** Ya implementado:
```kotlin
var selectedItem by rememberSaveable { mutableIntStateOf(0) }
```

### Problema: Múltiples instancias de Main

**Causa:** Navigate sin `launchSingleTop`.

**Solución:**
```kotlin
navController.navigate(Screen.Main.route) {
    launchSingleTop = true
}
```

---

## 🚀 Mejoras Futuras

- [ ] Deep linking para URLs externas
- [ ] Animaciones personalizadas entre pantallas
- [ ] Transiciones compartidas (Shared Element Transitions)
- [ ] Guardar estado de navegación en SavedStateHandle
- [ ] Navegación por gestos (swipe back)
- [ ] Tabs secundarios en algunas pantallas
- [ ] Breadcrumbs para navegación profunda
- [ ] Modo tablet con navegación lateral

---

## 📚 Referencias

- **Jetpack Navigation Compose:** https://developer.android.com/jetpack/compose/navigation
- **Material 3 Navigation:** https://m3.material.io/components/navigation-bar
- **Bottom Navigation Best Practices:** https://material.io/components/bottom-navigation

---

**Última actualización:** 2026-02-04  
**Autor:** Equipo FallApp  
**Versión:** 2.0.0
