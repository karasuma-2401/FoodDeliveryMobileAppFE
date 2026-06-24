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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.ui.screens.customer.search.components.SectionHeader
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.CategoryTabs
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.CategoryTabsSkeleton
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.FoodItemCard
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.FoodItemCardSkeleton
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantHeader
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.RestaurantHeaderSkeleton
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components.SectionHeaderSkeleton
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun RestaurantDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    viewModel: RestaurantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is RestaurantDetailUiEffect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
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
    onEvent: (RestaurantDetailEvent) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showVoucherSheet by remember { mutableStateOf(false) }

    val currentScrollIndex by remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 0
            visibleItems
                .filter { it.index >= 1 }
                .firstOrNull { it.offset + it.size > 100 }
                ?.index ?: 0
        }
    }

    val categoryToPositionMap = remember(state.categorizedFoodItem, state.categories) {
        val mapping = mutableMapOf<String, Int>()
        var currentIdx = 2 // Header(0) + Tabs(1)
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
        containerColor = Color.White
    ) { innerPadding ->
        if (state.isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
                userScrollEnabled = false
            ) {
                item { RestaurantHeaderSkeleton() }
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
        } else {
            state.restaurant?.let { restaurant ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        RestaurantHeader(
                            restaurant = restaurant,
                            vouchers = state.vouchers,
                            onBackClick = onNavigateBack,
                            onViewAllVouchers = { showVoucherSheet = true },
                            onFavoriteToggle = { onEvent(RestaurantDetailEvent.ToggleFavorite) }
                        )
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
                                        listState.animateScrollToItem(
                                            index = targetIndex,
                                            scrollOffset = -10
                                        )
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
                                            onEvent(RestaurantDetailEvent.AddFoodToCart(foodItem))
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

    if (showVoucherSheet) {
        VoucherBottomSheet(
            vouchers = state.vouchers,
            onDismiss = { showVoucherSheet = false }
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
                text = "Store Vouchers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vouchers) { voucher ->
                    VoucherItemCard(voucher = voucher)
                }
            }
        }
    }
}

@Composable
fun VoucherItemCard(voucher: Voucher) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voucher.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = voucher.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (voucher.expiryText != null) {
                    Text(
                        text = voucher.expiryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEE4D2D),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Button(
                onClick = { /* Collect voucher logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE4D2D)),
                contentPadding = PaddingValues(horizontal = 16.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = "Collect", fontSize = 12.sp)
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
            id = "1",
            code = "OFF15",
            title = "$15.00 OFF",
            description = "Discount for your first order. Min spend $0.",
            discountAmount = 15.0,
            minOrderAmount = 0.0,
            expiryText = "Exp. 30 Jun 2024",
            type = VoucherType.DISCOUNT
        ),
        Voucher(
            id = "2",
            code = "OFF16",
            title = "$16.00 OFF",
            description = "Special weekend offer. Min spend $0.",
            discountAmount = 16.0,
            minOrderAmount = 0.0,
            expiryText = "Exp. 15 Jul 2024",
            type = VoucherType.DISCOUNT
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
            onEvent = {}
        )
    }
}
