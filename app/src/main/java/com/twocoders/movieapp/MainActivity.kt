package com.twocoders.movieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.twocoders.movieapp.presentation.navigation.AppNavHost
import com.twocoders.movieapp.presentation.ui.theme.MovieAppTheme

/** The only Activity. Every screen is a Compose destination in [AppNavHost]. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieAppTheme {
                AppNavHost()
            }
        }
    }
}
