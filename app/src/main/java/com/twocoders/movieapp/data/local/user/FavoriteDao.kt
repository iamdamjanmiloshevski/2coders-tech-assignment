package com.twocoders.movieapp.data.local.user

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.twocoders.movieapp.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    /** Newest first. Room re-emits whenever the table changes. */
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id AND type = :type)")
    suspend fun exists(id: Int, type: MediaType): Boolean

    @Upsert
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id AND type = :type")
    suspend fun delete(id: Int, type: MediaType)
}
