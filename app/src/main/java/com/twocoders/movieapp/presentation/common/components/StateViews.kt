package com.twocoders.movieapp.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.presentation.common.iconRes
import com.twocoders.movieapp.presentation.common.toMessage
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme

/*
 * Full-screen placeholders for when there's no content to show yet: loading, error and empty.
 * They share one layout so every screen fails, waits and comes up empty in the same way.
 */

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FullScreenError(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MessageLayout(
        icon = error.iconRes()?.let { painterResource(it) } ?: rememberVectorPainter(Icons.Filled.Warning),
        title = null,
        message = error.toMessage(),
        modifier = modifier,
    ) {
        FilledTonalButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
fun EmptyState(
    icon: Painter,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    MessageLayout(icon = icon, title = title, message = message, modifier = modifier)
}

@Composable
private fun MessageLayout(
    icon: Painter,
    title: String?,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        if (title != null) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Box(Modifier.padding(top = 8.dp)) { action() }
        }
    }
}

@PreviewLightDark
@Composable
private fun FullScreenErrorPreview() {
    MovieAppTheme {
        Surface { FullScreenError(error = AppError.NoConnection, onRetry = {}) }
    }
}
