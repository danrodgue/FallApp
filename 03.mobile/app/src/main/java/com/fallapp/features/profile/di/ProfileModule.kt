package com.fallapp.features.profile.di

import com.fallapp.features.profile.data.remote.ProfileApiService
import com.fallapp.features.profile.data.repository.ProfileRepositoryImpl
import com.fallapp.features.profile.domain.repository.ProfileRepository
import com.fallapp.features.profile.domain.usecase.GetUserProfileUseCase
import com.fallapp.features.profile.domain.usecase.UploadUserImageUseCase
import com.fallapp.features.profile.domain.usecase.UpdateUserProfileUseCase
import com.fallapp.features.profile.presentation.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Módulo de perfil: API, repos, use cases y ViewModel
val profileModule = module {

    single { ProfileApiService(httpClient = get(), tokenManager = get()) }

    single<ProfileRepository> {
        ProfileRepositoryImpl(profileApiService = get())
    }

    factory { GetUserProfileUseCase(profileRepository = get()) }
    factory { UpdateUserProfileUseCase(profileRepository = get()) }
    factory { UploadUserImageUseCase(profileRepository = get()) }

    viewModel {
        ProfileViewModel(
            getUserProfileUseCase = get(),
            updateUserProfileUseCase = get(),
            uploadUserImageUseCase = get(),
            tokenManager = get()
        )
    }
}

