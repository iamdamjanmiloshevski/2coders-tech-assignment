package com.twocoders.movieapp.presentation.connectivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twocoders.movieapp.core.connectivity.ConnectivityObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest

/**
 * App-wide connectivity UI state.
 *
 * The offline banner is a notice, not a status bar. It appears when the connection drops, stays
 * for [BANNER_DURATION_MS], then gets out of the way while cached content keeps working. Coming
 * back online hides it at once. `transformLatest` cancels the pending timer on every change, so
 * flapping connections can't leave a stale banner behind.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityViewModel(
    connectivity: ConnectivityObserver,
) : ViewModel() {

    val showOfflineBanner: StateFlow<Boolean> = connectivity.isOnline
        .transformLatest { online ->
            if (online) {
                emit(false)
            } else {
                emit(true)
                delay(BANNER_DURATION_MS)
                emit(false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = false)

    companion object {
        const val BANNER_DURATION_MS = 5_500L
    }
}
