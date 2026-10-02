package com.twocoders.movieapp.data.mapper

/**
 * Builds absolute image URLs from the relative paths TMDB returns (e.g. `/abc.jpg`).
 *
 * Images come from a separate host (`image.tmdb.org/t/p/`), with the size as a path segment.
 * The base URL is injected so tests don't depend on BuildConfig.
 */
class ImageUrlBuilder(
    private val baseUrl: String,
) {
    fun poster(path: String?): String? = build(POSTER_SIZE, path)
    fun backdrop(path: String?): String? = build(BACKDROP_SIZE, path)
    fun profile(path: String?): String? = build(PROFILE_SIZE, path)

    private fun build(size: String, path: String?): String? =
        path?.takeIf { it.isNotBlank() }?.let { "${baseUrl.trimEnd('/')}/$size/${it.trimStart('/')}" }

    private companion object {
        const val POSTER_SIZE = "w500"
        const val BACKDROP_SIZE = "w780"
        const val PROFILE_SIZE = "w185"
    }
}
