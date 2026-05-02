package com.example.fooddelivery.ui.screens.restaurant_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.home.search.components.SectionHeader
import com.example.fooddelivery.ui.screens.restaurant_detail.components.CategoryTabs
import com.example.fooddelivery.ui.screens.restaurant_detail.components.FoodItemCard
import com.example.fooddelivery.ui.screens.restaurant_detail.components.RestaurantHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: RestaurantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Restaurant View",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            state.restaurant?.let { restaurant ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    item {
                        RestaurantHeader(restaurant = restaurant)
                    }
                    item {
                        CategoryTabs(
                            categories = state.categories,
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = { viewModel.onEvent(RestaurantDetailEvent.CategorySelected(it)) }
                        )
                    }
                    item {
                        SectionHeader(
                            title = "${state.selectedCategory} (${state.foodItems.size})",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(state.foodItems.chunked(2)) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { foodItem ->
                                FoodItemCard(
                                    foodItem = foodItem,
                                    onAddClick = { viewModel.onEvent(RestaurantDetailEvent.AddFoodToCart(foodItem)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}