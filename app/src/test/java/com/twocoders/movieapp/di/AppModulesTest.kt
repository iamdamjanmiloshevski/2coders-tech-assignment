package com.twocoders.movieapp.di

import com.twocoders.movieapp.core.connectivity.ConnectivityObserver
import com.twocoders.movieapp.data.local.MediaLocalDataSource
import com.twocoders.movieapp.data.remote.TmdbApi
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.repository.MovieRepository
import com.twocoders.movieapp.domain.repository.SearchRepository
import com.twocoders.movieapp.domain.repository.TvShowRepository
import com.twocoders.movieapp.domain.usecase.GetMediaDetailsUseCase
import com.twocoders.movieapp.domain.usecase.GetPopularMoviesUseCase
import com.twocoders.movieapp.domain.repository.FavoritesRepository
import com.twocoders.movieapp.domain.usecase.ObserveFavoritesUseCase
import com.twocoders.movieapp.domain.usecase.RestoreFavoriteUseCase
import com.twocoders.movieapp.domain.usecase.SearchMediaUseCase
import com.twocoders.movieapp.domain.usecase.ToggleFavoriteUseCase
import com.twocoders.movieapp.presentation.connectivity.ConnectivityViewModel
import com.twocoders.movieapp.presentation.details.DetailsViewModel
import com.twocoders.movieapp.presentation.favorites.FavoritesViewModel
import com.twocoders.movieapp.presentation.movies.MovieListViewModel
import com.twocoders.movieapp.presentation.search.SearchViewModel
import com.twocoders.movieapp.fakes.FakeConnectivityObserver
import com.twocoders.movieapp.fakes.FakeFavoritesRepository
import com.twocoders.movieapp.fakes.FakeMediaLocalDataSource
import com.twocoders.movieapp.testutil.MainDispatcherRule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import org.koin.test.get

/**
 * Starts the real production graph and resolves every entry point. A missing binding or
 * a constructor that changed without its module fails here, not at app launch.
 *
 * The only replacements are bindings that need an Android `Context`, because a JVM test has none.
 */
class AppModulesTest : KoinTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val koinRule = KoinTestRule.create { modules(appModules + androidFreeOverrides) }

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
        assertNotNull(get<ObserveFavoritesUseCase>())
        assertNotNull(get<ToggleFavoriteUseCase>())
        assertNotNull(get<RestoreFavoriteUseCase>())
    }

    @Test
    fun `view models resolve, including route parameters for details`() {
        assertNotNull(get<MovieListViewModel>())
        assertNotNull(get<SearchViewModel>())
        assertNotNull(get<DetailsViewModel> { parametersOf(1, MediaType.MOVIE) })
        assertNotNull(get<ConnectivityViewModel>())
        assertNotNull(get<FavoritesViewModel>())
    }
}

private val androidFreeOverrides = module {
    single<ConnectivityObserver> { FakeConnectivityObserver() }
    single<MediaLocalDataSource> { FakeMediaLocalDataSource() }
    single<FavoritesRepository> { FakeFavoritesRepository() }
}
