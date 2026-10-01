package com.twocoders.movieapp.presentation.navigation

import com.twocoders.movieapp.domain.model.MediaType
import kotlinx.serialization.Serializable

/*
 * Type-safe Navigation Compose destinations. Arguments are typed constructor
 * properties instead of string templates. Enums such as MediaType are supported directly.
 */

/** Start destination: the popular movies list. */
@Serializable
data object MovieListRoute

/** Movie and TV show search. */
@Serializable
data object SearchRoute

/** Details of one title. [type] matters because TMDB ids are only unique within a media type. */
@Serializable
data class DetailsRoute(val id: Int, val type: MediaType)
