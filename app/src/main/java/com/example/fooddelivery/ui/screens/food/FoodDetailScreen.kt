package com.example.fooddelivery.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.food.components.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FoodDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: FoodDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is FoodDetailUiEffect.NavigateBackWithSuccess -> {
                    onShowSnackbar(effect.message)
                    onNavigateBack()
                }
            }
        }
    }

    FoodDetailContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToRestaurant = onNavigateToRestaurant,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailContent(
    state: FoodDetailState,
    onNavigateBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onEvent: (FoodDetailEvent) -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Details",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            state.food?.let { food ->
                BottomCartBar(
                    price = String.format("%.0f", state.totalPrice),
                    quantity = state.quantity,
                    onUpdateQuantity = { onEvent(FoodDetailEvent.UpdateQuantity(it)) },
                    onAddToCart = { onEvent(FoodDetailEvent.AddToCart) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                item {
                    FoodImageHeader(
                        imageRes = state.food?.imageRes ?: R.drawable.food_bowl,
                        isFavorite = state.isFavorite,
                        onFavoriteToggle = { onEvent(FoodDetailEvent.ToggleFavorite) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    RestaurantChip(
                        name = state.food?.restaurantName ?: "Uttora Coffee House",
                        onClick = {
                            state.restaurant?.id?.let(onNavigateToRestaurant) ?: onNavigateBack()
                        }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    Text(
                        text = state.food?.name ?: "Pizza Calzone European",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.restaurant?.description ?: "Prosciutto e funghi is a pizza variety that is topped with tomato sauce.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    FoodInfoRow(
                        rating = state.restaurant?.rating ?: 4.7f,
                        deliveryFee = state.restaurant?.deliveryFee ?: "Free",
                        deliveryTime = state.restaurant?.deliveryTime ?: "20 min"
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    SizeSelection(
                        selectedSize = state.selectedSize,
                        onSizeSelected = { onEvent(FoodDetailEvent.SelectSize(it)) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    IngredientsSection()
                }
                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }
    }
}
