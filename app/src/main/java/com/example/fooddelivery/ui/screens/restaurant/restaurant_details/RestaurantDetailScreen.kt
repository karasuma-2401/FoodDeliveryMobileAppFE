package com.example.fooddelivery.ui.screens.restaurant.restaurant_details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.ui.components.cart.AddToCartBottomSheet
import com.example.fooddelivery.ui.components.cart.RestaurantCartBar
import com.example.fooddelivery.ui.screens.customer.search.components.SectionHeader
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.CategoryTabs
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.CategoryTabsSkeleton
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.FoodItemCard
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.FoodItemCardSkeleton
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantHeroActions
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantHeroImage
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantHeroPanelOverlap
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantInfoSection
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantInfoSectionSkeleton
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantHeroImageSkeleton
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantVoucherSection
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantVoucherSheetCard
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.restaurantHeroTotalHeight
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.SectionHeaderSkeleton
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.utils.rememberHeroOverlayState
import com.example.fooddelivery.ui.utils.shareText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun RestaurantDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    onNavigateToReviews: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToCheckout: (restaurantId: String, restaurantName: String) -> Unit,
    viewModel: RestaurantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is RestaurantDetailUiEffect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is RestaurantDetailUiEffect.LaunchShare -> {
                    context.shareText(
                        text = effect.text,
                        subject = effect.subject,
                        chooserTitle = "Share restaurant"
                    )
                }
            }
        }
    }

    RestaurantDetailContent(
        state = state,
        snackBarHostState = snackBarHostState,
        onNavigateBack = onNavigateBack,
        onNavigateToFoodDetail = onNavigateToFoodDetail,
        onNavigateToReviews = onNavigateToReviews,
        onNavigateToCart = onNavigateToCart,
        onNavigateToCheckout = onNavigateToCheckout,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RestaurantDetailContent(
    state: RestaurantDetailState,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    onNavigateToReviews: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToCheckout: (restaurantId: String, restaurantName: String) -> Unit,
    onEvent: (RestaurantDetailEvent) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showVoucherSheet by remember { mutableStateOf(false) }

    val firstCategoryIndex = if (state.vouchers.isEmpty()) 3 else 4

    val currentScrollIndex by remember(firstCategoryIndex) {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 0
            visibleItems
                .filter { it.index >= firstCategoryIndex }
                .firstOrNull { it.offset + it.size > 100 }
                ?.index ?: 0
        }
    }

    val categoryToPositionMap = remember(
        state.categorizedFoodItem,
        state.categories,
        state.vouchers.isEmpty()
    ) {
        val mapping = mutableMapOf<String, Int>()
        var currentIdx = firstCategoryIndex
        state.categories.forEach { category ->
            mapping[category] = currentIdx
            val itemsInCategory = state.categorizedFoodItem[category] ?: emptyList()
            val rowCount = (itemsInCategory.size + 1) / 2
            currentIdx += rowCount + 1
        }
        mapping
    }

    LaunchedEffect(currentScrollIndex) {
        val currentCategory = categoryToPositionMap.entries
            .lastOrNull { it.value <= currentScrollIndex }
            ?.key
            
        if (currentCategory != null && currentCategory != state.selectedCategory) {
            onEvent(RestaurantDetailEvent.CategorySelected(currentCategory))
        }
    }

    val showCartBar = state.restaurantCartItemCount > 0
    val cartBarPadding = if (showCartBar) 88.dp else 0.dp

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        bottomBar = {
            if (showCartBar) {
                RestaurantCartBar(
                    itemCount = state.restaurantCartItemCount,
                    subtotal = state.restaurantCartSubtotal,
                    onCartClick = onNavigateToCart,
                    onContinueClick = {
                        val restaurant = state.restaurant ?: return@RestaurantCartBar
                        onNavigateToCheckout(restaurant.id, restaurant.name)
                    }
                )
            }
        },
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (state.isLoading) {
            val heroHeight = restaurantHeroTotalHeight()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                RestaurantHeroImageSkeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heroHeight)
                        .align(Alignment.TopCenter)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp + cartBarPadding),
                    userScrollEnabled = false
                ) {
                    item {
                        Spacer(modifier = Modifier.height(heroHeight - RestaurantHeroPanelOverlap))
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White,
                                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                        ) {
                            RestaurantInfoSectionSkeleton()
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            CategoryTabsSkeleton()
                        }
                    }
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            SectionHeaderSkeleton()
                        }
                    }
                    items(2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FoodItemCardSkeleton(modifier = Modifier.weight(1f))
                            FoodItemCardSkeleton(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            state.restaurant?.let { restaurant ->
                val heroHeight = restaurantHeroTotalHeight()
                val heroOverlay = rememberHeroOverlayState(
                    listState = listState,
                    heroHeight = heroHeight,
                    panelOverlap = RestaurantHeroPanelOverlap
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    if (heroOverlay.showHeroImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heroHeight)
                                .align(Alignment.TopCenter)
                        ) {
                            RestaurantHeroImage(restaurant = restaurant)
                        }
                    }

                    if (heroOverlay.showHeroActions) {
                        RestaurantHeroActions(
                            isLiked = restaurant.isLiked,
                            onBackClick = onNavigateBack,
                            onFavoriteToggle = { onEvent(RestaurantDetailEvent.ToggleFavorite) },
                            onShareClick = { onEvent(RestaurantDetailEvent.ShareRestaurant) },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .zIndex(2f)
                        )
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        contentPadding = PaddingValues(bottom = 24.dp + cartBarPadding)
                    ) {
                        item(key = "hero_spacer") {
                            Spacer(modifier = Modifier.height(heroHeight - RestaurantHeroPanelOverlap))
                        }
                        item(key = "info") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color.White,
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    )
                            ) {
                                RestaurantInfoSection(
                                    restaurant = restaurant,
                                    onReviewsClick = { onNavigateToReviews(restaurant.id) }
                                )
                            }
                        }
                        if (state.vouchers.isNotEmpty()) {
                            item(key = "vouchers") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .padding(bottom = 20.dp)
                                ) {
                                    RestaurantVoucherSection(
                                        vouchers = state.vouchers,
                                        onViewAllClick = { showVoucherSheet = true },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                CategoryTabs(
                                    categories = state.categories,
                                    selectedCategory = state.selectedCategory,
                                    onCategorySelected = { category ->
                                        val targetIndex = categoryToPositionMap[category] ?: 0
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index = targetIndex)
                                        }
                                    }
                                )
                            }
                        }
                        state.categories.forEach { category ->
                            val itemsInCategory = state.categorizedFoodItem[category] ?: emptyList()
                            item {
                                SectionHeader(
                                    title = "$category (${itemsInCategory.size})",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(itemsInCategory.chunked(2)) { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { foodItem ->
                                        FoodItemCard(
                                            foodItem = foodItem,
                                            onAddClick = {
                                                onEvent(RestaurantDetailEvent.OpenAddToCartSheet(foodItem))
                                            },
                                            onItemClick = { onNavigateToFoodDetail(foodItem.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVoucherSheet) {
        VoucherBottomSheet(
            vouchers = state.vouchers,
            onDismiss = { showVoucherSheet = false }
        )
    }

    val sheet = state.addToCartSheet
    if (state.showAddToCartSheet && sheet != null) {
        AddToCartBottomSheet(
            foodName = sheet.foodItem.name,
            imageUrl = sheet.foodItem.imageUrl,
            description = sheet.description,
            sizes = sheet.sizes,
            unitPrice = sheet.foodItem.price,
            selectedSizeId = sheet.selectedSizeId,
            quantity = sheet.quantity,
            isLoading = sheet.isLoadingDetails,
            isSubmitting = state.isAddingToCart,
            onSizeSelected = { onEvent(RestaurantDetailEvent.SelectSheetSize(it)) },
            onQuantityChange = { onEvent(RestaurantDetailEvent.UpdateSheetQuantity(it)) },
            onConfirm = { onEvent(RestaurantDetailEvent.ConfirmAddToCart) },
            onDismiss = { onEvent(RestaurantDetailEvent.DismissAddToCartSheet) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherBottomSheet(
    vouchers: List<Voucher>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Store vouchers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vouchers, key = { it.id }) { voucher ->
                    RestaurantVoucherSheetCard(voucher = voucher)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantDetailScreenPreview() {
    val mockRestaurant = Restaurant(
        id = "1",
        name = "Heo Con - Cơm Gà Sốt, Da Gà & Hamburger",
        description = "Famous for its crispy chicken and unique sauces.",
        tags = listOf("Chicken", "Burger", "Asian"),
        rating = 4.6f,
        reviewCount = 999,
        deliveryFee = 2.0,
        isLiked = true,
        imageRes = R.drawable.food_bowl
    )

    val mockVouchers = listOf(
        Voucher(
            id = 1,
            code = "OFF15",
            title = "$15.00 OFF",
            description = "Discount for your first order. Min spend $0.",
            discountAmount = 15.0,
            minOrderAmount = 0.0,
            expiryText = "Exp. 30 Jun 2024",
            type = VoucherType.MONEY
        ),
        Voucher(
            id = 2,
            code = "OFF16",
            title = "$16.00 OFF",
            description = "Special weekend offer. Min spend $0.",
            discountAmount = 16.0,
            minOrderAmount = 0.0,
            expiryText = "Exp. 15 Jul 2024",
            type = VoucherType.MONEY
        )
    )

    val mockFoodItems = listOf(
        FoodItem(
            id = "f1",
            name = "Crispy Chicken with Sauce",
            restaurantId = "1",
            restaurantName = "Heo Con",
            categoryId = "Popular",
            price = 39000.0,
            soldCount = 1000,
            imageRes = R.drawable.food_bowl,
            promoTag = "1K+ Sold"
        ),
        FoodItem(
            id = "f2",
            name = "Classic Beef Burger",
            restaurantId = "1",
            restaurantName = "Heo Con",
            categoryId = "Popular",
            price = 45000.0,
            soldCount = 82,
            imageRes = R.drawable.food_bowl,
            promoTag = "82 Sold"
        ),
        FoodItem(
            id = "f3",
            name = "Fried Rice with Egg",
            restaurantId = "1",
            restaurantName = "Heo Con",
            categoryId = "Main Dishes",
            price = 35000.0,
            soldCount = 500,
            imageRes = R.drawable.food_bowl
        ),
        FoodItem(
            id = "f4",
            name = "Spicy Chicken Wings",
            restaurantId = "1",
            restaurantName = "Heo Con",
            categoryId = "Main Dishes",
            price = 55000.0,
            soldCount = 200,
            imageRes = R.drawable.food_bowl
        ),
        FoodItem(
            id = "f5",
            name = "Coca Cola",
            restaurantId = "1",
            restaurantName = "Heo Con",
            categoryId = "Drinks",
            price = 15000.0,
            soldCount = 2000,
            imageRes = R.drawable.food_bowl
        )
    )

    val categories = mockFoodItems.map { it.categoryId }.distinct()

    DFoodTheme(darkTheme = false) {
        RestaurantDetailContent(
            state = RestaurantDetailState(
                isLoading = false,
                restaurant = mockRestaurant,
                foodItems = mockFoodItems,
                categories = categories,
                categorizedFoodItem = mockFoodItems.groupBy { it.categoryId },
                selectedCategory = categories.firstOrNull() ?: "",
                vouchers = mockVouchers
            ),
            snackBarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onNavigateToFoodDetail = {},
            onNavigateToReviews = {},
            onNavigateToCart = {},
            onNavigateToCheckout = { _, _ -> },
            onEvent = {}
        )
    }
}
