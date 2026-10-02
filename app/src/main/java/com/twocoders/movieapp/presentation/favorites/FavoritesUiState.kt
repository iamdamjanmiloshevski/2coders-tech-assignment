package com.twocoders.movieapp.presentation.favorites

import com.twocoders.movieapp.domain.model.Favorite

/** What the favorites screen shows. Favorites are local, so there's no error state. */
sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState

    /** Favorites, newest first. */
    data class Content(val favorites: List<Favorite>) : FavoritesUiState
}

/** One-off events the screen reacts to once, such as showing a snackbar. They're not part of [FavoritesUiState]. */
sealed interface FavoritesEvent {
    /** [favorite] was removed. The screen offers Undo. */
    data class Removed(val favorite: Favorite) : FavoritesEvent
}
