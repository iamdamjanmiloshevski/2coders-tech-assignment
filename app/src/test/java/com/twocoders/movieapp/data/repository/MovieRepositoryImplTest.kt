package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.remote.TmdbServerRule
import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.Genre
import com.twocoders.movieapp.domain.model.MediaType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MovieRepositoryImplTest {

    @get:Rule
    val tmdb = TmdbServerRule()

    private val repository by lazy { MovieRepositoryImpl(tmdb.api, tmdb.apiCallHandler, tmdb.images, tmdb.local, tmdb.networkFirst) }

    @Test
    fun `popular movies are requested by page and mapped to summaries`() = runTest {
        tmdb.enqueue(
            body = """
            {"page":2,"total_pages":10,"total_results":200,"results":[
              {"id":550,"title":"Fight Club","overview":"An insomniac...","poster_path":"/fc.jpg",
               "vote_average":8.4,"release_date":"1999-10-15","adult":false}
            ]}
            """,
        )

        val page = (repository.getPopularMovies(page = 2) as DataResult.Success).data

        assertEquals("/3/movie/popular?page=2", tmdb.takeRequest().path)
        assertEquals(2, page.page)
        assertEquals(10, page.totalPages)
        with(page.items.single()) {
            assertEquals(550, id)
            assertEquals(MediaType.MOVIE, type)
            assertEquals("Fight Club", title)
            assertEquals("https://image.test/t/p/w500/fc.jpg", posterUrl)
            assertEquals(1999, releaseYear)
        }
    }

    @Test
    fun `null results and null fields fall back to defaults`() = runTest {
        tmdb.enqueue(
            body = """{"page":1,"total_pages":1,"results":null}""",
        )

        val page = (repository.getPopularMovies(page = 1) as DataResult.Success).data

        assertTrue(page.items.isEmpty())
    }

    @Test
    fun `movie with missing poster and release date maps to nulls`() = runTest {
        tmdb.enqueue(
            body = """{"page":1,"total_pages":1,"results":[{"id":1,"title":"Untitled","poster_path":null,"release_date":""}]}""",
        )

        val movie = (repository.getPopularMovies(page = 1) as DataResult.Success).data.items.single()

        assertNull(movie.posterUrl)
        assertNull(movie.releaseYear)
    }

    @Test
    fun `details include credits and map unknown money values to null`() = runTest {
        tmdb.enqueue(
            body = """
            {"id":550,"title":"Fight Club","tagline":"","overview":"An insomniac...","poster_path":"/fc.jpg",
             "backdrop_path":"/bd.jpg","genres":[{"id":18,"name":"Drama"}],"vote_average":8.4,"vote_count":30000,
             "release_date":"1999-10-15","status":"Released","runtime":139,"budget":63000000,"revenue":0,
             "credits":{
               "cast":[{"id":819,"name":"Edward Norton","character":"Narrator","profile_path":"/en.jpg"}],
               "crew":[
                 {"id":7467,"name":"David Fincher","job":"Director","department":"Directing"},
                 {"id":7468,"name":"Jim Uhls","job":"Screenplay","department":"Writing"},
                 {"id":7469,"name":"Chuck Palahniuk","job":"Novel","department":"Writing"},
                 {"id":7470,"name":"Someone","job":"Editor","department":"Editing"}
               ]}}
            """,
        )

        val details = (repository.getMovieDetails(id = 550) as DataResult.Success).data

        assertEquals("/3/movie/550?append_to_response=credits", tmdb.takeRequest().path)
        assertNull("blank tagline becomes null", details.tagline)
        assertEquals(listOf(Genre(18, "Drama")), details.genres)
        assertEquals(63_000_000L, details.budget)
        assertNull(details.revenue)
        assertEquals("https://image.test/t/p/w780/bd.jpg", details.backdropUrl)
        assertEquals(listOf("David Fincher"), details.credits.directors.map { it.name })
        assertEquals(listOf("Jim Uhls", "Chuck Palahniuk"), details.credits.writers.map { it.name })
        with(details.credits.cast.single()) {
            assertEquals("Narrator", role)
            assertEquals("https://image.test/t/p/w185/en.jpg", profileUrl)
        }
    }

    @Test
    fun `popular page is cached and served offline`() = runTest {
        tmdb.enqueue(body = """{"page":1,"total_pages":3,"results":[{"id":1,"title":"Cached"}]}""")
        val online = repository.getPopularMovies(page = 1)

        tmdb.goOffline()
        val offline = repository.getPopularMovies(page = 1)

        assertEquals(online, offline)
        assertEquals("Cached", (offline as DataResult.Success).data.items.single().title)
    }

    @Test
    fun `uncached page offline reports NoConnection`() = runTest {
        tmdb.goOffline()

        assertEquals(DataResult.Failure(AppError.NoConnection), repository.getPopularMovies(page = 4))
    }

    @Test
    fun `movie details are served offline once seen`() = runTest {
        tmdb.enqueue(body = """{"id":550,"title":"Fight Club","runtime":139}""")
        repository.getMovieDetails(id = 550)

        tmdb.goOffline()
        val offline = (repository.getMovieDetails(id = 550) as DataResult.Success).data

        assertEquals("Fight Club", offline.title)
        assertEquals(139, offline.runtimeMinutes)
    }
}
