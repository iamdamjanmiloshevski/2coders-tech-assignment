package com.twocoders.movieapp.di

import com.twocoders.movieapp.core.connectivity.AndroidConnectivityObserver
import com.twocoders.movieapp.core.connectivity.ConnectivityObserver
import com.twocoders.movieapp.core.logging.AndroidLogger
import com.twocoders.movieapp.core.logging.Logger
import com.twocoders.movieapp.core.time.Clock
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Cross-cutting platform services. Each sits behind an interface, so tests can replace it. */
val coreModule = module {
    single<Logger> { AndroidLogger() }
    single<Clock> { Clock.Default }
    single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
}
