package com.twocoders.movieapp.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twocoders.movieapp.core.connectivity.ConnectivityObserver
import com.twocoders.movieapp.core.connectivity.reconnections
import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.usecase.ObserveFavoritesUseCase
import com.twocoders.movieapp.domain.usecase.ToggleFavoriteUseCase
import com.twocoders.movieapp.presentation.favorites.FavoritesDelegate
import com.twocoders.movieapp.domain.usecase.GetPopularMoviesUseCase
import com.twocoders.movieapp.presentation.paging.PaginationState
import com.twocoders.movieapp.presentation.paging.Paginator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Main screen: an endless list of popular movies. A failed load retries on its own when the device reconnects. */
class MovieListViewModel(
    getPopularMovies: GetPopularMoviesUseCase,
    connectivity: ConnectivityObserver,
    observeFavorites: ObserveFavoritesUseCase,
    toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val favorites = FavoritesDelegate(observeFavorites, toggleFavorite, viewModelScope)

    private val paginator = Paginator(
        scope = viewModelScope,
        keyOf = MediaSummary::id,
        loadPage = { page -> getPopularMovies(page) },
    )

    val state: StateFlow<PaginationState<MediaSummary>> = paginator.state

    /** Titles shown with a filled heart. */
    val favoriteKeys: StateFlow<Set<MediaKey>> = favorites.favoriteKeys

    init {
        paginator.loadNext()
        // retry() does nothing unless the last load failed, so reconnecting while everything is fine is harmless.
        viewModelScope.launch { connectivity.reconnections().collect { paginator.retry() } }
    }

    /** Call when the user scrolls near the end of the list. */
    fun loadMore() = paginator.loadNext()

    /** Re-attempts the page that failed. Does nothing unless the last load failed. */
    fun retry() = paginator.retry()

    /** Drops the loaded pages and starts again from the first page. */
    fun refresh() = paginator.refresh()

    fun toggleFavorite(media: MediaSummary) = favorites.toggle(media)
}
