package com.example.fooddelivery.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToAllCategories: () -> Unit,
    onNavigateToAllRestaurants: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToCart -> onNavigateToCart()
                is HomeUiEffect.NavigateToAllCategories -> onNavigateToAllCategories()
                is HomeUiEffect.NavigateToAllRestaurants -> onNavigateToAllRestaurants()
                is HomeUiEffect.NavigateToCategory -> onNavigateToCategory(effect.categoryId)
                is HomeUiEffect.NavigateToRestaurant -> onNavigateToRestaurant(effect.restaurantId)
                is HomeUiEffect.NavigateToFoodDetail -> onNavigateToFoodDetail(effect.foodId)
            }
        }
    }

    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToOrders = onNavigateToOrders,
        onNavigateToSearch = onNavigateToSearch
    )
}

@Composable
fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
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
    
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                selectedLocation = state.selectedLocation,
                availableLocations = state.availableLocations,
                onLocationSelected = { onEvent(HomeEvent.LocationSelected(it)) },
                cartItemCount = state.cartItemCount,
                onCartClick = { onEvent(HomeEvent.CartClicked) }
            )
        },
        bottomBar = {
            DFoodBottomBar(
                currentRoute = "home",
                onItemClick = { item ->
                    when(item) {
                        BottomNavItem.Home -> { }
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
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "Hey ${state.user.fullName.ifEmpty { "Customer" }}, $greeting!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToSearch() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF6F6F6)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Search dishes, restaurants", color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                if (state.banners.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { state.banners.size})

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) { pagerIndex ->
                        val banner = state.banners[pagerIndex]
                        PromoBanner(
                            banner = banner,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onEvent(HomeEvent.BannerClicked(banner)) }
                        )
                    }
                    val scope = rememberCoroutineScope()
                    with(pagerState) {
                        LaunchedEffect(key1 = currentPage) {
                            launch {
                                delay(2500)
                                scope.launch {
                                    animateScrollToPage(
                                        page = (currentPage + 1).mod(pageCount)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                SectionHeader(
                    title = "All Categories",
                    onSeeAllClick = { onEvent(HomeEvent.SeeAllCategoriesClicked) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
                ) {
                    items(state.categories) { category ->
                        CategoryItem(
                            category = category,
                            onClick = { onEvent(HomeEvent.CategoryClicked(category.id)) }
                        )
                    }
                }
            }
            item {
                SectionHeader(
                    title = "Open Restaurants",
                    onSeeAllClick = { onEvent(HomeEvent.SeeAllRestaurantsClicked) }
                )
            }
            items(state.restaurants) { restaurant ->
                RestaurantItem(
                    restaurant = restaurant,
                    onClick = { onEvent(HomeEvent.RestaurantClicked(restaurant.id)) }
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
            onEvent = {},
            onNavigateToProfile = {},
            onNavigateToOrders = {},
            onNavigateToSearch = {}
        )
    }
}
