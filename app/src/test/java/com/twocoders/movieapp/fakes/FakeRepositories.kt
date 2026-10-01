package com.twocoders.movieapp.fakes

import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.model.TvShowDetails
import com.twocoders.movieapp.domain.repository.MovieRepository
import com.twocoders.movieapp.domain.repository.SearchRepository
import com.twocoders.movieapp.domain.repository.TvShowRepository

/*
 * Hand-written fakes for the domain repositories. Each one records its calls and
 * answers from a lambda that the test can swap, which reads better in assertions than mocks.
 */

class FakeMovieRepository : MovieRepository {
    val popularRequests = mutableListOf<Int>()
    val detailsRequests = mutableListOf<Int>()

    var popularResult: suspend (page: Int) -> DataResult<Page<MediaSummary>> =
        { page -> DataResult.Success(TestData.page(page, totalPages = 3, ids = (page * 10)..(page * 10 + 1))) }
    var detailsResult: suspend (id: Int) -> DataResult<MovieDetails> =
        { id -> DataResult.Success(TestData.movieDetails(id)) }

    override suspend fun getPopularMovies(page: Int): DataResult<Page<MediaSummary>> {
        popularRequests += page
        return popularResult(page)
    }

    override suspend fun getMovieDetails(id: Int): DataResult<MovieDetails> {
        detailsRequests += id
        return detailsResult(id)
    }
}

class FakeTvShowRepository : TvShowRepository {
    val detailsRequests = mutableListOf<Int>()

    var detailsResult: suspend (id: Int) -> DataResult<TvShowDetails> =
        { id -> DataResult.Success(TestData.tvShowDetails(id)) }

    override suspend fun getTvShowDetails(id: Int): DataResult<TvShowDetails> {
        detailsRequests += id
        return detailsResult(id)
    }
}

class FakeSearchRepository : SearchRepository {
    data class Request(val query: String, val type: MediaType, val page: Int)

    val requests = mutableListOf<Request>()

    var result: suspend (Request) -> DataResult<Page<MediaSummary>> =
        { request -> DataResult.Success(TestData.page(request.page, totalPages = 2, ids = 1..2, type = request.type)) }

    override suspend fun search(query: String, type: MediaType, page: Int): DataResult<Page<MediaSummary>> {
        val request = Request(query, type, page)
        requests += request
        return result(request)
    }
}
