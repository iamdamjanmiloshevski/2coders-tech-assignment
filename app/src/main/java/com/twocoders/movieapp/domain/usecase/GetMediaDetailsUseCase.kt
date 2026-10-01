package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaDetails
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.repository.MovieRepository
import com.twocoders.movieapp.domain.repository.TvShowRepository

/** Loads the details of a movie or TV show, sending the call to the matching repository. */
class GetMediaDetailsUseCase(
    private val movieRepository: MovieRepository,
    private val tvShowRepository: TvShowRepository,
) {
    suspend operator fun invoke(id: Int, type: MediaType): DataResult<MediaDetails> = when (type) {
        MediaType.MOVIE -> movieRepository.getMovieDetails(id)
        MediaType.TV_SHOW -> tvShowRepository.getTvShowDetails(id)
    }
}
