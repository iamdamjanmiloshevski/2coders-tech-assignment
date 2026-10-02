package com.twocoders.movieapp.domain.repository

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.TvShowDetails

/** TV show data. Same error and offline contract as [MovieRepository]. */
interface TvShowRepository {
    suspend fun getTvShowDetails(id: Int): DataResult<TvShowDetails>
}
