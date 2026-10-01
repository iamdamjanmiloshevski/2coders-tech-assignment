package com.twocoders.movieapp.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.model.key
import com.twocoders.movieapp.domain.usecase.ObserveFavoritesUseCase
import com.twocoders.movieapp.domain.usecase.RemoveFavoriteUseCase
import com.twocoders.movieapp.domain.usecase.RestoreFavoriteUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The favorites screen. Screen state ([state]) and one-off operation events ([events]) are
 * separate types. A removal changes the list through [state] and asks for an Undo snackbar
 * through [events], so the snackbar shows exactly once per removal, even across recomposition.
 */
class FavoritesViewModel(
    observeFavorites: ObserveFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
    private val restoreFavorite: RestoreFavoriteUseCase,
) : ViewModel() {

    val state: StateFlow<FavoritesUiState> = observeFavorites()
        .map { favorites -> if (favorites.isEmpty()) FavoritesUiState.Empty else FavoritesUiState.Content(favorites) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = FavoritesUiState.Loading)

    private val _events = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events: Flow<FavoritesEvent> = _events.receiveAsFlow()

    fun remove(favorite: Favorite) {
        viewModelScope.launch {
            removeFavorite(favorite.media.key)
            _events.send(FavoritesEvent.Removed(favorite))
        }
    }

    /** Puts a just-removed favorite back where it was. */
    fun undoRemove(favorite: Favorite) {
        viewModelScope.launch { restoreFavorite(favorite) }
    }
}
