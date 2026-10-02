package com.twocoders.movieapp.presentation.details

import com.twocoders.movieapp.domain.error.AppError
import com.twocoders.movieapp.domain.model.MediaDetails

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Content(val details: MediaDetails) : DetailsUiState
    data class Error(val error: AppError) : DetailsUiState
}
