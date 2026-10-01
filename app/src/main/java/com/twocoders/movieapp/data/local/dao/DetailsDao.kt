package com.twocoders.movieapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.twocoders.movieapp.data.local.entity.DetailsEntity
import com.twocoders.movieapp.domain.model.MediaType

@Dao
/** Reads and writes the details cache. A row is identified by (id, type), because a movie and a show can share an id. */
interface DetailsDao {

    @Query("SELECT * FROM details WHERE id = :id AND type = :type")
    suspend fun get(id: Int, type: MediaType): DetailsEntity?

    @Upsert
    suspend fun upsert(details: DetailsEntity)
}
