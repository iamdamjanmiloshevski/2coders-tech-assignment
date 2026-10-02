package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.repository.SearchRepository

/**
 * Searches movies or TV shows by title.
 *
 * Trims the query, and returns an empty page for a blank one without calling the
 * network: TMDB answers a blank query with an error, and there's nothing to search for anyway.
 */
class SearchMediaUseCase(
    private val searchRepository: SearchRepository,
) {
    suspend operator fun invoke(query: String, type: MediaType, page: Int): DataResult<Page<MediaSummary>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return DataResult.Success(Page.empty())
        return searchRepository.search(trimmed, type, page)
    }
}
