package com.twocoders.movieapp.domain.repository

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.TvShowDetails

interface TvShowRepository {
    suspend fun getTvShowDetails(id: Int): DataResult<TvShowDetails>
}
