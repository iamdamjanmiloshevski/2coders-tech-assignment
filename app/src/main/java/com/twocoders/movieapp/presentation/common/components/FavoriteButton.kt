package com.twocoders.movieapp.presentation.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.twocoders.movieapp.R

/**
 * The heart that adds a title to favorites or removes it. It's a toggle button, so screen
 * readers announce its on/off state as well as the action.
 *
 * Turning it on gives a short "pop". Only an actual change animates, not a favorite that scrolls into view.
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    offTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val scale = remember { Animatable(1f) }
    var wasFavorite by remember { mutableStateOf(isFavorite) }
    LaunchedEffect(isFavorite) {
        if (isFavorite && !wasFavorite) {
            scale.animateTo(1.3f, tween(durationMillis = 100))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        wasFavorite = isFavorite
    }

    IconToggleButton(checked = isFavorite, onCheckedChange = { onToggle() }, modifier = modifier) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(if (isFavorite) R.string.favorite_remove else R.string.favorite_add),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else offTint,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        )
    }
}
