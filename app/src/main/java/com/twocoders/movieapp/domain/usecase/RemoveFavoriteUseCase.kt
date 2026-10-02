package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.repository.FavoritesRepository

/**
 * Removes a favorite. Unlike [ToggleFavoriteUseCase], calling it twice can't add the title back,
 * so it's the safe choice wherever removal is offered with Undo.
 */
class RemoveFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(key: MediaKey) = favoritesRepository.remove(key)
}
