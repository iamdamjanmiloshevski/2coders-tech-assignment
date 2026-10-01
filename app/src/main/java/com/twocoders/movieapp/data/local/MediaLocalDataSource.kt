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
    suspend fun getPage(listKey: String, page: Int): Page<MediaSummary>?
    suspend fun savePage(listKey: String, page: Page<MediaSummary>)
    suspend fun getMovieDetails(id: Int): MovieDetails?
    suspend fun getTvShowDetails(id: Int): TvShowDetails?
    suspend fun saveDetails(details: MediaDetails)
}
