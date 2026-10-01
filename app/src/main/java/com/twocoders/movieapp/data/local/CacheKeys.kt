package com.twocoders.movieapp.data.local

import com.twocoders.movieapp.domain.model.MediaType

/**
 * Keys identifying each cached list in `pages.listKey`.
 *
 * Search keys are lowercased, because TMDB search ignores case: "Dune" and "dune" share one cache entry.
 * The `search:` prefix is what pruning looks for.
 */
object CacheKeys {
    const val POPULAR_MOVIES = "popular:movie"
    const val SEARCH_PREFIX = "search:"

    fun search(query: String, type: MediaType): String =
        "$SEARCH_PREFIX${type.name.lowercase()}:${query.trim().lowercase()}"
}
