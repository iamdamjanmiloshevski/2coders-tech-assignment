package com.twocoders.movieapp.presentation.paging

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Runs the [PaginationState] machine: decides when a load may start, launches it in
 * [scope], and applies the result.
 *
 * Only one load runs at a time. The status changes to loading synchronously, before the
 * coroutine starts, so a scrolling list can call [loadNext] on every frame without
 * starting duplicate requests.
 *
 * @param keyOf stable identity of an item, used to de-duplicate across pages.
 * @param loadPage fetches one 1-based page.
 */
class Paginator<T>(
    private val scope: CoroutineScope,
    private val keyOf: (T) -> Any,
    private val loadPage: suspend (page: Int) -> DataResult<Page<T>>,
) {
    private val _state = MutableStateFlow(PaginationState<T>())
    val state: StateFlow<PaginationState<T>> = _state.asStateFlow()

    private var loadJob: Job? = null

    /** Loads the next page if idle. Ignored while loading, after an error (use [retry]) or at the end. */
    fun loadNext() {
        if (_state.value.status == PaginationStatus.Idle) load()
    }

    /** Re-attempts the page that failed. Ignored unless the last load failed. */
    fun retry() {
        if (_state.value.status is PaginationStatus.Error) load()
    }

    /** Drops everything, cancels any in-flight load and starts again from the first page. */
    fun refresh() {
        loadJob?.cancel()
        _state.value = PaginationState()
        load()
    }

    private fun load() {
        val page = _state.value.nextPage
        _state.update { it.onLoadStarted() }
        loadJob = scope.launch {
            val result = loadPage(page)
            // A refresh may have cancelled this load after the call returned. Its stale result must not land.
            ensureActive()
            _state.update { current ->
                when (result) {
                    is DataResult.Success -> current.onPageLoaded(result.data, keyOf)
                    is DataResult.Failure -> current.onLoadFailed(result.error)
                }
            }
        }
    }
}
