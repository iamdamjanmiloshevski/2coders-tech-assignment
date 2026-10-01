package com.twocoders.movieapp.data.repository

import com.twocoders.movieapp.data.local.user.FavoriteDao
import com.twocoders.movieapp.data.local.user.FavoriteEntity
import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.key
import com.twocoders.movieapp.fakes.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesRepositoryImplTest {

    private val dao = InMemoryFavoriteDao()
    private var now = 100L
    private val repository = FavoritesRepositoryImpl(dao, clock = { now })

    @Test
    fun `add stamps the current time and round-trips the snapshot`() = runTest {
        val movie = TestData.summary(1).copy(posterUrl = "https://img/p.jpg")

        repository.add(movie)

        assertEquals(listOf(Favorite(movie, addedAtMillis = 100)), repository.observeFavorites().first())
    }

    @Test
    fun `favorites come newest first`() = runTest {
        repository.add(TestData.summary(1)); now = 200
        repository.add(TestData.summary(2)); now = 300
        repository.add(TestData.summary(3))

        assertEquals(listOf(3, 2, 1), repository.observeFavorites().first().map { it.media.id })
    }

    @Test
    fun `isFavorite distinguishes media types`() = runTest {
        repository.add(TestData.summary(7, MediaType.MOVIE))

        assertTrue(repository.isFavorite(TestData.summary(7, MediaType.MOVIE).key))
        assertFalse(repository.isFavorite(TestData.summary(7, MediaType.TV_SHOW).key))
    }

    @Test
    fun `restore keeps the original timestamp`() = runTest {
        repository.add(TestData.summary(1))
        val saved = repository.observeFavorites().first().single()
        repository.remove(saved.media.key)

        now = 999
        repository.restore(saved)

        assertEquals(100, repository.observeFavorites().first().single().addedAtMillis)
    }

    /** Minimal DAO with the same ordering as the SQL query. The real queries run in `FavoriteDaoTest` on a device. */
    private class InMemoryFavoriteDao : FavoriteDao {
        private val rows = MutableStateFlow<Map<Pair<Int, MediaType>, FavoriteEntity>>(emptyMap())

        override fun observeAll(): Flow<List<FavoriteEntity>> =
            rows.map { it.values.sortedByDescending(FavoriteEntity::addedAt) }

        override suspend fun exists(id: Int, type: MediaType) = (id to type) in rows.value

        override suspend fun upsert(favorite: FavoriteEntity) {
            rows.update { it + ((favorite.id to favorite.type) to favorite) }
        }

        override suspend fun delete(id: Int, type: MediaType) {
            rows.update { it - (id to type) }
        }
    }
}
