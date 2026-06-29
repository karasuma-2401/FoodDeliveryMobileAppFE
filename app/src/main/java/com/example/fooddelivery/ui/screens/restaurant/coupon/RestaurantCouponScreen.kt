package com.example.fooddelivery.ui.screens.restaurant.coupon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.ActiveRestaurantCoupon
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponTabs
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponSearchBarAndFilters
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantCouponScreen(
    onNavigateBack: () -> Unit,
    onCreateCouponClick: () -> Unit,
    viewModel: RestaurantCouponViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Coupon Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help */ }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CouponTabs(
                selectedTabIndex = uiState.selectedTab,
                onTabSelected = { viewModel.onTabSelected(it) }
            )

            if (uiState.selectedTab == 0) {
                RestaurantCouponsTab(
                    uiState = uiState,
                    onCreateCouponClick = onCreateCouponClick,
                    onToggleCoupon = viewModel::toggleRestaurantCoupon,
                )
            } else {
                SystemCouponsTab(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                    onEvent = viewModel::onEvent,
                )
            }
        }
    }
}

@Composable
private fun RestaurantCouponsTab(
    uiState: RestaurantCouponUiState,
    onCreateCouponClick: () -> Unit,
    onToggleCoupon: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Button(
                onClick = onCreateCouponClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create New Coupons",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Active Restaurant Coupons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(uiState.restaurantVouchers) { voucherItem ->
            ActiveRestaurantCoupon(
                code = voucherItem.code,
                desc = voucherItem.description,
                usage = voucherItem.usageText,
                isActive = voucherItem.isActive,
                onToggle = { onToggleCoupon(voucherItem.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SystemCouponsTab(
    uiState: RestaurantCouponUiState,
    onSearchQueryChange: (String) -> Unit,
    onEvent: (RestaurantCouponEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        CouponSearchBarAndFilters(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.systemTotalCount > 0) {
            Text(
                text = stringResource(
                    R.string.system_coupons_showing_count,
                    uiState.systemVouchers.size,
                    uiState.systemTotalCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        PullToRefreshBox(
            isRefreshing = uiState.isSystemRefreshing,
            onRefresh = { onEvent(RestaurantCouponEvent.RefreshSystemVouchers) },
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                uiState.isSystemLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.systemVouchers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 24.dp),
                        ) {
                            if (uiState.systemErrorMessage != null) {
                                Text(
                                    text = uiState.systemErrorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                )
                                TextButton(
                                    onClick = { onEvent(RestaurantCouponEvent.LoadSystemVouchers) },
                                ) {
                                    Text("Try again")
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.system_coupons_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(
                            items = uiState.systemVouchers,
                            key = { _, voucher -> voucher.id },
                        ) { index, voucher ->
                            if (index >= uiState.systemVouchers.lastIndex &&
                                !uiState.isSystemEndReached &&
                                !uiState.isSystemPaginating
                            ) {
                                LaunchedEffect(uiState.systemVouchers.size, uiState.searchQuery) {
                                    onEvent(RestaurantCouponEvent.LoadMoreSystemVouchers)
                                }
                            }

                            CouponItemCard(voucher = voucher)
                        }

                        if (uiState.isSystemPaginating) {
                            item(key = "system_coupons_loading_more") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }

                        if (uiState.isSystemEndReached) {
                            item(key = "system_coupons_end") {
                                Text(
                                    text = stringResource(R.string.system_coupons_end_of_list),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantCouponScreenPreview() {
    DFoodTheme {
        RestaurantCouponScreen(
            onNavigateBack = {},
            onCreateCouponClick = {}
        )
    }
}
