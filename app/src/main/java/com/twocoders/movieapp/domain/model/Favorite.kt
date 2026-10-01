package com.twocoders.movieapp.domain.model

/**
 * A title the user saved.
 *
 * It keeps a [media] snapshot, so the favorites list renders instantly and offline, without
 * fetching each title again. [addedAtMillis] orders the list (newest first), and lets an
 * undone removal go back to its original position.
 */
data class Favorite(
    val media: MediaSummary,
    val addedAtMillis: Long,
)

/** The list-card form of these details, used as the snapshot when the user favorites from the details screen. */
fun MediaDetails.toSummary(): MediaSummary = MediaSummary(
    id = id,
    type = type,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    voteAverage = voteAverage,
    releaseYear = releaseYear,
)
