package com.twocoders.movieapp.presentation.paging

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.model.Page
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The pure transitions of the pagination state machine. */
class PaginationStateTest {

    private val keyOf: (Int) -> Any = { it }

    @Test
    fun `first load starts as LoadingFirstPage, later loads as LoadingNextPage`() {
        assertEquals(PaginationStatus.LoadingFirstPage, PaginationState<Int>().onLoadStarted().status)
        assertEquals(PaginationStatus.LoadingNextPage, PaginationState<Int>(nextPage = 2).onLoadStarted().status)
    }

    @Test
    fun `loaded page appends items and advances to Idle while pages remain`() {
        val state = PaginationState(items = listOf(1, 2), nextPage = 2)
            .onPageLoaded(Page(items = listOf(3, 4), page = 2, totalPages = 5), keyOf)

        assertEquals(listOf(1, 2, 3, 4), state.items)
        assertEquals(3, state.nextPage)
        assertEquals(PaginationStatus.Idle, state.status)
    }

    @Test
    fun `loading the last page reaches EndReached`() {
        val state = PaginationState<Int>().onPageLoaded(Page(listOf(1), page = 1, totalPages = 1), keyOf)

        assertEquals(PaginationStatus.EndReached, state.status)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `an empty single page is reported as empty`() {
        assertTrue(PaginationState<Int>().onPageLoaded(Page.empty(), keyOf).isEmpty)
    }

    @Test
    fun `items repeated across pages are de-duplicated`() {
        val state = PaginationState(items = listOf(1, 2), nextPage = 2)
            .onPageLoaded(Page(items = listOf(2, 3), page = 2, totalPages = 3), keyOf)

        assertEquals(listOf(1, 2, 3), state.items)
    }

    @Test
    fun `failure keeps items and records whether it was the first page`() {
        val first = PaginationState<Int>().onLoadFailed(AppError.NoConnection)
        val later = PaginationState(items = listOf(1), nextPage = 2).onLoadFailed(AppError.NoConnection)

        assertEquals(PaginationStatus.Error(AppError.NoConnection, isFirstPage = true), first.status)
        assertEquals(PaginationStatus.Error(AppError.NoConnection, isFirstPage = false), later.status)
        assertEquals(listOf(1), later.items)
    }
}
