package com.twocoders.movieapp.data.local

import com.twocoders.movieapp.data.local.mapper.toDomain
import com.twocoders.movieapp.data.local.mapper.toEntity
import com.twocoders.movieapp.data.local.mapper.toMovieDetails
import com.twocoders.movieapp.data.local.mapper.toTvShowDetails
import com.twocoders.movieapp.domain.model.Credits
import com.twocoders.movieapp.domain.model.Genre
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.Person
import com.twocoders.movieapp.fakes.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** What goes into the cache must come back out unchanged, or offline screens would differ from online ones. */
class LocalMappersTest {

    private val credits = Credits(
        cast = listOf(Person(1, "Actor", "Hero", "https://img/a.jpg")),
        directors = listOf(Person(2, "Director", "Director", null)),
        writers = listOf(Person(3, "Writer", "Screenplay", null)),
    )

    @Test
    fun `summary round-trips`() {
        val summary = TestData.summary(7, MediaType.TV_SHOW).copy(posterUrl = "https://img/p.jpg")

        assertEquals(summary, summary.toEntity().toDomain())
    }

    @Test
    fun `movie details round-trip including genres and credits`() {
        val movie = TestData.movieDetails(5).copy(
            genres = listOf(Genre(18, "Drama")),
            credits = credits,
            budget = 1_000_000,
            revenue = null,
        )

        val entity = movie.toEntity(cachedAt = 42)

        assertEquals(movie, entity.toMovieDetails())
        assertNull("TV-only columns stay empty for movies", entity.numberOfSeasons)
    }

    @Test
    fun `tv show details round-trip`() {
        val show = TestData.tvShowDetails(9).copy(credits = credits)

        val entity = show.toEntity(cachedAt = 42)

        assertEquals(show, entity.toTvShowDetails())
        assertNull("movie-only columns stay empty for shows", entity.runtimeMinutes)
    }

    @Test
    fun `json columns round-trip through the converters`() {
        val converters = CacheConverters()
        val entity = TestData.movieDetails().copy(genres = listOf(Genre(1, "Action")), credits = credits).toEntity(0)

        assertEquals(entity.genres, converters.genresFromJson(converters.genresToJson(entity.genres)))
        assertEquals(entity.credits, converters.creditsFromJson(converters.creditsToJson(entity.credits)))
    }

    @Test
    fun `search keys ignore case and surrounding spaces`() {
        assertEquals(CacheKeys.search("Dune", MediaType.MOVIE), CacheKeys.search("  dune ", MediaType.MOVIE))
        assertEquals("search:tv_show:dark", CacheKeys.search("Dark", MediaType.TV_SHOW))
    }
}
