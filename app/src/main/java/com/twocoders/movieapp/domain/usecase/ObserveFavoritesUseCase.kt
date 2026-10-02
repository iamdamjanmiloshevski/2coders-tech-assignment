package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

/** The favorites list, newest first, updated live. */
class ObserveFavoritesUseCase(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(): Flow<List<Favorite>> = favoritesRepository.observeFavorites()
}
