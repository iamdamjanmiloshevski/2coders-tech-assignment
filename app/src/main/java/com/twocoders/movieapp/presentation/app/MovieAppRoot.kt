package com.twocoders.movieapp.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twocoders.movieapp.presentation.connectivity.ConnectivityViewModel
import com.twocoders.movieapp.presentation.connectivity.OfflineBanner
import com.twocoders.movieapp.presentation.navigation.AppNavHost
import org.koin.compose.viewmodel.koinViewModel

/** Everything MainActivity shows: the navigation graph, plus app-wide overlays such as the offline banner. */
@Composable
fun MovieAppRoot(connectivityViewModel: ConnectivityViewModel = koinViewModel()) {
    val showOfflineBanner by connectivityViewModel.showOfflineBanner.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        AppNavHost()
        OfflineBanner(
            visible = showOfflineBanner,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // Above the navigation bar, or above the keyboard while it's open.
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(16.dp),
        )
    }
}
