package com.twocoders.movieapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.twocoders.movieapp.data.local.dao.DetailsDao
import com.twocoders.movieapp.data.local.dao.PageDao
import com.twocoders.movieapp.data.local.entity.DetailsEntity
import com.twocoders.movieapp.data.local.entity.MediaEntity
import com.twocoders.movieapp.data.local.entity.PageEntity
import com.twocoders.movieapp.data.local.entity.PageItemEntity

/**
 * Offline cache of TMDB content: list pages and details. It's only a cache, never the source of
 * truth, so a schema change can rebuild it from scratch instead of migrating (see `LocalModule`).
 * The schema is still exported to `app/schemas`, so changes show up in review.
 *
 * User data never goes here. Favorites live in [com.twocoders.movieapp.data.local.user.UserDatabase],
 * which is never wiped.
 */
@Database(
    entities = [MediaEntity::class, PageEntity::class, PageItemEntity::class, DetailsEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(CacheConverters::class)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun pageDao(): PageDao
    abstract fun detailsDao(): DetailsDao

    companion object {
        const val NAME = "movie_cache.db"
    }
}
