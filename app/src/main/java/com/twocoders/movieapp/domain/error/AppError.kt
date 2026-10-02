package com.twocoders.movieapp.domain.error

/**
 * Every failure the app knows how to handle, independent of where it came from
 * (HTTP, parsing, connectivity). The presentation layer maps these to
 * user-facing messages; the data layer is responsible for producing them.
 */
sealed interface AppError {
    /** The device couldn't reach the server (offline, DNS failure, timeout). */
    data object NoConnection : AppError

    /** TMDB rejected the credentials (HTTP 401). Usually a bad or missing token. */
    data class Unauthorized(val message: String?) : AppError

    /** The requested resource doesn't exist (HTTP 404). */
    data class NotFound(val message: String?) : AppError

    /** Any other non-2xx response. [message] is TMDB's `status_message` when present. */
    data class Http(val code: Int, val message: String?) : AppError

    /** The server answered 2xx but the body couldn't be decoded. */
    data object Parsing : AppError

    /** Anything not covered above. Logged in full by the data layer. */
    data object Unknown : AppError
}
