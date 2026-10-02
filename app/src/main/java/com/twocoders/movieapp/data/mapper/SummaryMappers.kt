package com.twocoders.movieapp.data.mapper

import com.twocoders.movieapp.data.remote.dto.MovieDto
import com.twocoders.movieapp.data.remote.dto.PagedResponseDto
import com.twocoders.movieapp.data.remote.dto.TvShowDto
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.Page

/**
 * TMDB rejects requests beyond page 500 on list and search endpoints, even when
 * `total_pages` reports more. Clamping stops the paginator from asking for pages that don't exist.
 */
internal const val TMDB_MAX_PAGE = 500

/** Maps a TMDB page envelope to a domain [Page], clamping `total_pages` to [TMDB_MAX_PAGE]. */
internal fun <T, R> PagedResponseDto<T>.toPage(mapItem: (T) -> R): Page<R> = Page(
    items = results.map(mapItem),
    page = page,
    totalPages = totalPages.coerceAtMost(TMDB_MAX_PAGE),
)

internal fun MovieDto.toSummary(images: ImageUrlBuilder) = MediaSummary(
    id = id,
    type = MediaType.MOVIE,
    title = title,
    overview = overview,
    posterUrl = images.poster(posterPath),
    voteAverage = voteAverage,
    releaseYear = releaseDate.toYear(),
)

internal fun TvShowDto.toSummary(images: ImageUrlBuilder) = MediaSummary(
    id = id,
    type = MediaType.TV_SHOW,
    title = name,
    overview = overview,
    posterUrl = images.poster(posterPath),
    voteAverage = voteAverage,
    releaseYear = firstAirDate.toYear(),
)

/** TMDB dates are `yyyy-MM-dd`, and an empty string or null for unknown dates. */
internal fun String?.toYear(): Int? = this?.take(4)?.toIntOrNull()
