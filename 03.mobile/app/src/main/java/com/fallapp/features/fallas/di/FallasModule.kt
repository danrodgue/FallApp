package com.fallapp.features.fallas.di

import com.fallapp.features.fallas.data.remote.ComentariosApiService
import com.fallapp.features.fallas.data.remote.FallasApiService
import com.fallapp.features.fallas.data.remote.NinotsApiService
import com.fallapp.features.fallas.data.remote.VotosApiService
import com.fallapp.features.fallas.data.remote.EstadisticasApiService
import com.fallapp.features.auth.domain.usecase.GetCurrentUserUseCase
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

// Módulo de fallas: APIs, repos, use cases y ViewModels
val fallasModule = module {
    
    // APIs
    single { FallasApiService(httpClient = get()) }
    single { VotosApiService(client = get(), tokenManager = get()) }
    single { ComentariosApiService(client = get(), tokenManager = get()) }
    single { NinotsApiService(client = get()) }
    single { EstadisticasApiService(client = get()) }
    
    // Repos
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
    
    // Use cases fallas
    factory { GetFallasUseCase(repository = get()) }
    factory { GetFallaByIdUseCase(repository = get()) }
    factory { SearchFallasUseCase(repository = get()) }
    factory { GetFallasByCategoriaUseCase(repository = get()) }
    
    // Use cases votos
    factory { VotarFallaUseCase(repository = get()) }
    factory { GetVotosUsuarioUseCase(repository = get()) }
    factory { GetMisVotosUseCase(repository = get()) }
    factory { EliminarVotoUseCase(repository = get()) }
    factory { GetVotosFallaUseCase(repository = get()) }
    factory { GetRankingUseCase(apiService = get()) }
    factory { CrearComentarioUseCase(comentariosApiService = get(), getCurrentUserUseCase = get()) }
    
    // ViewModels
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
            getMisVotosUseCase = get(),
            eliminarVotoUseCase = get(),
            getVotosFallaUseCase = get(),
            getRankingUseCase = get(),
            crearComentarioUseCase = get()
        )
    }
}
