package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.repository.FavoritesRepository

/** Undoes a removal: the favorite comes back with its original position in the list. */
class RestoreFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(favorite: Favorite) = favoritesRepository.restore(favorite)
}
