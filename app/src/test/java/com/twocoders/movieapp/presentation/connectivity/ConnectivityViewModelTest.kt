package com.twocoders.movieapp.presentation.connectivity

import com.twocoders.movieapp.fakes.FakeConnectivityObserver
import com.twocoders.movieapp.presentation.connectivity.ConnectivityViewModel.Companion.BANNER_DURATION_MS
import com.twocoders.movieapp.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConnectivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val connectivity = FakeConnectivityObserver(online = true)
    private val viewModel by lazy { ConnectivityViewModel(connectivity) }

    /** The banner flow is WhileSubscribed, so a collector stands in for the UI. */
    private fun TestScope.observeBanner() {
        viewModel.showOfflineBanner.launchIn(backgroundScope)
        runCurrent()
    }

    @Test
    fun `no banner while online`() = runTest {
        observeBanner()

        assertFalse(viewModel.showOfflineBanner.value)
    }

    @Test
    fun `banner shows when the connection drops and hides by itself`() = runTest {
        observeBanner()

        connectivity.isOnline.value = false
        runCurrent()
        assertTrue(viewModel.showOfflineBanner.value)

        advanceTimeBy(BANNER_DURATION_MS - 1)
        assertTrue("still visible just before the timeout", viewModel.showOfflineBanner.value)

        advanceTimeBy(2)
        assertFalse(viewModel.showOfflineBanner.value)
    }

    @Test
    fun `reconnecting hides the banner immediately`() = runTest {
        observeBanner()
        connectivity.isOnline.value = false
        runCurrent()

        connectivity.isOnline.value = true
        runCurrent()

        assertFalse(viewModel.showOfflineBanner.value)
    }

    @Test
    fun `every new drop shows the banner again`() = runTest {
        observeBanner()
        connectivity.isOnline.value = false
        advanceTimeBy(BANNER_DURATION_MS + 1)
        connectivity.isOnline.value = true
        runCurrent()

        connectivity.isOnline.value = false
        runCurrent()

        assertTrue(viewModel.showOfflineBanner.value)
    }
}
