package com.twocoders.movieapp.domain.model

/**
 * The compact form of a movie or TV show, used wherever media is listed
 * (popular movies, search results).
 *
 * Movies and TV shows use different TMDB field names (`title`/`name`,
 * `release_date`/`first_air_date`); the data layer unifies them here so the
 * UI never has to care which one it is rendering.
 */
data class MediaSummary(
    val id: Int,
    val type: MediaType,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    /** Average user rating on a 0–10 scale. */
    val voteAverage: Double,
    val releaseYear: Int?,
)
