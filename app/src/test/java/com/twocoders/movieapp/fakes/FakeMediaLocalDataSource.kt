package com.twocoders.movieapp.fakes

import com.twocoders.movieapp.data.local.MediaLocalDataSource
import com.twocoders.movieapp.domain.model.MediaDetails
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Page
import com.twocoders.movieapp.domain.model.TvShowDetails

/** In-memory cache with the same contract as the Room one. Tests read the maps to check what was saved. */
class FakeMediaLocalDataSource : MediaLocalDataSource {
    val pages = mutableMapOf<Pair<String, Int>, Page<MediaSummary>>()
    val movieDetails = mutableMapOf<Int, MovieDetails>()
    val tvShowDetails = mutableMapOf<Int, TvShowDetails>()

    override suspend fun getPage(listKey: String, page: Int) = pages[listKey to page]

    override suspend fun savePage(listKey: String, page: Page<MediaSummary>) {
        pages[listKey to page.page] = page
    }

    override suspend fun getMovieDetails(id: Int) = movieDetails[id]

    override suspend fun getTvShowDetails(id: Int) = tvShowDetails[id]

    override suspend fun saveDetails(details: MediaDetails) {
        when (details) {
            is MovieDetails -> movieDetails[details.id] = details
            is TvShowDetails -> tvShowDetails[details.id] = details
        }
    }
}
