package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.local.MediaLocalDataSource
import com.twocoders.movieapp.data.mapper.ImageUrlBuilder
import com.twocoders.movieapp.data.mapper.toDomain
import com.twocoders.movieapp.data.remote.ApiCallHandler
import com.twocoders.movieapp.data.remote.TmdbApi
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.TvShowDetails
import com.twocoders.movieapp.domain.repository.TvShowRepository

/** TV show details: fetched from TMDB, and served from the Room cache when offline (see [NetworkFirst]). */
class TvShowRepositoryImpl(
    private val api: TmdbApi,
    private val apiCallHandler: ApiCallHandler,
    private val images: ImageUrlBuilder,
    private val local: MediaLocalDataSource,
    private val networkFirst: NetworkFirst,
) : TvShowRepository {

    override suspend fun getTvShowDetails(id: Int): DataResult<TvShowDetails> = networkFirst(
        fetch = {
            apiCallHandler.execute(
                request = { api.getTvShowDetails(id) },
                map = { it.toDomain(images) },
            )
        },
        saveToCache = { local.saveDetails(it) },
        loadFromCache = { local.getTvShowDetails(id) },
    )
}
