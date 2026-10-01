package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.core.time.Clock
import com.twocoders.movieapp.data.local.user.FavoriteDao
import com.twocoders.movieapp.data.local.user.toDomain
import com.twocoders.movieapp.data.local.user.toEntity
import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Favorites stored in [com.twocoders.movieapp.data.local.user.UserDatabase]. Local only, so there's no network or error path. */
class FavoritesRepositoryImpl(
    private val dao: FavoriteDao,
    private val clock: Clock,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<Favorite>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun isFavorite(key: MediaKey): Boolean = dao.exists(key.id, key.type)

    override suspend fun add(media: MediaSummary) {
        dao.upsert(Favorite(media, addedAtMillis = clock.nowMillis()).toEntity())
    }

    override suspend fun remove(key: MediaKey) {
        dao.delete(key.id, key.type)
    }

    override suspend fun restore(favorite: Favorite) {
        dao.upsert(favorite.toEntity())
    }
}
