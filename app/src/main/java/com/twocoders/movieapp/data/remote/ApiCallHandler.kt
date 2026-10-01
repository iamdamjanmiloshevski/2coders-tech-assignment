package com.twocoders.movieapp.data.remote

import com.twocoders.movieapp.core.logging.Logger
import com.twocoders.movieapp.data.remote.dto.TmdbErrorDto
import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

/**
 * Runs a Retrofit call and turns every outcome into a [DataResult], so repositories never
 * see exceptions or HTTP codes.
 *
 * - A 2xx body is decoded (Retrofit only runs the converter on success) and passed to `map`.
 * - A non-2xx response has its TMDB error body (`status_code`, `status_message`) read and is
 *   mapped to an [AppError]. The server message is kept so the UI can show something useful.
 * - Transport and decoding exceptions become [AppError.NoConnection] and [AppError.Parsing].
 *
 * Full details are logged here. Only the [AppError] goes up to the caller.
 */
class ApiCallHandler(
    private val json: Json,
    private val logger: Logger,
) {

    suspend fun <T, R> execute(
        request: suspend () -> Response<T>,
        map: (T) -> R,
    ): DataResult<R> = try {
        val response = request()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                DataResult.Success(map(body))
            } else {
                logger.error(TAG, "HTTP ${response.code()} with empty body for ${response.url()}")
                DataResult.Failure(AppError.Parsing)
            }
        } else {
            DataResult.Failure(response.toAppError())
        }
    } catch (e: CancellationException) {
        // Never swallow cancellation, or structured concurrency (e.g. flatMapLatest) breaks.
        throw e
    } catch (e: IOException) {
        logger.warn(TAG, "Network failure", e)
        DataResult.Failure(AppError.NoConnection)
    } catch (e: SerializationException) {
        logger.error(TAG, "Failed to decode response", e)
        DataResult.Failure(AppError.Parsing)
    } catch (e: Exception) {
        logger.error(TAG, "Unexpected failure", e)
        DataResult.Failure(AppError.Unknown)
    }

    private fun Response<*>.toAppError(): AppError {
        val rawError = errorBody()?.string().orEmpty()
        val tmdbError = runCatching { json.decodeFromString<TmdbErrorDto>(rawError) }.getOrNull()
        val message = tmdbError?.statusMessage
        logger.error(
            TAG,
            "HTTP ${code()} for ${url()} (tmdb status_code=${tmdbError?.statusCode}): ${rawError.take(500)}",
        )
        return when (code()) {
            401 -> AppError.Unauthorized(message)
            404 -> AppError.NotFound(message)
            else -> AppError.Http(code(), message)
        }
    }

    private fun Response<*>.url(): String = raw().request.url.encodedPath

    private companion object {
        const val TAG = "ApiCallHandler"
    }
}
