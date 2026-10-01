package com.twocoders.movieapp.presentation.common.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection

/*
 * Edge-to-edge lists: a Scaffold's padding is applied on every side except the bottom, which
 * goes to the list's contentPadding instead. Rows then scroll behind the navigation bar, and
 * the last row still ends above it.
 */

/** Applies all of [padding] except the bottom, which belongs in [bottomContentPadding]. */
@Composable
fun Modifier.paddingExceptBottom(padding: PaddingValues): Modifier {
    val direction = LocalLayoutDirection.current
    return this.padding(
        start = padding.calculateStartPadding(direction),
        top = padding.calculateTopPadding(),
        end = padding.calculateEndPadding(direction),
    )
}

fun bottomContentPadding(padding: PaddingValues) = PaddingValues(bottom = padding.calculateBottomPadding())
