package com.twocoders.movieapp.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twocoders.movieapp.domain.model.MediaSummary
import com.twocoders.movieapp.domain.model.MediaType

/** Minimal wiring of search. The finished UI replaces the body, not the contract. */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onMediaClick: (MediaSummary) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row {
                MediaType.entries.forEach { type ->
                    FilterChip(
                        selected = state.mediaType == type,
                        onClick = { viewModel.onMediaTypeChange(type) },
                        label = { Text(type.name) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }
            when (val results = state.results) {
                SearchResults.Idle -> Text("Type to search")
                is SearchResults.Content -> LazyColumn(Modifier.fillMaxSize()) {
                    item { Text("Status: ${results.pagination.status}") }
                    items(results.pagination.items, key = { it.id }) { media ->
                        Text(media.title, Modifier.clickable { onMediaClick(media) }.padding(vertical = 12.dp))
                    }
                    item { Button(onClick = viewModel::loadMore) { Text("Load more") } }
                }
            }
        }
    }
}
