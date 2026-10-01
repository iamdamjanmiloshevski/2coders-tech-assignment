package com.twocoders.movieapp.data.local.user

import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.model.MediaSummary

internal fun Favorite.toEntity() = FavoriteEntity(
    id = media.id,
    type = media.type,
    title = media.title,
    overview = media.overview,
    posterUrl = media.posterUrl,
    voteAverage = media.voteAverage,
    releaseYear = media.releaseYear,
    addedAt = addedAtMillis,
)

internal fun FavoriteEntity.toDomain() = Favorite(
    media = MediaSummary(
        id = id,
        type = type,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
        releaseYear = releaseYear,
    ),
    addedAtMillis = addedAt,
)
