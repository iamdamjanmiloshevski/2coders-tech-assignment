package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.repository.MovieRepository

/**
 * Loads one page of the popular-movies feed shown on the main screen.
 *
 * A thin pass-through today, kept as a use case so the ViewModel depends on an
 * intent ("popular movies") rather than on a repository's whole API.
 */
class GetPopularMoviesUseCase(
    private val movieRepository: MovieRepository,
) {
    suspend operator fun invoke(page: Int): DataResult<Page<MediaSummary>> =
        movieRepository.getPopularMovies(page)
}
