package com.twocoders.movieapp.presentation.common

import com.twocoders.movieapp.domain.model.Credits
import com.twocoders.movieapp.domain.model.Genre
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Person

/** Sample data for `@Preview`s only. Images are null, so previews render offline. */
internal object PreviewData {

    val movie = MediaSummary(
        id = 1,
        type = MediaType.MOVIE,
        title = "Dune: Part Two",
        overview = "Paul Atreides unites with Chani and the Fremen while on a path of revenge " +
            "against the conspirators who destroyed his family.",
        posterUrl = null,
        voteAverage = 8.2,
        releaseYear = 2024,
    )

    val movies = List(6) { index -> movie.copy(id = index + 1, title = if (index == 0) movie.title else "Movie ${index + 1}") }

    val movieDetails = MovieDetails(
        id = 1,
        title = movie.title,
        tagline = "Long live the fighters.",
        overview = movie.overview,
        posterUrl = null,
        backdropUrl = null,
        genres = listOf(Genre(878, "Science Fiction"), Genre(12, "Adventure")),
        voteAverage = 8.2,
        voteCount = 6_120,
        releaseYear = 2024,
        status = "Released",
        credits = Credits(
            cast = listOf(
                Person(1, "Timothée Chalamet", "Paul Atreides", null),
                Person(2, "Zendaya", "Chani", null),
                Person(3, "Rebecca Ferguson", "Jessica", null),
            ),
            directors = listOf(Person(10, "Denis Villeneuve", "Director", null)),
            writers = listOf(Person(10, "Denis Villeneuve", "Screenplay", null), Person(11, "Jon Spaihts", "Screenplay", null)),
        ),
        runtimeMinutes = 167,
        budget = 190_000_000,
        revenue = 714_000_000,
    )
}
