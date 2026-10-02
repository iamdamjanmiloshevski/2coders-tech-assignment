package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.mapper.ImageUrlBuilder
import com.twocoders.movieapp.data.mapper.toDomain
import com.twocoders.movieapp.data.mapper.toPage
import com.twocoders.movieapp.data.mapper.toSummary
import com.twocoders.movieapp.data.remote.ApiCallHandler
import com.twocoders.movieapp.data.remote.TmdbApi
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.repository.MovieRepository

class MovieRepositoryImpl(
    private val api: TmdbApi,
    private val apiCallHandler: ApiCallHandler,
    private val images: ImageUrlBuilder,
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): DataResult<Page<MediaSummary>> =
        apiCallHandler.execute(
            request = { api.getPopularMovies(page) },
            map = { response -> response.toPage { it.toSummary(images) } },
        )

    override suspend fun getMovieDetails(id: Int): DataResult<MovieDetails> =
        apiCallHandler.execute(
            request = { api.getMovieDetails(id) },
            map = { it.toDomain(images) },
        )
}
