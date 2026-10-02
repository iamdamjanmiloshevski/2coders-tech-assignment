package com.twocoders.movieapp.core.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Whether the device can currently reach the internet. Behind an interface so ViewModels can be
 * tested with a fake instead of `ConnectivityManager`.
 */
interface ConnectivityObserver {
    /** Emits the current state first, then every change. Never emits the same value twice in a row. */
    val isOnline: Flow<Boolean>
}

/**
 * Emits once every time the device goes from offline back to online. The initial state isn't a
 * reconnection, so nothing is emitted for it, even when the app starts online.
 */
fun ConnectivityObserver.reconnections(): Flow<Unit> = flow {
    var wasOnline: Boolean? = null
    isOnline.collect { online ->
        if (wasOnline == false && online) emit(Unit)
        wasOnline = online
    }
}
