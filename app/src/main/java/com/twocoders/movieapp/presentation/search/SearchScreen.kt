package com.twocoders.movieapp.presentation.search

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.presentation.common.PreviewData
import com.twocoders.movieapp.presentation.common.components.EmptyState
import com.twocoders.movieapp.presentation.common.components.PaginatedMediaList
import com.twocoders.movieapp.presentation.common.components.bottomContentPadding
import com.twocoders.movieapp.presentation.common.components.paddingExceptBottom
import com.twocoders.movieapp.presentation.paging.PaginationState
import com.twocoders.movieapp.presentation.paging.PaginationStatus
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme

/** Search over movies or TV series. The user picks the type before or while typing. */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onMediaClick: (MediaSummary) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onMediaTypeChange = viewModel::onMediaTypeChange,
        onMediaClick = onMediaClick,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
        onBack = onBack,
    )
}

/** Stateless body of [SearchScreen], kept separate so it can be previewed. */
@Composable
fun SearchContent(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onMediaTypeChange: (MediaType) -> Unit,
    onMediaClick: (MediaSummary) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val hideKeyboardOnScroll = remember(keyboard) { HideKeyboardOnScroll(keyboard) }

    Scaffold(
        // With the IME included, the bottom padding is the keyboard while it's open and the
        // navigation bar otherwise, so results and empty states never end up behind the keyboard.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
        topBar = {
            SearchTopBar(
                query = state.query,
                onQueryChange = onQueryChange,
                onSearch = { keyboard?.hide() },
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .paddingExceptBottom(padding),
        ) {
            MediaTypeSelector(
                selected = state.mediaType,
                onSelect = onMediaTypeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when (val results = state.results) {
                SearchResults.Idle -> EmptyState(
                    icon = rememberVectorPainter(Icons.Filled.Search),
                    title = stringResource(R.string.search_idle_title),
                    message = stringResource(R.string.search_idle_message),
                    modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                )

                is SearchResults.Content -> PaginatedMediaList(
                    state = results.pagination,
                    onItemClick = onMediaClick,
                    onLoadMore = onLoadMore,
                    onRetry = onRetry,
                    contentPadding = bottomContentPadding(padding),
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(hideKeyboardOnScroll),
                    emptyContent = {
                        EmptyState(
                            icon = painterResource(R.drawable.ic_movie),
                            title = stringResource(R.string.search_empty_title),
                            message = stringResource(R.string.search_empty_message, state.query.trim()),
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    // Focus (and open the keyboard) only on first entry. Coming back from details shouldn't pop it up again.
    var autoFocused by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!autoFocused) {
            focusRequester.requestFocus()
            autoFocused = true
        }
    }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
        },
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                // Borderless, so the field reads as the bar's title rather than as a form input.
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        },
    )
}

@Composable
private fun MediaTypeSelector(
    selected: MediaType,
    onSelect: (MediaType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val types = MediaType.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = type == selected,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                label = { Text(stringResource(type.selectorLabel())) },
            )
        }
    }
}

@StringRes
private fun MediaType.selectorLabel(): Int = when (this) {
    MediaType.MOVIE -> R.string.search_type_movies
    MediaType.TV_SHOW -> R.string.search_type_tv
}

/** Hides the keyboard once the user starts scrolling the results, so it stops covering half of them. */
private class HideKeyboardOnScroll(private val keyboard: SoftwareKeyboardController?) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (source == NestedScrollSource.UserInput && available.y != 0f) keyboard?.hide()
        return Offset.Zero
    }
}

@PreviewLightDark
@Composable
private fun SearchContentPreview() {
    MovieAppTheme {
        SearchContent(
            state = SearchUiState(
                query = "dune",
                results = SearchResults.Content(
                    PaginationState(items = PreviewData.movies.take(3), status = PaginationStatus.EndReached),
                ),
            ),
            onQueryChange = {},
            onMediaTypeChange = {},
            onMediaClick = {},
            onLoadMore = {},
            onRetry = {},
            onBack = {},
        )
    }
}
