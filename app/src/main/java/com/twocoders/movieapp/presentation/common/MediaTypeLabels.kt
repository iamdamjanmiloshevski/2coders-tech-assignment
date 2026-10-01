package com.twocoders.movieapp.presentation.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.model.MediaType

@StringRes
fun MediaType.labelRes(): Int = when (this) {
    MediaType.MOVIE -> R.string.media_type_movie
    MediaType.TV_SHOW -> R.string.media_type_tv
}

@DrawableRes
fun MediaType.iconRes(): Int = when (this) {
    MediaType.MOVIE -> R.drawable.ic_movie
    MediaType.TV_SHOW -> R.drawable.ic_tv
}
