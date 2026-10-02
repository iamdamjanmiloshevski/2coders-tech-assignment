package com.twocoders.movieapp.domain.error

/**
 * The outcome of a data operation: either a value or a typed [AppError].
 *
 * Used instead of `kotlin.Result` because the error side is a closed set the UI
 * can `when` over, rather than an arbitrary `Throwable`.
 */
sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class Failure(val error: AppError) : DataResult<Nothing>
}

/** Transforms the success value, passing a failure through unchanged. */
inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}
