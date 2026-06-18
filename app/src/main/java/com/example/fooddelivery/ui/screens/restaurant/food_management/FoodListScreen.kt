package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.food_management.CategoryTabRow
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodBottomBar
import com.example.fooddelivery.ui.screens.restaurant.component.food_management.FoodList
import com.example.fooddelivery.ui.screens.restaurant.component.food_management.ItemCountText
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun MyFoodListScreen(
    onNavigateBack: () -> Unit,
    onEditFood: (String) -> Unit,
    onAddFoodClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: MyFoodListViewModel = hiltViewModel()
) {
    val state by viewModel.state
    
    MyFoodListScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onEditFood = onEditFood,
        onAddFoodClick = onAddFoodClick,
        onNavigate = onNavigate,
        onCategorySelected = viewModel::onCategorySelected,
        onDeleteFood = viewModel::deleteFood
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFoodListScreenContent(
    state: MyFoodListState,
    onNavigateBack: () -> Unit,
    onEditFood: (String) -> Unit,
    onAddFoodClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCategorySelected: (Int) -> Unit = {},
    onDeleteFood: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "My Food List",
                onBackClick = onNavigateBack,
            )
        },
        bottomBar = {
            DFoodBottomBar(
                currentRoute = "menu",
                onNavigate = onNavigate,
                onAddClick = onAddFoodClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CategoryTabRow(
                        categories = state.categories,
                        selectedIndex = state.selectedCategoryIndex,
                        onTabSelected = onCategorySelected
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ItemCountText(state.totalItems)

                    Spacer(modifier = Modifier.height(16.dp))

                    FoodList(
                        items = state.filteredFoodList,
                        onEditClick = { food -> onEditFood(food.id) },
                        onDeleteClick = { food -> onDeleteFood(food.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoodListScreenPreview() {
    DFoodTheme {
        // Use the stateless MyFoodListScreenContent for the preview to avoid Hilt/ViewModel issues
        MyFoodListScreenContent(
            state = MyFoodListState(
                foodList = listOf(
                    FoodResponse(
                        id = "1",
                        name = "Classic Burger",
                        price = 10.0,
                        details = "Juicy beef patty with cheese",
                        category = "Lunch",
                        rating = 4.5f,
                        reviewCount = 120
                    ),
                    FoodResponse(
                        id = "2",
                        name = "Pancakes",
                        price = 7.0,
                        details = "Fluffy pancakes with syrup",
                        category = "Breakfast",
                        rating = 4.8f,
                        reviewCount = 85
                    )
                ),
                filteredFoodList = listOf(
                    FoodResponse(
                        id = "1",
                        name = "Classic Burger",
                        price = 10.0,
                        details = "Juicy beef patty with cheese",
                        category = "Lunch",
                        rating = 4.5f,
                        reviewCount = 120
                    ),
                    FoodResponse(
                        id = "2",
                        name = "Pancakes",
                        price = 7.0,
                        details = "Fluffy pancakes with syrup",
                        category = "Breakfast",
                        rating = 4.8f,
                        reviewCount = 85
                    )
                ),
                totalItems = 2
            ),
            onNavigateBack = {},
            onNavigate = {},
            onAddFoodClick = {},
            onEditFood = {},
            onCategorySelected = {},
            onDeleteFood = {}
        )
    }
}
