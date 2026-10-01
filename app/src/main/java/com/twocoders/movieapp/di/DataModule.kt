package com.twocoders.movieapp.di

import com.twocoders.movieapp.BuildConfig
import com.twocoders.movieapp.data.mapper.ImageUrlBuilder
import com.twocoders.movieapp.data.repository.MovieRepositoryImpl
import com.twocoders.movieapp.data.repository.NetworkFirst
import com.twocoders.movieapp.data.repository.SearchRepositoryImpl
import com.twocoders.movieapp.data.repository.TvShowRepositoryImpl
import com.twocoders.movieapp.domain.repository.MovieRepository
import com.twocoders.movieapp.domain.repository.SearchRepository
import com.twocoders.movieapp.domain.repository.TvShowRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Binds the domain repository interfaces to their implementations: TMDB first, the Room cache as fallback. */
val dataModule = module {
    single { ImageUrlBuilder(baseUrl = BuildConfig.IMAGE_BASE_URL) }
    singleOf(::NetworkFirst)

    singleOf(::MovieRepositoryImpl) { bind<MovieRepository>() }
    singleOf(::TvShowRepositoryImpl) { bind<TvShowRepository>() }
    singleOf(::SearchRepositoryImpl) { bind<SearchRepository>() }
}
