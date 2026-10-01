package com.twocoders.movieapp.presentation.movies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twocoders.movieapp.domain.model.MediaSummary

/** Minimal wiring of the movie list. The finished UI replaces the body, not the contract. */
@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel,
    onMediaClick: (MediaSummary) -> Unit,
    onSearchClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(Modifier.padding(padding)) {
            Button(onClick = onSearchClick, modifier = Modifier.padding(16.dp)) { Text("Search") }
            Text("Status: ${state.status}", modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.items, key = { it.id }) { movie ->
                    Text(
                        text = movie.title,
                        modifier = Modifier.clickable { onMediaClick(movie) }.padding(16.dp),
                    )
                }
                item { Button(onClick = viewModel::loadMore, modifier = Modifier.padding(16.dp)) { Text("Load more") } }
            }
        }
    }
}
