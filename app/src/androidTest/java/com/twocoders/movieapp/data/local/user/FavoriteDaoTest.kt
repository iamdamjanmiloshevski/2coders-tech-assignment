package com.twocoders.movieapp.data.local.user

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.twocoders.movieapp.domain.model.MediaType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs the real favorites schema and queries against an in-memory SQLite database on a device. */
@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var database: UserDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), UserDatabase::class.java).build()
        dao = database.favoriteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun newestFirst() = runTest {
        dao.upsert(favorite(1, addedAt = 10))
        dao.upsert(favorite(2, addedAt = 30))
        dao.upsert(favorite(3, addedAt = 20))

        assertEquals(listOf(2, 3, 1), dao.observeAll().first().map { it.id })
    }

    @Test
    fun observeReEmitsOnAddAndRemove() = runTest {
        dao.observeAll().test {
            assertEquals(emptyList<FavoriteEntity>(), awaitItem())

            dao.upsert(favorite(1, addedAt = 10))
            assertEquals(listOf(1), awaitItem().map { it.id })

            dao.delete(1, MediaType.MOVIE)
            assertEquals(emptyList<FavoriteEntity>(), awaitItem())
        }
    }

    @Test
    fun upsertReplacesInsteadOfDuplicating() = runTest {
        dao.upsert(favorite(1, addedAt = 10, title = "Old"))
        dao.upsert(favorite(1, addedAt = 10, title = "New"))

        assertEquals(listOf("New"), dao.observeAll().first().map { it.title })
    }

    @Test
    fun movieAndShowWithTheSameIdAreSeparate() = runTest {
        dao.upsert(favorite(7, addedAt = 10, type = MediaType.MOVIE))
        dao.upsert(favorite(7, addedAt = 20, type = MediaType.TV_SHOW))

        dao.delete(7, MediaType.MOVIE)

        assertFalse(dao.exists(7, MediaType.MOVIE))
        assertTrue(dao.exists(7, MediaType.TV_SHOW))
    }

    private fun favorite(id: Int, addedAt: Long, type: MediaType = MediaType.MOVIE, title: String = "Title $id") =
        FavoriteEntity(
            id = id, type = type, title = title, overview = "", posterUrl = null,
            voteAverage = 7.0, releaseYear = 2020, addedAt = addedAt,
        )
}
