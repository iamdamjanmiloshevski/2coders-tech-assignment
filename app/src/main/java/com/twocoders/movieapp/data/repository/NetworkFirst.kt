package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.core.logging.Logger
import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.error.DataResult
import kotlinx.coroutines.CancellationException

/**
 * The repositories' offline strategy: **network first, cache fallback.**
 *
 * - Online: fetch fresh data, save it to the cache, return it.
 * - Offline ([AppError.NoConnection]): return the cached copy if there is one, otherwise the
 *   original offline error, so the UI can say this needs the internet.
 * - Any other failure (401, 404, 5xx, parsing) is returned as is. Old data must not hide a real
 *   server or client error.
 *
 * The cache is best-effort. A failed read or write is logged and treated as "not cached",
 * and never turns a successful network response into an error.
 */
class NetworkFirst(
    private val logger: Logger,
) {
    suspend operator fun <T> invoke(
        fetch: suspend () -> DataResult<T>,
        saveToCache: suspend (T) -> Unit,
        loadFromCache: suspend () -> T?,
    ): DataResult<T> {
        val remote = fetch()
        return when {
            remote is DataResult.Success -> {
                bestEffort("write") { saveToCache(remote.data) }
                remote
            }
            remote is DataResult.Failure && remote.error == AppError.NoConnection ->
                bestEffort("read") { loadFromCache() }?.let { DataResult.Success(it) } ?: remote
            else -> remote
        }
    }

    private suspend fun <R> bestEffort(operation: String, block: suspend () -> R): R? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.error(TAG, "Cache $operation failed", e)
        null
    }

    private companion object {
        const val TAG = "NetworkFirst"
    }
}
