package com.twocoders.movieapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `tv/{id}?append_to_response=credits`. TV shows use `name`, `first_air_date` and `created_by`. */
@Serializable
data class TvShowDetailsDto(
    val id: Int,
    val name: String = "",
    val tagline: String? = null,
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    val status: String? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int = 0,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int = 0,
    @SerialName("created_by") val createdBy: List<CreatorDto> = emptyList(),
    val credits: CreditsDto = CreditsDto(),
)

@Serializable
data class CreatorDto(
    val id: Int,
    val name: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
)
