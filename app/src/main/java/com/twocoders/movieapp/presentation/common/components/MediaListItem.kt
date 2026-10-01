package com.twocoders.movieapp.presentation.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

/** One row in a media list: the poster, then the title, a meta line (rating, year) and a short overview. */
@Composable
fun MediaListItem(
    media: MediaSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PosterImage(
            url = media.posterUrl,
            fallbackIcon = media.type.iconRes(),
            modifier = Modifier.size(width = 92.dp, height = 138.dp),
        )
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
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
