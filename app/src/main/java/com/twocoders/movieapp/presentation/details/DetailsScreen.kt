package com.twocoders.movieapp.presentation.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Minimal wiring of the details screen. The finished UI replaces the body, not the contract. */
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            when (val current = state) {
                DetailsUiState.Loading -> Text("Loading…")
                is DetailsUiState.Error -> {
                    Text("Error: ${current.error}")
                    Button(onClick = viewModel::retry) { Text("Retry") }
                }
                is DetailsUiState.Content -> with(current.details) {
                    Text(title)
                    Text("★ $voteAverage ($voteCount votes)")
                    Text("Directed by: ${credits.directors.joinToString { it.name }}")
                    Text(overview)
                }
            }
        }
    }
}
