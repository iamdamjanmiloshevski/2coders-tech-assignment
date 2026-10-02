package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.mapper.ImageUrlBuilder
import com.twocoders.movieapp.data.mapper.toDomain
import com.twocoders.movieapp.data.remote.ApiCallHandler
import com.twocoders.movieapp.data.remote.TmdbApi
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.TvShowDetails
import com.twocoders.movieapp.domain.repository.TvShowRepository

class TvShowRepositoryImpl(
    private val api: TmdbApi,
    private val apiCallHandler: ApiCallHandler,
    private val images: ImageUrlBuilder,
) : TvShowRepository {

    override suspend fun getTvShowDetails(id: Int): DataResult<TvShowDetails> =
        apiCallHandler.execute(
            request = { api.getTvShowDetails(id) },
            map = { it.toDomain(images) },
        )
}
