package com.twocoders.movieapp.presentation.common

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/*
 * Pure display formatting, kept free of Android and Compose so it can be unit-tested on the JVM.
 * The locale is a parameter, so tests stay deterministic.
 */

/** `8.2`, or null for 0.0 (TMDB's value for titles nobody has rated yet), so the UI can hide the rating. */
fun formatRating(voteAverage: Double, locale: Locale = Locale.getDefault()): String? =
    if (voteAverage <= 0.0) null else String.format(locale, "%.1f", voteAverage)

/** `950`, `1.2K`, `30.2K`, `1.5M`. A trailing `.0` is dropped (`10K`, not `10.0K`). */
fun formatCompactCount(count: Long, locale: Locale = Locale.getDefault()): String = when {
    abs(count) >= 1_000_000_000 -> compact(count / 1_000_000_000.0, "B", locale)
    abs(count) >= 1_000_000 -> compact(count / 1_000_000.0, "M", locale)
    abs(count) >= 1_000 -> compact(count / 1_000.0, "K", locale)
    else -> count.toString()
}

/** `2h 19m`, `2h`, `45m`. */
fun formatRuntime(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "${rest}m"
        rest == 0 -> "${hours}h"
        else -> "${hours}h ${rest}m"
    }
}

/** US dollars, as TMDB reports them: `$63M`, `$1.3B`. */
fun formatUsd(amount: Long, locale: Locale = Locale.getDefault()): String = "$" + formatCompactCount(amount, locale)

private fun compact(value: Double, suffix: String, locale: Locale): String {
    // Half-up rounding to one decimal: kotlin.math.round rounds a half to the nearest even digit (1.25 -> 1.2).
    val rounded = floor(value * 10 + 0.5) / 10
    val number = if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String.format(locale, "%.1f", rounded)
    return number + suffix
}
