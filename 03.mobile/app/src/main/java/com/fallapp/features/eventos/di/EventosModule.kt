package com.fallapp.features.eventos.di

import com.fallapp.features.eventos.data.remote.EventosApiService
import com.fallapp.features.eventos.data.repository.EventosRepositoryImpl
import com.fallapp.features.eventos.domain.repository.EventosRepository
import com.fallapp.features.eventos.domain.usecase.GetEventosByFallaUseCase
import com.fallapp.features.eventos.domain.usecase.GetProximosEventosUseCase
import com.fallapp.features.eventos.presentation.EventosViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val eventosModule = module {

    // Data
    single { EventosApiService(client = get()) }
    single<EventosRepository> { EventosRepositoryImpl(apiService = get()) }

    // Use cases
    factory { GetEventosByFallaUseCase(repository = get()) }
    factory { GetProximosEventosUseCase(repository = get()) }

    // ViewModel
    viewModel {
        EventosViewModel(
            getFallasUseCase = get(),
            getEventosByFallaUseCase = get(),
            getProximosEventosUseCase = get()
        )
    }
}

