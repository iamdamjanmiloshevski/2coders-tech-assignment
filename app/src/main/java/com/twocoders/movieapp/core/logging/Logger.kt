package com.twocoders.movieapp.core.logging

/**
 * Logging behind an interface, so layers that log (mainly data) stay testable on the
 * JVM without `android.util.Log`. Production binds [AndroidLogger]; tests bind a fake.
 */
interface Logger {
    fun debug(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
