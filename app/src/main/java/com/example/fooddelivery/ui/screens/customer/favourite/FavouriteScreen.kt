package com.example.fooddelivery.ui.screens.customer.favourite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.customer.home.components.RestaurantItem
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    viewModel: FavouriteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(FavouriteEvent.ErrorDismissed)
        }
    }

    FavouriteContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToRestaurant = onNavigateToRestaurant,
        onToggleFavorite = { restaurantId ->
            viewModel.onEvent(FavouriteEvent.ToggleFavourite(restaurantId))
        },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteContent(
    state: FavouriteState,
    onNavigateBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Favourite Restaurants",
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading && state.favouriteRestaurants.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (state.favouriteRestaurants.isEmpty() && !state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No favourites yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your liked restaurants will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(
                        items = state.favouriteRestaurants,
                        key = { it.id }
                    ) { restaurant ->
                        RestaurantItem(
                            restaurant = restaurant,
                            onClick = { onNavigateToRestaurant(restaurant.id) },
                            onFavoriteClick = { onToggleFavorite(restaurant.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavouriteScreenPreview() {
    DFoodTheme(darkTheme = false) {
        FavouriteContent(
            state = FavouriteState(),
            onNavigateBack = {},
            onNavigateToRestaurant = {},
            onToggleFavorite = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
