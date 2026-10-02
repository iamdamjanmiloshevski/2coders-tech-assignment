package com.twocoders.movieapp.di

import androidx.room.Room
import com.twocoders.movieapp.data.local.MediaLocalDataSource
import com.twocoders.movieapp.data.local.MovieDatabase
import com.twocoders.movieapp.data.local.RoomMediaLocalDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** The Room offline cache. Only `MediaLocalDataSource` is meant to be injected elsewhere. */
val localModule = module {
    single {
        Room.databaseBuilder(androidContext(), MovieDatabase::class.java, MovieDatabase.NAME)
            // It's a cache, so after a schema change it's rebuilt from the network instead of migrated.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<MovieDatabase>().pageDao() }
    single { get<MovieDatabase>().detailsDao() }
    single<MediaLocalDataSource> { RoomMediaLocalDataSource(get(), get(), get()) }
}
