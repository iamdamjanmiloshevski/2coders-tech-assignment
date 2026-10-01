package com.twocoders.movieapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A movie as it appears in list endpoints (popular, search/movie). */
@Serializable
data class MovieDto(
    val id: Int,
    val title: String = "",
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    /** `yyyy-MM-dd`, or an empty string for unreleased titles. */
    @SerialName("release_date") val releaseDate: String? = null,
)
