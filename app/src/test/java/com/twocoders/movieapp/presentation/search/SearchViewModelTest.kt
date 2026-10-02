package com.twocoders.movieapp.presentation.search

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.usecase.SearchMediaUseCase
import com.twocoders.movieapp.fakes.FakeConnectivityObserver
import com.twocoders.movieapp.fakes.FakeSearchRepository
import com.twocoders.movieapp.fakes.FakeSearchRepository.Request
import com.twocoders.movieapp.fakes.TestData
import com.twocoders.movieapp.presentation.search.SearchViewModel.Companion.SEARCH_DEBOUNCE_MS
import com.twocoders.movieapp.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSearchRepository()
    private val connectivity = FakeConnectivityObserver()
    private val viewModel by lazy { SearchViewModel(SearchMediaUseCase(repository), connectivity) }

    private val SearchViewModel.content: SearchResults.Content
        get() = state.value.results as SearchResults.Content

    /** Types [text] one character at a time, 100 ms apart, which is faster than the debounce. */
    private fun TestScope.typeQuickly(text: String) {
        text.indices.forEach { end ->
            viewModel.onQueryChange(text.substring(0, end + 1))
            advanceTimeBy(100)
        }
    }

    @Test
    fun `starts idle without searching`() = runTest {
        viewModel
        advanceUntilIdle()

        assertEquals(SearchResults.Idle, viewModel.state.value.results)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `fast typing is debounced into a single request`() = runTest {
        typeQuickly("dune")
        assertTrue("nothing sent before the debounce elapses", repository.requests.isEmpty())
        assertEquals("the text field updates on every keystroke", "dune", viewModel.state.value.query)

        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        advanceUntilIdle()

        assertEquals(listOf(Request("dune", MediaType.MOVIE, 1)), repository.requests)
        assertEquals(2, viewModel.content.pagination.items.size)
    }

    @Test
    fun `switching media type searches again right away`() = runTest {
        viewModel.onQueryChange("dark")
        advanceUntilIdle()

        viewModel.onMediaTypeChange(MediaType.TV_SHOW)
        runCurrent()

        assertEquals(Request("dark", MediaType.TV_SHOW, 1), repository.requests.last())
        advanceUntilIdle()
        assertTrue(viewModel.content.pagination.items.all { it.type == MediaType.TV_SHOW })
    }

    @Test
    fun `a newer query cancels the stale request`() = runTest {
        val slowGate = CompletableDeferred<Unit>()
        repository.result = { request ->
            if (request.query == "old") slowGate.await()
            DataResult.Success(TestData.page(1, totalPages = 1, ids = if (request.query == "old") 1..1 else 2..2))
        }

        viewModel.onQueryChange("old")
        advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)
        viewModel.onQueryChange("new")
        advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)
        slowGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(2), viewModel.content.pagination.items.map { it.id })
    }

    @Test
    fun `clearing the query returns to idle immediately`() = runTest {
        viewModel.onQueryChange("dune")
        advanceUntilIdle()

        viewModel.onQueryChange("")
        runCurrent()

        assertEquals(SearchResults.Idle, viewModel.state.value.results)
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun `loadMore fetches the next page of the current search`() = runTest {
        viewModel.onQueryChange("dune")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(1, 2), repository.requests.map { it.page })
    }

    @Test
    fun `a search that failed offline runs again on reconnect`() = runTest {
        connectivity.isOnline.value = false
        repository.result = { DataResult.Failure(AppError.NoConnection) }
        viewModel.onQueryChange("dune")
        advanceUntilIdle()

        repository.result = { request -> DataResult.Success(TestData.page(request.page, totalPages = 1, ids = 1..1)) }
        connectivity.isOnline.value = true
        advanceUntilIdle()

        assertEquals(2, repository.requests.size)
        assertEquals(1, viewModel.content.pagination.items.size)
    }
}
