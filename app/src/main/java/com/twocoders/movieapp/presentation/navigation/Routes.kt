package com.twocoders.movieapp.presentation.navigation

import com.twocoders.movieapp.domain.model.MediaType
import kotlinx.serialization.Serializable

/*
 * Type-safe Navigation Compose destinations. Arguments are typed constructor
 * properties instead of string templates. Enums such as MediaType are supported directly.
 */

@Serializable
data object MovieListRoute

@Serializable
data object SearchRoute

@Serializable
data class DetailsRoute(val id: Int, val type: MediaType)
