package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.TvShowDetails
import com.twocoders.movieapp.fakes.FakeMovieRepository
import com.twocoders.movieapp.fakes.FakeTvShowRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetMediaDetailsUseCaseTest {

    private val movieRepository = FakeMovieRepository()
    private val tvShowRepository = FakeTvShowRepository()
    private val useCase = GetMediaDetailsUseCase(movieRepository, tvShowRepository)

    @Test
    fun `movie type is loaded from the movie repository`() = runTest {
        val result = useCase(id = 42, type = MediaType.MOVIE)

        assertEquals(listOf(42), movieRepository.detailsRequests)
        assertTrue(tvShowRepository.detailsRequests.isEmpty())
        assertTrue((result as DataResult.Success).data is MovieDetails)
    }

    @Test
    fun `tv show type is loaded from the tv show repository`() = runTest {
        val result = useCase(id = 7, type = MediaType.TV_SHOW)

        assertEquals(listOf(7), tvShowRepository.detailsRequests)
        assertTrue(movieRepository.detailsRequests.isEmpty())
        assertTrue((result as DataResult.Success).data is TvShowDetails)
    }

    @Test
    fun `propagates failures`() = runTest {
        movieRepository.detailsResult = { DataResult.Failure(AppError.NotFound("missing")) }

        assertEquals(DataResult.Failure(AppError.NotFound("missing")), useCase(id = 1, type = MediaType.MOVIE))
    }
}
