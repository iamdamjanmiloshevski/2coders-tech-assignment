package com.twocoders.movieapp.core.time

/** The current time in epoch milliseconds. Injected so cache timestamps and expiry are testable. */
fun interface Clock {
    fun nowMillis(): Long

    companion object {
        val Default = Clock { System.currentTimeMillis() }
    }
}
