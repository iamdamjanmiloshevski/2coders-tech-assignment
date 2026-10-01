package com.twocoders.movieapp.data.local.user

import androidx.room.Entity
import androidx.room.Index
import com.twocoders.movieapp.domain.model.MediaType

/** One saved title: the list-card snapshot plus when it was added. Indexed on [addedAt] for newest-first reads. */
@Entity(
    tableName = "favorites",
    primaryKeys = ["id", "type"],
    indices = [Index("addedAt")],
)
data class FavoriteEntity(
    val id: Int,
    val type: MediaType,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val voteAverage: Double,
    val releaseYear: Int?,
    val addedAt: Long,
)
