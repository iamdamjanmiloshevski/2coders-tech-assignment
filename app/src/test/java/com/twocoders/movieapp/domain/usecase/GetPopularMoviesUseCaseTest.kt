package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.fakes.FakeMovieRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPopularMoviesUseCaseTest {

    private val repository = FakeMovieRepository()
    private val useCase = GetPopularMoviesUseCase(repository)

    @Test
    fun `requests the given page from the repository`() = runTest {
        val result = useCase(page = 2)

        assertEquals(listOf(2), repository.popularRequests)
        assertEquals(2, (result as DataResult.Success).data.page)
    }

    @Test
    fun `propagates repository failures unchanged`() = runTest {
        repository.popularResult = { DataResult.Failure(AppError.NoConnection) }

        assertEquals(DataResult.Failure(AppError.NoConnection), useCase(page = 1))
    }
}
