package com.twocoders.movieapp.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class FormattersTest {

    private val us = Locale.US

    @Test
    fun `rating has one decimal and is hidden when unrated`() {
        assertEquals("8.2", formatRating(8.237, us))
        assertEquals("7.0", formatRating(7.0, us))
        assertNull(formatRating(0.0, us))
    }

    @Test
    fun `rating follows the locale decimal separator`() {
        assertEquals("8,2", formatRating(8.2, Locale.GERMANY))
    }

    @Test
    fun `counts are compacted and drop trailing zero decimals`() {
        assertEquals("950", formatCompactCount(950, us))
        assertEquals("1.2K", formatCompactCount(1_234, us))
        assertEquals("30.2K", formatCompactCount(30_210, us))
        assertEquals("10K", formatCompactCount(10_000, us))
        assertEquals("1.5M", formatCompactCount(1_500_000, us))
        assertEquals("2B", formatCompactCount(2_000_000_000, us))
    }

    @Test
    fun `runtime reads as hours and minutes`() {
        assertEquals("2h 19m", formatRuntime(139))
        assertEquals("2h", formatRuntime(120))
        assertEquals("45m", formatRuntime(45))
    }

    @Test
    fun `money is compact US dollars`() {
        assertEquals("$63M", formatUsd(63_000_000, us))
        assertEquals("$1.3B", formatUsd(1_250_000_000, us))
    }
}
