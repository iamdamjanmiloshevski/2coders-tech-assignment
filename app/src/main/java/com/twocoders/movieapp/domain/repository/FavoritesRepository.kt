package com.twocoders.movieapp.domain.repository

import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.model.MediaSummary
import kotlinx.coroutines.flow.Flow

/** The user's favorites, stored only on the device. */
interface FavoritesRepository {

    /** All favorites, newest first. Emits again after every change. */
    fun observeFavorites(): Flow<List<Favorite>>

    suspend fun isFavorite(key: MediaKey): Boolean

    /** Saves [media] as a favorite added now. Saving one that's already a favorite just updates its snapshot. */
    suspend fun add(media: MediaSummary)

    suspend fun remove(key: MediaKey)

    /** Puts back a removed [favorite] with its original timestamp, so it returns to the same place in the list. */
    suspend fun restore(favorite: Favorite)
}
