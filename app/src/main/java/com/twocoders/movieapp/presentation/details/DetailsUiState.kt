package com.twocoders.movieapp.presentation.details

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.model.MediaDetails

/** Everything the details screen can show. There's no partial content: details arrive in one response. */
sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Content(val details: MediaDetails) : DetailsUiState
    data class Error(val error: AppError) : DetailsUiState
}
