package com.twocoders.movieapp.presentation.search

import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.presentation.paging.PaginationState

/**
 * @property query what's in the search field right now. It updates on every keystroke,
 *   while [results] only follows after the debounce.
 * @property mediaType what to search for, chosen by the user before searching.
 */
data class SearchUiState(
    val query: String = "",
    val mediaType: MediaType = MediaType.MOVIE,
    val results: SearchResults = SearchResults.Idle,
)

sealed interface SearchResults {
    /** No query entered yet. The UI shows a prompt rather than an empty list. */
    data object Idle : SearchResults

    data class Content(val pagination: PaginationState<MediaSummary>) : SearchResults
}
