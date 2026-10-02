package com.twocoders.movieapp.di

import com.twocoders.movieapp.core.logging.AndroidLogger
import com.twocoders.movieapp.core.logging.Logger
import org.koin.dsl.module

val coreModule = module {
    single<Logger> { AndroidLogger() }
}
