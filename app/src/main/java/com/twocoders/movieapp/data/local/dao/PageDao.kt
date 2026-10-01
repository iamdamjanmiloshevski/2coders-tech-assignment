package com.twocoders.movieapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.twocoders.movieapp.data.local.entity.MediaEntity
import com.twocoders.movieapp.data.local.entity.PageEntity
import com.twocoders.movieapp.data.local.entity.PageItemEntity

@Dao
abstract class PageDao {

    /** The page's metadata and its titles in order, or null if this page was never cached. */
    @Transaction
    open suspend fun getPage(listKey: String, page: Int): Pair<PageEntity, List<MediaEntity>>? {
        val info = getPageInfo(listKey, page) ?: return null
        return info to getPageMedia(listKey, page)
    }

    /** Replaces a page atomically, so a reader never sees half of an old page and half of a new one. */
    @Transaction
    open suspend fun replacePage(page: PageEntity, media: List<MediaEntity>) {
        deletePageItems(page.listKey, page.page)
        deletePage(page.listKey, page.page)
        upsertMedia(media)
        insertPage(page)
        insertItems(
            media.mapIndexed { position, item ->
                PageItemEntity(page.listKey, page.page, position, item.id, item.type)
            },
        )
    }

    /** Drops pages under [keyPrefix] cached before [cutoff], then any titles no page refers to anymore. */
    @Transaction
    open suspend fun pruneOlderThan(keyPrefix: String, cutoff: Long) {
        deleteStaleItems("$keyPrefix%", cutoff)
        deleteStalePages("$keyPrefix%", cutoff)
        deleteOrphanMedia()
    }

    @Query("SELECT * FROM pages WHERE listKey = :listKey AND page = :page")
    protected abstract suspend fun getPageInfo(listKey: String, page: Int): PageEntity?

    @Query(
        """
        SELECT media.* FROM page_items
        INNER JOIN media ON media.id = page_items.mediaId AND media.type = page_items.mediaType
        WHERE page_items.listKey = :listKey AND page_items.page = :page
        ORDER BY page_items.position
        """,
    )
    protected abstract suspend fun getPageMedia(listKey: String, page: Int): List<MediaEntity>

    @Upsert
    protected abstract suspend fun upsertMedia(media: List<MediaEntity>)

    @Insert
    protected abstract suspend fun insertPage(page: PageEntity)

    @Insert
    protected abstract suspend fun insertItems(items: List<PageItemEntity>)

    // Items are deleted explicitly too, so correctness doesn't depend on SQLite foreign keys being enabled.
    @Query("DELETE FROM page_items WHERE listKey = :listKey AND page = :page")
    protected abstract suspend fun deletePageItems(listKey: String, page: Int)

    @Query("DELETE FROM pages WHERE listKey = :listKey AND page = :page")
    protected abstract suspend fun deletePage(listKey: String, page: Int)

    @Query(
        """
        DELETE FROM page_items WHERE EXISTS (
            SELECT 1 FROM pages
            WHERE pages.listKey = page_items.listKey AND pages.page = page_items.page
              AND pages.listKey LIKE :keyPattern AND pages.cachedAt < :cutoff
        )
        """,
    )
    protected abstract suspend fun deleteStaleItems(keyPattern: String, cutoff: Long)

    @Query("DELETE FROM pages WHERE listKey LIKE :keyPattern AND cachedAt < :cutoff")
    protected abstract suspend fun deleteStalePages(keyPattern: String, cutoff: Long)

    @Query(
        """
        DELETE FROM media WHERE NOT EXISTS (
            SELECT 1 FROM page_items
            WHERE page_items.mediaId = media.id AND page_items.mediaType = media.type
        )
        """,
    )
    protected abstract suspend fun deleteOrphanMedia()
}
