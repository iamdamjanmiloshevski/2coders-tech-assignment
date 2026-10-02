package com.twocoders.movieapp.presentation.favorites

import app.cash.turbine.test
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.usecase.ObserveFavoritesUseCase
import com.twocoders.movieapp.domain.usecase.RemoveFavoriteUseCase
import com.twocoders.movieapp.domain.usecase.RestoreFavoriteUseCase
import com.twocoders.movieapp.fakes.FakeFavoritesRepository
import com.twocoders.movieapp.fakes.TestData
import com.twocoders.movieapp.testutil.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeFavoritesRepository()

    private fun viewModel() = FavoritesViewModel(
        ObserveFavoritesUseCase(repository),
        RemoveFavoriteUseCase(repository),
        RestoreFavoriteUseCase(repository),
    )

    @Test
    fun `no favorites shows the empty state`() = runTest {
        viewModel().state.test {
            assertEquals(FavoritesUiState.Loading, awaitItem())
            assertEquals(FavoritesUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `favorites are listed newest first, movies and shows together`() = runTest {
        repository.add(TestData.summary(1, MediaType.MOVIE))
        repository.add(TestData.summary(2, MediaType.TV_SHOW))

        viewModel().state.test {
            skipItems(1)
            val content = awaitItem() as FavoritesUiState.Content
            assertEquals(listOf(2 to MediaType.TV_SHOW, 1 to MediaType.MOVIE), content.favorites.map { it.media.id to it.media.type })
        }
    }

    @Test
    fun `removing sends a Removed event and drops the item`() = runTest {
        repository.add(TestData.summary(1))
        val favorite = repository.current.single()
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.remove(favorite)
            assertEquals(FavoritesEvent.Removed(favorite), awaitItem())
        }
        assertEquals(emptyList<Any>(), repository.current)
    }

    @Test
    fun `undo restores the favorite in its original position`() = runTest {
        repository.add(TestData.summary(1))
        repository.add(TestData.summary(2))
        repository.add(TestData.summary(3))
        val middle = repository.current[1]
        val viewModel = viewModel()

        viewModel.remove(middle)
        advanceUntilIdle()
        viewModel.undoRemove(middle)
        advanceUntilIdle()

        assertEquals(listOf(3, 2, 1), repository.current.map { it.media.id })
    }
}
