package com.twocoders.movieapp.domain.repository

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.Page

interface SearchRepository {
    suspend fun search(query: String, type: MediaType, page: Int): DataResult<Page<MediaSummary>>
}
