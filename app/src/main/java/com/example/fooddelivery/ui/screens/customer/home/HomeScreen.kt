package com.example.fooddelivery.ui.screens.customer.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.bounceClick
import com.example.fooddelivery.ui.theme.CustomerDimens
import com.example.fooddelivery.ui.screens.customer.home.components.CategoryItem
import com.example.fooddelivery.ui.screens.customer.home.components.CategoryItemSkeleton
import com.example.fooddelivery.ui.screens.customer.home.components.HomeGreetingCard
import com.example.fooddelivery.ui.screens.customer.home.components.HomeGreetingSkeleton
import com.example.fooddelivery.ui.screens.customer.home.components.HomeTopBar
import com.example.fooddelivery.ui.screens.customer.home.components.PromoBanner
import com.example.fooddelivery.ui.screens.customer.home.components.PromoBannerSkeleton
import com.example.fooddelivery.ui.screens.customer.home.components.RestaurantItem
import com.example.fooddelivery.ui.screens.customer.home.components.RestaurantItemSkeleton
import com.example.fooddelivery.ui.screens.customer.home.components.SearchBarSkeleton
import com.example.fooddelivery.ui.screens.customer.home.components.SectionHeader
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToAllCategories: () -> Unit,
    onNavigateToAllRestaurants: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToManageAddress: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToCart -> onNavigateToCart()
                is HomeUiEffect.NavigateToConversations -> onNavigateToConversations()
                is HomeUiEffect.NavigateToAllCategories -> onNavigateToAllCategories()
                is HomeUiEffect.NavigateToAllRestaurants -> onNavigateToAllRestaurants()
                is HomeUiEffect.NavigateToCategory -> onNavigateToCategory(effect.categoryId)
                is HomeUiEffect.NavigateToRestaurant -> onNavigateToRestaurant(effect.restaurantId)
                is HomeUiEffect.NavigateToFoodDetail -> onNavigateToFoodDetail(effect.foodId)
                HomeUiEffect.NavigateToEditProfile -> onNavigateToEditProfile()
                HomeUiEffect.NavigateToManageAddress -> onNavigateToManageAddress()
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(HomeEvent.ErrorDismissed)
        }
    }

    if (state.isPhoneMissing) {
        PhoneRequiredDialog(
            onDismiss = { viewModel.onEvent(HomeEvent.PhoneUpdateDismissed) },
            onUpdate = onNavigateToEditProfile
        )
    }

    HomeContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToOrders = onNavigateToOrders,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToManageAddress = onNavigateToManageAddress,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneRequiredDialog(
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(CustomerDimens.cardCornerRadius),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Phone Number Required",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "To provide the best delivery experience, we need your phone number for contact purposes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Update Now", style = MaterialTheme.typography.labelLarge)
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Maybe Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    snackbarHostState: SnackbarHostState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToManageAddress: () -> Unit,
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                selectedLocationLabel = state.selectedLocationLabel,
                selectedLocationDetail = state.selectedLocationDetail,
                addressOptions = state.addressOptions,
                onAddressSelected = { onEvent(HomeEvent.AddressSelected(it)) },
                onManageAddressClick = { onEvent(HomeEvent.ManageAddressClicked) },
                cartItemCount = state.cartItemCount,
                unreadMessageCount = state.unreadMessageCount,
                onCartClick = { onEvent(HomeEvent.CartClicked) },
                onMessageClick = { onEvent(HomeEvent.MessageClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(HomeEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (state.isLoading) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = CustomerDimens.screenHorizontalPadding)) {
                            HomeGreetingSkeleton()
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    item { Box(modifier = Modifier.padding(horizontal = CustomerDimens.screenHorizontalPadding)) { SearchBarSkeleton() }; Spacer(modifier = Modifier.height(24.dp)) }
                    item { Box(modifier = Modifier.padding(horizontal = CustomerDimens.screenHorizontalPadding)) { PromoBannerSkeleton() }; Spacer(modifier = Modifier.height(32.dp)) }
                    item {
                        SectionHeader(title = "All Categories", onSeeAllClick = { })
                        LazyRow(contentPadding = PaddingValues(horizontal = CustomerDimens.screenHorizontalPadding), horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp, bottom = 32.dp), userScrollEnabled = false) { items(5) { CategoryItemSkeleton() } }
                    }
                    item { SectionHeader(title = "All Restaurants", onSeeAllClick = { }) }
                    items(3) { RestaurantItemSkeleton() }
                } else {
                    item {
                        HomeGreetingCard(
                            userName = state.user.fullName,
                            modifier = Modifier.padding(horizontal = CustomerDimens.screenHorizontalPadding),
                            onClick = onNavigateToProfile,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    item {
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = CustomerDimens.screenHorizontalPadding)
                                .fillMaxWidth()
                                .height(56.dp)
                                .bounceClick { onNavigateToSearch() },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Search dishes, restaurants",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        if (state.banners.isNotEmpty()) {
                            val pagerState = rememberPagerState(pageCount = { state.banners.size })

                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = CustomerDimens.screenHorizontalPadding),
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
                            with(pagerState) {
                                LaunchedEffect(key1 = currentPage) {
                                    delay(2500)
                                    animateScrollToPage(
                                        page = (currentPage + 1).mod(pageCount)
                                    )
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
                            contentPadding = PaddingValues(horizontal = CustomerDimens.screenHorizontalPadding),
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
                            title = "All Restaurants",
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    DFoodTheme(darkTheme = false) {
        HomeContent(
            state = HomeState(),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onNavigateToProfile = {},
            onNavigateToOrders = {},
            onNavigateToSearch = {},
            onNavigateToManageAddress = {},
        )
    }
}
