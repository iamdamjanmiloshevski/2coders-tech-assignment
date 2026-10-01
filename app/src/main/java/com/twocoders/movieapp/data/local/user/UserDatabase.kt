package com.twocoders.movieapp.data.local.user

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Data the user created: favorites. Unlike the TMDB cache ([com.twocoders.movieapp.data.local.MovieDatabase]),
 * it can't be re-downloaded, so it lives in its own database with its own lifecycle:
 * - **Never destructive.** Every schema change needs a real migration (or an `@AutoMigration`),
 *   tested against the exported schema in `app/schemas/`.
 * - **Backed up.** Auto Backup includes it, and excludes the cache (see `backup_rules.xml` and `data_extraction_rules.xml`).
 */
@Database(
    entities = [FavoriteEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val NAME = "user_data.db"
    }
}
