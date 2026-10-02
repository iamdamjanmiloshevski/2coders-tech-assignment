package com.twocoders.movieapp.presentation.details

import app.cash.turbine.test
import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.usecase.GetMediaDetailsUseCase
import com.twocoders.movieapp.fakes.FakeConnectivityObserver
import com.twocoders.movieapp.fakes.FakeMovieRepository
import com.twocoders.movieapp.fakes.FakeTvShowRepository
import com.twocoders.movieapp.fakes.TestData
import com.twocoders.movieapp.testutil.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movieRepository = FakeMovieRepository()
    private val tvShowRepository = FakeTvShowRepository()
    private val connectivity = FakeConnectivityObserver()

    private fun viewModel(id: Int, type: MediaType) =
        DetailsViewModel(id, type, GetMediaDetailsUseCase(movieRepository, tvShowRepository), connectivity)

    @Test
    fun `emits Loading then Content for a movie`() = runTest {
        viewModel(id = 5, type = MediaType.MOVIE).state.test {
            assertEquals(DetailsUiState.Loading, awaitItem())
            assertEquals(DetailsUiState.Content(TestData.movieDetails(5)), awaitItem())
        }
        assertEquals(listOf(5), movieRepository.detailsRequests)
    }

    @Test
    fun `loads TV shows from the TV repository`() = runTest {
        viewModel(id = 9, type = MediaType.TV_SHOW).state.test {
            skipItems(1)
            assertEquals(DetailsUiState.Content(TestData.tvShowDetails(9)), awaitItem())
        }
    }

    @Test
    fun `failure emits Error and retry loads again`() = runTest {
        movieRepository.detailsResult = { DataResult.Failure(AppError.NotFound("gone")) }
        val viewModel = viewModel(id = 1, type = MediaType.MOVIE)

        viewModel.state.test {
            assertEquals(DetailsUiState.Loading, awaitItem())
            assertEquals(DetailsUiState.Error(AppError.NotFound("gone")), awaitItem())

            movieRepository.detailsResult = { DataResult.Success(TestData.movieDetails(it)) }
            viewModel.retry()

            assertEquals(DetailsUiState.Loading, awaitItem())
            assertEquals(DetailsUiState.Content(TestData.movieDetails(1)), awaitItem())
        }
    }

    @Test
    fun `an offline error reloads by itself on reconnect`() = runTest {
        connectivity.isOnline.value = false
        movieRepository.detailsResult = { DataResult.Failure(AppError.NoConnection) }
        val viewModel = viewModel(id = 3, type = MediaType.MOVIE)
        advanceUntilIdle()
        assertEquals(DetailsUiState.Error(AppError.NoConnection), viewModel.state.value)

        movieRepository.detailsResult = { DataResult.Success(TestData.movieDetails(it)) }
        connectivity.isOnline.value = true
        advanceUntilIdle()

        assertEquals(DetailsUiState.Content(TestData.movieDetails(3)), viewModel.state.value)
    }

    @Test
    fun `loaded details are not reloaded on reconnect`() = runTest {
        viewModel(id = 3, type = MediaType.MOVIE)
        advanceUntilIdle()

        connectivity.isOnline.value = false
        connectivity.isOnline.value = true
        advanceUntilIdle()

        assertEquals(listOf(3), movieRepository.detailsRequests)
    }
}
