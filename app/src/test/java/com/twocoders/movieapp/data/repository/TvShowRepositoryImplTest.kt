package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.remote.TmdbServerRule
import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TvShowRepositoryImplTest {

    @get:Rule
    val tmdb = TmdbServerRule()

    private val repository by lazy { TvShowRepositoryImpl(tmdb.api, tmdb.apiCallHandler, tmdb.images, tmdb.local, tmdb.networkFirst) }

    @Test
    fun `details map name to title and creators to directors`() = runTest {
        tmdb.enqueue(
            body = """
            {"id":1396,"name":"Breaking Bad","first_air_date":"2008-01-20","number_of_seasons":5,
             "number_of_episodes":62,"vote_average":8.9,"vote_count":15000,
             "created_by":[{"id":66633,"name":"Vince Gilligan","profile_path":null}],
             "credits":{"cast":[{"id":17419,"name":"Bryan Cranston","character":"Walter White"}],"crew":null}}
            """,
        )

        val details = (repository.getTvShowDetails(id = 1396) as DataResult.Success).data

        assertEquals("/3/tv/1396?append_to_response=credits", tmdb.takeRequest().path)
        assertEquals("Breaking Bad", details.title)
        assertEquals(2008, details.releaseYear)
        assertEquals(5, details.numberOfSeasons)
        assertEquals(listOf("Vince Gilligan" to "Creator"), details.credits.directors.map { it.name to it.role })
        assertEquals(emptyList<Any>(), details.credits.writers)
    }

    @Test
    fun `unknown id surfaces NotFound`() = runTest {
        tmdb.enqueue(code = 404, body = """{"status_code":34,"status_message":"Not found"}""")

        assertEquals(DataResult.Failure(AppError.NotFound("Not found")), repository.getTvShowDetails(id = -1))
    }

    @Test
    fun `tv details are served offline once seen`() = runTest {
        tmdb.enqueue(body = """{"id":1396,"name":"Breaking Bad","number_of_seasons":5}""")
        repository.getTvShowDetails(id = 1396)

        tmdb.goOffline()

        assertEquals("Breaking Bad", (repository.getTvShowDetails(id = 1396) as DataResult.Success).data.title)
    }
}
