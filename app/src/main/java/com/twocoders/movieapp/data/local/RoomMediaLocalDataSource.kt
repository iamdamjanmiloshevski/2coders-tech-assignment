package com.twocoders.movieapp.data.local

import com.twocoders.movieapp.core.time.Clock
import com.twocoders.movieapp.data.local.dao.DetailsDao
import com.twocoders.movieapp.data.local.dao.PageDao
import com.twocoders.movieapp.data.local.entity.PageEntity
import com.twocoders.movieapp.data.local.mapper.toDomain
import com.twocoders.movieapp.data.local.mapper.toEntity
import com.twocoders.movieapp.data.local.mapper.toMovieDetails
import com.twocoders.movieapp.data.local.mapper.toTvShowDetails
import com.twocoders.movieapp.domain.model.MediaDetails
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.model.TvShowDetails
import java.util.concurrent.TimeUnit

/**
 * [MediaLocalDataSource] backed by [MovieDatabase]. Saving a search page also prunes searches
 * older than 7 days, so the cache can't grow without bound.
 */
class RoomMediaLocalDataSource(
    private val pageDao: PageDao,
    private val detailsDao: DetailsDao,
    private val clock: Clock,
) : MediaLocalDataSource {

    override suspend fun getPage(listKey: String, page: Int): Page<MediaSummary>? =
        pageDao.getPage(listKey, page)?.let { (info, media) ->
            Page(items = media.map { it.toDomain() }, page = info.page, totalPages = info.totalPages)
        }

    override suspend fun savePage(listKey: String, page: Page<MediaSummary>) {
        val now = clock.nowMillis()
        pageDao.replacePage(
            page = PageEntity(listKey = listKey, page = page.page, totalPages = page.totalPages, cachedAt = now),
            media = page.items.map { it.toEntity() },
        )
        // Search results are the only list that keeps growing, one key per query, so they expire.
        // The popular feed is overwritten in place.
        if (listKey.startsWith(CacheKeys.SEARCH_PREFIX)) {
            pageDao.pruneOlderThan(CacheKeys.SEARCH_PREFIX, cutoff = now - SEARCH_TTL_MILLIS)
        }
    }

    override suspend fun getMovieDetails(id: Int): MovieDetails? =
        detailsDao.get(id, MediaType.MOVIE)?.toMovieDetails()

    override suspend fun getTvShowDetails(id: Int): TvShowDetails? =
        detailsDao.get(id, MediaType.TV_SHOW)?.toTvShowDetails()

    override suspend fun saveDetails(details: MediaDetails) {
        detailsDao.upsert(details.toEntity(cachedAt = clock.nowMillis()))
    }

    private companion object {
        val SEARCH_TTL_MILLIS = TimeUnit.DAYS.toMillis(7)
    }
}
