package com.twocoders.movieapp.di

import androidx.room.Room
import com.twocoders.movieapp.data.local.user.UserDatabase
import com.twocoders.movieapp.data.repository.FavoritesRepositoryImpl
import com.twocoders.movieapp.domain.repository.FavoritesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * User-created data (favorites). Deliberately has **no** destructive migration fallback: a missing
 * migration should crash in development, not silently wipe the user's favorites.
 */
val userDataModule = module {
    single { Room.databaseBuilder(androidContext(), UserDatabase::class.java, UserDatabase.NAME).build() }
    single { get<UserDatabase>().favoriteDao() }
    singleOf(::FavoritesRepositoryImpl) { bind<FavoritesRepository>() }
}
