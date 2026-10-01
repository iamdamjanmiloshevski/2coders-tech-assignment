package com.twocoders.movieapp.domain.repository

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.Page

/** Title search. Same error and offline contract as [MovieRepository]. Past searches are available offline. */
interface SearchRepository {
    /** One page of results for [query] among titles of [type]. [query] must not be blank. [page] is 1-based. */
    suspend fun search(query: String, type: MediaType, page: Int): DataResult<Page<MediaSummary>>
}
