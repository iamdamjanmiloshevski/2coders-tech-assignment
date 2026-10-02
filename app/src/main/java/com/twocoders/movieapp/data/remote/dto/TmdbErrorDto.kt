package com.twocoders.movieapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body TMDB returns with non-2xx responses, e.g.
 * `{"status_code": 7, "status_message": "Invalid API key: You must be granted a valid key."}`.
 */
@Serializable
data class TmdbErrorDto(
    @SerialName("status_code") val statusCode: Int? = null,
    @SerialName("status_message") val statusMessage: String? = null,
)
