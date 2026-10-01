package com.twocoders.movieapp.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.presentation.common.PreviewData
import com.twocoders.movieapp.presentation.common.iconRes
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme

private val FloatingElevation = 16.dp
private val PressedElevation = 4.dp

/**
 * One floating media card: the poster, then the title, a meta line (rating, year) and a short overview.
 * With [onToggleFavorite] set, a heart next to the title adds or removes the title from favorites.
 *
 * The card floats on a wide, soft shadow over a lighter surface than the page. When pressed,
 * it settles toward the page (smaller shadow, slight scale-down). Shadows barely show on dark
 * surfaces, so in the dark theme a raised tone and a faint border do the lifting instead.
 */
@Composable
fun MediaListItem(
    media: MediaSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f
    val shape = MaterialTheme.shapes.large

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(if (pressed) PressedElevation else FloatingElevation, label = "cardElevation")
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "cardScale")

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            // A large elevation spreads the shadow wide and soft. The platform already makes shadows
            // translucent (it multiplies the colour's alpha by the theme's shadow alpha), so the
            // colours stay at the opaque default. A lower alpha here would hide the shadow almost completely.
            .shadow(elevation = elevation, shape = shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) colors.surfaceContainerHigh else colors.surfaceContainerLowest,
        ),
        border = if (isDark) BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)) else null,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PosterImage(
                url = media.posterUrl,
                fallbackIcon = media.type.iconRes(),
                // A smaller radius than the card's, so the corners stay concentric inside the 12dp padding.
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(width = 88.dp, height = 132.dp),
            )
            Column(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (onToggleFavorite != null) {
                        FavoriteButton(
                            isFavorite = isFavorite,
                            onToggle = onToggleFavorite,
                            // Pulled into the card's padding, so the 48dp touch target doesn't push the title down.
                            modifier = Modifier.offset(x = 8.dp, y = (-10).dp),
                        )
                    }
                }
                MetaLine(media)
                if (media.overview.isNotBlank()) {
                    Text(
                        text = media.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaLine(media: MediaSummary) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RatingBadge(voteAverage = media.voteAverage)
        media.releaseYear?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun MediaListItemPreview() {
    MovieAppTheme {
        Surface {
            Column {
                MediaListItem(media = PreviewData.movie, onClick = {})
                MediaListItem(
                    media = PreviewData.movie.copy(id = 2, type = MediaType.TV_SHOW, posterUrl = null, voteAverage = 0.0),
                    onClick = {},
                )
            }
        }
    }
}
