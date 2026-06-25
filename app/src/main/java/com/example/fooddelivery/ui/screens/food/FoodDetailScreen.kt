package com.example.fooddelivery.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.food.components.*
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

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
                FoodDetailUiEffect.NavigateBack -> {
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
                    price = String.format(Locale.US, "%.0f", state.totalPrice),
                    quantity = state.quantity,
                    onUpdateQuantity = { onEvent(FoodDetailEvent.UpdateQuantity(it)) },
                    onAddToCart = { onEvent(FoodDetailEvent.AddToCart) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(24.dp, 8.dp),
                userScrollEnabled = false
            ) {
                item { FoodImageHeaderSkeleton() }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item { RestaurantChipSkeleton() }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { FoodTitleAndDescSkeleton() }
                item { Spacer(modifier = Modifier.height(16.dp))}
                item { FoodInfoRowSkeleton() }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item { SizeSelectionSkeleton() }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        } else if (state.food == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "Food not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val food = state.food ?: return@Scaffold
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                item {
                    FoodImageHeader(
                        imageUrl = food.imageUrl
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    RestaurantChip(
                        name = food.restaurantName,
                        onClick = {
                            state.restaurant?.id?.let(onNavigateToRestaurant) ?: onNavigateBack()
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.foodDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    FoodInfoRow(
                        rating = food.rating.takeIf { it > 0f }
                            ?: state.restaurant?.rating
                            ?: 0f,
                        deliveryFee = state.restaurant?.deliveryFee ?: 0.0
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    SizeSelection(
                        sizes = state.sizes,
                        selectedFoodSizeId = state.selectedFoodSizeId,
                        onSizeSelected = { size ->
                            onEvent(
                                FoodDetailEvent.SelectSize(
                                    foodSizeId = size.foodSizeId,
                                    sizeName = size.name,
                                    price = size.price
                                )
                            )
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    IngredientsSection(ingredients = state.ingredients)
                }

                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
fun IngredientsSection(ingredients: List<FoodIngredient>) {
    if (ingredients.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(ingredients) { ingredient ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(72.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        AsyncImage(
                            model = ingredient.iconUrl,
                            contentDescription = ingredient.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))


                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoodDetailScreenPreview() {
    DFoodTheme(darkTheme = false) {
        FoodDetailContent(
            state = FoodDetailState(),
            onNavigateBack = {},
            onNavigateToRestaurant = {},
            onEvent = {}
        )
    }
}