package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.mapper.TMDB_MAX_PAGE
import com.twocoders.movieapp.data.remote.TmdbServerRule
import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchRepositoryImplTest {

    @get:Rule
    val tmdb = TmdbServerRule()

    private val repository by lazy { SearchRepositoryImpl(tmdb.api, tmdb.apiCallHandler, tmdb.images, tmdb.local, tmdb.networkFirst) }

    @Test
    fun `movie search hits search-movie with an encoded query`() = runTest {
        tmdb.enqueue(body = """{"page":1,"total_pages":1,"results":[{"id":1,"title":"The Matrix"}]}""")

        val page = (repository.search("the matrix", MediaType.MOVIE, page = 1) as DataResult.Success).data

        assertEquals("/3/search/movie?query=the%20matrix&page=1", tmdb.takeRequest().path)
        assertEquals(MediaType.MOVIE, page.items.single().type)
    }

    @Test
    fun `tv search hits search-tv and maps name to title`() = runTest {
        tmdb.enqueue(body = """{"page":1,"total_pages":1,"results":[{"id":2,"name":"Dark","first_air_date":"2017-12-01"}]}""")

        val item = (repository.search("dark", MediaType.TV_SHOW, page = 1) as DataResult.Success).data.items.single()

        assertEquals("/3/search/tv?query=dark&page=1", tmdb.takeRequest().path)
        assertEquals(MediaType.TV_SHOW, item.type)
        assertEquals("Dark", item.title)
        assertEquals(2017, item.releaseYear)
    }

    @Test
    fun `total pages are clamped to the TMDB maximum`() = runTest {
        tmdb.enqueue(body = """{"page":1,"total_pages":9000,"results":[]}""")

        val page = (repository.search("a", MediaType.MOVIE, page = 1) as DataResult.Success).data

        assertEquals(TMDB_MAX_PAGE, page.totalPages)
    }

    @Test
    fun `a past search works offline, regardless of letter case`() = runTest {
        tmdb.enqueue(body = """{"page":1,"total_pages":1,"results":[{"id":1,"title":"Dune"}]}""")
        repository.search("dune", MediaType.MOVIE, page = 1)

        tmdb.goOffline()
        val offline = repository.search("Dune", MediaType.MOVIE, page = 1)

        assertEquals("Dune", (offline as DataResult.Success).data.items.single().title)
    }

    @Test
    fun `cached movie results are not served for a tv search`() = runTest {
        tmdb.enqueue(body = """{"page":1,"total_pages":1,"results":[{"id":1,"title":"Dune"}]}""")
        repository.search("dune", MediaType.MOVIE, page = 1)

        tmdb.goOffline()

        assertEquals(DataResult.Failure(AppError.NoConnection), repository.search("dune", MediaType.TV_SHOW, page = 1))
    }
}
