package com.twocoders.movieapp.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twocoders.movieapp.core.connectivity.ConnectivityObserver
import com.twocoders.movieapp.core.connectivity.reconnections
import com.twocoders.movieapp.domain.error.DataResult
import com.twocoders.movieapp.domain.model.MediaKey
import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.domain.model.key
import com.twocoders.movieapp.domain.model.toSummary
import com.twocoders.movieapp.domain.usecase.GetMediaDetailsUseCase
import com.twocoders.movieapp.domain.usecase.ObserveFavoritesUseCase
import com.twocoders.movieapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()

    /** Whether this title is a favorite. Kept separate from [state], because it changes independently of loading. */
    val isFavorite: StateFlow<Boolean> = observeFavorites()
        .map { favorites -> favorites.any { it.media.key == MediaKey(mediaId, mediaType) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            connectivity.reconnections().collect {
                if (_state.value is DetailsUiState.Error) retry()
            }
        }
    }

    /** Adds or removes this title from favorites. Only possible once the details have loaded, since they provide the snapshot. */
    fun toggleFavorite() {
        val details = (_state.value as? DetailsUiState.Content)?.details ?: return
        viewModelScope.launch { toggleFavorite(details.toSummary()) }
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
