package com.fallapp.features.map.di

import com.fallapp.features.map.presentation.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Módulo de inyección de dependencias para Map feature.
 */
val mapModule = module {
    
    // ViewModel
    viewModel { MapViewModel(get()) }
}
