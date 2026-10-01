package com.twocoders.movieapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.twocoders.movieapp.domain.model.MediaType

/**
 * One cached page of a paginated list.
 *
 * [listKey] identifies the list. See [com.twocoders.movieapp.data.local.CacheKeys] for the format.
 * [cachedAt] (epoch ms) is used to prune old search results.
 */
@Entity(tableName = "pages", primaryKeys = ["listKey", "page"])
data class PageEntity(
    val listKey: String,
    val page: Int,
    val totalPages: Int,
    val cachedAt: Long,
)

/** The ordered contents of a [PageEntity]: which titles it holds, at which [position]. */
@Entity(
    tableName = "page_items",
    primaryKeys = ["listKey", "page", "position"],
    foreignKeys = [
        ForeignKey(
            entity = PageEntity::class,
            parentColumns = ["listKey", "page"],
            childColumns = ["listKey", "page"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    // Speeds up the join to `media` and the orphan clean-up.
    indices = [Index("mediaId", "mediaType")],
)
data class PageItemEntity(
    val listKey: String,
    val page: Int,
    val position: Int,
    val mediaId: Int,
    val mediaType: MediaType,
)
