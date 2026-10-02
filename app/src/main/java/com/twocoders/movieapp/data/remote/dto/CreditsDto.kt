package com.twocoders.movieapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The `credits` object added to details responses by `append_to_response=credits`. */
@Serializable
data class CreditsDto(
    val cast: List<CastMemberDto> = emptyList(),
    val crew: List<CrewMemberDto> = emptyList(),
)

@Serializable
data class CastMemberDto(
    val id: Int,
    val name: String = "",
    val character: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
)

@Serializable
data class CrewMemberDto(
    val id: Int,
    val name: String = "",
    val job: String? = null,
    val department: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
)
