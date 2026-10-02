package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.fakes.FakeSearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMediaUseCaseTest {

    private val repository = FakeSearchRepository()
    private val useCase = SearchMediaUseCase(repository)

    @Test
    fun `blank query returns an empty page without hitting the repository`() = runTest {
        val result = useCase(query = "   ", type = MediaType.MOVIE, page = 1)

        assertEquals(DataResult.Success(Page.empty<Any>()), result)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `query is trimmed before searching`() = runTest {
        useCase(query = "  dune ", type = MediaType.TV_SHOW, page = 3)

        assertEquals(listOf(FakeSearchRepository.Request("dune", MediaType.TV_SHOW, 3)), repository.requests)
    }
}
