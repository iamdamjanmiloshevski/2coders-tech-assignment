package com.twocoders.movieapp.presentation.favorites

import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.key
import com.twocoders.movieapp.domain.usecase.ObserveFavoritesUseCase
import com.twocoders.movieapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Favorite hearts for any screen that lists media. Composed into ViewModels instead of being
 * copied into each one. It runs in the owner's [scope], usually `viewModelScope`.
 */
class FavoritesDelegate(
    observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val scope: CoroutineScope,
) {
    /** Which titles are favorites right now, so every card can show its heart. */
    val favoriteKeys: StateFlow<Set<MediaKey>> = observeFavorites()
        .map { favorites -> favorites.mapTo(HashSet()) { it.media.key } }
        .stateIn(scope, SharingStarted.Eagerly, initialValue = emptySet())

    fun toggle(media: MediaSummary) {
        scope.launch { toggleFavorite(media) }
    }
}
