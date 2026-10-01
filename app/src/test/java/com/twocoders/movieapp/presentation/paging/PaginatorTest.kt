package com.twocoders.movieapp.presentation.paging

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.Page
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PaginatorTest {

    private val requestedPages = mutableListOf<Int>()

    /** Three pages of two items each: page n holds [n*10, n*10+1]. */
    private var loadPage: suspend (Int) -> DataResult<Page<Int>> = { page ->
        DataResult.Success(Page(items = listOf(page * 10, page * 10 + 1), page = page, totalPages = 3))
    }

    private fun TestScope.paginator() = Paginator<Int>(
        scope = this,
        keyOf = { it },
        loadPage = { page -> requestedPages += page; loadPage(page) },
    )

    @Test
    fun `loads pages in order until the end`() = runTest {
        val paginator = paginator()

        repeat(5) {
            paginator.loadNext()
            advanceUntilIdle()
        }

        assertEquals(listOf(1, 2, 3), requestedPages)
        assertEquals(listOf(10, 11, 20, 21, 30, 31), paginator.state.value.items)
        assertEquals(PaginationStatus.EndReached, paginator.state.value.status)
    }

    @Test
    fun `calls while a load is in flight are ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val original = loadPage
        loadPage = { page -> gate.await(); original(page) }
        val paginator = paginator()

        paginator.loadNext()
        runCurrent()
        paginator.loadNext()
        paginator.loadNext()
        assertEquals(PaginationStatus.LoadingFirstPage, paginator.state.value.status)

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(1), requestedPages)
    }

    @Test
    fun `error blocks loadNext until retry re-requests the same page`() = runTest {
        loadPage = { DataResult.Failure(AppError.NoConnection) }
        val paginator = paginator()

        paginator.loadNext()
        advanceUntilIdle()
        assertEquals(PaginationStatus.Error(AppError.NoConnection, isFirstPage = true), paginator.state.value.status)

        paginator.loadNext()
        advanceUntilIdle()
        assertEquals("loadNext must not auto-retry", listOf(1), requestedPages)

        loadPage = { page -> DataResult.Success(Page(listOf(page), page = page, totalPages = 3)) }
        paginator.retry()
        advanceUntilIdle()

        assertEquals(listOf(1, 1), requestedPages)
        assertEquals(PaginationStatus.Idle, paginator.state.value.status)
    }

    @Test
    fun `refresh drops loaded items and drops the in-flight result`() = runTest {
        val paginator = paginator()
        paginator.loadNext()
        advanceUntilIdle()

        val slowGate = CompletableDeferred<Unit>()
        val original = loadPage
        loadPage = { page -> if (page == 2) slowGate.await(); original(page) }
        paginator.loadNext()
        runCurrent()

        paginator.refresh()
        slowGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(10, 11), paginator.state.value.items)
        assertEquals(2, paginator.state.value.nextPage)
    }
}
