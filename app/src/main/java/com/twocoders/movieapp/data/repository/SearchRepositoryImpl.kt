package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.local.CacheKeys
import com.twocoders.movieapp.data.local.MediaLocalDataSource
import com.twocoders.movieapp.data.mapper.ImageUrlBuilder
import com.twocoders.movieapp.data.mapper.toPage
import com.twocoders.movieapp.data.mapper.toSummary
import com.twocoders.movieapp.data.remote.ApiCallHandler
import com.twocoders.movieapp.data.remote.TmdbApi
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.repository.SearchRepository

/** Searches TMDB. Every result page is cached per query and type, so past searches work offline. */
class SearchRepositoryImpl(
    private val api: TmdbApi,
    private val apiCallHandler: ApiCallHandler,
    private val images: ImageUrlBuilder,
    private val local: MediaLocalDataSource,
    private val networkFirst: NetworkFirst,
) : SearchRepository {

    override suspend fun search(query: String, type: MediaType, page: Int): DataResult<Page<MediaSummary>> {
        val cacheKey = CacheKeys.search(query, type)
        return networkFirst(
            fetch = { fetch(query, type, page) },
            saveToCache = { local.savePage(cacheKey, it) },
            loadFromCache = { local.getPage(cacheKey, page) },
        )
    }

    private suspend fun fetch(query: String, type: MediaType, page: Int): DataResult<Page<MediaSummary>> =
        when (type) {
            MediaType.MOVIE -> apiCallHandler.execute(
                request = { api.searchMovies(query, page) },
                map = { response -> response.toPage { it.toSummary(images) } },
            )
            MediaType.TV_SHOW -> apiCallHandler.execute(
                request = { api.searchTvShows(query, page) },
                map = { response -> response.toPage { it.toSummary(images) } },
            )
        }
}
