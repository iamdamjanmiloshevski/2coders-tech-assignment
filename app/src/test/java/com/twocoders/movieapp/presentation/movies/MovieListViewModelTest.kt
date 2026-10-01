package com.twocoders.movieapp.presentation.movies

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.usecase.GetPopularMoviesUseCase
import com.twocoders.movieapp.fakes.FakeConnectivityObserver
import com.twocoders.movieapp.fakes.FakeMovieRepository
import com.twocoders.movieapp.fakes.TestData
import com.twocoders.movieapp.presentation.paging.PaginationStatus
import com.twocoders.movieapp.testutil.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MovieListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()
    private val connectivity = FakeConnectivityObserver()

    private fun viewModel() = MovieListViewModel(GetPopularMoviesUseCase(repository), connectivity)

    @Test
    fun `loads the first page on creation`() = runTest {
        val viewModel = viewModel()
        assertEquals(PaginationStatus.LoadingFirstPage, viewModel.state.value.status)

        advanceUntilIdle()

        assertEquals(listOf(1), repository.popularRequests)
        assertEquals(PaginationStatus.Idle, viewModel.state.value.status)
        assertEquals(2, viewModel.state.value.items.size)
    }

    @Test
    fun `loadMore appends the next page`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(1, 2), repository.popularRequests)
        assertEquals(listOf(10, 11, 20, 21), viewModel.state.value.items.map { it.id })
    }

    @Test
    fun `first page failure is exposed and retry recovers`() = runTest {
        repository.popularResult = { DataResult.Failure(AppError.NoConnection) }
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(PaginationStatus.Error(AppError.NoConnection, isFirstPage = true), viewModel.state.value.status)

        repository.popularResult = { page -> DataResult.Success(TestData.page(page, totalPages = 1, ids = 1..3)) }
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(PaginationStatus.EndReached, viewModel.state.value.status)
        assertEquals(3, viewModel.state.value.items.size)
    }

    @Test
    fun `a failed first page reloads by itself when the device reconnects`() = runTest {
        connectivity.isOnline.value = false
        repository.popularResult = { DataResult.Failure(AppError.NoConnection) }
        val viewModel = viewModel()
        advanceUntilIdle()

        repository.popularResult = { page -> DataResult.Success(TestData.page(page, totalPages = 2, ids = 1..2)) }
        connectivity.isOnline.value = true
        advanceUntilIdle()

        assertEquals(listOf(1, 1), repository.popularRequests)
        assertEquals(PaginationStatus.Idle, viewModel.state.value.status)
    }

    @Test
    fun `reconnecting does not reload when nothing failed`() = runTest {
        viewModel()
        advanceUntilIdle()

        connectivity.isOnline.value = false
        connectivity.isOnline.value = true
        advanceUntilIdle()

        assertEquals(listOf(1), repository.popularRequests)
    }
}
