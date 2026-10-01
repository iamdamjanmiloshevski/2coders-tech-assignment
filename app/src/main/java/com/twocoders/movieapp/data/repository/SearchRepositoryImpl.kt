package com.twocoders.movieapp.data.repository

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

class SearchRepositoryImpl(
    private val api: TmdbApi,
    private val apiCallHandler: ApiCallHandler,
    private val images: ImageUrlBuilder,
) : SearchRepository {

    override suspend fun search(query: String, type: MediaType, page: Int): DataResult<Page<MediaSummary>> =
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
