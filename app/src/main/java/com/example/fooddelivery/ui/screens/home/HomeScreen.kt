package com.example.fooddelivery.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.bottombar.DFoodBottomBar
import com.example.fooddelivery.ui.components.bottombar.BottomNavItem
import com.example.fooddelivery.ui.screens.home.components.*
import com.example.fooddelivery.ui.theme.DFoodTheme
import java.util.Calendar

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
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..15 -> "Good Afternoon"
        in 16..20 -> "Good Evening"
        else -> "Good Night"
    }

    // Show loading state
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show error state
    state.errorMessage?.let { error ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        return
    }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "Hey ${state.user.fullName.ifEmpty { "Halal" }}, $greeting!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToSearch() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF6F6F6)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search dishes, restaurants",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                PromoBanner(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    onOrderNowClick = { /* Xử lý sự kiện */ }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                SectionHeader(
                    title = "All Categories",
                    onSeeAllClick = onNavigateToAllCategories
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
                ) {
                    items(state.categories) { category ->
                        CategoryItem(
                            category = category,
                            onClick = { onNavigateToCategory(category.id) }
                        )
                    }
                }
            }

            // 5. Section Restaurants
            item {
                SectionHeader(
                    title = "Open Restaurants",
                    onSeeAllClick = onNavigateToAllRestaurants
                )
            }

            // Tối ưu hóa: Dùng items trực tiếp của LazyColumn cho danh sách nhà hàng
            items(state.restaurants) { restaurant ->
                RestaurantItem(
                    restaurant = restaurant,
                    onClick = { onNavigateToRestaurant(restaurant.id) }
                )
            }
        }
    }
}

@Preview (showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    DFoodTheme {
        HomeContent(
            state = HomeState(),
            onNavigateToCart = {},
            onNavigateToRestaurant = {},
            onNavigateToCategory = {},
            onNavigateToAllCategories = {},
            onNavigateToAllRestaurants = {},
            onOpenMenu = {},
            onOpenLocationPicker = {},
            onNavigateToProfile = {},
            onNavigateToOrders = {},
            onNavigateToSearch = {},
        )
    }
}