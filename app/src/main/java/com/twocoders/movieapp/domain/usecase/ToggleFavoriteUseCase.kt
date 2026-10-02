package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.key
import com.twocoders.movieapp.domain.repository.FavoritesRepository

/**
 * Adds [media] to favorites, or removes it if it's already there. It checks the stored state
 * rather than trusting what the UI shows, so a double tap can't add the same title twice.
 *
 * @return true if [media] is a favorite afterwards.
 */
class ToggleFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(media: MediaSummary): Boolean =
        if (favoritesRepository.isFavorite(media.key)) {
            favoritesRepository.remove(media.key)
            false
        } else {
            favoritesRepository.add(media)
            true
        }
}
