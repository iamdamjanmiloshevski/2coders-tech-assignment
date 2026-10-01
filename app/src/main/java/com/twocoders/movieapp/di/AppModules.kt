package com.twocoders.movieapp.di

/** Every Koin module the app needs, in one list for [com.twocoders.movieapp.MovieApp] and the DI test. */
val appModules = listOf(
    coreModule,
    networkModule,
    dataModule,
    domainModule,
)
