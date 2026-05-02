package com.example.fooddelivery.ui.screens.home.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.ui.components.bottombar.BottomNavItem
import com.example.fooddelivery.ui.components.bottombar.DFoodBottomBar
import com.example.fooddelivery.ui.screens.home.components.HomeTopBar
import com.example.fooddelivery.ui.screens.home.search.components.*
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun SearchScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToRestaurant: (Restaurant) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            DFoodBottomBar(
                currentRoute = "search",
                onItemClick = { item ->
                    when (item) {
                        BottomNavItem.Home -> onNavigateToHome()
                        BottomNavItem.Search -> { /* Đang ở màn hình Search */ }
                        BottomNavItem.Orders -> onNavigateToOrders()
                        BottomNavItem.Profile -> onNavigateToProfile()
                    }
                }
            )
        },
        topBar = {
            HomeTopBar(
                location = "Search",
                cartItemCount = state.cartItemCount,
                onMenuClick = { /* Xử lý Menu */ },
                onLocationClick = { /* Xử lý Vị trí */ },
                onCartClick = onNavigateToCart
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        SearchContent(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateToRestaurant = onNavigateToRestaurant,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun SearchContent(
    state: SearchState,
    onEvent: (SearchEvent) -> Unit,
    onNavigateToRestaurant: (Restaurant) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        item {
            SearchInputField(
                query = state.searchQuery,
                onQueryChange = { onEvent(SearchEvent.QueryChanged(it)) },
                onClear = { onEvent(SearchEvent.ClearSearch) }
            )
        }
        item {
            RecentKeywordsList(
                keywords = state.recentKeyWords,
                onKeywordClick = { onEvent(SearchEvent.KeywordClicked(it)) }
            )
        }
        item {
            SectionHeader(title = "Suggested Restaurants")
        }

        items(state.suggestedRestaurants) { restaurant ->
            SearchRestaurantItem(
                restaurant = restaurant,
                onClick = { onNavigateToRestaurant(restaurant) }
            )
        }
        item {
            SectionHeader(title = "Popular Fast Food", modifier = Modifier.padding(top = 24.dp))
        }

        item {
            PopularFoodRow(popularFood = state.popularFood)
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}
@Preview (showBackground = true, showSystemUi = true)
@Composable
fun SearchScreenPreview() {
    DFoodTheme(darkTheme = false) {
        SearchContent(
            state = SearchState(),
            onEvent = {},
            onNavigateToRestaurant = {}
        )
    }
}

