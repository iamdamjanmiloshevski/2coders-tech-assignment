package com.twocoders.movieapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.twocoders.movieapp.presentation.details.DetailsScreen
import com.twocoders.movieapp.presentation.details.DetailsViewModel
import com.twocoders.movieapp.presentation.favorites.FavoritesScreen
import com.twocoders.movieapp.presentation.movies.MovieListScreen
import com.twocoders.movieapp.presentation.search.SearchScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The app's single navigation graph. Screens never see the NavController. They expose
 * navigation as callbacks, which keeps them previewable and independent of each other.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MovieListRoute, modifier = modifier) {
        composable<MovieListRoute> {
            MovieListScreen(
                viewModel = koinViewModel(),
                onMediaClick = { navController.navigate(DetailsRoute(it.id, it.type)) },
                onSearchClick = { navController.navigate(SearchRoute) },
                onFavoritesClick = { navController.navigate(FavoritesRoute) },
            )
        }
        composable<FavoritesRoute> {
            FavoritesScreen(
                viewModel = koinViewModel(),
                onMediaClick = { navController.navigate(DetailsRoute(it.id, it.type)) },
                onBack = navController::navigateUp,
            )
        }
        composable<SearchRoute> {
            SearchScreen(
                viewModel = koinViewModel(),
                onMediaClick = { navController.navigate(DetailsRoute(it.id, it.type)) },
                onBack = navController::navigateUp,
            )
        }
        composable<DetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailsRoute>()
            DetailsScreen(
                viewModel = koinViewModel<DetailsViewModel> { parametersOf(route.id, route.type) },
                onBack = navController::navigateUp,
            )
        }
    }
}
