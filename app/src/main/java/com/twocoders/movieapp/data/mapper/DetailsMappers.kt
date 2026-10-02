package com.twocoders.movieapp.data.mapper

import com.twocoders.movieapp.data.remote.dto.CreditsDto
import com.twocoders.movieapp.data.remote.dto.CreatorDto
import com.twocoders.movieapp.data.remote.dto.CrewMemberDto
import com.twocoders.movieapp.data.remote.dto.GenreDto
import com.twocoders.movieapp.data.remote.dto.MovieDetailsDto
import com.twocoders.movieapp.data.remote.dto.TvShowDetailsDto
import com.twocoders.movieapp.domain.model.Credits
import com.twocoders.movieapp.domain.model.Genre
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Person
import com.twocoders.movieapp.domain.model.TvShowDetails

/** The details screen shows top-billed cast only. TMDB can return hundreds of entries. */
internal const val MAX_CAST = 20

private const val JOB_DIRECTOR = "Director"
private const val DEPARTMENT_WRITING = "Writing"
private const val ROLE_CREATOR = "Creator"

internal fun MovieDetailsDto.toDomain(images: ImageUrlBuilder) = MovieDetails(
    id = id,
    title = title,
    tagline = tagline?.takeIf { it.isNotBlank() },
    overview = overview.orEmpty(),
    posterUrl = images.poster(posterPath),
    backdropUrl = images.backdrop(backdropPath),
    genres = genres.map { it.toDomain() },
    voteAverage = voteAverage,
    voteCount = voteCount,
    releaseYear = releaseDate.toYear(),
    status = status,
    credits = credits.toDomain(
        images = images,
        directors = credits.crew.filter { it.job == JOB_DIRECTOR }.map { it.toPerson(images) },
    ),
    runtimeMinutes = runtime?.takeIf { it > 0 },
    // TMDB uses 0 for "unknown". Null makes that explicit to the UI.
    budget = budget.takeIf { it > 0 },
    revenue = revenue.takeIf { it > 0 },
)

internal fun TvShowDetailsDto.toDomain(images: ImageUrlBuilder) = TvShowDetails(
    id = id,
    title = name,
    tagline = tagline?.takeIf { it.isNotBlank() },
    overview = overview.orEmpty(),
    posterUrl = images.poster(posterPath),
    backdropUrl = images.backdrop(backdropPath),
    genres = genres.map { it.toDomain() },
    voteAverage = voteAverage,
    voteCount = voteCount,
    releaseYear = firstAirDate.toYear(),
    status = status,
    // TV shows rarely credit a single director, so their creators play that role.
    credits = credits.toDomain(images = images, directors = createdBy.map { it.toPerson(images) }),
    numberOfSeasons = numberOfSeasons,
    numberOfEpisodes = numberOfEpisodes,
)

private fun CreditsDto.toDomain(images: ImageUrlBuilder, directors: List<Person>) = Credits(
    cast = cast.take(MAX_CAST).map {
        Person(id = it.id, name = it.name, role = it.character.orEmpty(), profileUrl = images.profile(it.profilePath))
    },
    directors = directors.distinctBy { it.id },
    writers = crew.filter { it.department == DEPARTMENT_WRITING }.map { it.toPerson(images) }.distinctBy { it.id },
)

private fun CrewMemberDto.toPerson(images: ImageUrlBuilder) =
    Person(id = id, name = name, role = job.orEmpty(), profileUrl = images.profile(profilePath))

private fun CreatorDto.toPerson(images: ImageUrlBuilder) =
    Person(id = id, name = name, role = ROLE_CREATOR, profileUrl = images.profile(profilePath))

private fun GenreDto.toDomain() = Genre(id = id, name = name)
