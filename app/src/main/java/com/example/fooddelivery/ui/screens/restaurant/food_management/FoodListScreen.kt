package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.ui.components.dialog.ConfirmDialogType
import com.example.fooddelivery.ui.components.dialog.DFoodConfirmDialog
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.food_management.CategoryTabRow
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
    var foodToDelete by remember { mutableStateOf<FoodResponse?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadFoods()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    MyFoodListScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onEditFood = onEditFood,
        onAddFoodClick = onAddFoodClick,
        onNavigate = onNavigate,
        onCategorySelected = viewModel::onCategorySelected,
        onDeleteFood = { food -> foodToDelete = food }
    )

    foodToDelete?.let { food ->
        DFoodConfirmDialog(
            title = "Delete Item",
            message = "Delete \"${food.name}\" from your menu?",
            confirmText = "Delete",
            type = ConfirmDialogType.Destructive,
            onConfirm = {
                viewModel.deleteFood(food.id)
                foodToDelete = null
            },
            onDismiss = { foodToDelete = null }
        )
    }
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
    onDeleteFood: (FoodResponse) -> Unit = {}
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DFoodTopBar(
                title = "My Food List",
                onBackClick = onNavigateBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
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
                        onEditClick = { food -> onEditFood(food.id.toString()) },
                        onDeleteClick = { food -> onDeleteFood(food) }
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
        MyFoodListScreenContent(
            state = MyFoodListState(
                categories = listOf("All", "Burger", "Pizza"),
                selectedCategoryIndex = 0,
                foodList = listOf(
                    FoodResponse(
                        id = 1,
                        name = "Classic Burger",
                        price = 45000.0,
                        description = "Juicy beef patty with cheese",
                        categoryId = 1,
                        restaurantId = 1,
                        rating = 4.5f,
                        reviewCount = 120
                    ),
                    FoodResponse(
                        id = 2,
                        name = "Cheese Pizza",
                        price = 120000.0,
                        description = "Fluffy pizza with double cheese",
                        categoryId = 2,
                        restaurantId = 1,
                        rating = 4.8f,
                        reviewCount = 85
                    )
                ),
                filteredFoodList = listOf(
                    FoodResponse(
                        id = 1,
                        name = "Classic Burger",
                        price = 45000.0,
                        description = "Juicy beef patty with cheese",
                        categoryId = 1,
                        restaurantId = 1,
                        rating = 4.5f,
                        reviewCount = 120
                    ),
                    FoodResponse(
                        id = 2,
                        name = "Cheese Pizza",
                        price = 120000.0,
                        description = "Fluffy pizza with double cheese",
                        categoryId = 2,
                        restaurantId = 1,
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