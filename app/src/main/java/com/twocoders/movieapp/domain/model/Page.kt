package com.twocoders.movieapp.domain.model

/** One page of a paginated TMDB result. TMDB pages are 1-based. */
data class Page<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
) {
    val hasNextPage: Boolean get() = page < totalPages

    companion object {
        fun <T> empty(): Page<T> = Page(items = emptyList(), page = 1, totalPages = 1)
    }
}
