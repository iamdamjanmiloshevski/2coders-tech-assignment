package com.twocoders.movieapp.data.local.entity

import androidx.room.Entity
import com.twocoders.movieapp.domain.model.MediaType
import kotlinx.serialization.Serializable

/**
 * The details screen's data for a movie or TV show. One table serves both types: shared columns,
 * plus nullable columns that only one type fills.
 *
 * Genres and credits are only ever read together with their title, so they're stored as JSON
 * columns (see `CacheConverters`) instead of separate tables.
 */
@Entity(tableName = "details", primaryKeys = ["id", "type"])
data class DetailsEntity(
    val id: Int,
    val type: MediaType,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<GenreJson>,
    val voteAverage: Double,
    val voteCount: Int,
    val releaseYear: Int?,
    val status: String?,
    val credits: CreditsJson,
    // Movie only
    val runtimeMinutes: Int?,
    val budget: Long?,
    val revenue: Long?,
    // TV only
    val numberOfSeasons: Int?,
    val numberOfEpisodes: Int?,
    val cachedAt: Long,
)

/*
 * JSON shapes for the list columns. They mirror the domain models but are kept separate,
 * because the domain layer must not depend on kotlinx.serialization.
 */

@Serializable
data class GenreJson(val id: Int, val name: String)

@Serializable
data class PersonJson(val id: Int, val name: String, val role: String, val profileUrl: String?)

@Serializable
data class CreditsJson(
    val cast: List<PersonJson> = emptyList(),
    val directors: List<PersonJson> = emptyList(),
    val writers: List<PersonJson> = emptyList(),
)
