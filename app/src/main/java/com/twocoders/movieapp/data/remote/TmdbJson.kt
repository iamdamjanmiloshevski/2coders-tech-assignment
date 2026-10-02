package com.twocoders.movieapp.data.remote

import kotlinx.serialization.json.Json

/**
 * The single [Json] configuration for TMDB payloads, shared by Retrofit and error-body parsing.
 *
 * - `ignoreUnknownKeys`: TMDB responses have far more fields than the DTOs model.
 * - `coerceInputValues` + `explicitNulls = false`: TMDB sometimes sends `null` where a list or
 *   number is expected; such fields fall back to their Kotlin defaults instead of failing to decode.
 */
val TmdbJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}
