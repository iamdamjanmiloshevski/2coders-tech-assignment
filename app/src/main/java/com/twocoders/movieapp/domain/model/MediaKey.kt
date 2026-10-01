package com.twocoders.movieapp.domain.model

/**
 * Identifies one title. TMDB ids are only unique within a media type: movie 1396 and TV show 1396
 * are different titles. Anything that tracks titles, such as favorites, keys on both fields.
 */
data class MediaKey(
    val id: Int,
    val type: MediaType,
)

val MediaSummary.key: MediaKey get() = MediaKey(id, type)

val MediaDetails.key: MediaKey get() = MediaKey(id, type)
