package com.fallapp.features.auth.di

import com.fallapp.features.auth.data.remote.AuthApiService
import com.fallapp.features.auth.data.repository.AuthRepositoryImpl
import com.fallapp.features.auth.domain.repository.AuthRepository
import com.fallapp.features.auth.domain.usecase.LoginUseCase
import com.fallapp.features.auth.domain.usecase.LogoutUseCase
import com.fallapp.features.auth.domain.usecase.RegisterUseCase
import com.fallapp.features.auth.presentation.login.LoginViewModel
import com.fallapp.features.auth.presentation.register.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Módulo de Koin para el feature de autenticación.
 * 
 * Provee:
 * - AuthApiService
 * - AuthRepository (implementación)
 * - Casos de uso (LoginUseCase, RegisterUseCase, LogoutUseCase)
 * - ViewModels (LoginViewModel, RegisterViewModel)
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
val authModule = module {
    
    // Data / Remote - API Service
    single<AuthApiService> {
        AuthApiService(httpClient = get())
    }
    
    // Repository
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApiService = get(),
            tokenManager = get(),
            usuarioDao = get()
        )
    }
    
    // Use Cases
    factory<LoginUseCase> {
        LoginUseCase(authRepository = get())
    }
    
    factory<RegisterUseCase> {
        RegisterUseCase(authRepository = get())
    }
    
    factory<LogoutUseCase> {
        LogoutUseCase(authRepository = get())
    }
    
    // ViewModels
    viewModel { LoginViewModel(loginUseCase = get()) }
    viewModel { RegisterViewModel(registerUseCase = get()) }
}
