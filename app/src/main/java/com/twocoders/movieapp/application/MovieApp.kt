package com.twocoders.movieapp.application

import android.app.Application
import com.twocoders.movieapp.BuildConfig
import com.twocoders.movieapp.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MovieApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.NONE)
            androidContext(this@MovieApp)
            modules(appModules)
        }
    }
}
