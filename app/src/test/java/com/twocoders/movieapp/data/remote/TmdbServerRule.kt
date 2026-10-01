package com.twocoders.movieapp.data.remote

import com.twocoders.movieapp.data.mapper.ImageUrlBuilder
import com.twocoders.movieapp.data.repository.NetworkFirst
import com.twocoders.movieapp.fakes.FakeLogger
import com.twocoders.movieapp.fakes.FakeMediaLocalDataSource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.rules.ExternalResource
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * JUnit rule that starts a [MockWebServer] and builds the **real** Retrofit stack against it:
 * the same [TmdbJson], converter and [AuthInterceptor] as production. Data-layer tests then
 * cover the real serialization and error handling, not mocks of them.
 */
class TmdbServerRule : ExternalResource() {

    val server = MockWebServer()
    val logger = FakeLogger()
    val images = ImageUrlBuilder("https://image.test/t/p/")
    val local = FakeMediaLocalDataSource()
    val networkFirst = NetworkFirst(logger)

    lateinit var api: TmdbApi
        private set
    lateinit var apiCallHandler: ApiCallHandler
        private set

    override fun before() {
        server.start()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(accessToken = TEST_TOKEN))
            .readTimeout(2, TimeUnit.SECONDS)
            .build()
        api = Retrofit.Builder()
            .baseUrl(server.url("/3/"))
            .client(client)
            .addConverterFactory(TmdbJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApi::class.java)
        apiCallHandler = ApiCallHandler(TmdbJson, logger)
    }

    override fun after() {
        server.shutdown()
    }

    fun enqueue(code: Int = 200, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    /**
     * Simulates airplane mode: the server goes away, so every following request fails to connect.
     * Dropping a single response isn't enough, because OkHttp silently retries on a pooled connection.
     */
    fun goOffline() {
        server.shutdown()
    }

    fun takeRequest(): RecordedRequest = server.takeRequest(1, TimeUnit.SECONDS)!!

    companion object {
        const val TEST_TOKEN = "test-token"
    }
}
