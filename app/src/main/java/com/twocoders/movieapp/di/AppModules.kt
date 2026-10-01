package com.twocoders.movieapp.di

/** Every Koin module the app needs, in one list for [com.twocoders.movieapp.application.MovieApp] and the DI test. */
val appModules = listOf(
    coreModule,
    networkModule,
    localModule,
    userDataModule,
    dataModule,
    domainModule,
    presentationModule,
)
