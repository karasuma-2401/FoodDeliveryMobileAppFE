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


import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.ActiveRestaurantCoupon
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponTabs
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CreateCouponCard
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.SystemCouponItem
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.SystemWideSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantCouponScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

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
        Column(modifier = Modifier.padding(padding)) {
            CouponTabs(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (selectedTab == 0) {
                    item { CreateCouponCard() }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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

                    items(3) { index ->
                        ActiveRestaurantCoupon(
                            code = if(index == 0) "SUMMER25" else "FREESHIP",
                            desc = "25% off • Min. $30 order",
                            usage = "Used 45 / 100",
                            isActive = index != 2,
                            onToggle = { /* Handle Toggle */ }
                        )
                    }
                    item { SystemWideSection() }

                    items(2) {
                        SystemCouponLockedItem()
                    }

                } else {
                    item {
                        // Banner thông báo quyền hạn
                        Surface(
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "System coupons are applied automatically by the platform and cannot be modified by the restaurant.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "Active System Rewards",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Danh sách Coupon hệ thống
                    val systemCoupons = listOf(
                        Triple("WELCOME2026", "20% OFF for first order", "31 Dec 2026"),
                        Triple("DFOODLOVE", "Free delivery on all orders over $20", "30 Jun 2026"),
                        Triple("WEEKEND_PROMO", "$5 flat discount on Saturday & Sunday", "Continuous")
                    )

                    items(systemCoupons) { coupon ->
                        SystemCouponItem(
                            code = coupon.first,
                            desc = coupon.second,
                            expiry = coupon.third
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemCouponLockedItem() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("PLATFORM PROMO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text("FIRSTORDER", fontWeight = FontWeight.Bold)
                Text("$5.00 off for first-time users", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray)
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CouponScreenPreview() {
    DFoodTheme {
        RestaurantCouponScreen(
            onNavigateBack = {}
        )
    }
}