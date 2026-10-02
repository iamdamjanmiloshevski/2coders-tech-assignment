package com.twocoders.movieapp.domain.model

/**
 * The full description of a title, shown on the details screen.
 *
 * Sealed, so the UI can render the common fields once and use an exhaustive
 * `when` for the movie- or show-specific extras.
 */
sealed interface MediaDetails {
    val id: Int
    val title: String
    val tagline: String?
    val overview: String
    val posterUrl: String?
    val backdropUrl: String?
    val genres: List<Genre>
    /** Average user rating on a 0–10 scale. */
    val voteAverage: Double
    val voteCount: Int
    val releaseYear: Int?
    val status: String?
    val credits: Credits

    val type: MediaType
        get() = when (this) {
            is MovieDetails -> MediaType.MOVIE
            is TvShowDetails -> MediaType.TV_SHOW
        }
}

data class MovieDetails(
    override val id: Int,
    override val title: String,
    override val tagline: String?,
    override val overview: String,
    override val posterUrl: String?,
    override val backdropUrl: String?,
    override val genres: List<Genre>,
    override val voteAverage: Double,
    override val voteCount: Int,
    override val releaseYear: Int?,
    override val status: String?,
    override val credits: Credits,
    val runtimeMinutes: Int?,
    /** Budget in US dollars, or null when TMDB reports 0 (unknown). */
    val budget: Long?,
    /** Revenue in US dollars, or null when TMDB reports 0 (unknown). */
    val revenue: Long?,
) : MediaDetails

data class TvShowDetails(
    override val id: Int,
    override val title: String,
    override val tagline: String?,
    override val overview: String,
    override val posterUrl: String?,
    override val backdropUrl: String?,
    override val genres: List<Genre>,
    override val voteAverage: Double,
    override val voteCount: Int,
    override val releaseYear: Int?,
    override val status: String?,
    override val credits: Credits,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
) : MediaDetails
