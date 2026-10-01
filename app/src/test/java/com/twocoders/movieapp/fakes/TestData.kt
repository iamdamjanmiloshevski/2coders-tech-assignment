package com.twocoders.movieapp.fakes

import com.twocoders.movieapp.domain.model.Credits
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.model.TvShowDetails

/** Small builders for domain objects, so tests only spell out the fields they care about. */
object TestData {

    fun summary(id: Int, type: MediaType = MediaType.MOVIE, title: String = "Title $id") = MediaSummary(
        id = id,
        type = type,
        title = title,
        overview = "Overview $id",
        posterUrl = null,
        voteAverage = 7.5,
        releaseYear = 2024,
    )

    fun page(page: Int, totalPages: Int, ids: IntRange, type: MediaType = MediaType.MOVIE) = Page(
        items = ids.map { summary(it, type) },
        page = page,
        totalPages = totalPages,
    )

    fun movieDetails(id: Int = 1) = MovieDetails(
        id = id,
        title = "Movie $id",
        tagline = null,
        overview = "",
        posterUrl = null,
        backdropUrl = null,
        genres = emptyList(),
        voteAverage = 8.0,
        voteCount = 100,
        releaseYear = 2024,
        status = "Released",
        credits = Credits.EMPTY,
        runtimeMinutes = 120,
        budget = null,
        revenue = null,
    )

    fun tvShowDetails(id: Int = 1) = TvShowDetails(
        id = id,
        title = "Show $id",
        tagline = null,
        overview = "",
        posterUrl = null,
        backdropUrl = null,
        genres = emptyList(),
        voteAverage = 8.0,
        voteCount = 100,
        releaseYear = 2024,
        status = "Ended",
        credits = Credits.EMPTY,
        numberOfSeasons = 2,
        numberOfEpisodes = 20,
    )
}
