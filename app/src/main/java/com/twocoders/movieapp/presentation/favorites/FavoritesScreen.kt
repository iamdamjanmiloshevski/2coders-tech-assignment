package com.twocoders.movieapp.presentation.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.model.Favorite
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.presentation.common.PreviewData
import com.twocoders.movieapp.presentation.common.components.EmptyState
import com.twocoders.movieapp.presentation.common.components.FullScreenLoading
import com.twocoders.movieapp.presentation.common.components.MediaListItem
import com.twocoders.movieapp.presentation.common.components.bottomContentPadding
import com.twocoders.movieapp.presentation.common.components.paddingExceptBottom
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme
import kotlinx.coroutines.launch

/** The user's saved movies and shows. Removing one offers Undo in a snackbar. */
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onMediaClick: (MediaSummary) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // Read once here: string resources can't be read inside the effect below.
    val removedMessage = stringResource(R.string.favorites_removed)
    val undoLabel = stringResource(R.string.action_undo)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FavoritesEvent.Removed -> launch {
                    // A new removal replaces the previous snackbar instead of queueing behind it.
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = removedMessage.format(event.favorite.media.title),
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoRemove(event.favorite)
                }
            }
        }
    }

    FavoritesContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onMediaClick = onMediaClick,
        onRemove = viewModel::remove,
        onBack = onBack,
    )
}

/** Stateless body of [FavoritesScreen], kept separate so it can be previewed. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
    state: FavoritesUiState,
    onMediaClick: (MediaSummary) -> Unit,
    onRemove: (Favorite) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .paddingExceptBottom(padding)
        when (state) {
            FavoritesUiState.Loading -> FullScreenLoading(modifier)
            FavoritesUiState.Empty -> EmptyState(
                icon = rememberVectorPainter(Icons.Filled.FavoriteBorder),
                title = stringResource(R.string.favorites_empty_title),
                message = stringResource(R.string.favorites_empty_message),
                modifier = modifier.padding(bottom = padding.calculateBottomPadding()),
            )
            is FavoritesUiState.Content -> LazyColumn(modifier = modifier, contentPadding = bottomContentPadding(padding)) {
                items(state.favorites, key = { "${it.media.type}-${it.media.id}" }) { favorite ->
                    MediaListItem(
                        media = favorite.media,
                        onClick = { onMediaClick(favorite.media) },
                        isFavorite = true,
                        onToggleFavorite = { onRemove(favorite) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun FavoritesContentPreview() {
    MovieAppTheme {
        FavoritesContent(
            state = FavoritesUiState.Content(PreviewData.movies.take(3).mapIndexed { i, media -> Favorite(media, i.toLong()) }),
            onMediaClick = {},
            onRemove = {},
            onBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun FavoritesEmptyPreview() {
    MovieAppTheme {
        FavoritesContent(state = FavoritesUiState.Empty, onMediaClick = {}, onRemove = {}, onBack = {})
    }
}
