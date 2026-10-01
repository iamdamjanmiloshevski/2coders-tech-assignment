package com.twocoders.movieapp.domain.usecase

import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.key
import com.twocoders.movieapp.domain.model.toSummary
import com.twocoders.movieapp.fakes.FakeFavoritesRepository
import com.twocoders.movieapp.fakes.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteUseCasesTest {

    private val repository = FakeFavoritesRepository()
    private val toggle = ToggleFavoriteUseCase(repository)
    private val restore = RestoreFavoriteUseCase(repository)
    private val observe = ObserveFavoritesUseCase(repository)

    @Test
    fun `toggling adds a title that isn't a favorite and returns true`() = runTest {
        val movie = TestData.summary(1)

        assertTrue(toggle(movie))
        assertEquals(listOf(movie), observe().first().map { it.media })
    }

    @Test
    fun `toggling a favorite removes it and returns false`() = runTest {
        val movie = TestData.summary(1)
        toggle(movie)

        assertFalse(toggle(movie))
        assertTrue(observe().first().isEmpty())
    }

    @Test
    fun `a movie and a tv show with the same id are separate favorites`() = runTest {
        toggle(TestData.summary(7, MediaType.MOVIE))
        toggle(TestData.summary(7, MediaType.TV_SHOW))

        assertEquals(
            setOf(MediaKey(7, MediaType.MOVIE), MediaKey(7, MediaType.TV_SHOW)),
            observe().first().map { it.media.key }.toSet(),
        )
    }

    @Test
    fun `restoring a removed favorite puts it back in its original position`() = runTest {
        toggle(TestData.summary(1))
        toggle(TestData.summary(2))
        toggle(TestData.summary(3))
        val middle = observe().first()[1]

        toggle(middle.media)
        restore(middle)

        assertEquals(listOf(3, 2, 1), observe().first().map { it.media.id })
    }

    @Test
    fun `details convert to the same summary the lists show`() {
        val details = TestData.movieDetails(5)

        with(details.toSummary()) {
            assertEquals(details.key, key)
            assertEquals(details.title, title)
            assertEquals(details.posterUrl, posterUrl)
            assertEquals(details.voteAverage, voteAverage, 0.0)
        }
        assertEquals(MediaType.TV_SHOW, TestData.tvShowDetails().toSummary().type)
    }
}
