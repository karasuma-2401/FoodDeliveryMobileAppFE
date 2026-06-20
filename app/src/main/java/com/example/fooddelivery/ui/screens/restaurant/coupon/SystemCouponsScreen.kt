package com.example.fooddelivery.ui.screens.restaurant.coupon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponTabs
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponSearchBarAndFilters
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponPaginationBar
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemCouponsScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }

    val dummyVouchers = remember {
        listOf(
            Voucher(
                id = "1",
                code = "SUMMER25",
                title = "25% Weekend Discount",
                description = "Get 25% off on all main dishes",
                discountAmount = 25.0,
                minOrderAmount = 30.0,
                expiryText = "31 Dec 2026",
                type = VoucherType.DISCOUNT,
                isApplicable = true
            ),
            Voucher(
                id = "2",
                code = "FREESHIP",
                title = "Free Delivery Code",
                description = "Free shipping across town",
                discountAmount = 5.0,
                minOrderAmount = 15.0,
                type = VoucherType.FREESHIP,
                isApplicable = true
            )
        )
    }

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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CouponTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            if (selectedTabIndex == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    CouponSearchBarAndFilters(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dummyVouchers) { voucher ->
                            CouponItemCard(voucher = voucher)
                        }
                    }

                    CouponPaginationBar(
                        startItem = 1,
                        endItem = dummyVouchers.size,
                        totalItems = 24,
                        currentPage = 1,
                        onPageClick = {}
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Restaurant Coupons Area",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SystemCouponsScreenPreview() {
    DFoodTheme {
        SystemCouponsScreen()
    }
}