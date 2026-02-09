package com.fallapp.features.fallas.di

import com.fallapp.features.fallas.data.remote.FallasApiService
import com.fallapp.features.fallas.data.remote.NinotsApiService
import com.fallapp.features.fallas.data.remote.VotosApiService
import com.fallapp.features.fallas.data.repository.FallasRepositoryImpl
import com.fallapp.features.fallas.data.repository.VotosRepositoryImpl
import com.fallapp.features.fallas.domain.repository.FallasRepository
import com.fallapp.features.fallas.domain.repository.VotosRepository
import com.fallapp.features.fallas.domain.usecase.*
import com.fallapp.features.fallas.presentation.detail.FallaDetailViewModel
import com.fallapp.features.fallas.presentation.list.FallasListViewModel
import com.fallapp.features.votos.presentation.VotosViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Módulo de Koin para la feature Fallas.
 * Provee todas las dependencias (API, Repository, UseCases, ViewModels).
 */
val fallasModule = module {
    
    // Data Layer - API
    single { FallasApiService(httpClient = get()) }
    single { VotosApiService(client = get(), tokenManager = get()) }
    single { NinotsApiService(client = get()) }
    
    // Data Layer - Repository
    single<FallasRepository> {
        FallasRepositoryImpl(
            apiService = get(),
            fallaDao = get(),
            networkMonitor = get()
        )
    }
    
    single<VotosRepository> {
        VotosRepositoryImpl(
            apiService = get(),
            ninotsApiService = get()
        )
    }
    
    // Domain Layer - Use Cases (Fallas)
    factory { GetFallasUseCase(repository = get()) }
    factory { GetFallaByIdUseCase(repository = get()) }
    factory { SearchFallasUseCase(repository = get()) }
    factory { GetFallasByCategoriaUseCase(repository = get()) }
    
    // Domain Layer - Use Cases (Votos)
    factory { VotarFallaUseCase(repository = get()) }
    factory { GetVotosUsuarioUseCase(repository = get()) }
    factory { EliminarVotoUseCase(repository = get()) }
    factory { GetVotosFallaUseCase(repository = get()) }
    
    // Presentation Layer - ViewModels
    viewModel {
        FallasListViewModel(
            getFallasUseCase = get(),
            searchFallasUseCase = get(),
            getFallasByCategoriaUseCase = get()
        )
    }
    
    viewModel {
        FallaDetailViewModel(
            getFallaByIdUseCase = get(),
            votarFallaUseCase = get(),
            getVotosFallaUseCase = get(),
            eliminarVotoUseCase = get()
        )
    }
    
    viewModel {
        VotosViewModel(
            getFallasUseCase = get(),
            votarFallaUseCase = get(),
            getVotosUsuarioUseCase = get(),
            eliminarVotoUseCase = get(),
            getVotosFallaUseCase = get()
        )
    }
}
