package com.twocoders.movieapp.fakes

import com.twocoders.movieapp.core.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow

/** Connectivity the test switches by hand, e.g. `connectivity.isOnline.value = false`. */
class FakeConnectivityObserver(online: Boolean = true) : ConnectivityObserver {
    override val isOnline = MutableStateFlow(online)
}
