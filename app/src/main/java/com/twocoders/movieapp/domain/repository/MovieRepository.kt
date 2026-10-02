package com.twocoders.movieapp.domain.repository

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page

/**
 * Movie data, wherever it comes from. Implementations never throw: every failure comes back as
 * a [DataResult.Failure] with an [com.twocoders.movieapp.domain.error.AppError]. Data seen before
 * is also available offline.
 */
interface MovieRepository {
    /** One page of TMDB's popular movies. [page] is 1-based. */
    suspend fun getPopularMovies(page: Int): DataResult<Page<MediaSummary>>

    suspend fun getMovieDetails(id: Int): DataResult<MovieDetails>
}
