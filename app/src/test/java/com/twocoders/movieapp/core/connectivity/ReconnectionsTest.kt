package com.twocoders.movieapp.core.connectivity

import app.cash.turbine.test
import com.twocoders.movieapp.fakes.FakeConnectivityObserver
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ReconnectionsTest {

    @Test
    fun `starting online is not a reconnection, going offline and back is`() = runTest {
        val connectivity = FakeConnectivityObserver(online = true)

        connectivity.reconnections().test {
            expectNoEvents()

            connectivity.isOnline.value = false
            expectNoEvents()

            connectivity.isOnline.value = true
            awaitItem()
        }
    }

    @Test
    fun `starting offline then coming online counts as a reconnection`() = runTest {
        val connectivity = FakeConnectivityObserver(online = false)

        connectivity.reconnections().test {
            connectivity.isOnline.value = true
            awaitItem()
        }
    }
}
