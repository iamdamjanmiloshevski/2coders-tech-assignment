package com.twocoders.movieapp.data.local.entity

import androidx.room.Entity
import com.twocoders.movieapp.domain.model.MediaType

/**
 * A movie or TV show summary as shown in lists. One row per title, shared by every list that
 * contains it (the popular feed and any number of searches), so it's stored once.
 */
@Entity(tableName = "media", primaryKeys = ["id", "type"])
data class MediaEntity(
    val id: Int,
    val type: MediaType,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val voteAverage: Double,
    val releaseYear: Int?,
)
