package com.example.fooddelivery.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fooddelivery.ui.components.cart.AddToCartBottomSheet
import com.example.fooddelivery.ui.components.cart.RestaurantCartBar
import com.example.fooddelivery.ui.screens.food.components.*
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.utils.rememberHeroOverlayState
import com.example.fooddelivery.ui.utils.shareText
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FoodDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToCheckout: (restaurantId: String, restaurantName: String) -> Unit,
    viewModel: FoodDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is FoodDetailUiEffect.LaunchShare -> {
                    context.shareText(
                        text = effect.text,
                        subject = effect.subject,
                        chooserTitle = "Share food"
                    )
                }
            }
        }
    }

    FoodDetailContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToRestaurant = onNavigateToRestaurant,
        onNavigateToCart = onNavigateToCart,
        onNavigateToCheckout = onNavigateToCheckout,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailContent(
    state: FoodDetailState,
    onNavigateBack: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToCheckout: (restaurantId: String, restaurantName: String) -> Unit,
    onEvent: (FoodDetailEvent) -> Unit
) {
    val showCartBar = state.restaurantCartItemCount > 0
    val cartBarPadding = if (showCartBar) 88.dp else 0.dp

    if (state.showSizeSheet && state.food != null) {
        AddToCartBottomSheet(
            foodName = state.food.name,
            imageUrl = state.food.imageUrl,
            description = state.foodDescription,
            sizes = state.sizes,
            unitPrice = state.unitPrice,
            selectedSizeId = state.sheetSelectedSizeId,
            quantity = state.sheetQuantity,
            isLoading = false,
            isSubmitting = state.isAddingToCart,
            onSizeSelected = { onEvent(FoodDetailEvent.SelectSheetSize(it)) },
            onQuantityChange = { onEvent(FoodDetailEvent.UpdateSheetQuantity(it)) },
            onConfirm = { onEvent(FoodDetailEvent.ConfirmAddFromSheet) },
            onDismiss = { onEvent(FoodDetailEvent.DismissSizeSheet) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showCartBar) {
                RestaurantCartBar(
                    itemCount = state.restaurantCartItemCount,
                    subtotal = state.restaurantCartSubtotal,
                    onCartClick = onNavigateToCart,
                    onContinueClick = {
                        val food = state.food ?: return@RestaurantCartBar
                        onNavigateToCheckout(food.restaurantId, food.restaurantName)
                    }
                )
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                val heroHeight = foodHeroTotalHeight()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    FoodDetailHeaderSkeleton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heroHeight)
                            .align(Alignment.TopCenter)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = cartBarPadding),
                        userScrollEnabled = false
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(heroHeight - FoodHeroPanelOverlap))
                        }
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.background,
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    )
                            ) {
                                FoodInfoSectionSkeleton()
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    RestaurantChipSkeleton()
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    SizeSelectionSkeleton()
                                }
                            }
                        }
                    }
                }
            }
            state.food == null -> {
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
            }
            else -> {
                val food = state.food
                val listState = rememberLazyListState()
                val heroHeight = foodHeroTotalHeight()
                val heroOverlay = rememberHeroOverlayState(
                    listState = listState,
                    heroHeight = heroHeight,
                    panelOverlap = FoodHeroPanelOverlap
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (heroOverlay.showHeroImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heroHeight)
                                .align(Alignment.TopCenter)
                        ) {
                            FoodDetailHeroImage(imageUrl = food.imageUrl)
                        }
                    }

                    if (heroOverlay.showHeroActions) {
                        FoodDetailHeroActions(
                            onBackClick = onNavigateBack,
                            onShareClick = { onEvent(FoodDetailEvent.ShareFood) },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .zIndex(2f)
                        )
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp + cartBarPadding)
                    ) {
                        item(key = "hero_spacer") {
                            Spacer(modifier = Modifier.height(heroHeight - FoodHeroPanelOverlap))
                        }
                        item(key = "content") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.background,
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    )
                            ) {
                                FoodInfoSection(
                                    name = food.name,
                                    description = state.foodDescription,
                                    unitPrice = state.unitPrice,
                                    soldCount = food.soldCount,
                                    originalPrice = state.originalPrice,
                                    discountBadge = state.discountBadge,
                                    showAddSuccessPulse = state.showAddSuccessPulse,
                                    isAddingToCart = state.isAddingToCart,
                                    onQuickAdd = { onEvent(FoodDetailEvent.QuickAdd) },
                                    onClearAddSuccessPulse = {
                                        onEvent(FoodDetailEvent.ClearAddSuccessPulse)
                                    }
                                )
                                RestaurantChip(
                                    name = food.restaurantName,
                                    onClick = {
                                        state.restaurant?.id?.let(onNavigateToRestaurant)
                                            ?: onNavigateBack()
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
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
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                IngredientsSection(
                                    ingredients = state.ingredients,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientsSection(
    ingredients: List<FoodIngredient>,
    modifier: Modifier = Modifier
) {
    if (ingredients.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
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
            onNavigateToCart = {},
            onNavigateToCheckout = { _, _ -> },
            onEvent = {}
        )
    }
}
