# Feature: Auth (Autenticación)

## 📋 Descripción
Gestiona el flujo de autenticación de usuarios: login, registro y logout.

## 🏗️ Estructura Clean Architecture

```
auth/
├── data/               # Capa de datos (API + Repository)
│   ├── api/           # AuthApiService (endpoints)
│   ├── dto/           # DTOs (LoginRequest, LoginResponse, etc.)
│   └── repository/    # AuthRepositoryImpl
├── domain/            # Capa de dominio (lógica de negocio)
│   ├── model/         # User, AuthToken models
│   ├── repository/    # AuthRepository interface
│   └── usecase/       # LoginUseCase, RegisterUseCase, LogoutUseCase
└── presentation/      # Capa de presentación (UI)
    ├── login/         # LoginScreen, LoginViewModel
    └── register/      # RegisterScreen, RegisterViewModel
```

## 🔗 Endpoints API

- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/registro` - Registrar usuario
- `POST /api/auth/logout` - Cerrar sesión

## ✅ Estado

⏳ **Pendiente de implementación** (Steps 8-11)
