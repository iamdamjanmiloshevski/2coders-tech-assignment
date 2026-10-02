package com.twocoders.movieapp.domain.repository

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): DataResult<Page<MediaSummary>>

    suspend fun getMovieDetails(id: Int): DataResult<MovieDetails>
}
