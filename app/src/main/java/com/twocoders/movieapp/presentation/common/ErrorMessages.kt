package com.twocoders.movieapp.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.twocoders.movieapp.R
import com.twocoders.movieapp.domain.error.AppError

/**
 * The message shown to users for each [AppError]: short, accurate and actionable.
 * Technical details (status codes, stack traces) are logged by the data layer and never shown.
 */
@Composable
fun AppError.toMessage(): String = when (this) {
    AppError.NoConnection -> stringResource(R.string.error_no_connection)
    is AppError.Unauthorized -> stringResource(R.string.error_unauthorized)
    is AppError.NotFound -> stringResource(R.string.error_not_found)
    is AppError.Http -> stringResource(R.string.error_http_code, code)
    AppError.Parsing, AppError.Unknown -> stringResource(R.string.error_generic)
}

/** The offline error gets its own icon, so users can tell at a glance that it's their connection. */
@DrawableRes
fun AppError.iconRes(): Int? = if (this == AppError.NoConnection) R.drawable.ic_cloud_off else null
