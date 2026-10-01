package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.local.CacheKeys
import com.twocoders.movieapp.data.local.MediaLocalDataSource
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

/** Popular movies and movie details: fetched from TMDB, and served from the Room cache when offline (see [NetworkFirst]). */
class MovieRepositoryImpl(
    private val api: TmdbApi,
    private val apiCallHandler: ApiCallHandler,
    private val images: ImageUrlBuilder,
    private val local: MediaLocalDataSource,
    private val networkFirst: NetworkFirst,
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): DataResult<Page<MediaSummary>> = networkFirst(
        fetch = {
            apiCallHandler.execute(
                request = { api.getPopularMovies(page) },
                map = { response -> response.toPage { it.toSummary(images) } },
            )
        },
        saveToCache = { local.savePage(CacheKeys.POPULAR_MOVIES, it) },
        loadFromCache = { local.getPage(CacheKeys.POPULAR_MOVIES, page) },
    )

    override suspend fun getMovieDetails(id: Int): DataResult<MovieDetails> = networkFirst(
        fetch = {
            apiCallHandler.execute(
                request = { api.getMovieDetails(id) },
                map = { it.toDomain(images) },
            )
        },
        saveToCache = { local.saveDetails(it) },
        loadFromCache = { local.getMovieDetails(id) },
    )
}
