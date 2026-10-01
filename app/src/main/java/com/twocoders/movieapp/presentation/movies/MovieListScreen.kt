package com.twocoders.movieapp.presentation.movies

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.presentation.common.PreviewData
import com.twocoders.movieapp.presentation.common.components.EmptyState
import com.twocoders.movieapp.presentation.common.components.PaginatedMediaList
import com.twocoders.movieapp.presentation.common.components.bottomContentPadding
import com.twocoders.movieapp.presentation.common.components.paddingExceptBottom
import com.twocoders.movieapp.presentation.paging.PaginationState
import com.twocoders.movieapp.presentation.paging.PaginationStatus
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme

/** Main screen: popular movies with infinite scroll. Search is in the top bar. */
@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel,
    onMediaClick: (MediaSummary) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favoriteKeys by viewModel.favoriteKeys.collectAsStateWithLifecycle()
    MovieListContent(
        state = state,
        favoriteKeys = favoriteKeys,
        onToggleFavorite = viewModel::toggleFavorite,
        onMediaClick = onMediaClick,
        onSearchClick = onSearchClick,
        onFavoritesClick = onFavoritesClick,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
    )
}

/** Stateless body of [MovieListScreen], kept separate so it can be previewed. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListContent(
    state: PaginationState<MediaSummary>,
    onMediaClick: (MediaSummary) -> Unit,
    onSearchClick: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onFavoritesClick: () -> Unit = {},
    favoriteKeys: Set<MediaKey> = emptySet(),
    onToggleFavorite: (MediaSummary) -> Unit = {},
) {
    // Hides the bar while scrolling down through the feed, and brings it back on any scroll up.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.movie_list_title)) },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Filled.FavoriteBorder, contentDescription = stringResource(R.string.favorites_title))
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.action_search))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        PaginatedMediaList(
            state = state,
            onItemClick = onMediaClick,
            onLoadMore = onLoadMore,
            onRetry = onRetry,
            favoriteKeys = favoriteKeys,
            onToggleFavorite = onToggleFavorite,
            contentPadding = bottomContentPadding(padding),
            modifier = Modifier
                .fillMaxSize()
                .paddingExceptBottom(padding),
            emptyContent = {
                EmptyState(
                    icon = painterResource(R.drawable.ic_movie),
                    title = stringResource(R.string.list_empty_title),
                    message = stringResource(R.string.list_empty_message),
                )
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun MovieListContentPreview() {
    MovieAppTheme {
        MovieListContent(
            state = PaginationState(items = PreviewData.movies, nextPage = 2, status = PaginationStatus.LoadingNextPage),
            onMediaClick = {},
            onSearchClick = {},
            onLoadMore = {},
            onRetry = {},
        )
    }
}
