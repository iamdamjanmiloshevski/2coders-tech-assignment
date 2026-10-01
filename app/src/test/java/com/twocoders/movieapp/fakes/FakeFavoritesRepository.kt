package com.twocoders.movieapp.fakes

import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.key
import com.twocoders.movieapp.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory favorites with the same ordering contract as the real repository. [now] is the clock. */
class FakeFavoritesRepository(var now: Long = 1_000L) : FavoritesRepository {

    private val favorites = MutableStateFlow<Map<MediaKey, Favorite>>(emptyMap())

    val current: List<Favorite> get() = favorites.value.values.sortedByDescending { it.addedAtMillis }

    override fun observeFavorites(): Flow<List<Favorite>> =
        favorites.map { all -> all.values.sortedByDescending { it.addedAtMillis } }

    override suspend fun isFavorite(key: MediaKey) = key in favorites.value

    override suspend fun add(media: MediaSummary) {
        favorites.update { it + (media.key to Favorite(media, addedAtMillis = now++)) }
    }

    override suspend fun remove(key: MediaKey) {
        favorites.update { it - key }
    }

    override suspend fun restore(favorite: Favorite) {
        favorites.update { it + (favorite.media.key to favorite) }
    }
}
