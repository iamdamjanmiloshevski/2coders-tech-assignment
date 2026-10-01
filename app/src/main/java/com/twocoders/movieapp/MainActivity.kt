package com.twocoders.movieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.twocoders.movieapp.presentation.app.MovieAppRoot
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme

/** The only Activity. Everything it shows is in [MovieAppRoot]: the navigation graph and app-wide overlays. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate. It swaps the launch theme for the app theme once the first frame is ready.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Edge-to-edge is enforced from targetSdk 35. This call gives older Android versions the same
        // transparent system bars, with icon colours that follow the theme.
        enableEdgeToEdge()
        setContent {
            MovieAppTheme {
                MovieAppRoot()
            }
        }
    }
}
