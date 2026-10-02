package com.twocoders.movieapp.presentation.details

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.model.MediaDetails
import com.twocoders.movieapp.domain.model.MovieDetails
import com.twocoders.movieapp.domain.model.Person
import com.twocoders.movieapp.domain.model.TvShowDetails
import com.twocoders.movieapp.presentation.common.PreviewData
import com.twocoders.movieapp.presentation.common.components.FavoriteButton
import com.twocoders.movieapp.presentation.common.components.FullScreenError
import com.twocoders.movieapp.presentation.common.components.FullScreenLoading
import com.twocoders.movieapp.presentation.common.components.PosterImage
import com.twocoders.movieapp.presentation.common.formatCompactCount
import com.twocoders.movieapp.presentation.common.formatRating
import com.twocoders.movieapp.presentation.common.formatRuntime
import com.twocoders.movieapp.presentation.common.formatUsd
import com.twocoders.movieapp.presentation.common.iconRes
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme

/** How far the poster rises into the backdrop. */
private val PosterOverlap = 72.dp

private val TopBarHeight = 64.dp

/** Details of one movie or TV show. Collects [viewModel] state and renders [DetailsContent]. */
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    DetailsContent(
        state = state,
        isFavorite = isFavorite,
        onBack = onBack,
        onRetry = viewModel::retry,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

/**
 * Stateless body of [DetailsScreen].
 *
 * The top bar is transparent while the backdrop is visible, then turns solid and shows the
 * title once the backdrop scrolls away. Status bar icons switch between light and dark to stay legible.
 */
@Composable
fun DetailsContent(
    state: DetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    Surface(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints {
            val backdropHeight = maxWidth * 9f / 16f
            val density = LocalDensity.current
            val statusBarHeightPx = WindowInsets.statusBars.getTop(density)
            // Collapse when the backdrop's bottom edge reaches the bottom of the top bar.
            val collapseThresholdPx = with(density) { (backdropHeight - TopBarHeight).toPx() } - statusBarHeightPx
            val overBackdrop by remember(state, collapseThresholdPx) {
                derivedStateOf { state is DetailsUiState.Content && scrollState.value < collapseThresholdPx }
            }

            when (state) {
                DetailsUiState.Loading -> FullScreenLoading()
                is DetailsUiState.Error -> FullScreenError(
                    error = state.error,
                    onRetry = onRetry,
                    modifier = Modifier.systemBarsPadding(),
                )
                is DetailsUiState.Content -> DetailsBody(state.details, scrollState, backdropHeight)
            }
            DetailsTopBar(
                title = (state as? DetailsUiState.Content)?.details?.title,
                solid = !overBackdrop,
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
            )
            StatusBarIconsEffect(darkIcons = !overBackdrop && !isSystemInDarkTheme())
        }
    }
}

@Composable
private fun DetailsBody(details: MediaDetails, scrollState: ScrollState, backdropHeight: Dp) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Header(details, backdropHeight)
        RatingAndGenres(details)
        Section(title = stringResource(R.string.details_overview)) {
            Text(
                text = details.overview.ifBlank { stringResource(R.string.details_no_overview) },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Crew(details)
        if (details.credits.cast.isNotEmpty()) {
            Section(title = stringResource(R.string.details_cast), contentPadding = PaddingValues(0.dp)) {
                CastRow(details.credits.cast)
            }
        }
        Facts(details)
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun DetailsTopBar(
    title: String?,
    solid: Boolean,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    val backgroundAlpha by animateFloatAsState(targetValue = if (solid) 1f else 0f, label = "topBarBackground")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = backgroundAlpha))
            .statusBarsPadding()
            .height(TopBarHeight)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onClick = onBack)
        val titleAlpha by animateFloatAsState(targetValue = if (solid && title != null) 1f else 0f, label = "topBarTitle")
        if (titleAlpha > 0f) {
            Text(
                text = title.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .graphicsLayer { alpha = titleAlpha },
            )
        } else {
            // Keeps the heart at the end of the bar while the title is hidden.
            Spacer(Modifier.weight(1f))
        }
        // Only once the details have loaded: they're what gets saved as the favorite.
        if (title != null) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)) {
                FavoriteButton(
                    isFavorite = isFavorite,
                    onToggle = onToggleFavorite,
                    offTint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Sets the status bar icon colour while this composable is shown, and restores the previous colour when it leaves. */
@Composable
private fun StatusBarIconsEffect(darkIcons: Boolean) {
    val window = LocalActivity.current?.window ?: return
    val view = LocalView.current
    DisposableEffect(window, darkIcons) {
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = darkIcons
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}

/** A 16:9 backdrop that fades into the background, with the poster and title overlapping its bottom edge. */
@Composable
private fun Header(details: MediaDetails, backdropHeight: Dp) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(backdropHeight),
        ) {
            PosterImage(
                url = details.backdropUrl,
                shape = RectangleShape,
                fallbackIcon = details.type.iconRes(),
                modifier = Modifier.fillMaxSize(),
            )
            // Top scrim keeps the status bar icons and back button legible over bright images.
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent))),
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface))),
            )
        }
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = backdropHeight - PosterOverlap),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PosterImage(
                url = details.posterUrl,
                fallbackIcon = details.type.iconRes(),
                modifier = Modifier
                    .size(width = 112.dp, height = 168.dp)
                    .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.medium),
            )
            TitleBlock(details, Modifier.padding(top = PosterOverlap + 8.dp))
        }
    }
}

@Composable
private fun TitleBlock(details: MediaDetails, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = details.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        details.tagline?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val meta = listOfNotNull(details.releaseYear?.toString(), lengthLabel(details))
        if (meta.isNotEmpty()) {
            Text(
                text = meta.joinToString("  ·  "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "2h 47m" for movies, "4 seasons" for TV shows. */
@Composable
private fun lengthLabel(details: MediaDetails): String? = when (details) {
    is MovieDetails -> details.runtimeMinutes?.let(::formatRuntime)
    is TvShowDetails -> details.numberOfSeasons.takeIf { it > 0 }?.let {
        pluralStringResource(R.plurals.details_seasons_count, it, it)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RatingAndGenres(details: MediaDetails) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        formatRating(details.voteAverage)?.let { rating ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = rating,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp),
                )
                Text(
                    text = stringResource(R.string.details_out_of_ten),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.details_votes, formatCompactCount(details.voteCount.toLong())),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (details.genres.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                details.genres.forEach { genre ->
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            text = genre.name,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Directors for movies or creators for TV shows, then writers. The data layer has already grouped them. */
@Composable
private fun Crew(details: MediaDetails) {
    val leadLabel = when (details) {
        is MovieDetails -> R.string.details_directed_by
        is TvShowDetails -> R.string.details_created_by
    }
    val groups = listOf(
        stringResource(leadLabel) to details.credits.directors,
        stringResource(R.string.details_written_by) to details.credits.writers,
    ).filter { (_, people) -> people.isNotEmpty() }
    if (groups.isEmpty()) return

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        groups.forEach { (label, people) ->
            LabeledValue(label = label, value = people.joinToString { it.name })
        }
    }
}

@Composable
private fun CastRow(cast: List<Person>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(cast, key = { it.id }) { person ->
            Column(
                modifier = Modifier.width(88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CastAvatar(person)
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (person.role.isNotBlank()) {
                    Text(
                        text = person.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** A round profile photo over the person's initials, which show while loading and when there's no photo. */
@Composable
private fun CastAvatar(person: Person) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = person.name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (person.profileUrl != null) {
            AsyncImage(
                model = person.profileUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** The movie- or show-specific extras. The sealed type makes this `when` exhaustive. */
@Composable
private fun Facts(details: MediaDetails) {
    val facts = buildList {
        details.status?.let { add(stringResource(R.string.details_status) to it) }
        when (details) {
            is MovieDetails -> {
                details.runtimeMinutes?.let { add(stringResource(R.string.details_runtime) to formatRuntime(it)) }
                details.budget?.let { add(stringResource(R.string.details_budget) to formatUsd(it)) }
                details.revenue?.let { add(stringResource(R.string.details_revenue) to formatUsd(it)) }
            }
            is TvShowDetails -> {
                add(stringResource(R.string.details_seasons) to details.numberOfSeasons.toString())
                add(stringResource(R.string.details_episodes) to details.numberOfEpisodes.toString())
            }
        }
    }
    if (facts.isEmpty()) return

    Section(title = stringResource(R.string.details_facts)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            facts.forEach { (label, value) -> LabeledValue(label, value) }
        }
    }
}

@Composable
private fun Section(
    title: String,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { heading() },
        )
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    // The translucent backing keeps the button visible over any backdrop, and blends into the solid bar.
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        modifier = modifier,
    ) {
        IconButton(onClick = onClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
        }
    }
}

@PreviewLightDark
@Composable
private fun DetailsContentPreview() {
    MovieAppTheme {
        DetailsContent(state = DetailsUiState.Content(PreviewData.movieDetails), onBack = {}, onRetry = {})
    }
}
