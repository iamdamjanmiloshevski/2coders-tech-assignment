package com.twocoders.movieapp.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.usecase.GetPopularMoviesUseCase
import com.twocoders.movieapp.presentation.paging.PaginationState
import com.twocoders.movieapp.presentation.paging.Paginator
import kotlinx.coroutines.flow.StateFlow

/** Main screen: an endless list of popular movies. */
class MovieListViewModel(
    getPopularMovies: GetPopularMoviesUseCase,
) : ViewModel() {

    private val paginator = Paginator(
        scope = viewModelScope,
        keyOf = MediaSummary::id,
        loadPage = { page -> getPopularMovies(page) },
    )

    val state: StateFlow<PaginationState<MediaSummary>> = paginator.state

    init {
        paginator.loadNext()
    }

    /** Call when the user scrolls near the end of the list. */
    fun loadMore() = paginator.loadNext()

    fun retry() = paginator.retry()

    fun refresh() = paginator.refresh()
}
