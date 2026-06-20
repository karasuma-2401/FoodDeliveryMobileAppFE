package com.example.fooddelivery.ui.screens.restaurant.coupon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.ActiveRestaurantCoupon
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponTabs
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CreateCouponCard
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponSearchBarAndFilters
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponPaginationBar
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantCouponScreen(
    onNavigateBack: () -> Unit,
    viewModel: RestaurantCouponViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item { CreateCouponCard() }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Active Restaurant Coupons",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    items(uiState.restaurantVouchers) { voucherItem ->
                        ActiveRestaurantCoupon(
                            code = voucherItem.code,
                            desc = voucherItem.description,
                            usage = voucherItem.usageText,
                            isActive = voucherItem.isActive,
                            onToggle = { viewModel.toggleRestaurantCoupon(voucherItem.id) }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    CouponSearchBarAndFilters(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.systemVouchers) { voucher ->
                                CouponItemCard(voucher = voucher)
                            }
                        }
                    }

                    CouponPaginationBar(
                        startItem = if (uiState.systemVouchers.isEmpty()) 0 else 1,
                        endItem = uiState.systemVouchers.size,
                        totalItems = uiState.totalItems,
                        currentPage = uiState.currentPage,
                        onPageClick = { viewModel.onPageChanged(it) }
                    )
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
            onNavigateBack = {}
        )
    }
}