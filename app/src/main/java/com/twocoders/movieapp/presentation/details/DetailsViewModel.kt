package com.twocoders.movieapp.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twocoders.movieapp.core.connectivity.ConnectivityObserver
import com.twocoders.movieapp.core.connectivity.reconnections
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.usecase.GetMediaDetailsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Details screen for one movie or TV show.
 *
 * [mediaId] and [mediaType] come from the navigation route through Koin's `parametersOf`,
 * not from `SavedStateHandle`, so the ViewModel stays a plain class that's easy to test.
 * After a failed load, it retries on its own when the device reconnects.
 */
class DetailsViewModel(
    private val mediaId: Int,
    private val mediaType: MediaType,
    private val getMediaDetails: GetMediaDetailsUseCase,
    connectivity: ConnectivityObserver,
) : ViewModel() {

    private val _state = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            connectivity.reconnections().collect {
                if (_state.value is DetailsUiState.Error) retry()
            }
        }
    }

    /** Loads the details again. Ignored while a load is already running. */
    fun retry() {
        if (loadJob?.isActive == true) return
        load()
    }

    private fun load() {
        _state.value = DetailsUiState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = getMediaDetails(mediaId, mediaType)) {
                is DataResult.Success -> DetailsUiState.Content(result.data)
                is DataResult.Failure -> DetailsUiState.Error(result.error)
            }
        }
    }
}
