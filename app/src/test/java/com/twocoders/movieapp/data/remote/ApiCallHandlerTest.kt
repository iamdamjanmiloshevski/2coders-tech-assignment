package com.twocoders.movieapp.data.remote

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ApiCallHandlerTest {

    @get:Rule
    val tmdb = TmdbServerRule()

    private suspend fun callPopular() = tmdb.apiCallHandler.execute(
        request = { tmdb.api.getPopularMovies(page = 1) },
        map = { it.results.size },
    )

    @Test
    fun `2xx response is decoded and mapped`() = runTest {
        tmdb.enqueue(body = """{"page":1,"results":[{"id":1},{"id":2}],"total_pages":1}""")

        assertEquals(DataResult.Success(2), callPopular())
    }

    @Test
    fun `every request carries the bearer token`() = runTest {
        tmdb.enqueue(body = """{"page":1,"results":[]}""")

        callPopular()

        assertEquals("Bearer ${TmdbServerRule.TEST_TOKEN}", tmdb.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `401 maps to Unauthorized with the TMDB status message`() = runTest {
        tmdb.enqueue(
            code = 401,
            body = """{"status_code":7,"status_message":"Invalid API key: You must be granted a valid key.","success":false}""",
        )

        assertEquals(
            DataResult.Failure(AppError.Unauthorized("Invalid API key: You must be granted a valid key.")),
            callPopular(),
        )
        assertTrue(tmdb.logger.errors.single().contains("status_code=7"))
    }

    @Test
    fun `404 maps to NotFound`() = runTest {
        tmdb.enqueue(code = 404, body = """{"status_code":34,"status_message":"The resource you requested could not be found."}""")

        assertEquals(
            DataResult.Failure(AppError.NotFound("The resource you requested could not be found.")),
            callPopular(),
        )
    }

    @Test
    fun `other error codes with a non-JSON body map to Http without a message`() = runTest {
        tmdb.enqueue(code = 503, body = "<html>Service Unavailable</html>")

        assertEquals(DataResult.Failure(AppError.Http(503, null)), callPopular())
    }

    @Test
    fun `malformed 2xx body maps to Parsing`() = runTest {
        tmdb.enqueue(body = """{"page": "not-a-number"""")

        assertEquals(DataResult.Failure(AppError.Parsing), callPopular())
    }

    @Test
    fun `dropped connection maps to NoConnection`() = runTest {
        tmdb.server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(DataResult.Failure(AppError.NoConnection), callPopular())
    }
}
