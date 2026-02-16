package com.fallapp.features.profile.di

import com.fallapp.features.profile.data.remote.ProfileApiService
import com.fallapp.features.profile.data.repository.ProfileRepositoryImpl
import com.fallapp.features.profile.domain.repository.ProfileRepository
import com.fallapp.features.profile.domain.usecase.GetUserProfileUseCase
import com.fallapp.features.profile.domain.usecase.UpdateUserProfileUseCase
import com.fallapp.features.profile.presentation.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Módulo de Koin para la feature Profile.
 * Provee todas las dependencias (API, Repository, UseCases, ViewModels).
 */
val profileModule = module {

    // Data Layer - API
    single { ProfileApiService(httpClient = get()) }

    // Data Layer - Repository
    single<ProfileRepository> {
        ProfileRepositoryImpl(profileApiService = get())
    }

    // Domain Layer - Use Cases
    factory { GetUserProfileUseCase(profileRepository = get()) }
    factory { UpdateUserProfileUseCase(profileRepository = get()) }

    // Presentation Layer - ViewModels
    viewModel {
        ProfileViewModel(
            getUserProfileUseCase = get(),
            updateUserProfileUseCase = get(),
            tokenManager = get()
        )
    }
}

