package com.example.fooddelivery.ui.screens.restaurant_detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.home.search.components.SectionHeader
import com.example.fooddelivery.ui.screens.restaurant_detail.components.CategoryTabs
import com.example.fooddelivery.ui.screens.restaurant_detail.components.FoodItemCard
import com.example.fooddelivery.ui.screens.restaurant_detail.components.RestaurantHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RestaurantDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: RestaurantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

      val currentScrollIndex by remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 0
            visibleItems
                .filter { it.index >= 2 }
                .firstOrNull { it.offset + it.size > 200 }
                ?.index ?: 0
        }
    }

    val categoryToPositionMap = remember(state.categorizedFoodItem, state.categories) {
        val mapping = mutableMapOf<String, Int>()
        var currentIndex = 2
        
        state.categories.forEach { category ->
            mapping[category] = currentIndex
            val itemsInCategory = state.categorizedFoodItem[category] ?: emptyList()
            val rowCount = (itemsInCategory.size + 1) / 2
            currentIndex += rowCount + 1 
        }
        mapping
    }
    LaunchedEffect(currentScrollIndex) {
        val currentCategory = categoryToPositionMap.entries
            .lastOrNull { it.value <= currentScrollIndex }
            ?.key
            
        if (currentCategory != null && currentCategory != state.selectedCategory) {
            viewModel.onEvent(RestaurantDetailEvent.CategorySelected(currentCategory))
        }
    }

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Restaurant View",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                        Box(modifier = Modifier.padding(24.dp)) {
                            RestaurantHeader(restaurant = restaurant)
                        }
                    }
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFBFBFB))
                                .padding(horizontal = 24.dp)
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
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        items(itemsInCategory.chunked(2)) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
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
}
