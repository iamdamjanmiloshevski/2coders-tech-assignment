package com.twocoders.movieapp.data.remote

import com.twocoders.movieapp.data.remote.dto.MovieDetailsDto
import com.twocoders.movieapp.data.remote.dto.MovieDto
import com.twocoders.movieapp.data.remote.dto.PagedResponseDto
import com.twocoders.movieapp.data.remote.dto.TvShowDetailsDto
import com.twocoders.movieapp.data.remote.dto.TvShowDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TMDB v3 endpoints used by the app (https://developer.themoviedb.org/reference).
 *
 * Every call returns [Response] instead of the bare body. That way [ApiCallHandler] can check
 * the HTTP status before anything is decoded, and read TMDB's error body on failure.
 */
interface TmdbApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int): Response<PagedResponseDto<MovieDto>>

    /** `append_to_response=credits` returns cast and crew in the same round-trip. */
    @GET("movie/{id}?append_to_response=credits")
    suspend fun getMovieDetails(@Path("id") id: Int): Response<MovieDetailsDto>

    @GET("tv/{id}?append_to_response=credits")
    suspend fun getTvShowDetails(@Path("id") id: Int): Response<TvShowDetailsDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): Response<PagedResponseDto<MovieDto>>

    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): Response<PagedResponseDto<TvShowDto>>
}
