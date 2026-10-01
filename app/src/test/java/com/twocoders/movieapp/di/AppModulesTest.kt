package com.twocoders.movieapp.di

import com.twocoders.movieapp.data.remote.TmdbApi
import com.twocoders.movieapp.domain.repository.MovieRepository
import com.twocoders.movieapp.domain.repository.SearchRepository
import com.twocoders.movieapp.domain.repository.TvShowRepository
import com.twocoders.movieapp.domain.usecase.GetMediaDetailsUseCase
import com.twocoders.movieapp.domain.usecase.GetPopularMoviesUseCase
import com.twocoders.movieapp.domain.usecase.SearchMediaUseCase
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

/**
 * Starts the real production graph and resolves every entry point. A missing binding or
 * a constructor that changed without its module fails here, not at app launch.
 */
class AppModulesTest : KoinTest {

    @get:Rule
    val koinRule = KoinTestRule.create { modules(appModules) }

    @Test
    fun `network and data graph resolves`() {
        assertNotNull(get<TmdbApi>())
        assertNotNull(get<MovieRepository>())
        assertNotNull(get<TvShowRepository>())
        assertNotNull(get<SearchRepository>())
    }

    @Test
    fun `use cases resolve`() {
        assertNotNull(get<GetPopularMoviesUseCase>())
        assertNotNull(get<GetMediaDetailsUseCase>())
        assertNotNull(get<SearchMediaUseCase>())
    }
}
