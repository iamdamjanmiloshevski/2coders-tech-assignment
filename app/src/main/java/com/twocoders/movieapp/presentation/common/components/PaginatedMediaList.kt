package com.twocoders.movieapp.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.presentation.paging.PaginationState
import com.twocoders.movieapp.presentation.paging.PaginationStatus
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** Ask for the next page when the user is this many rows from the end, so it usually arrives before they get there. */
private const val PREFETCH_DISTANCE = 5

/**
 * Renders a [PaginationState] of media: full-screen states for the first page, then a list
 * with infinite scroll and a status footer. Shared by the movie feed and search results.
 *
 * @param emptyContent shown when loading finished with no items. Each screen explains "empty" differently.
 */
@Composable
fun PaginatedMediaList(
    state: PaginationState<MediaSummary>,
    onItemClick: (MediaSummary) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    emptyContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
) {
    val status = state.status
    when {
        status == PaginationStatus.LoadingFirstPage ||
            (status == PaginationStatus.Idle && state.items.isEmpty()) -> FullScreenLoading(modifier)

        status is PaginationStatus.Error && status.isFirstPage ->
            FullScreenError(error = status.error, onRetry = onRetry, modifier = modifier)

        state.isEmpty -> Box(modifier) { emptyContent() }

        else -> {
            LoadMoreWhenNearEnd(listState, onLoadMore)
            LazyColumn(state = listState, contentPadding = contentPadding, modifier = modifier) {
                items(items = state.items, key = { it.id }, contentType = { "media" }) { media ->
                    MediaListItem(
                        media = media,
                        onClick = { onItemClick(media) },
                        modifier = Modifier.animateItem(),
                    )
                }
                item(key = "footer", contentType = "footer") {
                    ListFooter(status = status, onRetry = onRetry)
                }
            }
        }
    }
}

/**
 * Calls [onLoadMore] when the last visible row is within [PREFETCH_DISTANCE] of the end.
 *
 * It emits the item count rather than a boolean, so a short page that still leaves the user
 * near the end triggers another load. `Paginator` ignores the call while a load runs or after
 * an error, so this can fire freely.
 */
@Composable
private fun LoadMoreWhenNearEnd(listState: LazyListState, onLoadMore: () -> Unit) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: return@snapshotFlow -1
            if (lastVisible >= layout.totalItemsCount - PREFETCH_DISTANCE) layout.totalItemsCount else -1
        }
            .distinctUntilChanged()
            .filter { it >= 0 }
            .collect { currentOnLoadMore() }
    }
}

@Composable
private fun ListFooter(status: PaginationStatus, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (status) {
            PaginationStatus.LoadingNextPage -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)

            is PaginationStatus.Error -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(
                        if (status.error == AppError.NoConnection) R.string.list_load_more_offline else R.string.list_load_more_failed,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            }

            PaginationStatus.EndReached -> Text(
                text = stringResource(R.string.list_end),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )

            // Idle near the end triggers the next load right away, so there's nothing to show.
            PaginationStatus.Idle, PaginationStatus.LoadingFirstPage -> Unit
        }
    }
}
