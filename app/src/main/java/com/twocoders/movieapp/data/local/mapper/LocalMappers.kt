package com.twocoders.movieapp.data.local.mapper

import com.twocoders.movieapp.data.local.entity.CreditsJson
import com.twocoders.movieapp.data.local.entity.DetailsEntity
import com.twocoders.movieapp.data.local.entity.GenreJson
import com.twocoders.movieapp.data.local.entity.MediaEntity
import com.twocoders.movieapp.data.local.entity.PersonJson
import com.twocoders.movieapp.domain.model.Credits
import com.twocoders.movieapp.domain.model.Genre
import com.twocoders.movieapp.domain.model.MediaDetails
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Person
import com.twocoders.movieapp.domain.model.TvShowDetails

/*
 * Domain <-> Room mapping. The cache stores what the app shows: URLs already built, and unknown
 * values already turned into null. Reading a row back therefore gives the same domain object
 * the network path produced.
 */

internal fun MediaSummary.toEntity() = MediaEntity(
    id = id,
    type = type,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    voteAverage = voteAverage,
    releaseYear = releaseYear,
)

internal fun MediaEntity.toDomain() = MediaSummary(
    id = id,
    type = type,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    voteAverage = voteAverage,
    releaseYear = releaseYear,
)

internal fun MediaDetails.toEntity(cachedAt: Long): DetailsEntity {
    val movie = this as? MovieDetails
    val show = this as? TvShowDetails
    return DetailsEntity(
        id = id,
        type = type,
        title = title,
        tagline = tagline,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        genres = genres.map { GenreJson(it.id, it.name) },
        voteAverage = voteAverage,
        voteCount = voteCount,
        releaseYear = releaseYear,
        status = status,
        credits = credits.toJson(),
        runtimeMinutes = movie?.runtimeMinutes,
        budget = movie?.budget,
        revenue = movie?.revenue,
        numberOfSeasons = show?.numberOfSeasons,
        numberOfEpisodes = show?.numberOfEpisodes,
        cachedAt = cachedAt,
    )
}

internal fun DetailsEntity.toMovieDetails() = MovieDetails(
    id = id,
    title = title,
    tagline = tagline,
    overview = overview,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    genres = genres.map { Genre(it.id, it.name) },
    voteAverage = voteAverage,
    voteCount = voteCount,
    releaseYear = releaseYear,
    status = status,
    credits = credits.toDomain(),
    runtimeMinutes = runtimeMinutes,
    budget = budget,
    revenue = revenue,
)

internal fun DetailsEntity.toTvShowDetails() = TvShowDetails(
    id = id,
    title = title,
    tagline = tagline,
    overview = overview,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    genres = genres.map { Genre(it.id, it.name) },
    voteAverage = voteAverage,
    voteCount = voteCount,
    releaseYear = releaseYear,
    status = status,
    credits = credits.toDomain(),
    numberOfSeasons = numberOfSeasons ?: 0,
    numberOfEpisodes = numberOfEpisodes ?: 0,
)

private fun Credits.toJson() = CreditsJson(
    cast = cast.map { it.toJson() },
    directors = directors.map { it.toJson() },
    writers = writers.map { it.toJson() },
)

private fun CreditsJson.toDomain() = Credits(
    cast = cast.map { it.toDomain() },
    directors = directors.map { it.toDomain() },
    writers = writers.map { it.toDomain() },
)

private fun Person.toJson() = PersonJson(id, name, role, profileUrl)

private fun PersonJson.toDomain() = Person(id, name, role, profileUrl)

