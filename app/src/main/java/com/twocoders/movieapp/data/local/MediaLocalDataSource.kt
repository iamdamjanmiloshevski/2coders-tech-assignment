package com.twocoders.movieapp.data.local

import com.twocoders.movieapp.domain.model.MediaDetails
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.model.TvShowDetails

/**
 * The offline cache, as seen by repositories. It works with domain models, so Room entities
 * and DAOs never leave `data/local`. Repository tests bind a fake.
 */
interface MediaLocalDataSource {
    /** The cached [page] of the list identified by [listKey] (see [CacheKeys]), or null if it was never cached. */
    suspend fun getPage(listKey: String, page: Int): Page<MediaSummary>?

    /** Stores [page] under [listKey], replacing any copy of the same page number. */
    suspend fun savePage(listKey: String, page: Page<MediaSummary>)

    /** Cached movie details, or null if never cached. */
    suspend fun getMovieDetails(id: Int): MovieDetails?

    /** Cached TV show details, or null if never cached. */
    suspend fun getTvShowDetails(id: Int): TvShowDetails?

    /** Stores [details], replacing any older copy of the same title. */
    suspend fun saveDetails(details: MediaDetails)
}
