package com.twocoders.movieapp.di

import com.twocoders.movieapp.domain.model.MediaType
import com.twocoders.movieapp.presentation.details.DetailsViewModel
import com.twocoders.movieapp.presentation.movies.MovieListViewModel
import com.twocoders.movieapp.presentation.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::MovieListViewModel)
    viewModelOf(::SearchViewModel)
    // Route arguments are passed at the call site: koinViewModel { parametersOf(id, type) }.
    viewModel { (mediaId: Int, mediaType: MediaType) -> DetailsViewModel(mediaId, mediaType, get()) }
}
