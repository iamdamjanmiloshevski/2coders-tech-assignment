package com.twocoders.movieapp.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.presentation.common.PreviewData
import com.twocoders.movieapp.presentation.common.iconRes
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme

/**
 * One media card: the poster, then the title, a meta line (rating, year) and a short overview.
 *
 * The card gets a soft shadow plus a hairline border. Shadows barely show on dark surfaces,
 * so the border is what separates the cards in the dark theme.
 */
@Composable
fun MediaListItem(
    media: MediaSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
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
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
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
