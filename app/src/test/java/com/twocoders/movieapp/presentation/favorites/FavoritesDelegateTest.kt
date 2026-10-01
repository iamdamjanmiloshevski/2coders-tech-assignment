package com.twocoders.movieapp.presentation.favorites

import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.key
import com.twocoders.movieapp.domain.usecase.ObserveFavoritesUseCase
import com.twocoders.movieapp.domain.usecase.ToggleFavoriteUseCase
import com.twocoders.movieapp.fakes.FakeFavoritesRepository
import com.twocoders.movieapp.fakes.TestData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesDelegateTest {

    private val repository = FakeFavoritesRepository()

    /**
     * The delegate's `stateIn` never completes, so it needs a background scope that's cancelled
     * when the test ends. The unconfined dispatcher makes its work run eagerly, because
     * `advanceUntilIdle()` doesn't drive background tasks.
     */
    private fun TestScope.delegate() = FavoritesDelegate(
        ObserveFavoritesUseCase(repository),
        ToggleFavoriteUseCase(repository),
        CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
    )

    @Test
    fun `keys follow the repository`() = runTest {
        val delegate = delegate()
        repository.add(TestData.summary(1, MediaType.MOVIE))
        repository.add(TestData.summary(1, MediaType.TV_SHOW))
        advanceUntilIdle()

        assertEquals(
            setOf(TestData.summary(1, MediaType.MOVIE).key, TestData.summary(1, MediaType.TV_SHOW).key),
            delegate.favoriteKeys.value,
        )
    }

    @Test
    fun `toggle adds then removes`() = runTest {
        val delegate = delegate()
        val movie = TestData.summary(5)

        delegate.toggle(movie)
        advanceUntilIdle()
        assertEquals(setOf(movie.key), delegate.favoriteKeys.value)

        delegate.toggle(movie)
        advanceUntilIdle()
        assertEquals(emptySet<Any>(), delegate.favoriteKeys.value)
    }
}
