package com.twocoders.movieapp.di

import com.twocoders.movieapp.BuildConfig
import com.twocoders.movieapp.data.remote.ApiCallHandler
import com.twocoders.movieapp.data.remote.AuthInterceptor
import com.twocoders.movieapp.data.remote.TmdbApi
import com.twocoders.movieapp.data.remote.TmdbJson
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** The TMDB HTTP stack: JSON config, OkHttp with auth and logging, Retrofit, and [ApiCallHandler]. */
val networkModule = module {
    single<Json> { TmdbJson }

    single {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(accessToken = BuildConfig.TMDB_ACCESS_TOKEN))
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                    // The token must never end up in Logcat, even in debug builds.
                    redactHeader("Authorization")
                },
            )
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    single<TmdbApi> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApi::class.java)
    }

    singleOf(::ApiCallHandler)
}
