package com.twocoders.movieapp.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageUrlBuilderTest {

    private val images = ImageUrlBuilder("https://image.tmdb.org/t/p/")

    @Test
    fun `joins base, size and path without doubling slashes`() {
        assertEquals("https://image.tmdb.org/t/p/w500/abc.jpg", images.poster("/abc.jpg"))
    }

    @Test
    fun `null or blank path gives no url`() {
        assertNull(images.poster(null))
        assertNull(images.backdrop(""))
    }
}
