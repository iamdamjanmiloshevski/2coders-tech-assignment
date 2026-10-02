package com.twocoders.movieapp.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds TMDB's API Read Access Token as a Bearer header to every request.
 * This is TMDB's recommended auth method, and it keeps the credential out of URLs and logs.
 */
class AuthInterceptor(
    private val accessToken: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
