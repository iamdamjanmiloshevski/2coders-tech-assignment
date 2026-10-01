package com.twocoders.movieapp.di

import com.twocoders.movieapp.domain.usecase.GetMediaDetailsUseCase
import com.twocoders.movieapp.domain.usecase.GetPopularMoviesUseCase
import com.twocoders.movieapp.domain.usecase.SearchMediaUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Use cases hold no state, so a fresh instance per injection is fine. */
val domainModule = module {
    factoryOf(::GetPopularMoviesUseCase)
    factoryOf(::GetMediaDetailsUseCase)
    factoryOf(::SearchMediaUseCase)
}
