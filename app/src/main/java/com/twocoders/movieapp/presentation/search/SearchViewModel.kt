package com.twocoders.movieapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.usecase.SearchMediaUseCase
import com.twocoders.movieapp.presentation.paging.Paginator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Throttled search over movies or TV shows.
 *
 * Pipeline: `query` → debounce → trim → combine with `mediaType` → distinctUntilChanged →
 * flatMapLatest. In practice this means:
 * - typing fast sends one request, [SEARCH_DEBOUNCE_MS] after the last keystroke
 * - clearing the field resets right away, with no debounce and no request
 * - a type switch searches immediately, since only the query is debounced
 * - a new query cancels the request still running for the old one, so a stale response can never replace newer results
 *
 * Each search gets its own [Paginator], scoped to the inner flow, so infinite scroll works on search results too.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchMedia: SearchMediaUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val mediaType = MutableStateFlow(MediaType.MOVIE)

    /** The paginator of the search currently on screen, or null when idle. */
    private var activePaginator: Paginator<MediaSummary>? = null

    private val results: StateFlow<SearchResults> =
        combine(
            query.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }.map(String::trim),
            mediaType,
            ::SearchRequest,
        )
            .distinctUntilChanged()
            .flatMapLatest(::resultsFor)
            // Eagerly: results (and loaded pages) survive the screen briefly going to the back stack.
            .stateIn(viewModelScope, SharingStarted.Eagerly, SearchResults.Idle)

    val state: StateFlow<SearchUiState> =
        combine(query, mediaType, results, ::SearchUiState)
            .stateIn(viewModelScope, SharingStarted.Eagerly, SearchUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onMediaTypeChange(type: MediaType) {
        mediaType.value = type
    }

    fun loadMore() {
        activePaginator?.loadNext()
    }

    fun retry() {
        activePaginator?.retry()
    }

    private fun resultsFor(request: SearchRequest): Flow<SearchResults> {
        if (request.query.isEmpty()) {
            activePaginator = null
            return flowOf(SearchResults.Idle)
        }
        // The channelFlow scope is cancelled when flatMapLatest moves on, which also cancels this paginator's in-flight load.
        return channelFlow {
            val paginator = Paginator(
                scope = this,
                keyOf = MediaSummary::id,
                loadPage = { page -> searchMedia(request.query, request.type, page) },
            )
            activePaginator = paginator
            paginator.loadNext()
            paginator.state.collect { send(SearchResults.Content(it)) }
        }
    }

    private data class SearchRequest(val query: String, val type: MediaType)

    companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
