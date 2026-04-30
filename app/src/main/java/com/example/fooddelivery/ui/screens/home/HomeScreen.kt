package com.example.fooddelivery.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fooddelivery.ui.components.bottombar.DFoodBottomBar
import com.example.fooddelivery.ui.components.bottombar.BottomNavItem
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.screens.home.components.*
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToAllCategories: () -> Unit,
    onNavigateToAllRestaurants: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToCart = onNavigateToCart,
        onNavigateToRestaurant = onNavigateToRestaurant,
        onNavigateToCategory = onNavigateToCategory,
        onNavigateToAllCategories = onNavigateToAllCategories,
        onNavigateToAllRestaurants = onNavigateToAllRestaurants,
        onOpenMenu = onOpenMenu,
        onOpenLocationPicker = onOpenLocationPicker,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToOrders = onNavigateToOrders,
        onNavigateToSearch = onNavigateToSearch
    )
}

@Composable
fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToAllCategories: () -> Unit,
    onNavigateToAllRestaurants: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                location = state.selectedLocation,
                cartItemCount = state.cartItemCount,
                onMenuClick = onOpenMenu,
                onLocationClick = onOpenLocationPicker,
                onCartClick = onNavigateToCart
            )
        },
        bottomBar = {
            DFoodBottomBar(
                currentRoute = "home",
                onItemClick = { item ->
                    when(item) {
                        BottomNavItem.Home -> { /* Already here */ }
                        BottomNavItem.Search -> onNavigateToSearch()
                        BottomNavItem.Orders -> onNavigateToOrders()
                        BottomNavItem.Profile -> onNavigateToProfile()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Hey ${state.user.fullName.ifEmpty { "Halal" }}, Good Afternoon!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DFoodFTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(HomeEvent.SearchQueryChanged(it)) },
                label = "Search dishes, restaurants",
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(
                title = "All Categories",
                onSeeAllClick = {
//                    viewModel.onEvent(HomeEvent.SeeAllCategoriesClicked)
                    onNavigateToAllCategories()

                }
            )
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                items(state.categories) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onNavigateToCategory(category.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(
                title = "Open Restaurants",
                onSeeAllClick = onNavigateToAllRestaurants
            )

            state.restaurants.forEach { restaurant ->
                RestaurantItem(
                    restaurant = restaurant,
                    onClick = { onNavigateToRestaurant(restaurant.id) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview (showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    DFoodTheme{
        HomeContent(
            state = HomeState(),
            onEvent = {},
            onNavigateToCart = {},
            onNavigateToRestaurant = {},
            onNavigateToCategory = {},
            onNavigateToAllCategories = {},
            onNavigateToAllRestaurants = {},
            onOpenMenu = {},
            onOpenLocationPicker = {},
            onNavigateToProfile = {},
            onNavigateToOrders = {},
            onNavigateToSearch = {}
        )
    }
}
