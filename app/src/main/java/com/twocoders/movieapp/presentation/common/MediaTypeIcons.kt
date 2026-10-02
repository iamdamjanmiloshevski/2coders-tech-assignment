package com.twocoders.movieapp.presentation.common

import androidx.annotation.DrawableRes
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.model.MediaType

/** Placeholder icon for a title without artwork. */
@DrawableRes
fun MediaType.iconRes(): Int = when (this) {
    MediaType.MOVIE -> R.drawable.ic_movie
    MediaType.TV_SHOW -> R.drawable.ic_tv
}
