package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.fakes.FakeLogger
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkFirstTest {

    private val logger = FakeLogger()
    private val networkFirst = NetworkFirst(logger)
    private var cached: String? = null

    private suspend fun run(remote: DataResult<String>) = networkFirst(
        fetch = { remote },
        saveToCache = { cached = it },
        loadFromCache = { cached },
    )

    @Test
    fun `online result is returned and saved`() = runTest {
        assertEquals(DataResult.Success("fresh"), run(DataResult.Success("fresh")))
        assertEquals("fresh", cached)
    }

    @Test
    fun `offline falls back to the cached copy`() = runTest {
        cached = "saved"

        assertEquals(DataResult.Success("saved"), run(DataResult.Failure(AppError.NoConnection)))
    }

    @Test
    fun `offline without a cached copy keeps the offline error`() = runTest {
        assertEquals(DataResult.Failure(AppError.NoConnection), run(DataResult.Failure(AppError.NoConnection)))
    }

    @Test
    fun `server errors are never hidden behind cached data`() = runTest {
        cached = "saved"

        assertEquals(DataResult.Failure(AppError.NotFound("gone")), run(DataResult.Failure(AppError.NotFound("gone"))))
        assertEquals(DataResult.Failure(AppError.Http(500, null)), run(DataResult.Failure(AppError.Http(500, null))))
    }

    @Test
    fun `a failing cache write does not fail a successful request`() = runTest {
        val result = networkFirst(
            fetch = { DataResult.Success("fresh") },
            saveToCache = { error("disk full") },
            loadFromCache = { null },
        )

        assertEquals(DataResult.Success("fresh"), result)
        assertTrue(logger.errors.single().contains("write"))
    }

    @Test
    fun `a failing cache read keeps the offline error`() = runTest {
        val result = networkFirst<String>(
            fetch = { DataResult.Failure(AppError.NoConnection) },
            saveToCache = {},
            loadFromCache = { error("corrupt") },
        )

        assertEquals(DataResult.Failure(AppError.NoConnection), result)
        assertNull(cached)
    }
}
