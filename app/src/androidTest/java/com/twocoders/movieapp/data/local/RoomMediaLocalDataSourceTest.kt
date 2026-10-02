package com.twocoders.movieapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.twocoders.movieapp.domain.model.Credits
import com.twocoders.movieapp.domain.model.Genre
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.model.Person
import com.twocoders.movieapp.domain.model.TvShowDetails
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/** Runs the real Room schema and queries against an in-memory SQLite database on a device. */
@RunWith(AndroidJUnit4::class)
class RoomMediaLocalDataSourceTest {

    private lateinit var database: MovieDatabase
    private lateinit var dataSource: RoomMediaLocalDataSource
    private var now = TimeUnit.DAYS.toMillis(100)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MovieDatabase::class.java).build()
        dataSource = RoomMediaLocalDataSource(database.pageDao(), database.detailsDao(), clock = { now })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun pageRoundTripsInOrder() = runTest {
        val page = Page(items = listOf(summary(3), summary(1), summary(2)), page = 2, totalPages = 9)

        dataSource.savePage(CacheKeys.POPULAR_MOVIES, page)

        assertEquals(page, dataSource.getPage(CacheKeys.POPULAR_MOVIES, 2))
    }

    @Test
    fun missingPageIsNull() = runTest {
        assertNull(dataSource.getPage(CacheKeys.POPULAR_MOVIES, 1))
    }

    @Test
    fun savingAPageAgainReplacesIt() = runTest {
        dataSource.savePage(CacheKeys.POPULAR_MOVIES, Page(listOf(summary(1), summary(2)), page = 1, totalPages = 5))
        dataSource.savePage(CacheKeys.POPULAR_MOVIES, Page(listOf(summary(3)), page = 1, totalPages = 6))

        assertEquals(Page(listOf(summary(3)), page = 1, totalPages = 6), dataSource.getPage(CacheKeys.POPULAR_MOVIES, 1))
    }

    @Test
    fun aTitleSharedByTwoListsIsStoredOnceAndAppearsInBoth() = runTest {
        val searchKey = CacheKeys.search("one", MediaType.MOVIE)
        dataSource.savePage(CacheKeys.POPULAR_MOVIES, Page(listOf(summary(1), summary(2)), page = 1, totalPages = 1))
        dataSource.savePage(searchKey, Page(listOf(summary(1)), page = 1, totalPages = 1))

        assertEquals(listOf(1), dataSource.getPage(searchKey, 1)!!.items.map { it.id })
        assertEquals(listOf(1, 2), dataSource.getPage(CacheKeys.POPULAR_MOVIES, 1)!!.items.map { it.id })
    }

    @Test
    fun searchesOlderThanAWeekArePrunedButPopularIsKept() = runTest {
        val oldSearch = CacheKeys.search("old", MediaType.MOVIE)
        dataSource.savePage(CacheKeys.POPULAR_MOVIES, Page(listOf(summary(1)), page = 1, totalPages = 1))
        dataSource.savePage(oldSearch, Page(listOf(summary(2)), page = 1, totalPages = 1))

        now += TimeUnit.DAYS.toMillis(8)
        // Saving any search triggers the clean-up.
        dataSource.savePage(CacheKeys.search("new", MediaType.MOVIE), Page(listOf(summary(3)), page = 1, totalPages = 1))

        assertNull(dataSource.getPage(oldSearch, 1))
        assertNotNull(dataSource.getPage(CacheKeys.POPULAR_MOVIES, 1))
        assertNotNull(dataSource.getPage(CacheKeys.search("new", MediaType.MOVIE), 1))
    }

    @Test
    fun movieDetailsRoundTrip() = runTest {
        val movie = MovieDetails(
            id = 550, title = "Fight Club", tagline = "Mischief.", overview = "…", posterUrl = "p", backdropUrl = null,
            genres = listOf(Genre(18, "Drama")), voteAverage = 8.4, voteCount = 30_000, releaseYear = 1999,
            status = "Released", credits = credits(), runtimeMinutes = 139, budget = 63_000_000, revenue = null,
        )

        dataSource.saveDetails(movie)

        assertEquals(movie, dataSource.getMovieDetails(550))
        assertNull("same id, different type", dataSource.getTvShowDetails(550))
    }

    @Test
    fun tvShowDetailsRoundTrip() = runTest {
        val show = TvShowDetails(
            id = 1396, title = "Breaking Bad", tagline = null, overview = "…", posterUrl = null, backdropUrl = "b",
            genres = emptyList(), voteAverage = 8.9, voteCount = 15_000, releaseYear = 2008, status = "Ended",
            credits = credits(), numberOfSeasons = 5, numberOfEpisodes = 62,
        )

        dataSource.saveDetails(show)

        assertEquals(show, dataSource.getTvShowDetails(1396))
    }

    private fun summary(id: Int) = MediaSummary(
        id = id, type = MediaType.MOVIE, title = "Title $id", overview = "Overview $id",
        posterUrl = "https://img/$id.jpg", voteAverage = 7.0, releaseYear = 2020,
    )

    private fun credits() = Credits(
        cast = listOf(Person(1, "Actor", "Hero", null)),
        directors = listOf(Person(2, "Director", "Director", null)),
        writers = emptyList(),
    )
}
