package com.twocoders.movieapp.fakes

import com.twocoders.movieapp.core.logging.Logger

/** Collects log lines in memory so tests can check that failures were logged. */
class FakeLogger : Logger {
    val errors = mutableListOf<String>()
    val warnings = mutableListOf<String>()

    override fun debug(tag: String, message: String) = Unit

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        warnings += message
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        errors += message
    }
}
